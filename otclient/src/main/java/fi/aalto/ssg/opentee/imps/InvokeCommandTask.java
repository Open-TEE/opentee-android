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

import fi.aalto.ssg.opentee.ISyncOperation;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Task for invokeCommand
 */
public class InvokeCommandTask implements Runnable {
    final String TAG = "InvokeCommandTask";

    ProxyApis mProxyApis;
    int mSid;
    int mCommandId;
    byte[] mTeecOperation = null;
    int opHashCode = 0;

    byte[] mNewTeecOperation = null;
    OTLock mLock;
    ReturnValueWrapper mReturnValue = null;

    private ISyncOperation mSyncOperationCallBack = new ISyncOperation.Stub(){
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void syncOperation(byte[] teecOperationInBytes) throws RemoteException {
            Log.e(TAG, "sync op called");

            mNewTeecOperation = teecOperationInBytes;
            if (mLock != null) mLock.unlock();
            else Log.e(TAG, "[Internal error] lock is null");
        }
    };

    public InvokeCommandTask(ProxyApis proxyApis, int sid, int commandId, byte[] teecOperation, OTLock lock, int opHashCode){
        this.mProxyApis = proxyApis;
        this.mSid = sid;
        this.mCommandId = commandId;
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
        if(mLock != null) mLock.lock();

        try {
            mReturnValue = mProxyApis.teecInvokeCommand(mSid, mCommandId, mTeecOperation, mSyncOperationCallBack, opHashCode);
        } catch (CommunicationErrorException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
