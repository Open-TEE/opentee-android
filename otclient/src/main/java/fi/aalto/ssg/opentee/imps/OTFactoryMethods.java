/*
 * Copyright (c ) 2016 Aalto University
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
package fi.aalto.ssg.opentee.imps;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.AccessConflictException;
import fi.aalto.ssg.opentee.exception.AccessDeniedException;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.BadStateException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CancelErrorException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.ItemNotFoundException;
import fi.aalto.ssg.opentee.exception.NoDataException;
import fi.aalto.ssg.opentee.exception.NoStorageSpaceException;
import fi.aalto.ssg.opentee.exception.NotImplementedException;
import fi.aalto.ssg.opentee.exception.NotSupportedException;
import fi.aalto.ssg.opentee.exception.OutOfMemoryException;
import fi.aalto.ssg.opentee.exception.OverflowException;
import fi.aalto.ssg.opentee.exception.SecurityErrorException;
import fi.aalto.ssg.opentee.exception.ShortBufferException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.exception.TargetDeadException;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * Shared factory methods.
 */
public class OTFactoryMethods {
    public static void print_op(String tag, GPDataTypes.TeecOperation opToPrint){
        if(opToPrint == null){
            Log.e(tag, "op is null");
            return;
        }

        Log.d(tag, "started:" + opToPrint.getMStarted());
        for(GPDataTypes.TeecParameter param: opToPrint.getMParamsList()){
            if (param.getType() == GPDataTypes.TeecParameter.Type.smr){
                GPDataTypes.TeecSharedMemory sm = param.getTeecSharedMemoryReference().getParent();
                Log.d(tag, "[SMR] flag:" + sm.getMFlag() +
                        " buffer:" + sm.getMBuffer().toStringUtf8().toString() +
                        " size:" + sm.getMBuffer().size());
            }
            else if (param.getType() == GPDataTypes.TeecParameter.Type.val){
                GPDataTypes.TeecValue var = param.getTeecValue();
                Log.d(tag, "[VALUE] flag:" + var.getMFlag() +
                        " a:" + Integer.toHexString(var.getA()) +
                        " b:" + Integer.toHexString(var.getB()) );
            }
            else{
                Log.d(tag, "param is null");
            }
        }
    }

    public static void print_op_in_bytes(String tag, byte[] opInBytes){
        Log.i(tag, "[start] print_op_in_bytes");

        if (opInBytes == null){
            Log.e(tag, "op is null");
        }

        print_op(tag, transferOpInBytesToOperation(tag, opInBytes));

        Log.i(tag, "[end] print_op_in_bytes");
    }

