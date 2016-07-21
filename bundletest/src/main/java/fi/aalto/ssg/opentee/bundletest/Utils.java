package fi.aalto.ssg.opentee.bundletest;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fi.aalto.ssg.opentee.OTUtils;

/**
 * a standalone application which includes the Open-TEE.
 */
public class Utils {
    final static String TAG = "Utils";

    public static byte[] getFileFromLib(Context context, String fileName){
        if(fileName == null || fileName.length() == 0){
            Log.e(TAG, "Invalid ta file name.");
            return null;
        }

        /* read ta to byte array */
        String libPath = context.getApplicationInfo().dataDir + File.separator + "lib";
        String taPath = libPath + File.separator + fileName;

        InputStream i;
        try {
            i = OTUtils.fileToInputStream(taPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        if(i == null) return null;

        byte[] taInBytes ;
        try {
            taInBytes = OTUtils.readBytesFromInputStream(i);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return taInBytes;
    }
}
