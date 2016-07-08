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
package fi.aalto.ssg.opentee.openteeandroid;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import fi.aalto.ssg.opentee.OT;

/**
 * prepare and start the open-tee engine and TAs.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TEE Proxy Service";

    private HandlerThread mWorkerHandler;
    Handler handler;
    OT mOT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* create another thread to avoid too much work in main thread */
        mWorkerHandler = new HandlerThread("tough worker");
        mWorkerHandler.start();
        handler = new Handler(mWorkerHandler.getLooper());

        /* active the installation task */
        this.mOT = new OT(getApplication());
        handler.post(mOT.installationTask);
    }

    @Override
    protected void onDestroy() {
        /* stop open-tee engine */
        handler.post(mOT.stopEngineTask);

        Log.d(TAG, "Stopping the engine");

        if ( mWorkerHandler != null ){
            mWorkerHandler.quitSafely();
        }

        super.onDestroy();
    }

}
