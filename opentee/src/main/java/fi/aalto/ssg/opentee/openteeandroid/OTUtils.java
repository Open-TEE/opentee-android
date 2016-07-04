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
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

}
