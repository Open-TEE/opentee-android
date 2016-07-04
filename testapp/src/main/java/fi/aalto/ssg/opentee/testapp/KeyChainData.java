package fi.aalto.ssg.opentee.testapp;

import android.util.Log;

import java.util.Arrays;

/**
 *
 */
public class KeyChainData {
    final String TAG = "KeyChainData";

    int keyCount;
    int keyLen;
    byte[] key;

    public KeyChainData(int c, int l, byte[] k){
        this.keyCount = c;
        this.keyLen = l;
        this.key = k.clone();

        if(this.key == k){
            Log.e(TAG, "internal error. Incorrect usage of clone().");
        }
    }

    public KeyChainData(){ keyCount = keyLen = 0; key = null;}

    public byte[] asByteArray(){
        if( this.keyCount < 0 || this.keyLen < 0 || key == null) return null;

        byte[] result = new byte[8 + key.length];

        for(int i = 0; i < 4; i++){
            int tmpVar = keyCount;

            tmpVar = tmpVar >> ( 8 * i );

            tmpVar &= 0xFF;

            result[i] = (byte)tmpVar;
        }

        for(int i = 4; i < 8; i++){
            int tmpVar = keyLen;

            tmpVar = tmpVar >> (8 * (i - 4));

            tmpVar &= 0xFF;

            result[i] = (byte)tmpVar;
        }

        for(int i = 0; i < this.key.length; i++){
            result[i + 8] = this.key[i];
        }

        return result;
    }
}
