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
package fi.aalto.ssg.opentee.testapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.OpenTEE;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "Test_APP";

    /* Data buffer used for testing */
    private byte data[] =  new byte[]{
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF
    };

    /* UI Elements */
    private Button InitializeButton;
    private Button FinalizeButton;
    private Button CreateDirectoryKeyButton;
    private Button EncryptFileButton;
    private Button DecryptFileButton;

    private TextView logView;

    public static final int CMD_UPDATE_LOGVIEW = 1;

    public static final int ID_CREATE_ROOT_KEY_BUTTON = 0xffff0000;
    public static final int ID_INI_BUTTON = 0xffff0001;
    public static final int ID_FINALIZE_BUTTON = 0xffff0002;
    public static final int ID_DO_ENCRY_BUTTON = 0xffff0003;
    public static final int ID_DO_DECRY_BUTTON = 0xffff0004;

    /* update UI callback function */
    Handler.Callback updateUiCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                /* update logview msg */
                case CMD_UPDATE_LOGVIEW:

                    if(msg.obj != null){

                        switch (msg.arg1){
                            case ID_CREATE_ROOT_KEY_BUTTON:
                                if(msg.arg2 == 1) InitializeButton.setEnabled(true);
                                break;

                            case ID_INI_BUTTON:
                                if(msg.arg2 == 1){
                                    CreateDirectoryKeyButton.setEnabled(true);
                                    EncryptFileButton.setEnabled(true);
                                    InitializeButton.setEnabled(false);
                                    FinalizeButton.setEnabled(true);
                                }
                                break;

                            case ID_FINALIZE_BUTTON:
                                if(msg.arg2 == 1){
                                    CreateDirectoryKeyButton.setEnabled(false);
                                    EncryptFileButton.setEnabled(false);
                                    DecryptFileButton.setEnabled(false);
                                    InitializeButton.setEnabled(true);
                                    FinalizeButton.setEnabled(false);
                                }
                                break;

                            case ID_DO_ENCRY_BUTTON:
                                if(msg.arg2 == 1) DecryptFileButton.setEnabled(true);
                                break;

                            default:
                                Log.e(TAG, "unknown id");
                                break;
                        }

                        logView.append( msg.obj + "\n");
                    } //end of if msg.obj != null.
                    break;

                default:
                    Log.e(TAG, "unknown msg");
                    break;
            } //end of switch(msg.what).
            return true;
        } //end of handleMessage.
    };

    /* UI updater handler for the main thread */
    Handler mUpdateUi;

    /**
     * mWorker is an instance which is started in a separate thread from main thread. It carries
     * out our major tasks, such as interacting with the remote TEE Proxy service to generate root key etc.
     */
    Worker mWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get handles to buttons */
        InitializeButton = (Button) findViewById(R.id.button_initialize);
        FinalizeButton = (Button) findViewById(R.id.button_finalize);
        CreateDirectoryKeyButton = (Button) findViewById(R.id.button_create_directory_key);
        EncryptFileButton = (Button) findViewById(R.id.button_encrypt_file);
        DecryptFileButton = (Button) findViewById(R.id.button_decrypt_file);

        logView = (TextView)findViewById(R.id.view_log);
        logView.append("Initial data buffer: " + HexUtils.encodeHexString(data) + "\n");

        /* initialize the UI updater with callback function */
        mUpdateUi = new Handler(updateUiCallBack);

        /* initialize the mWorker */
        mWorker = new Worker("LOL tough worker", mUpdateUi, getApplicationContext());
        mWorker.start();
    }


    /**
     * Trigger root key generation from UI.
     *
     * @param v Parent view
     */
    public void doCreateRootKey(View v) {
        /* get the handler to the mWorker thread */
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to generate a root key */
        Message msg = workerHandler.obtainMessage(Worker.CMD_GENERATE_ROOT_KEY);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger trustlet initialization from UI.
     *
     * @param v Parent view
     */
    public void doInitialize(View v) {
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to initialize environment in TEE */
        Message msg = workerHandler.obtainMessage(Worker.CMD_INIT);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger trustlet finalization from UI.
     *
     * @param v Parent view
     */
    public void doFinalize(View v) {
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to finalize the environment
         * initialized earlier */
        Message msg = workerHandler.obtainMessage(Worker.CMD_FINALIZE);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger directory key generation from UI.
     *
     * @param v Parent view
     */
    public void doCreateDirectoryKey(View v) {
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to create a directory key */
        Message msg = workerHandler.obtainMessage(Worker.CMD_CREATE_DIR_KEY);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger encrypt operation from UI.
     *
     * @param v Parent view.
     */
    public void doEncryptFile(View v) {
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to do encryption */
        Message msg = workerHandler.obtainMessage(Worker.CMD_DO_ENC);
        workerHandler.sendMessage(msg);
    }

    /**
     * Trigger decrypt operation from UI.
     *
     * @param v Parent view.
     */
    public void doDecryptFile(View v) {
        Handler workerHandler = mWorker.getHandler();

        /* create and send a message to notify the mWorker thread to do decryption */
        Message msg = workerHandler.obtainMessage(Worker.CMD_DO_DEC);
        workerHandler.sendMessage(msg);
    }
}