package fi.aalto.ssg.opentee.imps;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility function wrapper for otclient.
 */
public class OTUtils {
    final static String TAG = "OTUtils.otclient";

    /**
     * read inputstream and return the byte array
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        if(inputStream == null){
            Log.e(TAG, "invalid inputStream");
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read = inputStream.read();
        while( read != -1 ){
            byteArrayOutputStream.write(read);
            read = inputStream.read();
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static InputStream fileToInputStream(String fileName) throws FileNotFoundException {
        File file = new File(fileName);

        if(!file.exists()){
            Log.e(TAG, fileName + " not found.");
            return null;
        }

        return new FileInputStream(file);
    }
}
