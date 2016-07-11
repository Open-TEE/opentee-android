/*
 * Copyright (c) 2016 Aalto University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "LibteeWrapper.h"
#include "tee_shared_data_types.h"
#include "LibteeeWrapperConstants.h"
#include "gpdatatypes/GPDataTypes.pb.h"

#include <pthread.h>
#include <stdbool.h>
#include <android/log.h>

#include <google/protobuf/message.h>
#include <string>
#include <vector>
#include <unordered_map>
#include <sstream>

#ifdef ANDROID
#  define LOG_TAG "[JNI]"
#  define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#  define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif

using namespace std;
using namespace google::protobuf;
using namespace fi::aalto::ssg::opentee::imps::pbdatatypes;

#ifdef __cplusplus
extern "C" {
#endif
#include "tee_client_api.h"

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the 'OTGuard.java' and it can be passed in and out.
 * */
static TEEC_Context g_contextRecord = {0};

static unordered_map<int, TEEC_SharedMemory*> sharedmemory_map;
static unordered_map<int, TEEC_Session*> sessions_map;
static unordered_map<int, TEEC_Operation*> operations_map; // <hashcodeWithPid, TEEC_Operation>

static int open_tee_socket_env_set = 0;

//test code
__inline void hex_dump(void* buffer, int size){
    ostringstream ss;
    ss <<hex;

    uint8_t* data = (uint8_t*)buffer;
    for(int i = 0; i < size; i++){
        if ( i != 0 && i % 8 == 0) ss<<"\n ";
        ss <<" "<< static_cast<int>(*data++);
    }
    data = NULL;

    LOGI("%p:\n %s", buffer, ss.str().c_str());

    ss.clear();
}

//test code
__inline void print_sharedmemory(const TEEC_SharedMemory* sm){
    if(sm == NULL) return;
    LOGI("flags:%d, size:%d", sm->flags, sm->size);
}

