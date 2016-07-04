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

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;

/**
 * Example code to deal with remote TEE service in a separate thread.
 */
public class Worker extends HandlerThread implements WorkerCallback {
    final int OMS_MAX_RSA_MODULO_SIZE = 128;

    final String TAG = "Worker";

    public final static int CMD_GENERATE_ROOT_KEY = 1;
    public final static int CMD_INIT = 2;
    public final static int CMD_FINALIZE = 3;
    public final static int CMD_CREATE_DIR_KEY = 4;
    public final static int CMD_DO_ENC = 5;
    public final static int CMD_DO_DEC = 6;

    Handler mUiHandler;
    Context mContext;
    WorkerCallback mCallback = null;
    int mLineNum = 1;

    ITEEClient client = null;
    ITEEClient.IContext ctx = null;
    ITEEClient.ISession ses = null;

    /* Data buffer used for testing */
    private byte data[] =  new byte[]{
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF,
            (byte)0xdDE, (byte)0xAD, (byte)0xBE, (byte)0xAF
    };

    /* RSA wrapped root key blob */
    private byte[] rootKey;

    /* Chain of directory key blobs wrapped with the root key */
    private Keychain keychain;

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMsg in " + currentThread().getId());

            if(Looper.getMainLooper() == Looper.myLooper()){
                Log.e(TAG, "within the main thread");
            }

            switch (msg.what){
                case CMD_GENERATE_ROOT_KEY:
                    Log.i(TAG, "asked to generate root key");

                    rootKey = new byte[OMS_MAX_RSA_MODULO_SIZE];
                    boolean status = Omnishare.generateRootKey(rootKey, mContext, mCallback);

                    Log.d(TAG, "size of root key = " + rootKey.length);

                    Message uiMsg = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_CREATE_ROOT_KEY_BUTTON,
                            status? 1 : 0,
                            status? "\n" + (mLineNum++) + ")INFO: Root key generated\n" : "root key generation failed, try again\n");
                    mUiHandler.sendMessage(uiMsg);
                    break;

                case CMD_INIT:
                    Log.i(TAG, "asked to initialize");

                    boolean status_init = Omnishare.omnishareInit(rootKey, mContext, mCallback);

                    Message uiMsg_init = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_INI_BUTTON,
                            status_init? 1: 0,
                            status_init? (mLineNum++) + ")INFO: Environment initialized.\n" : " fail to initialize\n");

                    mUiHandler.sendMessage(uiMsg_init);
                    break;

                case CMD_FINALIZE:
                    Log.i(TAG, "asked to finalize");

                    Omnishare.omnishareFinalize(ctx, ses);

                    Message uiMsg_finalize = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_FINALIZE_BUTTON,
                            1,
                            (mLineNum++) + ")INFO: Environment finalized.\n");

                    mUiHandler.sendMessage(uiMsg_finalize);
                    break;

                case CMD_CREATE_DIR_KEY:
                    boolean status_create_dir_key = true;
                    if(keychain.getKeyCount() == 0){
                        try {
                            byte[] res = Omnishare.doCrypto(
                                    client, ctx, ses,
                                    Omnishare.CRYPTO_OP.CRYPTO_CREATE_DIR_KEY,
                                    null,
                                    keychain.getKeyCount(),
                                    keychain.getKeySize(),
                                    null
                            );

                            if (res != null)keychain.append(res);
                            else status_create_dir_key = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (BadParametersException e) {
                            e.printStackTrace();
                        } catch (BadFormatException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            byte[] res = Omnishare.doCrypto(
                                    client, ctx, ses,
                                    Omnishare.CRYPTO_OP.CRYPTO_CREATE_DIR_KEY,
                                    keychain.toByteArray(),
                                    keychain.getKeyCount(),
                                    keychain.getKeySize(),
                                    null
                            );

                            if (res != null)keychain.append(res);
                            else status_create_dir_key = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (BadParametersException e) {
                            e.printStackTrace();
                        } catch (BadFormatException e) {
                            e.printStackTrace();
                        }

                    }

                    Message uiMsg_createDirKey = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_CREATE_ROOT_KEY_BUTTON,
                            status_create_dir_key? 1 : 0,
                            status_create_dir_key?
                                    (mLineNum++) + ")INFO: Directory Key Generated, Key Count: " + keychain.getKeyCount() + " Key Size:" + keychain.getKeySize() + "\n" :
                                    "ERROR: Directory Key Generation Failed\n");

                    mUiHandler.sendMessage(uiMsg_createDirKey);

                    break;

                case CMD_DO_ENC:
                    boolean status_do_enc = true;

                    try {
                        data = Omnishare.doCrypto(client, ctx, ses,
                                Omnishare.CRYPTO_OP.CRYPTO_ENC_FILE,
                                keychain.toByteArray(),
                                keychain.getKeyCount(),
                                keychain.getKeySize(),
                                data);
                    } catch (BadParametersException e) {
                        e.printStackTrace();
                        status_do_enc = false;
                    } catch (BadFormatException e) {
                        e.printStackTrace();
                        status_do_enc = false;
                    }

                    Message uiMsg_doEnc = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_DO_ENCRY_BUTTON,
                            status_do_enc? 1 : 0,
                            status_do_enc?
                                    (mLineNum++) + ")INFO: Encryption Complete, Key Count: " + keychain.getKeyCount() + " Key Size:" + keychain.getKeySize() + "\n" +  "Encrypted buffer: " + HexUtils.encodeHexString(data) + "\n":
                                    "ERROR: File Encryption Failed\n\n");

                    mUiHandler.sendMessage(uiMsg_doEnc);

                    break;

                case CMD_DO_DEC:
                    boolean status_do_dec = true;

                    try {
                        data = Omnishare.doCrypto(client, ctx, ses,
                                Omnishare.CRYPTO_OP.CRYPTO_DEC_FILE,
                                keychain.toByteArray(),
                                keychain.getKeyCount(),
                                keychain.getKeySize(),
                                data);
                    } catch (BadParametersException e) {
                        e.printStackTrace();
                        status_do_dec = false;
                    } catch (BadFormatException e) {
                        e.printStackTrace();
                        status_do_dec = false;
                    }

                    Message uiMsg_doDec = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
                            MainActivity.ID_DO_DECRY_BUTTON,
                            status_do_dec? 1:0,
                            status_do_dec? (mLineNum++) + ")INFO: Decryption Complete, Key Count: " + keychain.getKeyCount() + " Key Size:" + keychain.getKeySize() + "\n" +  "Decrypted buffer: \n" + HexUtils.encodeHexString(data) + "\n":
                                    "ERROR: File Encryption Failed\n\n");
                    mUiHandler.sendMessage(uiMsg_doDec);

                    break;

                default:
                    Log.e(TAG, "unknown message type or unhandled message");
                    break;
            }
            return true;
        }
    };

    Handler mHandler;

    public Worker(String name, Handler uiHandler, Context context) {
        super(name);
        this.mUiHandler = uiHandler;
        this.mContext = context;
        this.mCallback = this;
        keychain = new Keychain();
    }

    public Handler getHandler(){
        return this.mHandler;
    }

    @Override
    protected void onLooperPrepared(){
        super.onLooperPrepared();
        mHandler = new Handler(getLooper(), callback);
    }

    @Override
    public void updateClient(ITEEClient client) {
        this.client = client;
    }

    @Override
    public void updateContext(ITEEClient.IContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void updateSession(ITEEClient.ISession ses) {
        this.ses = ses;
    }

    @Override
    public void updateRootKey(byte[] newKey){
        this.rootKey = newKey;
    }
}
