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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * This class implements the IContext interface
 */
public class OTContext implements ITEEClient.IContext, OTContextCallback {
    final String TAG = "OTContext";

    String mTeeName = null;
    boolean mInitialized = false;
    ProxyApis mProxyApis = null; // one service connection per context
    Random smIdGenerator = null;
    Context mContext;

    List<OTSharedMemory> mSharedMemory = new ArrayList<>();
    HashMap<Integer, Integer> mSessionMap = new HashMap<>(); // <sessionId, placeHolder>

    public OTContext(String teeName, Context context) throws TEEClientException {
        this.mTeeName = teeName;
        this.smIdGenerator = new Random();
        this.mContext = context;

        /**
         * connect to the IOpenTEE
         */
        OTLock lock = new OTLock();
        ServiceGetterThread serviceGetterThread = new ServiceGetterThread(teeName, context, lock);
        Thread st = new Thread(serviceGetterThread);
        st.start();

        try {
            st.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // wait until service bound.
        lock.lock();
        lock.unlock();

        mProxyApis = serviceGetterThread.getProxyApis();

        if(mProxyApis == null){
            throw new CommunicationErrorException("Unable to connect to remote TEE service",
                    ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }
        this.mInitialized = true;

        Log.d(TAG, "Service connected.");
    }

    @Override
    public void finalizeContext() throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Nothing to finalize");
            return;
        }

        if ( mProxyApis != null ){
            try {
                mProxyApis.teecFinalizeContext();
            } catch (RemoteException e) {
                throw new CommunicationErrorException("Communication error with remote TEE service.");
            }
            mProxyApis.terminateConnection();
        }

        mTeeName = null;
        mInitialized = false;
        smIdGenerator = null;
        mContext = null;
        mProxyApis = null;

        //clear up resources.
        mSharedMemory.clear();
        mSessionMap.clear();

        Log.i(TAG, "context finalized and connection terminated");
    }

