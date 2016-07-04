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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * prepare and start the open-tee engine and TAs.
 */
public class MainActivity extends AppCompatActivity {
    public static final String OT_ENGINE_STATUS = "ot_engine_status";

    private static final String SETTING_FILE_NAME = "setting_OTConnectionService";
    private static final String TAG = "TEE Proxy Service";

    private HandlerThread mWorkerHandler;
    Handler handler;

    /* Overall installation task after initial launch of the application */
    Runnable installationTask = new Runnable() {
        @Override
        public void run() {
            Worker worker = new Worker();
            Context context = getApplicationContext();

            Log.d(TAG, "Ready files for opentee to run");

            /* fresh copy for open-tee and TAs */
            boolean overwrite = true;

            /* install configuration file */
            worker.installConfigToHomeDir(context, OTConstants.OPENTEE_CONF_NAME);

            /* install open-tee */
            worker.installAssetToHomeDir(context, OTConstants.OPENTEE_ENGINE_ASSET_BIN_NAME, OTConstants.OT_BIN_DIR, overwrite);
            worker.installAssetToHomeDir(context, OTConstants.LIB_LAUNCHER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);
            worker.installAssetToHomeDir(context, OTConstants.LIB_MANAGER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);

            /* install TAs */
            Setting setting = new Setting(getApplicationContext());
            String propertiesStr = setting.getProperties().getProperty("TA_List");
            if(propertiesStr == null){
                Log.e(TAG, "no TA to be deployed!");
            }
            else{
                Log.i(TAG, "-------- begin installing TAs -----------");

                String[] properties = propertiesStr.split(",");
                for(String taName: properties){
                    if(!taName.isEmpty()){
                        Log.i(TAG, "installing TA:" + taName);
                        worker.installAssetToHomeDir(context, taName, OTConstants.OT_TA_DIR, overwrite);
                    }
                }

                Log.i(TAG, "-----------------------------------------");
            }

            /* put the status of the open-tee engine to setting */
            SharedPreferences.Editor editor = getSharedPreferences(SETTING_FILE_NAME, 0).edit();
            editor.putBoolean(OT_ENGINE_STATUS, true);
            editor.commit();

            /* start the open-tee engine */
            try {
                worker.startOpenTEEEngine(context);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            worker.stopExecutor();

            Log.d(TAG, "Installation ready");

        }
    }; // end of installation task.

    Runnable stopEngineTask = new Runnable() {
        @Override
        public void run() {
            Worker worker = new Worker();
            worker.stopOpenTEEEngine(getApplicationContext());
            worker.stopExecutor();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* clear the setting for open-tee engine status */
        SharedPreferences.Editor editor = getSharedPreferences(OT_ENGINE_STATUS, 0).edit();
        editor.clear();
        editor.commit();

        /* create another thread to avoid too much work in main thread */
        mWorkerHandler = new HandlerThread("tough worker");
        mWorkerHandler.start();

        /* active the installation task */
        handler = new Handler(mWorkerHandler.getLooper());
        handler.post(installationTask);

    }

    @Override
    protected void onDestroy() {
        /* stop open-tee engine */
        handler.post(stopEngineTask);

        Log.d(TAG, "Stopping the engine");

        if ( mWorkerHandler != null ){
            mWorkerHandler.quitSafely();
        }

        super.onDestroy();
    }

}
