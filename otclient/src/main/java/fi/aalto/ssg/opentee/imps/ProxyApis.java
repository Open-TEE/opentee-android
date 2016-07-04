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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HandshakeCompletedListener;

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ISyncOperation;
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
 * This class handles the communication with the service on behalf of the Client Application.
 * Be aware that the mContextCApi is corresponding to the context in C API while the Context instance is
 * also called context.
 */
public class ProxyApis {
    static String TAG = "ProxyApis";

    Context mContext;
    boolean mConnected;
    String mTeeName;
    IOTConnectionInterface mService;
    OTLock mLock;

    public ProxyApis(String teeName, Context context, OTLock lock){
        this.mContext = context;
        this.mTeeName = teeName;
        this.mLock = lock;

        mConnected = false;
        mService = null;

        initConnection();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected");

            mConnected = true;
            mService = IOTConnectionInterface.Stub.asInterface(service);

            //after connected, call initializeContext.
            try {
                teecInitializeContext();
            } catch (TEEClientException e) {
                e.printStackTrace();
            }

            Log.d(TAG, Thread.currentThread().getId() + " try to call unlock");

            mLock.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected");

            mConnected = false;
            mService = null;
        }
    };

    private void initConnection(){
        if ( !mConnected ){
            Intent intent = new Intent();
            intent.setClassName(TeecConstants.OT_SERVICE_PACK_NAME,
                    TeecConstants.OT_SERVICE_CLASS_NAME);
            this.mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);

            Log.d(TAG, "Trying to create connection");
        }else{
            Log.e(TAG, "Already connected.");
        }
    }

    public void terminateConnection(){
        if ( mService != null ){
            this.mContext.unbindService(mServiceConnection);
        }
    }

    public ProxyApis teecInitializeContext() throws TEEClientException {
        if(mService == null) return null;

        int return_code = 0;
        try {
            return_code = mService.teecInitializeContext(mTeeName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Return code from OT: " + Integer.toHexString(return_code));

        if ( return_code != OTReturnCode.TEEC_SUCCESS ){
            OTFactoryMethods.throwExceptionBasedOnReturnCode(return_code);
        }

        return this;
    }

    public void teecFinalizeContext() throws RemoteException {
        if ( mService != null ){
            mService.teecFinalizeContext();
        }
    }

    public void teecRegisterSharedMemory(OTSharedMemory otSharedMemory) throws TEEClientException, RemoteException {
        if ( mService == null ){
            throw new GenericErrorException("Service unavailable");
        }

        if(otSharedMemory.asByteArray() == null){
            Log.e(TAG, "otshared memory is null");
            return;
        }else{
            Log.e(TAG, new String(otSharedMemory.asByteArray()));
        }

        // call IPC
        int return_code = mService.teecRegisterSharedMemory(otSharedMemory);

        Log.d(TAG, "teecRegisterSharedMemory return code: " + return_code);

        if ( return_code == OTReturnCode.TEEC_SUCCESS ){
            return;
        }
        else{
            OTFactoryMethods.throwExceptionBasedOnReturnCode(return_code);
        }

    }

    public void teecReleaseSharedMemory(int smId) throws GenericErrorException, RemoteException {
        if ( mService == null ){
            throw new GenericErrorException("Service unavailable");
        }

        mService.teecReleaseSharedMemory(smId);
    }

    public ReturnValueWrapper teecOpenSession(int sessionId,
                                UUID uuid,
                                ITEEClient.IContext.ConnectionMethod connectionMethod,
                                int connectionData,
                                byte[] opInArray,
                                ISyncOperation syncOperation,
                                int opHashCode) throws TEEClientException, RemoteException {
        if ( mService == null ){
            throw new CommunicationErrorException("Service unavailable");
        }

        /**
         * IPC open session call.
         */
        int rc;
        int[] retOrigin = new int[1];
        if (opInArray == null){
            rc = mService.teecOpenSessionWithoutOp(sessionId,
                    new ParcelUuid(uuid),
                    connectionMethod.ordinal(),
                    connectionData,
                    retOrigin);
        }else{
            rc = mService.teecOpenSession(sessionId,
                    new ParcelUuid(uuid),
                    connectionMethod.ordinal(),
                    connectionData,
                    opInArray,
                    retOrigin,
                    syncOperation,
                    opHashCode);
        }

        Log.d(TAG, "teecOpenSession return code: " + rc);

        ReturnValueWrapper returnValueWrapper = new ReturnValueWrapper(rc, retOrigin[0]);

        return returnValueWrapper;
    }

    public void teecCloseSession(int sessionId) throws RemoteException, CommunicationErrorException {
        if ( mService == null ){
            throw new CommunicationErrorException("Service unavailable");
        }

        mService.teecCloseSession(sessionId);
    }

    public ReturnValueWrapper teecInvokeCommand(int sid, int commandId, byte[] opInArray, ISyncOperation iSyncOperation, int opHashCode) throws CommunicationErrorException, RemoteException {
        if ( mService == null ){
            throw new CommunicationErrorException("Service unavailable");
        }

        int rc = -1;
        int returnOrigin[] = new int[1];

        if(opInArray == null){
            //no operation
            rc = mService.teecInvokeCommandWithoutOp(sid, commandId, returnOrigin);
        }
        else{
            rc = mService.teecInvokeCommand(sid, commandId, opInArray, returnOrigin, iSyncOperation, opHashCode);
        }

        Log.d(TAG, "teecInvokeCommand return code: " + Integer.toHexString(rc) );

        ReturnValueWrapper returnValueWrapper = new ReturnValueWrapper(rc, returnOrigin[0]);

        return returnValueWrapper;
    }
}