    @Override
    public ITEEClient.ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.e(TAG, "Not ready to register shared memory");
            return null;
        }

        if( buffer == null ){
            throw new BadParametersException("provided buffer is null", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }

        if( flags != ITEEClient.ISharedMemory.TEEC_MEM_INPUT &&
            flags != ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT &&
            flags != ( ITEEClient.ISharedMemory.TEEC_MEM_INPUT | ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT)){
            throw new BadParametersException("incorrect flags.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }

        int smId = generateSmId();

        // create a shared memory
        OTSharedMemory otSharedMemory = new OTSharedMemory(buffer, flags, smId);

        // register the shared memory
        try {
            mProxyApis.teecRegisterSharedMemory(otSharedMemory);
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // add the registered shared memory to mSharedMemory list.
        mSharedMemory.add(otSharedMemory);

        return otSharedMemory;
    }

    @Override
    public void releaseSharedMemory(ITEEClient.ISharedMemory sharedMemory) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to release shared memory");
            return;
        }

        if(sharedMemory == null){
            throw new BadParametersException("input shared memory interface is null", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }

        // tell remote tee to release the shared memory.
        try {
            mProxyApis.teecReleaseSharedMemory(((OTSharedMemory)sharedMemory).getId());
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // remove it from shared memory list.
        if (!mSharedMemory.remove(sharedMemory)){
            throw new BadParametersException("Unable to find the input shared memory.",
                    ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }
    }

    private void updateOperation(OTOperation otOperation, byte[] opInBytes) throws ExcessDataException, BadFormatException {
        GPDataTypes.TeecOperation op = OTFactoryMethods.transferOpInBytesToOperation(TAG, opInBytes);

        if(op == null) return;

        //test code
        Log.d(TAG, "length of operation " + opInBytes.length);
        OTFactoryMethods.print_op(TAG, op);

        for(int i = 0; i < op.getMParamsCount(); i++){
            GPDataTypes.TeecParameter param = op.getMParamsList().get(i);
            if(param.getType() == GPDataTypes.TeecParameter.Type.val){
                OTValue otValue = (OTValue)otOperation.getParam(i);

                otValue.setA(param.getTeecValue().getA());
                otValue.setB(param.getTeecValue().getB());
            }
            else if (param.getType() == GPDataTypes.TeecParameter.Type.smr){
                OTRegisteredMemoryReference otRmr = (OTRegisteredMemoryReference)otOperation.getParam(i);
                OTSharedMemory otSm = (OTSharedMemory)otRmr.getSharedMemory();

                GPDataTypes.TeecSharedMemoryReference teecSmr = param.getTeecSharedMemoryReference();

                Log.d(TAG, "size of returned buffer " + teecSmr.getParent().getMBuffer().toByteArray().length);

                otSm.updateBuffer(teecSmr.getParent().getMBuffer().toByteArray(),
                        otRmr.getOffset(),
                        teecSmr.getParent().getMReturnSize());

            }
            else{
                Log.d(TAG, "Null or unknown type of parameter.");
            }
        }
    }

    @Override
    public ITEEClient.ISession openSession(UUID uuid,
                                ConnectionMethod connectionMethod,
                                Integer connectionData,
                                ITEEClient.IOperation teecOperation) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        // sid is used to identify different sessions of one context in the OTGuard.
        int sid = generateSessionId();

        OpenSessionTask openSessionThread = null;
        Thread opWorker = null;
        ReturnValueWrapper rv = null;

        if(connectionData == null) connectionData = 0;

        if(teecOperation == null){
            openSessionThread = new OpenSessionTask(mProxyApis,
                    sid,
                    uuid,
                    connectionMethod,
                    connectionData,
                    null,   // without operation.
                    null,
                    0);  // without lock.

            opWorker = new Thread(openSessionThread);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new GenericErrorException(e.getMessage());
            }
        }
        else{
            //teecOperation started field check
            if(teecOperation.isStarted()){
                throw new BusyException("the referenced operation is under usage.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
            }

            OTOperation otOperation = (OTOperation)teecOperation;

            //update started field.
            otOperation.setStarted(1);

            /**
             * parse teecOperation into byte array using protocol buffer.
             */
            byte[] opInArray = OTFactoryMethods.OperationAsByteArray(TAG, teecOperation);

            OTLock otLock = new OTLock();
            openSessionThread = new OpenSessionTask(mProxyApis,
                    sid,
                    uuid,
                    connectionMethod,
                    connectionData,
                    opInArray,
                    otLock,
                    otOperation.hashCode());

            opWorker = new Thread(openSessionThread);
            opWorker.start();

            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }

            //test code
            Log.d(TAG, "Thread id " + Thread.currentThread().getId());

            // wait util operation synced back.
            otLock.lock();
            otLock.unlock();

            byte[] teecOperationInBytes = openSessionThread.getNewOperationInBytes();

            if(teecOperationInBytes != null &&
               openSessionThread.getReturnValue().getReturnCode() == OTReturnCode.TEEC_SUCCESS){
                updateOperation(otOperation, teecOperationInBytes);
            }
            else{
                Log.e(TAG, "op is empty or open session failed");
            }

            // operation is no longer in use.
            otOperation.setStarted(0);
        }

        rv = openSessionThread.getReturnValue();

        if(rv != null && rv.getReturnCode() != OTReturnCode.TEEC_SUCCESS){
            OTFactoryMethods.throwExceptionWithReturnOrigin(TAG, rv.getReturnCode(), rv.getReturnOrigin());

        }

        // upon success
        OTContextCallback otContextCallback = this;
        OTSession otSession =  new OTSession(sid, otContextCallback);
        mSessionMap.put(sid, 0);
        return otSession;
    }

    @Override
    public void requestCancellation(ITEEClient.IOperation iOperation) {
        // don't call if it is not started.
        if(!iOperation.isStarted()) {
            Log.i(TAG, "operation not started yet. No need to cancel");
            return;
        }

        //new thread to cancel operation.
        Thread rc = new Thread(new RequestCancellationTask(mContext, (OTOperation)iOperation ));
        rc.start();

        Log.i(TAG, "sending request cancellation finished");
    }

    private int generateSmId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(occupiedSmId(id));

        Log.i(TAG, "generating shared memory id:" + id);
        return id;
    }

    private boolean occupiedSmId(int id){
        for(OTSharedMemory sm: mSharedMemory ){
            if ( sm.getId() == id ) return true;
        }
        return false;
    }

    private int generateSessionId(){
        // reuse the random generator of shared memory.
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(mSessionMap.containsKey(id));

        Log.i(TAG, "generating session id:" + id);
        return id;
    }

    @Override
    public void closeSession(int sid) throws RemoteException, CommunicationErrorException {
        Log.i(TAG, "closing session with id " + sid);

        // call remote to close session.
        mProxyApis.teecCloseSession(sid);

        // remote session.
        mSessionMap.remove(sid);
    }

    @Override
    public ReturnValueWrapper invokeCommand(int sid, int commandId, ITEEClient.IOperation teecOperation) throws TEEClientException {
        Log.i(TAG, "invoking command with commandId " + commandId);

        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        InvokeCommandTask invokeCommandTask = null;
        Thread opWorker = null;

        //teecOperation started check
        if(teecOperation == null){
            invokeCommandTask = new InvokeCommandTask(mProxyApis,
                    sid,
                    commandId,
                    null,   // no operation
                    null,
                    0);  // no lock

            opWorker = new Thread(invokeCommandTask);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }
        }
        else{
            if(teecOperation.isStarted()){
                throw new BusyException("the referenced operation is under usage.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
            }

            OTOperation otOperation = (OTOperation)teecOperation;

            //update started field.
            otOperation.setStarted(1);

            /**
             * parse teecOperation into byte array using protocol buffer.
             */
            byte[] opInArray = OTFactoryMethods.OperationAsByteArray(TAG, teecOperation);

            OTLock otLock = new OTLock();
            invokeCommandTask = new InvokeCommandTask(mProxyApis,
                    sid,
                    commandId,
                    opInArray,
                    otLock,
                    otOperation.hashCode());

            opWorker = new Thread(invokeCommandTask);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }

            //wait operations synced back.
            otLock.lock();
            otLock.unlock();

            byte[] teecOperationInBytes = invokeCommandTask.getNewOperationInBytes();

            if(teecOperationInBytes != null &&
               invokeCommandTask.getReturnValue().getReturnCode() == OTReturnCode.TEEC_SUCCESS){
                updateOperation(otOperation, teecOperationInBytes);
            }
            else{
                Log.e(TAG, "op is empty or invoke command failed");
            }

            // operation is no longer in use.
            otOperation.setStarted(0);
        }

        return invokeCommandTask.getReturnValue();
    }
}
