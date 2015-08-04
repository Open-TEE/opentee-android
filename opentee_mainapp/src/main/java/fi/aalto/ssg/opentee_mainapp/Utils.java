/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee_mainapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by code on 6/24/15.
 */
public class Utils {

    public static final String UTILS_TAG = "Utils";

    /**
     * Executes UNIX command.
     * @param command command to execute + arguments in a string array (e.g. [/system/bin/ls, -l, -a, somedirectory] )
     * @param environmentVars The environment variables to be passed upon execution
     * @return A string containing the standard and error output.
     * @throws IOException
     * @throws InterruptedException
     */
    public static String execUnixCommand(String[] command, Map<String, String> environmentVars) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        if (environmentVars != null) {
            Map<String, String> env = processBuilder.environment();
            env.clear();
            env.putAll(environmentVars);
        }
        Process process = processBuilder.start();
        String output = loadStream(process.getInputStream());
        String error = loadStream(process.getErrorStream());
        process.waitFor();
        return output + "\n" + error;
    }

    private static String loadStream(InputStream s) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line).append("\n");
        br.close();
        return sb.toString();
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
                String output = execUnixCommand(("/system/bin/chmod 755 " + dataPath).split(" "), null);
                if (!output.isEmpty()) {
                    Log.d(UTILS_TAG, "Chmod returned: " + output);
                }
            } catch (SecurityException | InterruptedException | IOException e) {
                Log.e(OpenTEEService.OPEN_TEE_SERVICE_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return dataPath;
    }

    public static String getFullFileDataPath(Context context) {
        File dataPath = checkAndCreateDir(context.getApplicationInfo().dataDir + File.separator + Constants.OPENTEE_DIR_NAME);
        return dataPath.getAbsolutePath();
    }
}