//test code
void print_sharedmemory_map(){
    LOGD("[start]%s", __FUNCTION__);

    if ( sharedmemory_map.empty() ){
        LOGI("\tshared memory map is empty");
    }

    for(unordered_map<int, TEEC_SharedMemory*>::iterator sm = sharedmemory_map.begin(); sm != sharedmemory_map.end(); sm++){
        LOGI("\t[%p] shared memory id:%d, size:%d, flag:%d, buffer_addr:%p, buffer:%.*s",
                     sm->second,
                     sm->first,
                     sm->second->size,
                     sm->second->flags,
                     sm->second->buffer,
                     sm->second->size,
                     sm->second->buffer);

        hex_dump(sm->second->buffer, sm->second->size);
    }
    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

//test code
void print_bytearray(uint8_t* buffer, int len){
    LOGD("[start]%s", __FUNCTION__);

    for(int i = 0; i < len; i++){
        LOGD("%x", *(buffer + i));
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

/*
    find shared memory instance by id.
    If found, return a pointer to the shared memory.
    Else, return a NULL pointer.
*/
TEEC_SharedMemory* find_sharedmemory_by_id(int smid){
    LOGD("[start]%s", __FUNCTION__);

    unordered_map<int, TEEC_SharedMemory*>::iterator sm = sharedmemory_map.find(smid);
    if(sm == sharedmemory_map.end()){
        LOGE("\tunable to find shared memory with id %d", smid);
        return NULL;
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);

    return sm->second;
}

/*
    remove the shared memory with id.
    return true if succeed. Otherwise return false.
*/
bool remove_sharedmemory_by_id(int smid){
    LOGD("[start]%s", __FUNCTION__);

    TEEC_SharedMemory* sm = find_sharedmemory_by_id(smid);
    if ( sm != NULL ){
        free(sm->buffer);
        free(sm);
        sharedmemory_map.erase(smid);

        LOGD("[end  ]%s\n\r", __FUNCTION__);
        return true;
    }

    return false;
}

/*
    initial set up.
*/
void preparationFunc(JNIEnv *env, jstring otSocketFilePathInJava) {
    LOGD("[start]%s", __FUNCTION__);

    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    /**
     * set up OPENTEE_SOCKET_FILE_PATH env var.
     */
    char *tmpEnv = getenv("OPENTEE_SOCKET_FILE_PATH");

    if (NULL == tmpEnv) {
        LOGE("\tOPENTEE_SOCKET_FILE_PATH not set. Try to overwrite.");

        const char *otSocketFilePath = env->GetStringUTFChars(otSocketFilePathInJava, 0);

        int return_code = setenv("OPENTEE_SOCKET_FILE_PATH",
                                 otSocketFilePath,
                                 1);
        env->ReleaseStringUTFChars(otSocketFilePathInJava, otSocketFilePath);

        if (return_code == 0) {
            LOGI("\tSet socket env val succeed.");
            open_tee_socket_env_set = 1;
        } else {
            LOGE("\tSet socket env val failed");
        }
    }
    else {
        LOGI("\t%s is already set.", tmpEnv);
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

/**
 * Initialize Context.
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecInitializeContext
        (JNIEnv *env, jclass jc, jstring teeName, jstring otSocketFilePathInJava) {
    LOGD("[start]%s", __FUNCTION__);


    // Is the environment variable for the path of open_tee_socket set?
    if (0 == open_tee_socket_env_set)
        preparationFunc(env, otSocketFilePathInJava);

    TEEC_Result tmpResult;

    if (teeName == NULL) {
        // initialize context with default TEE.
        tmpResult = TEEC_InitializeContext(NULL, &g_contextRecord);
    }
    else {
        // get the name of the TEE.
        const char *teeNameInC = env->GetStringUTFChars(teeName, 0);

        // initialize context with specific TEE.
        tmpResult = TEEC_InitializeContext(teeNameInC, &g_contextRecord);

        // release the string
        env->ReleaseStringUTFChars(teeName, teeNameInC);
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);

    return tmpResult;
}

/*
    clean all the shared memory.
*/
void clean_sharedmemory_buffer(){
    LOGD("[start]%s", __FUNCTION__);

    for(unordered_map<int, TEEC_SharedMemory*>::iterator sm = sharedmemory_map.begin(); sm != sharedmemory_map.end(); sm++){
        free(sm->second->buffer);
        free(sm->second);
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

/*
 * Finalize Context.
 */
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecFinalizeContext
        (JNIEnv *env, jclass jc) {
    LOGI("%s: Shutdown libprotobuf lib and Finalize Context", __FUNCTION__);

    //clean resources
    clean_sharedmemory_buffer();
    sharedmemory_map.clear();

    sessions_map.clear();

    TEEC_FinalizeContext(&g_contextRecord);
    g_contextRecord = {0};

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();

    LOGI("%s: done", __FUNCTION__);
}

/*
    Register shared memory.
*/
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecRegisterSharedMemory
        (JNIEnv *env, jclass jc, jbyteArray jOTSharedMemory, jint jSmId) {
    LOGD("[start]%s", __FUNCTION__);

    if(jOTSharedMemory == NULL){
        return TEEC_ERROR_NO_DATA;
    }

    int l = env->GetArrayLength(jOTSharedMemory);
    uint8_t otSharedMemory[l];
    env->GetByteArrayRegion(jOTSharedMemory, 0, l, (jbyte* )otSharedMemory);

    // transfer java type shared memory into c type.
    string data(otSharedMemory, otSharedMemory + l);

    // parse shard memory from string.
    TeecSharedMemory pbsm;
    pbsm.ParseFromString(data);

    uint32_t mFlag = pbsm.mflag();
    string mBuffer = pbsm.mbuffer();

    TEEC_SharedMemory* cOTSharedMemory;
    if( (cOTSharedMemory = (TEEC_SharedMemory*)malloc(sizeof(TEEC_SharedMemory)) ) == NULL){
        LOGE("Unable to malloc for shared memory");
        return TEEC_ERROR_OUT_OF_MEMORY;
    }

    cOTSharedMemory->size = mBuffer.length();
    if( ( cOTSharedMemory->buffer = (void*)malloc(cOTSharedMemory->size * sizeof(uint8_t)) ) == NULL){
        LOGE("Unable to malloc for shared memory buffer");
        return TEEC_ERROR_OUT_OF_MEMORY;
    }

    memcpy(cOTSharedMemory->buffer, mBuffer.c_str(), cOTSharedMemory->size);
    cOTSharedMemory->flags = mFlag;

    TEEC_Result return_code = TEEC_RegisterSharedMemory(&g_contextRecord, cOTSharedMemory);

    LOGI("\t flag: %x, buffer:%s, size:%d, return_code:%x",
         cOTSharedMemory->flags,
         cOTSharedMemory->buffer,
         cOTSharedMemory->size,
         return_code);

    // if register shared memory succeed, add it to the global shared memory array.
    if ( return_code == TEEC_SUCCESS ){
        sharedmemory_map.emplace(jSmId, cOTSharedMemory);
    }

    // test code
    print_sharedmemory_map();

    LOGD("[end  ]%s\n\r", __FUNCTION__);

    return return_code;
}

/*
    Release shared memory.
*/
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecReleaseSharedMemory
        (JNIEnv* env, jclass jc, jint jsmId){
    LOGD("[start]%s", __FUNCTION__);

    TEEC_SharedMemory* sm = find_sharedmemory_by_id(jsmId);
    if(sm == NULL) return;

    LOGI("\t%s is to be released.", sm->buffer);

    TEEC_ReleaseSharedMemory(sm);

    LOGI("\t%d is released.", jsmId);

    remove_sharedmemory_by_id(jsmId);

    //test code.
    print_sharedmemory_map();

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

//test code
void printSharedMemory(const TEEC_SharedMemory* sm){
    if(sm == NULL) return;
    LOGI("\t\t[%p]buffer:%s, flag:%x, size:%d", sm->buffer, sm->buffer, sm->flags, sm->size);
}

//test code
void print_TEEC_Operation(const TEEC_Operation* op){
    LOGD("[start]%s", __FUNCTION__);

    LOGE("\tstarted:%d, paraType:%x", op->started, op->paramTypes);

    for(int i = 0; i < 4; i++){
        uint32_t  type = TEEC_PARAM_TYPE_GET(op->paramTypes, i);
        if( type == 0){
            LOGI("\t\tParam is none");
        }
        else if(type == TEEC_VALUE_INPUT ||
                type == TEEC_VALUE_OUTPUT ||
                type == TEEC_VALUE_INOUT){
            TEEC_Value value = op->params[i].value;
            LOGI("\t\t[Value]a:0x%x, b:0x%x", value.a, value.b);
        }
        else if(type == TEEC_MEMREF_WHOLE ||
                type == TEEC_MEMREF_PARTIAL_INPUT ||
                type == TEEC_MEMREF_PARTIAL_OUTPUT ||
                type == TEEC_MEMREF_PARTIAL_INOUT){
            TEEC_SharedMemory* sm = op->params[i].memref.parent;

            LOGD("\t\t Param is memory reference. memref.size:%d", op->params[i].memref.size);
            printSharedMemory(sm);
        }
    }

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

__inline void print_clock_seq_and_node(uint8_t vars[8]){
    LOGI("ClockSeqAndNode:%.*s", 8, (char*)vars);
}

/*
    set return origin.
*/
__inline void set_return_origin(JNIEnv* env, jobject returnOrigin, int var){
    jclass jcRetOrigin = env->GetObjectClass(returnOrigin);
    jfieldID jfROC = env->GetFieldID(jcRetOrigin, "mValue", "I");
    env->SetIntField(returnOrigin, jfROC, var);
}

/*
    set return code.
*/
__inline void set_return_code(JNIEnv* env, jobject returnCode, int var){
    set_return_origin(env, returnCode, var);
}

/*
    transfer operation in the format of c++ string to TEEC_Operation.
*/
bool transfer_opString_to_TEEC_Operation(JNIEnv* env, const string opsInString, TEEC_Operation* teec_operation){
    LOGD("[start]%s", __FUNCTION__);

    TeecOperation op;

    op.ParseFromString(opsInString);

    LOGI("\tstarted %d. num of params:%d",
          op.mstarted(),
          op.mparams_size());

    teec_operation->started = op.mstarted();

    uint32_t paramTypesArray[] = {TEEC_NONE, TEEC_NONE, TEEC_NONE, TEEC_NONE};

    for(int i = 0; i < op.mparams_size(); i++){

        const TeecParameter param = op.mparams(i);

        if( param.has_teecsharedmemoryreference() ){
            // param is TEEC_RegisteredMemoryReference.
            LOGI("\t\tparam is TEEC_RMR.");
            const TeecSharedMemoryReference rmr = param.teecsharedmemoryreference();

            int smId = rmr.parent().mid();

            TEEC_SharedMemory* sm = find_sharedmemory_by_id(smId);

            if(sm == NULL){
                LOGE("\t\tinternal error -- unable to find shared memory with id.", smId);
                return false;
            }

            LOGD("\t\told buffer with flag:%x", sm->flags);
            hex_dump(sm->buffer, sm->size);

            //the share memory is not only output for TA. So sync it back to CA.
            if(rmr.parent().mflag() != JavaConstants::MEMREF_OUTPUT){
                int8_t * smBuffer = (int8_t*)rmr.parent().mbuffer().c_str();

                memcpy(sm->buffer, smBuffer, sizeof(smBuffer));

                LOGE("\t\tnew buffer:");
                hex_dump(sm->buffer, sm->size);
            }

            teec_operation->params[i].memref.parent = sm;
            teec_operation->params[i].memref.size = sm->size;
            teec_operation->params[i].memref.offset = rmr.moffset();

            // set flag for shared memory.
            if(rmr.moffset() > 0){
                // using part of the memory.
                switch(rmr.mflag()){
                    case JavaConstants::MEMREF_INPUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_INPUT;
                        break;
                    case JavaConstants::MEMREF_OUTPUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_OUTPUT;
                        break;
                    case JavaConstants::MEMREF_INOUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_INOUT;
                        break;
                    default:
                        break;
                }
            }
            else{
                // using whole memory.
                paramTypesArray[i] = TEEC_MEMREF_WHOLE;
            }
        }
        else if(param.has_teecvalue()){
            // param is TEEC_Value.
            LOGI("\t\tparam is TEEC_VALUE");

            const TeecValue value = param.teecvalue();
            teec_operation->params[i].value.a = value.a();
            teec_operation->params[i].value.b = value.b();

            // set the flag based on the flag value from java layer.
            switch ( value.mflag() ){
                case JavaConstants::VALUE_INPUT:
                    paramTypesArray[i] = TEEC_VALUE_INPUT;
                    break;
                case JavaConstants::VALUE_OUTPUT:
                    paramTypesArray[i] = TEEC_VALUE_OUTPUT;
                    break;
                case JavaConstants::VALUE_INOUT:
                    paramTypesArray[i] = TEEC_VALUE_INOUT;
                    break;
                default:
                    LOGE("\t\t\tunaccepted flag for Value %x", value.mflag());
                    break;
            }

        }else{
            LOGE("\t\tIncorrect param or null.");
            continue;
        }
    }

    // set paramTypes field.
    teec_operation->paramTypes = TEEC_PARAM_TYPES(paramTypesArray[0],
                                                 paramTypesArray[1],
                                                 paramTypesArray[2],
                                                 paramTypesArray[3]);

    LOGD("[end  ]%s\n\r", __FUNCTION__);

    return true;
} // end of transfer_opString_to_TEEC_Operation.

/**
 * transfer the operation in jbyteArray into TEEC_Operation.
 */
void transfer_op_to_TEEC_Operation(JNIEnv* env, const jbyteArray opInBytes, TEEC_Operation* teec_operation){
    LOGD("[start]%s", __FUNCTION__);

    if(opInBytes == NULL){
        LOGI("\top is null");
        return;
    }

    int l = env->GetArrayLength(opInBytes);

    uint8_t* opInBytesBuffer = (uint8_t *)malloc(l*sizeof(uint8_t));

    env->GetByteArrayRegion(opInBytes, 0, l, (jbyte*)opInBytesBuffer);

    // create a string to store this buffer.
    string opsInString(opInBytesBuffer, opInBytesBuffer + l);

    free(opInBytesBuffer);

    transfer_opString_to_TEEC_Operation(env, opsInString, teec_operation);

    LOGD("[end  ]%s\n\r", __FUNCTION__);
}

/**
 * transfer TEEC_Operation to jbyteArray.
 */
jbyteArray transfer_TEEC_Operation_to_op(JNIEnv* env, const TEEC_Operation* teec_operation, jbyteArray opInBytes){
    LOGD("\n[start]%s", __FUNCTION__);

    if(opInBytes == NULL){
        LOGE("incorrect operation.");
        return NULL;
    }

    int l = env->GetArrayLength(opInBytes);

    uint8_t* opInBytesBuffer = (uint8_t *)malloc(l*sizeof(uint8_t));

    // store the byte array into buffer.
    env->GetByteArrayRegion(opInBytes, 0, l, (jbyte*)opInBytesBuffer);

    // create a string to store this buffer.
    string opsInString(opInBytesBuffer, opInBytesBuffer + l);

    free(opInBytesBuffer);

    TeecOperation op;
    op.ParseFromString(opsInString);

    op.set_mstarted(teec_operation->started);

    /**
     * check and validate changes.
     */
    for(int i = 0; i < 4; i++){
        uint32_t type = TEEC_PARAM_TYPE_GET(teec_operation->paramTypes, i);

        LOGI("\ttype=%x", type);

        // for TEEC_Value with output flag.
        if(type == TEEC_VALUE_OUTPUT ||
           type == TEEC_VALUE_INOUT){
            TeecValue value = op.mparams(i).teecvalue();

            LOGI("\t[Old value]a:%x b:%x", value.a(), value.b());
            LOGI("\t[New value]a:%x b:%x", teec_operation->params[i].value.a, teec_operation->params[i].value.b);

            // regardless whether value is changed or not, just sync it back.
            op.mutable_mparams(i)->mutable_teecvalue()->set_a(teec_operation->params[i].value.a);
            op.mutable_mparams(i)->mutable_teecvalue()->set_b(teec_operation->params[i].value.b);
        }

        // for TEEC_SharedMemory with output flag.
        else if(type == TEEC_MEMREF_WHOLE ||
                type == TEEC_MEMREF_PARTIAL_OUTPUT ||
                type == TEEC_MEMREF_PARTIAL_INOUT){
            TeecSharedMemory* sm = op.mutable_mparams(i)->mutable_teecsharedmemoryreference()->mutable_parent();
            const TEEC_SharedMemory* shared_memory = teec_operation->params[i].memref.parent;

            //test code
            print_sharedmemory(shared_memory);

            if(shared_memory->flags >= TEEC_MEM_OUTPUT){
                sm->set_mbuffer((char*)shared_memory->buffer, shared_memory->size);
                sm->set_mreturnsize(teec_operation->params[i].memref.size);

                LOGE("\tnew sm size:%d", sm->size());
            }
            else{
                LOGI("\tshared memory is not for output, will not synced back.");
            }

        }
        else{
            LOGI("\tTEEC_NONE or without OUTPUT field, will not be synced back.");
        }
    }

    string new_op;
    op.SerializeToString(&new_op);

    LOGD("\tnew_op len:%d", new_op.length());

    jbyteArray new_op_in_bytes = env->NewByteArray(new_op.length());
    env->SetByteArrayRegion(new_op_in_bytes, 0, new_op.length(), (jbyte*)new_op.c_str());

    LOGD("[end  ]%s\n\r", __FUNCTION__);

    return new_op_in_bytes;
} // end of transfer_TEEC_Operation_to_op

/*
    Open session.
*/
JNIEXPORT jbyteArray JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecOpenSession
(JNIEnv* env, jclass jc, jint sid, jobject uuid, jint connMethod, jint connData, jbyteArray opInBytes, jobject returnOrigin, jobject returnCode, jint opHashCodeWithPid){
    LOGI("[start] %s", __FUNCTION__);

    /* UUID uuid to TEEC_UUID */
    jclass jcUuid = env->GetObjectClass(uuid);
    jmethodID jmGetLeastSignificantBits = env->GetMethodID(jcUuid,
                                                           "getLeastSignificantBits", // method name.
                                                           "()J" // input void, return long.
                                                            );
    jmethodID jmGetMostSignificantBits = env->GetMethodID(jcUuid,
                                                           "getMostSignificantBits", // method name.
                                                           "()J" // input void, return long.
    );

    if( jmGetLeastSignificantBits == 0 || jmGetMostSignificantBits == 0){
        // set return origin TEEC_ORIGIN_API.
        set_return_origin(env, returnOrigin, TEEC_ORIGIN_API);

        set_return_code(env, returnCode, TEEC_ERROR_BAD_PARAMETERS);
    }

    uint64_t lsBits, msBits;
    lsBits = msBits = 0;
    lsBits = env->CallLongMethod(uuid, jmGetLeastSignificantBits);
    msBits = env->CallLongMethod(uuid, jmGetMostSignificantBits);

    LOGI("\tuuid:%llx %llx.", msBits, lsBits);

    TEEC_UUID teec_uuid = { .timeLow = (uint32_t)(msBits >> 32),
                            .timeMid = (uint16_t)(msBits >> 16),
                            .timeHiAndVersion = (uint16_t)msBits};

    for(int i = 7; i >= 0; i--){
        teec_uuid.clockSeqAndNode[i] = (uint8_t)lsBits;
        lsBits = lsBits >> 8;
    }

    LOGI("\ttimeLow:%x, timeMid:%x, timeHighAndVersion:%x",
                        teec_uuid.timeLow,
                        teec_uuid.timeMid,
                        teec_uuid.timeHiAndVersion);

    print_clock_seq_and_node(teec_uuid.clockSeqAndNode);

    jbyteArray new_op_in_bytes = NULL;
    uint32_t teec_ret_ori = 0;
    TEEC_Result teec_ret = TEEC_SUCCESS;
    static TEEC_Session teec_session = {0};

    if( opInBytes != NULL ){
        // Parsing TEEC_Operation from op in bytes.
        TEEC_Operation teec_operation = {0};
        transfer_op_to_TEEC_Operation(env, opInBytes, &teec_operation);

        print_TEEC_Operation(&teec_operation);

        // add operation into operations_map for future cancellation.
        operations_map.emplace(opHashCodeWithPid, &teec_operation);

        /**
         * call TEEC_OpenSession with operation.
         */
        teec_ret = TEEC_OpenSession(
                &g_contextRecord,
                &teec_session,
                &teec_uuid,
                (uint32_t)connMethod,
                &connData,
                &teec_operation,
                &teec_ret_ori
        );


        LOGD("\tconnMethod:%.8x, connData:%.8x, return code:%.8x, return origin:%.8x",
             connMethod,
             connData,
             teec_ret,
             teec_ret_ori
        );
        // remove the operation once done using it.
        operations_map.erase(opHashCodeWithPid);

        /**
        * sync shared memory and Value back.
        */
        new_op_in_bytes = transfer_TEEC_Operation_to_op(env, &teec_operation, opInBytes);
    }
    else{
        // open session without operation.
        teec_ret = TEEC_OpenSession(
                        &g_contextRecord,
                        &teec_session,
                        &teec_uuid,
                        (uint32_t)connMethod,
                        &connData,
                        NULL,
                        &teec_ret_ori
                );
    }

    //store the session upon success.
    if( teec_ret == TEEC_SUCCESS ){
        LOGI("\tsucceed");
        sessions_map.emplace((int)sid, &teec_session);
    }

    // set return origin
    set_return_origin(env, returnOrigin, teec_ret_ori);

    // set return code
    set_return_code(env, returnCode, teec_ret);

    LOGI("[end  ] %s\n\r", __FUNCTION__);

    // return updated operation.
    return new_op_in_bytes;
}// end of Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecOpenSession

/*
    Close session.
*/
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecCloseSession
        (JNIEnv* env, jclass jc, jint sidInJni){
    auto sessionWithId = sessions_map.find(sidInJni);

    if( sessionWithId == sessions_map.end() ){
        LOGE("Unable to find session with id %d.", sidInJni);
        return;
    }

    TEEC_CloseSession(sessionWithId->second);
    LOGI("Session with id %d is closed.", sidInJni);
}

/*
    Find session by id.
*/
TEEC_Session* find_session_by_id(int sid){
    LOGI("[start] %s", __FUNCTION__);

    unordered_map<int,TEEC_Session*>::iterator session = sessions_map.find(sid);

    if(session == sessions_map.end()){
        LOGE("\tcannot find session with id %d", sid);
        return NULL;
    }

    LOGI("[end  ] %s\n\r", __FUNCTION__);

    return session->second;
}

/*
    Invoke command.
*/
JNIEXPORT jbyteArray JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecInvokeCommand
(JNIEnv* env, jclass jc, jint sid, jint commandId, jbyteArray opInBytes, jobject returnOrigin, jobject returnCode, jint opHashCodeWithPid){
    LOGD("[start] %s", __FUNCTION__);

    LOGD("\tIssuing command with id = %d", commandId);

    // test code
    print_sharedmemory_map();

    jbyteArray new_op_in_bytes = NULL;

    uint32_t teec_ret_ori = TEEC_ORIGIN_API;
    TEEC_Result teec_ret = TEEC_ERROR_BAD_PARAMETERS;

    TEEC_Session* teec_session = NULL;
    teec_session = find_session_by_id(sid);
    if(teec_session != NULL){
            if(opInBytes != NULL){
                //Parsing TEEC_Operation from op in bytes.
                TEEC_Operation teec_operation = {0};
                transfer_op_to_TEEC_Operation(env, opInBytes, &teec_operation);

                print_TEEC_Operation(&teec_operation);

                // add the operation to operations_map for cancel if there is any.
                operations_map.emplace(opHashCodeWithPid, &teec_operation);

                /**
                 * call TEEC_InvokeCommand. It changed the size of shared memory.
                 */
                LOGD("--------------- Issuing TEEC_InvokeComamnd with operation here.---------------------");

                teec_ret = TEEC_InvokeCommand(
                        teec_session,
                        commandId,
                        &teec_operation,
                        &teec_ret_ori
                );

                LOGD("\t\tTEEC_InvokeCommand return code:%.8x", teec_ret);

                // remove the operation from the operations_map after done using it.
                operations_map.erase(opHashCodeWithPid);

                // sync shared memory and Value back.
                new_op_in_bytes = transfer_TEEC_Operation_to_op(env, &teec_operation, opInBytes);
            }
            else{
                LOGD("--------------- Issuing TEEC_InvokeComamnd without operation here.---------------------");

                teec_ret = TEEC_InvokeCommand(
                                teec_session,
                                commandId,
                                NULL,
                                &teec_ret_ori
                );
            }
    }

    // set return origin
    set_return_origin(env, returnOrigin, teec_ret_ori);

    // set return code
    set_return_code(env, returnCode, teec_ret);

    // test code
    print_sharedmemory_map();

    LOGI("[end  ] %s\n\r", __FUNCTION__);

    //return updated operation in byte array;
    return new_op_in_bytes;

}// end of Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecInvokeCommand

/*
    Request cancellation.
*/
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecRequestCancellation
(JNIEnv* env, jclass jc, jint opHashCodeWithPid){
    LOGI("[start] %s", __FUNCTION__);

    // find operation.
    unordered_map<int, TEEC_Operation*>::iterator iOp = operations_map.find(opHashCodeWithPid);
    if( iOp == operations_map.end() ){
        LOGI("\tUnable to find the operation with hash code %d. Maybe it is already finished", opHashCodeWithPid);
        LOGI("[end  ] %s\n\r", __FUNCTION__);
        return;
    }

    if(iOp->second != NULL){
        LOGI("\t %d found and it is not null. Sending request cancellation to opentee", opHashCodeWithPid);
        TEEC_RequestCancellation(iOp->second);
    }
    else{
        LOGE("\t internal error, operation is null");
    }

    LOGI("[end  ] %s\n\r", __FUNCTION__);
}

#ifdef __cplusplus
}
#endif