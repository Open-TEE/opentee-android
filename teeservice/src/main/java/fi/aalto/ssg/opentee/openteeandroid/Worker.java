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
package fi.aalto.ssg.opentee.openteeandroid;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this class is a single thread executor which can perform a list of task in a certain order
 * Credit: modified from previous opentee-android project
 */
public class Worker{
    public static final String TAG_CLASS = "worker.class";
    public static final String CONF_ENCODING_STYLE = "UTF-8";

    private ExecutorService mExecutor;

    public Worker(){
        mExecutor = Executors.newSingleThreadExecutor();
        Log.d(TAG_CLASS, "executor started without context");
    }

    public Worker(Context context){
        mExecutor = Executors.newSingleThreadExecutor();
        Log.d(TAG_CLASS, "executor started with context");
    }

    /**
     * do remember to kill the executor when Worker instance is unused!
     */
    public void stopExecutor(){
        //disable new task coming
        if (mExecutor != null && !mExecutor.isShutdown()) mExecutor.shutdown();

        /*
        try {
            if (mExecutor != null && !mExecutor.awaitTermination(100, TimeUnit.SECONDS))
                mExecutor.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        Log.d(TAG_CLASS, "executor will no longer accept new task.");
    }

    /**
     * install files from asset folder to home directory
     * @param context
     * @param assetName
     * @param destSubdir
     * @param overwrite
     */
    public void installAssetToHomeDir(final Context context,
                                      final String assetName,
                                      final String destSubdir,
                                      final boolean overwrite){
        if ( mExecutor != null ){
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    /**
     * copy a file to home directory
     * @param context
     * @param srcFilePath
     * @param destDir
     * @param overwrite
     */
    public void installFileToHomeDir(final Context context,
                                     final String srcFilePath,
                                     final String destDir,
                                     final boolean overwrite){
        if ( mExecutor != null ){
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG_CLASS, "Copy from " + srcFilePath);

                    try {
                        InputStream inputStream = new FileInputStream(srcFilePath);
                        //take only the name of the file
                        String fileName = srcFilePath.substring(srcFilePath.lastIndexOf(File.pathSeparatorChar));
                        byte[] fileContentBytes = OTUtils.readBytesFromInputStream(inputStream);
                        //store the file into the destination directory
                        installBytesToHomeDir(context,
                                fileContentBytes,
                                destDir,
                                fileName,
                                overwrite);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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
    public void installBytesToHomeDir(final Context context,
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


    /**
     * execute a binary file in home folder
     * @param context
     * @param binaryFileName
     * @param envVars
     */
    public void execBinaryInHomeDir(final Context context,
                                    final String binaryFileName,
                                    final Map<String, String> envVars){
        try {
            //construct the command. With "&" at the end to run the command as background process;
            String commandString =
                    OTUtils.getFullPath(context) + File.separator + OTConstants.OT_BIN_DIR + File.separator + binaryFileName + " &";
            String outputString = OTUtils.execUnixCommand(commandString.split(" "), envVars);
            Log.d(TAG_CLASS, "Run binary " + binaryFileName);
            Log.d(TAG_CLASS, "Output: " + outputString);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * install the configuration file to home directory
     * @param context
     * @param configName
     */
    public void installConfigToHomeDir(final Context context,
                                       final String configName){
        if ( mExecutor != null ){
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String destPath = OTUtils.getFullPath(context) + File.separator + configName;


                        File configFile = new File(destPath);
                        if (!configFile.exists()) {
                            /**
                             * copy the configuration file from the asset folder and change its content in the fly;
                             */
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                    context.getAssets().open(configName), CONF_ENCODING_STYLE)
                            );

                            PrintWriter printWriter = new PrintWriter(configFile, CONF_ENCODING_STYLE);
                            String line = "";
                            String appHomePath = OTUtils.getFullPath(context);
                            while ((line = reader.readLine()) != null) {
                                //replace the PLACEHOLDER with the actual app home directory
                                line = line.replaceAll("\\b" + OTConstants.OPENTEE_DIR_CONF_PLACEHOLDER + "\\b", appHomePath);
                                printWriter.println(line);
                            }
                            printWriter.close();

                            /**
                             * change the mod of the configuration to 640
                             */
                            String commandOutput = OTUtils.execUnixCommand(("/system/bin/chmod 640 " + destPath).split(" "), null);
                            Log.d(TAG_CLASS, "/system/bin/chmod 640 " + destPath);
                            Log.d(TAG_CLASS, commandOutput);

                        }

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void startOpenTEEEngine(final Context context) throws IOException, InterruptedException {
        if ( mExecutor != null ){
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String appHomePath = OTUtils.getFullPath(context);

                        String command = OTConstants.OPENTEE_ENGINE_ASSET_BIN_NAME
                                + " -c " + appHomePath + File.separator + OTConstants.OPENTEE_CONF_NAME
                                + " -p " + appHomePath;

                        //Log.d(TAG_CLASS, "Starting opentee engine with command " + command);
                        Map<String, String> envVars = OTUtils.getOTEnvVars(context);
                        execBinaryInHomeDir(context, command, envVars);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void stopOpenTEEEngine(final Context context){
        if ( mExecutor != null ){
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String appHomePath = OTUtils.getFullPath(context);
                        String pid = OTUtils.readFileToString(
                                appHomePath
                                        + File.separator
                                        + OTConstants.OPENTEE_PID_FILENAME
                        );

                        /**
                         * kill the process
                         */
                        if ( pid != null ){
                            pid = pid.trim();
                            if ( !pid.isEmpty() ){
                                android.os.Process.killProcess(Integer.parseInt(pid));
                                Log.d(TAG_CLASS, "PID " + pid + " found. Killing rigth now ");
                            }
                        }

                        /**
                         * Delete the socket file
                         */
                        String command = "/system/bin/rm " + appHomePath + File.separator + OTConstants.OPENTEE_SOCKET_FILENAME;
                        OTUtils.execUnixCommand(command.split(" "), null);

                        /**
                         * Delete the pid file
                         */
                        command = "/system/bin/rm " + appHomePath + File.separator + OTConstants.OPENTEE_PID_FILENAME;
                        OTUtils.execUnixCommand(command.split(" "), null);

                        Log.d(TAG_CLASS, "open-tee engine stopped");
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

}
