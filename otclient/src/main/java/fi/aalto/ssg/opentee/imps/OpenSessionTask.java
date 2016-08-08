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

import android.os.RemoteException;
import android.util.Log;

import java.util.UUID;

import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Task to open a session.
 */
public class OpenSessionTask implements Runnable {
    final String TAG = "OpenSessionTask";

    ProxyApis mProxyApis = null;
    int mSid;
    UUID mUuid;
    ITEEClient.IContext.ConnectionMethod mConnectionMethod;
    int mConnectionData;

    byte[] mTeecOperation = null;
    byte[] mNewTeecOperation = null;
    OTLock mLock = null;
    ReturnValueWrapper mReturnValue = null;
    int opHashCode = 0;

    private ISyncOperation mSyncOperationCallBack = new ISyncOperation.Stub(){
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void syncOperation(byte[] teecOperationInBytes) throws RemoteException {
            Log.e(TAG, "sync op called in thread with id " + Thread.currentThread().getId());

            mNewTeecOperation = teecOperationInBytes;

            if(mLock != null) mLock.unlock();
            else{
                Log.e(TAG, "[internal error] lock is null");
            }
        }
    };

    public OpenSessionTask(ProxyApis proxyApis,
                           int sid,
                           UUID uuid,
                           ITEEClient.IContext.ConnectionMethod connectionMethod,
                           int connectionData,
                           byte[] teecOperation,
                           OTLock lock,
                           int opHashCode){
        this.mProxyApis = proxyApis;
        this.mSid = sid;
        this.mUuid = uuid;
        this.mConnectionMethod = connectionMethod;
        this.mConnectionData = connectionData;
        this.mTeecOperation = teecOperation;
        this.mLock = lock;
        this.opHashCode = opHashCode;
    }

    public synchronized byte[] getNewOperationInBytes(){
        return this.mNewTeecOperation;
    }

    public synchronized ReturnValueWrapper getReturnValue(){ return this.mReturnValue; }

    @Override
    public void run() {
        if (mLock != null) mLock.lock();

        try {
            mReturnValue = mProxyApis.teecOpenSession(mSid,
                    mUuid,
                    mConnectionMethod,
                    mConnectionData,
                    mTeecOperation,
                    mSyncOperationCallBack,
                    opHashCode);
        } catch (RemoteException e) {
            Log.e(TAG, "Communication error with remote TEE service.");
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
    }
}
