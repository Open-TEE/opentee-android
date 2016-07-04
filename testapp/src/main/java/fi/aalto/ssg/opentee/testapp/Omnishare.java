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
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.OpenTEE;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * This class utilizes the Java API to perform certain actions.
 */
public class Omnishare {

    public static final String TAG = "Omnishare";

    public static synchronized boolean generateRootKey(byte[] key, Context context, WorkerCallback callback){
        if(key == null || key.length == 0 || callback == null) return false;

        ITEEClient client = OpenTEE.newTEEClient();

        ITEEClient.IContext ctx = null;
        try {
            ctx = client.initializeContext(null, context);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return false;
        }

        ITEEClient.ISession ses = null;
        try {
            ses = ctx.openSession(OmnishareUtils.getOmnishareTaUuid(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {
            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }
            return false;
        }

        ITEEClient.ISharedMemory sm = null;
        try {
            sm = ctx.registerSharedMemory(key, ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT);
        } catch (TEEClientException e) {

            try {
                ses.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

            return false;
        }

        ITEEClient.IRegisteredMemoryReference rmr = null;
        try {
            rmr = client.newRegisteredMemoryReference(sm,
                            ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT,
                            0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IOperation op = client.newOperation(rmr);

        int CMD_CREATE_ROOT_KEY = 0x00000001;

        boolean succ = true;

        try {
            ses.invokeCommand(CMD_CREATE_ROOT_KEY, op);
        } catch (TEEClientException e) {
            e.printStackTrace();
            succ = false;
        }

        //test code
        Log.d(TAG, "the returned size of shared memory = " + rmr.getReturnSize());

        if(key.length < rmr.getReturnSize()){
            Log.e(TAG, "internal error: incorrect size of returned shared memory");
        }else{
            byte[] newKey = Arrays.copyOf(key, rmr.getReturnSize());
            callback.updateRootKey(newKey);
        }

        try {
            ctx.releaseSharedMemory(sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ses.closeSession();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ctx.finalizeContext();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return succ;

    }

    public static synchronized boolean omnishareInit(byte[] rootkey, Context context, WorkerCallback callback){
        if(rootkey == null || rootkey.length == 0 || callback == null) return false;

        ITEEClient client = OpenTEE.newTEEClient();

        ITEEClient.IContext ctx = null;
        try {
            ctx = client.initializeContext(null, context);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return  false;
        }

        ITEEClient.ISharedMemory sm = null;
        try {
            sm = ctx.registerSharedMemory(rootkey, ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();

            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }
            return  false;
        }

        ITEEClient.IRegisteredMemoryReference rmr = null;
        try {
            rmr = client.newRegisteredMemoryReference(sm,
                            ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT,
                            0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IOperation op = client.newOperation(rmr);

        ITEEClient.ISession ses = null;
        try {
            ses = ctx.openSession(OmnishareUtils.getOmnishareTaUuid(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    op);
        } catch (TEEClientException e) {

            try {
                ctx.releaseSharedMemory(sm);
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }

            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }

            e.printStackTrace();
        }

        try {
            ctx.releaseSharedMemory(sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return false;
        }

        callback.updateContext(ctx);
        callback.updateSession(ses);
        callback.updateClient(client);

        return true;
    }

    public static synchronized void omnishareFinalize(ITEEClient.IContext ctx, ITEEClient.ISession ses){
        if(ses != null){
            try {
                ses.closeSession();
            } catch (TEEClientException e) {
                e.printStackTrace();
            }
        }

        if(ctx != null){
            try {
                ctx.finalizeContext();
            } catch (TEEClientException e) {
                e.printStackTrace();
            }
        }
    }

    public enum CRYPTO_OP{
        CRYPTO_ENC_FILE(0),
        CRYPTO_DEC_FILE(1),
        CRYPTO_CREATE_DIR_KEY(2);

        private int var;

        CRYPTO_OP(int v){this.var = v;}
    }

    public static final int OMS_AES_KEY_SIZE = 32;

    private static byte[] omnishareDoCrypto(ITEEClient client, ITEEClient.IContext ctx, ITEEClient.ISession ses, byte[] keyChain, int keyCount, int keyLen, CRYPTO_OP opCmd, byte[] src, byte[] des) throws BadFormatException, BadParametersException {
        if(client == null || ctx == null || ses == null ||
           opCmd == null ||
           //src == null || src.length == 0 ||
           des == null || des.length == 0) throw new BadFormatException("incorrect input parameters.");

        ITEEClient.IOperation op = null;

        ITEEClient.IRegisteredMemoryReference srcRmr = null;
        ITEEClient.IRegisteredMemoryReference desRmr = null;
        ITEEClient.IRegisteredMemoryReference keyRmr = null;

        ITEEClient.IValue val = client.newValue(ITEEClient.IValue.Flag.TEEC_VALUE_INPUT, opCmd.var, 0);

        ITEEClient.ISharedMemory srcSm = null;
        if(opCmd == CRYPTO_OP.CRYPTO_ENC_FILE || opCmd == CRYPTO_OP.CRYPTO_DEC_FILE){
            if (src == null || src.length == 0) throw new BadFormatException("incorrect input parameters of src.");

            try {
                srcSm = ctx.registerSharedMemory(src, ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
            } catch (TEEClientException e) {
                e.printStackTrace();
                return null;
            }

            srcRmr = client.newRegisteredMemoryReference(srcSm, ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT, 0);
        }

        ITEEClient.ISharedMemory keySm = null;
        if(keyChain != null && keyCount >= 0 && keyLen >= 0){
            KeyChainData kcData = new KeyChainData(keyCount, keyLen, keyChain);
            byte[] keySmBuffer = kcData.asByteArray();

            try {
                keySm = ctx.registerSharedMemory(keySmBuffer, ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
            } catch (TEEClientException e) {
                e.printStackTrace();

                if(srcSm != null){
                    try {
                        ctx.releaseSharedMemory(srcSm);
                    } catch (TEEClientException e1) {
                        e1.printStackTrace();
                    }
                }

                return null;
            }

            keyRmr = client.newRegisteredMemoryReference(keySm,
                    ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT,
                    0);
        }

        // return buffer
        ITEEClient.ISharedMemory desSm = null;
        try {
            desSm = ctx.registerSharedMemory(des, ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();

            if(keySm != null){
                try {
                    ctx.releaseSharedMemory(keySm);
                } catch (TEEClientException e1) {
                    e1.printStackTrace();
                }
            }

            if(srcSm != null){
                try {
                    ctx.releaseSharedMemory(srcSm);
                } catch (TEEClientException e1) {
                    e1.printStackTrace();
                }
            }

            return null;
        }

        desRmr = client.newRegisteredMemoryReference(desSm,
                ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT,
                0);

        op = client.newOperation(keyRmr, val, srcRmr, desRmr);

        int CMD_DO_CRYPTO = 0X00000002;
        try {
            ses.invokeCommand(CMD_DO_CRYPTO, op);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        if(desRmr.getReturnSize() != des.length){
            des = Arrays.copyOf(des, desRmr.getReturnSize());
        }

        try {
            if(keySm != null) ctx.releaseSharedMemory(keySm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            if(srcSm != null) ctx.releaseSharedMemory(srcSm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            if(desSm != null) ctx.releaseSharedMemory(desSm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return des;

    }

    public static synchronized byte[] doCrypto(ITEEClient client, ITEEClient.IContext ctx, ITEEClient.ISession ses, CRYPTO_OP op, byte[] keys, int keyCount, int keyLen, byte[] data) throws BadParametersException, BadFormatException {
        if (client == null || ctx == null || ses == null ||
                keyCount < 0 || keyLen < 0
                //data == null || data.length == 0
                ) return null;

        byte[] src = data;
        byte[] des = new byte[ ((src != null)? src.length: 0 ) + OMS_AES_KEY_SIZE];

        if(keys != null && keys.length != keyCount * keyLen) throw new BadParametersException("invalid key array length");

        //if ( !omnishareDoCrypto(client, ctx, ses, keys, keyCount, keyLen, op, src, des) ) des = null;

        //return des;
        return omnishareDoCrypto(client, ctx, ses, keys, keyCount, keyLen, op, src, des);
    }
}
