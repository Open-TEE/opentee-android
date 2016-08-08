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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import fi.aalto.ssg.opentee.IOTConnectionInterface;

/**
 * Request cancellation task
 */
public class RequestCancellationTask implements Runnable {
    final String TAG = "RequestCancellationTask";

    OTOperation mOp;
    Context mContext;

    boolean mConnected;
    IOTConnectionInterface mService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected");

            mConnected = true;
            mService = IOTConnectionInterface.Stub.asInterface(service);

            task();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected");

            mConnected = false;
            mService = null;
        }
    };

    public RequestCancellationTask(Context context, OTOperation op){
        this.mContext = context;
        this.mOp = op;
    }

    public void task(){
        try {
            mService.teecRequestCancellation(mOp.hashCode());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mContext.unbindService(mServiceConnection);
    }

    @Override
    public void run() {
        Intent intent = new Intent();
        intent.setClassName(TeecConstants.OT_SERVICE_PACK_NAME,
                TeecConstants.OT_SERVICE_CLASS_NAME);
        mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }
}
