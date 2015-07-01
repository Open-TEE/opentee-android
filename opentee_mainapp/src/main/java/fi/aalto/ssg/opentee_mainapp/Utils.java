package fi.aalto.ssg.opentee_mainapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by code on 6/24/15.
 */
public class Utils {

    // Executes UNIX command.
    public static String execUnixCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        int read;
        char[] buffer = new char[4096];
        StringBuffer output = new StringBuffer();
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
        }
        reader.close();
        process.waitFor();
        return output.toString();
    }

    public static void copyStream(Context context, InputStream in, FileOutputStream out) throws IOException {
        int read;
        byte[] buffer = new byte[4096];
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        out.close();
        in.close();
    }


    public static File checkAndCreateDir(String dirPath) {
        File dataPath = new File(dirPath);
        if (!dataPath.isDirectory()) {
            try {
                dataPath.mkdirs();
            } catch (SecurityException e) {
                Log.e(OpenTEEService.OPEN_TEE_SERVICE_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return dataPath;
    }

    static String getFullFileDataPath(Context context) {
        File dataPath = checkAndCreateDir(context.getApplicationInfo().dataDir + File.separator + OpenTEEService.OPENTEE_DIR_NAME);
        return dataPath.getAbsolutePath();
    }
}
