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
package fi.aalto.ssg.opentee;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class contains different Open-TEE utility functions
 * Credit: based on previous opentee-android project
 */
public class OTUtils {
    private final static String TAG_CLASS = "OTUtils.class";

    /**
     * read inputstream and return the byte array
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read = inputStream.read();
        while( read != -1 ){
            byteArrayOutputStream.write(read);
            read = inputStream.read();
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * check whether $dirPath exists or not, if not create one and chmod to 755
     * @param dirPath
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static File checkAndCreateDir(String dirPath) throws IOException, InterruptedException {
        File tmpDirPath = new File(dirPath);

        if ( !tmpDirPath.isDirectory() ){
            //create the directory if not there

            //create the parent directory if not exists
            tmpDirPath.mkdirs();

            //change the mod of the directory in order to hold executable file
            //remember to refer to the chmod under /system/bin directory
            String output =  execUnixCommand(
                    ("/system/bin/chmod 755 " + tmpDirPath).split(" "),
                    null);

            //Log.d(TAG_CLASS, "execUnixCommand: " + output);
        }

        return tmpDirPath;
    }

    /**
     * get the opentee absolute folder path
     * @param context
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getFullPath(Context context) throws IOException, InterruptedException {
        File tmpFile = checkAndCreateDir(context.getApplicationInfo().dataDir + File.separator + OTConstants.OT_DIR_NAME);
        return tmpFile.getAbsolutePath();
    }

    /**
     * create a child process with environment variables
     * @param command
     * @param envVars
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public static String execUnixCommand(String[] command,
                                         Map<String, String> envVars) throws InterruptedException, IOException {

        /*
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        if ( envVars != null ){
            Map<String, String> env = processBuilder.environment();
            env.clear();
            env.putAll(envVars);
        }

        Process process = processBuilder.start();
        processBuilder.redirectErrorStream(false);
        process.waitFor();
        String outputWithError = loadStream(process.getInputStream()) + loadStream(process.getErrorStream());
        return outputWithError;
        */

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        if (envVars != null) {
            Map<String, String> env = processBuilder.environment();
            env.clear();
            env.putAll(envVars);
        }
        Process process = processBuilder.start();
        String output = loadStream(process.getInputStream());
        String error = loadStream(process.getErrorStream());
        process.waitFor();
        return output + "\n" + error;
    }

    /**
     * read the inputstream to String
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String loadStream(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        String tmpLine;
        while( (tmpLine = br.readLine())  != null){
            sb.append(tmpLine);
        }
        br.close();
        return sb.toString();
    }

    /**
     * get the open-tee environment variables
     * @param context
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Map<String, String> getOTEnvVars(Context context) throws IOException, InterruptedException {
        String appHomePath = getFullPath(context);

        Map<String, String> map = new HashMap<>();

        map.put("OPENTEE_STORAGE_PATH", appHomePath + File.separator + OTConstants.OPENTEE_SECURE_STORAGE_DIRNAME + File.separator);
        map.put("OPENTEE_SOCKET_FILE_PATH", appHomePath + File.separator + OTConstants.OPENTEE_SOCKET_FILENAME);
        map.put("LD_LIBRARY_PATH", context.getApplicationInfo().dataDir + File.separator + OTConstants.OT_LIB_DIR);

        return map;
    }

    /**
     * read one file and return its content as string
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static String readFileToString(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if ( file.exists() ) {
            StringBuilder stringBuilder = new StringBuilder((int) file.length());
            Scanner scanner = new Scanner(file, "UTF-8");
            String lineSeparator = System.getProperty("line.separator");

            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine() + lineSeparator);
            }

            scanner.close();
            return stringBuilder.toString();
        }

        Log.e(TAG_CLASS, "no file called: " + fileName);
        return null;
    }

    public static InputStream fileToInputStream(final String fileName) throws FileNotFoundException {
        File file = new File(fileName);

        if(!file.exists()){
            Log.e(TAG_CLASS, fileName + " not found.");
            return null;
        }

        return new FileInputStream(file);
    }

    public static OutputStream fileToOutputStream(final String fileName) throws FileNotFoundException {
        return new FileOutputStream(new File(fileName));
    }

    public static String createDirInHome(Context context, final String subDir) throws IOException, InterruptedException {
        File tmpFile = checkAndCreateDir(context.getApplicationInfo().dataDir + File.separator + subDir);
        return tmpFile.getAbsolutePath();
    }

    public static void installAssetToHomeDir(final Context context,
                                      final String assetName,
                                      final String destSubdir,
                                      final boolean overwrite){
        //identify the running environment and take the path of corresponding file
        String suitableAssetPath = Build.SUPPORTED_ABIS[0] + File.separator + assetName;

        Log.d(TAG_CLASS, "Copy from : " + suitableAssetPath);

        try {
            InputStream inputStream = context.getAssets().open(suitableAssetPath);
            byte[] inputBytes = OTUtils.readBytesFromInputStream(inputStream);
            installBytesToHomeDir(context,
                    inputBytes,
                    destSubdir,
                    assetName,
                    overwrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy the byte arrays to @destPath + outputFilePathSub
     * @param context
     * @param inputBytes
     * @param outputFilePathSub
     * @param assetName
     * @param overwrite
     * @throws IOException
     * @throws InterruptedException
     */
    public static void installBytesToHomeDir(final Context context,
                                      final byte[] inputBytes,
                                      final String outputFilePathSub,
                                      final String assetName,
                                      final boolean overwrite) {
        try {
            /* input check */
            if (assetName == null || assetName.isEmpty()) return;

            /* preparation */
            //get the full path to opentee folder
            String assetPath = OTUtils.getFullPath(context);

            if (outputFilePathSub != null && !outputFilePathSub.isEmpty()) {
                //if subdirectory name is not empty, add it to the assetPath
                assetPath += File.separator + outputFilePathSub;

                //and create parent folder if needed
                OTUtils.checkAndCreateDir(assetPath);
            }

            //add the assertName to the assetPath to get the full path of the asset
            assetPath += File.separator + assetName;

            /* do the real job */
            File outputFile = new File(assetPath);
            if (!outputFile.exists() || overwrite) {
                Log.d(TAG_CLASS, "Copy to " + assetPath);
                FileOutputStream fos = new FileOutputStream(outputFile, false);
                fos.write(inputBytes);
                fos.close();
                //change the mod of destination file to read only and with no environment variabls passed in;
                String output =
                        OTUtils.execUnixCommand(("/system/bin/chmod 744 " + assetPath).split(" "), null);
                Log.d(TAG_CLASS, "chmod 744 " + assetPath);
                Log.d(TAG_CLASS, output);
            }
        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
