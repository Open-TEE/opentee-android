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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpenTEEService extends Service {

    public static final String OPEN_TEE_SERVICE_TAG = "OpenTEEService";

    // Fields used for passing around data in messages
    public static final int MSG_INSTALL_ASSET = 1;
    public static final int MSG_INSTALL_CONF = 2;
    public static final int MSG_INSTALL_ALL = 3;
    public static final int MSG_RUN_BIN = 4;
    public static final int MSG_SELINUX_TO_PERMISSIVE = 5;
    public static final int MSG_INSTALL_FILE = 6;
    public static final int MSG_STOP_OPENTEE_ENGINE = 7;
    public static final int MSG_START_OPENTEE_ENGINE = 8;
    public static final int MSG_RESTART_OPENTEE_ENGINE = 9;
    public static final String MSG_ASSET_NAME = "MSG_ASSET_NAME";
    public static final String MSG_FILE_NAME = "MSG_FILE_NAME";
    public static final String MSG_DEST_SUBDIR = "MSG_DEST_SUBDIR";
    public static final String MSG_CONF_NAME = "MSG_CONF_NAME";
    public static final String MSG_OVERWRITE = "MSG_OVERWRITE";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private ExecutorService executor;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        // A weak reference to the enclosing context
        private WeakReference<Context> mContext;

        public ServiceHandler(Context context, Looper looper) {
            super(looper);
            mContext = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            Log.i(OPEN_TEE_SERVICE_TAG, "CPU ABI " + Arrays.toString(Build.SUPPORTED_ABIS));

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
            Bundle data = msg.getData();
            switch (msg.what) {
                case OpenTEEService.MSG_INSTALL_ASSET:
                    installAssetToHomeDir(mContext.get(), data.getString(MSG_ASSET_NAME), data.getString(MSG_DEST_SUBDIR), data.getBoolean(MSG_OVERWRITE));
                    break;
                case OpenTEEService.MSG_INSTALL_CONF:
                    installConfigToHomeDir(mContext.get(), data.getString(MSG_CONF_NAME));
                    break;
                case OpenTEEService.MSG_SELINUX_TO_PERMISSIVE:
                    setSELinuxToPermissive(mContext.get());
                    break;
                case OpenTEEService.MSG_INSTALL_FILE:
                    installFileToHomeDir(mContext.get(), data.getString(MSG_FILE_NAME), data.getString(MSG_DEST_SUBDIR), data.getBoolean(MSG_OVERWRITE));
                    break;
                case OpenTEEService.MSG_STOP_OPENTEE_ENGINE:
                    stopOpenTEEEngine(mContext.get());
                    break;
                case OpenTEEService.MSG_START_OPENTEE_ENGINE:
                    startOpenTEEEngine(mContext.get());
                    break;
                case OpenTEEService.MSG_RESTART_OPENTEE_ENGINE:
                    stopOpenTEEEngine(mContext.get());
                    startOpenTEEEngine(mContext.get());
                    break;
                case OpenTEEService.MSG_RUN_BIN:
                    // Setup the environment variable HOME to point to data home directory
                    Map<String, String> environmentVars = new HashMap<>();
                    environmentVars.put("HOME", Utils.getFullFileDataPath(mContext.get()));
                    environmentVars.put("LD_LIBRARY_PATH", mContext.get().getApplicationInfo().dataDir + File.separator + "lib");
                    Log.d(OPEN_TEE_SERVICE_TAG, "LD_LIBRARY_PATH: " + mContext.get().getApplicationInfo().dataDir + File.separator + "lib");
                    execBinaryFromHomeDir(mContext.get(), data.getString(MSG_ASSET_NAME), environmentVars);
                    break;
                case OpenTEEService.MSG_INSTALL_ALL:
                    installConfigToHomeDir(mContext.get(), Constants.OPENTEE_CONF_NAME);
                    boolean overwrite = data.getBoolean(MSG_OVERWRITE);
                    installAssetToHomeDir(mContext.get(), Constants.OPENTEE_ENGINE_ASSET_BIN_NAME, Constants.OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.STORAGE_TEST_ASSET_BIN_NAME, Constants.OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.STORAGE_TEST_CA_ASSET_BIN_NAME, Constants.OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.PKCS11_TEST_ASSET_BIN_NAME, Constants.OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.LIB_TA_STORAGE_TEST_ASSET_TA_NAME, Constants.OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.LIB_TA_PKCS11_ASSET_TA_NAME, Constants.OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.LIB_TA_CONN_TEST_APP_ASSET_TA_NAME, Constants.OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.LIB_LAUNCHER_API_ASSET_TEE_NAME, Constants.OPENTEE_TEE_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), Constants.LIB_MANAGER_API_ASSET_TEE_NAME, Constants.OPENTEE_TEE_DIR, overwrite);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }


    }

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread("OpenTEEServiceHandler");
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(getApplicationContext(), mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(OPEN_TEE_SERVICE_TAG, "Service starting");

        // TEST: set selinux to permissive
        //mServiceHandler.sendMessage(mServiceHandler.obtainMessage(MSG_SELINUX_TO_PERMISSIVE));

        // TEST: kill opentee engine if it was previously running
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(MSG_STOP_OPENTEE_ENGINE));

        // For each start request, send a message to start a job
        if (false) {
            // INSTALL ALL
            Message msg = mServiceHandler.obtainMessage(MSG_INSTALL_ALL);
            Bundle b = new Bundle();
            b.putBoolean(MSG_OVERWRITE, true);
            msg.setData(b);
            mServiceHandler.sendMessage(msg);
        } else {
            // RUN OPENTEE
            Message msg = mServiceHandler.obtainMessage(MSG_RUN_BIN);
            Bundle b = new Bundle();
            String dataHomeDir = Utils.getFullFileDataPath(getApplicationContext());
            b.putString(MSG_ASSET_NAME, Constants.OPENTEE_BIN_DIR + File.separator + Constants.OPENTEE_ENGINE_ASSET_BIN_NAME + " -c "
                    + dataHomeDir + File.separator + Constants.OPENTEE_CONF_NAME
                    + " -p " + dataHomeDir);
            msg.setData(b);
            mServiceHandler.sendMessage(msg);
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {

        try {
            if (executor != null) {
                if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("task completed");
                } else {
                    System.out.println("Forcing shutdown...");
                    executor.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
        }
        Log.i(OPEN_TEE_SERVICE_TAG, "Service stopped");
    }

    private void installAssetToHomeDir(final Context context, final String assetName, final String destSubdir, final boolean overwrite) {
        if (executor != null)
            executor.submit(new Runnable() {
            public void run() {
                // Asset folder containing the binary based on the first
                // supported CPU architecture (ABI) (i.e. armeabi, armeabi-v7a, x86)
                String originAssetPath = Build.SUPPORTED_ABIS[0] + File.separator + assetName;
                Log.d(OPEN_TEE_SERVICE_TAG, "Copying from: " + originAssetPath);
                try (InputStream inFile = context.getAssets().open(originAssetPath)) {
                    installFileToHomedir(context, inFile, destSubdir, assetName, overwrite);
                } catch (IOException | InterruptedException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void installFileToHomeDir(final Context context, final String filePath, final String destSubdir, final boolean overwrite) {
        if (executor != null)
            executor.submit(new Runnable() {
            public void run() {
                Log.d(OPEN_TEE_SERVICE_TAG, "Copying from: " + filePath);
                try (InputStream inFile = new FileInputStream(filePath)) {
                    // last string after File.separator is our file's name
                    String fileName = filePath.substring(filePath.lastIndexOf(File.pathSeparatorChar));
                    installFileToHomedir(context, inFile, destSubdir, fileName, overwrite);
                } catch (IOException | InterruptedException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void installFileToHomedir(Context context, InputStream inFile, String destSubdir, String assetName, boolean overwrite) throws IOException, InterruptedException {
        String destPath = Utils.getFullFileDataPath(context);
        // if you have to install in a subdir, check and create it if necessary
        if (destSubdir != null) {
            destPath += File.separator + destSubdir;
            Utils.checkAndCreateDir(destPath);
        }
        destPath += File.separator + assetName;
        Log.d(OPEN_TEE_SERVICE_TAG, "App Data home Dir: " + destPath);

        File outFile = new File(destPath);

        // If the file doesn't exist or if we don't care (overwrite mode)
        if (!outFile.exists() || overwrite) {
            // Copy and chmod the new file
            Log.d(OPEN_TEE_SERVICE_TAG, "Copying to: " + destPath);
            Utils.copyStream(context,
                    inFile,
                    new FileOutputStream(outFile, false)); // we don't want to append, just (over)write
            String output = Utils.execUnixCommand(("/system/bin/chmod 744 " + destPath).split(" "), null);
            if (!output.isEmpty()) {
                Log.d(OPEN_TEE_SERVICE_TAG, "Chmod returned: " + output);
            }

        }
    }

    /**
     * Installs configuration file to app data home directory and changes the config's contents to
     * match this directory. In essense it replaces any occurence of OPENTEEDIR (a placeholder) in the
     * config to the proper data home directory (/data/data/some.package.name)
     *
     * @param context
     * @param confFileName
     */
    private void installConfigToHomeDir(final Context context, final String confFileName) {
        if (executor != null)
            executor.submit(new Runnable() {
            public void run() {
                String output = "";
                String destPath = Utils.getFullFileDataPath(context) + File.separator + confFileName;

                // Asset folder containing the binary based on the first
                // supported CPU architecture (ABI) (i.e. armeabi, armeabi-v7a, x86)
                Log.d(OPEN_TEE_SERVICE_TAG, "App Data home Dir: " + destPath);

                File outConfFile = new File(destPath);

                // If the file doesn't exist
                if (!outConfFile.exists()) {
                    try (BufferedReader inReader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 context.getAssets().open(confFileName)
                                                 , "UTF-8"))) {
                        // Copy and chmod the new file
                        Log.d(OPEN_TEE_SERVICE_TAG, "Copying " + destPath + " TO " + destPath);

                        try (PrintWriter outWriter = new PrintWriter(outConfFile, "UTF-8")) {

                            String line = null;
                            while ((line = inReader.readLine()) != null) {
                                line = line.replaceAll("\\b" + Constants.OPENTEE_DIR_CONF_PLACEHOLDER + "\\b", Utils.getFullFileDataPath(context));
                                System.out.println(line);
                                outWriter.println(line);
                            }

                        } catch (IOException e) {
                            Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                            e.printStackTrace();
                        } finally {
                            inReader.close();
                        }
                        output = Utils.execUnixCommand(("/system/bin/chmod 640 " + destPath).split(" "), null);
                        Log.d(OPEN_TEE_SERVICE_TAG, "Chmod returned: " + output);

                    } catch (InterruptedException | IOException e) {
                        Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    private void execBinaryFromHomeDir(final Context context, final String binaryName, final Map<String, String> environmentVars) {
        if (executor != null)
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        String destPath = Utils.getFullFileDataPath(context) + File.separator + binaryName;
                        String output = Utils.execUnixCommand(destPath.split(" "), environmentVars);
                        if (!output.isEmpty()) {
                            Log.d(OPEN_TEE_SERVICE_TAG, "Execution of binary " + destPath + " returned: " + output);
                        }
                    } catch (InterruptedException | IOException e) {
                        Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                    }
                }
            });
    }

    /**
     * Necessary for open_tee_sock to be created. open_tee_sock is used for communication between manager and
     * libtee and by default SELinux disallows the usage of /data/local/tmp which is hardcoded in OpenTEE.
     * Also chmod is run on the directory where open_tee_sock is by default created since it's still not configurable.
     * <p/>
     * Requires root permissions. Needs SuperSu to work. Alternatively just
     * run "su -c setenforce 0" through adb root.
     *
     * @param context
     */
    private void setSELinuxToPermissive(final Context context) {
        if (executor != null)
            executor.submit(new Runnable() {
            public void run() {
                try {
                    if (RootShell.isAccessGiven()) {
                        Command command = new Command(0, "/system/bin/setenforce 0", "/system/bin/chmod 777 /data/local/tmp/");
                        try {
                            RootShell.getShell(true).add(command);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (RootDeniedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                }
            }
        });
    }

    public void startOpenTEEEngine(Context context) {
        String dataHomeDir = Utils.getFullFileDataPath(context);
        String command = Constants.OPENTEE_BIN_DIR + File.separator + Constants.OPENTEE_ENGINE_ASSET_BIN_NAME + " -c "
                + dataHomeDir + File.separator + Constants.OPENTEE_CONF_NAME
                + " -p " + dataHomeDir;
        Map<String, String> environmentVars = new HashMap<>();
        environmentVars.put("HOME", dataHomeDir);
        environmentVars.put("LD_LIBRARY_PATH", context.getApplicationInfo().dataDir + File.separator + "lib");
        Log.d(OPEN_TEE_SERVICE_TAG, "LD_LIBRARY_PATH: " + context.getApplicationInfo().dataDir + File.separator + "lib");
        execBinaryFromHomeDir(context, command, environmentVars);
    }

    private void stopOpenTEEEngine(final Context context) {
        if (executor != null)
            executor.submit(new Runnable() {
            public void run() {
                try {
                    // Find PID of opentee-engine
                    String command = "/system/bin/ps | /system/bin/grep tee_manager";
                    String output = Utils.execUnixCommand(command.split(" "), null);
                    if (!output.isEmpty()) {
                        String[] lines = output.split("\n");
                        for (int i = 1; i < lines.length; i++) { // avoid first line with headers
                            // Process found
                            String pid = lines[i].trim().replaceAll("[ ]+", " ").split(" ")[1]; // take the second column (pid)
                            Log.d(OPEN_TEE_SERVICE_TAG, "OpenTEE PID is " + pid + ". Trying to kill...");
                            if (!pid.isEmpty()) {
                                try {
                                    android.os.Process.killProcess(Integer.parseInt(pid));
                                } catch (NumberFormatException e) {
                                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                                }
                            }
                        }
                        Log.d(OPEN_TEE_SERVICE_TAG, "Execution of " + command + " returned: " + output);
                    }
                    // Delete socket file
                    command = "/system/bin/rm " + Constants.OPENTEE_SOCKET_PATH;
                    output = Utils.execUnixCommand(command.split(" "), null);
                    if (!output.isEmpty()) {
                        Log.d(OPEN_TEE_SERVICE_TAG, "Execution of " + command + " returned: " + output);
                    }
                    // Delete pid file
                    command = "/system/bin/rm " + Utils.getFullFileDataPath(context) + File.separator + Constants.OPENTEE_PID_FILENAME;
                    output = Utils.execUnixCommand(command.split(" "), null);
                    if (!output.isEmpty()) {
                        Log.d(OPEN_TEE_SERVICE_TAG, "Execution of " + command + " returned: " + output);
                    }
                } catch (InterruptedException | IOException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                }
            }
        });
    }
}