    public static GPDataTypes.TeecOperation transferOpInBytesToOperation(String TAG, byte[] opInBytes){
        if(opInBytes == null){
            Log.e(TAG, "Empty operation");
            return null;
        }
        GPDataTypes.TeecOperation.Builder opBuilder = GPDataTypes.TeecOperation.newBuilder();
        try {
            opBuilder.mergeFrom(opInBytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return opBuilder.build();
    }

//    public static final GPDataTypes.TeecParameter paramPlaceHolder = GPDataTypes.TeecParameter.newBuilder().build();

    public static byte[] OperationAsByteArray(String TAG, ITEEClient.IOperation iOperation){
        if ( iOperation == null )return null;
        OTOperation teecOperation = (OTOperation)iOperation;

        GPDataTypes.TeecOperation.Builder toBuilder = GPDataTypes.TeecOperation.newBuilder();

        byte[] opInArray = null;
        if (teecOperation.getParams() != null && teecOperation.getParams().size() > 0){
            /**
             * determine which type of parameter to parse.
             */

            List<ITEEClient.IParameter> parameterList = teecOperation.getParams();

            for(int i = 0 ; i < parameterList.size(); i++){
                ITEEClient.IParameter param = parameterList.get(i);
            //for ( ITEEClient.IParameter param: parameterList ){
                if(param == null){
                    Log.i(TAG, "Param is null");
                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.empty);
                    toBuilder.addMParams(paramBuilder.build());
                    continue;
                }

                if( param.getType() == ITEEClient.IParameter.Type.TEEC_PTYPE_VAL){
                    Log.i(TAG, "Param is " + ITEEClient.IParameter.Type.TEEC_PTYPE_VAL);

                    GPDataTypes.TeecValue.Builder builder = GPDataTypes.TeecValue.newBuilder();
                    ITEEClient.IValue iVal = (ITEEClient.IValue)param;
                    OTValue val = (OTValue)iVal;

                    builder.setA(val.getA());
                    builder.setB(val.getB());

                    builder.setMFlag(GPDataTypes.TeecValue.Flag.values()[val.getFlag().ordinal()]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.val);
                    paramBuilder.setTeecValue(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else if ( param.getType() == ITEEClient.IParameter.Type.TEEC_PTYPE_RMR ){
                    Log.i(TAG, "Param is " + ITEEClient.IParameter.Type.TEEC_PTYPE_RMR);

                    GPDataTypes.TeecSharedMemoryReference.Builder builder
                            = GPDataTypes.TeecSharedMemoryReference.newBuilder();
                    ITEEClient.IRegisteredMemoryReference iRmr
                            = (ITEEClient.IRegisteredMemoryReference)param;
                    OTRegisteredMemoryReference rmr = (OTRegisteredMemoryReference)iRmr;
                    OTSharedMemory teecSM = (OTSharedMemory)rmr.getSharedMemory();

                    //create gp shared memory from teec shared memory.
                    GPDataTypes.TeecSharedMemory.Builder gpSMBuilder = GPDataTypes.TeecSharedMemory.newBuilder();
                    gpSMBuilder.setSize(teecSM.getSize());
                    gpSMBuilder.setMID(teecSM.getId());
                    gpSMBuilder.setMFlag(teecSM.getFlags());
                    gpSMBuilder.setMBuffer(ByteString.copyFrom(teecSM.asByteArray()));
                    gpSMBuilder.setMReturnSize(teecSM.getReturnSize());

                    builder.setParent(gpSMBuilder.build());
                    builder.setMOffset(rmr.getOffset());
                    builder.setMFlag(GPDataTypes.TeecSharedMemoryReference.Flag.values()[rmr.getFlag().ordinal()]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.smr);
                    paramBuilder.setTeecSharedMemoryReference(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else{
                    Log.e(TAG, "Unsupported Operation type. Set the operation to null");
                }

            }
        }

        toBuilder.setMStarted(teecOperation.getStarted());
        opInArray = toBuilder.build().toByteArray();

        return opInArray;
    }

    //note: switch statement can also apply in here.
    public static void throwExceptionBasedOnReturnCode(int return_code) throws TEEClientException {
        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new AccessConflictException("Access conflict.");
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new AccessDeniedException("Access denied.");
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new BadFormatException("Bad format");
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new BadParametersException("Bad parameters.");
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new BadStateException("Bad state");
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new BusyException("Busy");
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new CancelErrorException("Cancel");
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new CommunicationErrorException("Communication error");
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ExcessDataException("Excess data");
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new GenericErrorException("Generic error");
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ItemNotFoundException("Item not found");
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new NoDataException("Not data provided");
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new NotImplementedException("Not impelemented");
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new NotSupportedException("Not supported");
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new OutOfMemoryException("Out of memory");
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new SecurityErrorException("Security check failed");
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ShortBufferException("Short buffer");
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ExternalCancelException("External cancel");
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new OverflowException("Overflow");
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new TargetDeadException("TEE: target dead");
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new NoStorageSpaceException("Storage no space");
            default:
                break;
            //throw new TEEClientException("Unknown error");
        }

    }

    private static ITEEClient.ReturnOriginCode intToReturnOrigin(int roInt){
        int len = ITEEClient.ReturnOriginCode.values().length;
        if( len < roInt || roInt <= 0) {
            Log.e("intToReturnOrigin", "return origin in int = " + roInt);

            return null;
        }

        return ITEEClient.ReturnOriginCode.values()[roInt - 1];
    }

    public static void throwExceptionWithReturnOrigin(String TAG, int return_code, int retOrigin) throws TEEClientException{
        ITEEClient.ReturnOriginCode returnOriginCode = intToReturnOrigin(retOrigin);

        if ( returnOriginCode == null ){
            Log.e(TAG, "Incorrect return code with return origin =  " + retOrigin);
        }

        switch (return_code){
            case OTReturnCode.TEEC_ERROR_ACCESS_CONFLICT:
                throw new AccessConflictException("Access conflict.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ACCESS_DENIED:
                throw new AccessDeniedException("Access denied.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_FORMAT:
                throw new BadFormatException("Bad format", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_PARAMETERS:
                throw new BadParametersException("Bad parameters.", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BAD_STATE:
                throw new BadStateException("Bad state", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_BUSY:
                throw new BusyException("Busy", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_CANCEL:
                throw new CancelErrorException("Cancel", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_COMMUNICATION:
                throw new CommunicationErrorException("Communication error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_EXCESS_DATA:
                throw new ExcessDataException("Excess data", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_GENERIC:
                throw new GenericErrorException("Generic error", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_ITEM_NOT_FOUND:
                throw new ItemNotFoundException("Item not found", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NO_DATA:
                throw new NoDataException("Not data provided", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_IMPLEMENTED:
                throw new NotImplementedException("Not impelemented", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_NOT_SUPPORTED:
                throw new NotSupportedException("Not supported", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_OUT_OF_MEMORY:
                throw new OutOfMemoryException("Out of memory", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SECURITY:
                throw new SecurityErrorException("Security check failed", returnOriginCode);
            case OTReturnCode.TEEC_ERROR_SHORT_BUFFER:
                throw new ShortBufferException("Short buffer", returnOriginCode);
            case OTReturnCode.TEE_ERROR_EXTERNAL_CANCEL:
                throw new ExternalCancelException("External cancel", returnOriginCode);
            case OTReturnCode.TEE_ERROR_OVERFLOW:
                throw new OverflowException("Overflow", returnOriginCode);
            case OTReturnCode.TEE_ERROR_TARGET_DEAD:
                throw new TargetDeadException("TEE: target dead", returnOriginCode);
            case OTReturnCode.TEE_ERROR_STORAGE_NO_SPACE:
                throw new NoStorageSpaceException("Storage no space", returnOriginCode);
            default:
                break;
            //throw new TEEClientException("Unknown error", returnOriginCode);
        }
    }
}
