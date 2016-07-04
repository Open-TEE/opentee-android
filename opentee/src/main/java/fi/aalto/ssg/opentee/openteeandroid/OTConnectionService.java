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
package fi.aalto.ssg.opentee.openteeandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.IOTConnectionInterface;
import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/* This class runs as an Android service. It is started automatically when the application is launched */
public class OTConnectionService extends Service {
    final String TAG = "OTConnectionService.Imp";
    boolean mAllowRebind = false;
    final String mQuote = "You Shall Not Pass!";
    static OTGuard mOTGuard = null; // only need one OTGuard.

    public OTConnectionService() {
        super();

        Log.d(TAG, "creating OTConnectionService");

        this.mOTGuard = new OTGuard(this.mQuote, this);
    }


    private final IOTConnectionInterface.Stub mBinder = new IOTConnectionInterface.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public synchronized int teecInitializeContext(String teeName) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to initialize context.");

            return mOTGuard.initializeContext(Binder.getCallingPid(), teeName);
        }

        @Override
        public synchronized void teecFinalizeContext() throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to finalize context.");

            mOTGuard.teecFinalizeContext(Binder.getCallingPid());
        }

        @Override
        public synchronized int teecRegisterSharedMemory(OTSharedMemory sharedMemory) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid() + " is calling me to register shared memory.");

            return mOTGuard.teecRegisterSharedMemory(Binder.getCallingPid(), sharedMemory);
            //return 0;
        }

        @Override
        public synchronized void teecReleaseSharedMemory(int smId){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to release shared memory with id:"
                    + smId);

            mOTGuard.teecReleaseSharedMemory(Binder.getCallingPid(),
                    smId);
        }

        @Override
        public synchronized int teecOpenSessionWithoutOp(int sid, ParcelUuid parcelUuid, int connMethod, int connData, int[] retOrigin) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to open session without operation.");

            return mOTGuard.teecOpenSession(Binder.getCallingPid(),
                    sid,
                    parcelUuid.getUuid(),
                    connMethod,
                    connData,
                    null,
                    retOrigin,
                    null,
                    0);
        }

        @Override
        public synchronized int teecOpenSession(int sid, ParcelUuid parcelUuid, int connMethod, int connData, byte[] teecOperation, int[] retOrigin, ISyncOperation iSyncOperation, int opHashcode) throws RemoteException {
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to open session with operations " + opHashcode);

            Log.d(TAG, "OTGuard hash code " + mOTGuard.hashCode());

            return mOTGuard.teecOpenSession(Binder.getCallingPid(),
                    sid,
                    parcelUuid.getUuid(),
                    connMethod,
                    connData,
                    teecOperation,
                    retOrigin,
                    iSyncOperation,
                    opHashcode);
        }

        @Override
        public synchronized void teecCloseSession(int sid){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to close session.");

            mOTGuard.teecCloseSession(Binder.getCallingPid(), sid);
        }

        @Override
        public synchronized int teecInvokeCommandWithoutOp(int sid, int commandId, int[] returnOrigin){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to invoke command without operation.");

            return mOTGuard.teecInvokeCommand(Binder.getCallingPid(),
                    sid,
                    commandId,
                    returnOrigin,
                    null,
                    null,
                    0);
        }

        @Override
        public synchronized int teecInvokeCommand(int sid, int commandId, byte[] teecOperation, int[] returnOrigin, ISyncOperation syncOperation, int opHashCode){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to invoke command with operations " + opHashCode);

            return mOTGuard.teecInvokeCommand(Binder.getCallingPid(),
                    sid,
                    commandId,
                    returnOrigin,
                    teecOperation,
                    syncOperation,
                    opHashCode);
        }

        @Override
        public void teecRequestCancellation(int opId){
            Log.d(TAG, Binder.getCallingPid()
                    + " is calling me to request cancellation operation with id " + opId);

            Log.d(TAG, "OTGuard hash code " + mOTGuard.hashCode());

            mOTGuard.teecRequestCancellation(Binder.getCallingPid(),
                    opId);
        }
    }; // end of implementing the IOTConnectionInterface.Stub()

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service bound for " + Binder.getCallingPid());

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()

        Log.i(TAG, Binder.getCallingPid() + " unbind the service ");

        //TODO: clean up resources in OTGuard ?

        return mAllowRebind;
    }

    @Override
    public void onDestroy(){
        mOTGuard = null;

        Log.i(TAG, "OTGuard destroyed");

        super.onDestroy();
    }
}
