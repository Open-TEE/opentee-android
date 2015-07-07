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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OpenTEEService extends Service {

    public static final String OPEN_TEE_SERVICE_TAG = "OpenTEEService";

    // Used as name for the directory containing our whole installation
    public static final String OPENTEE_DIR_NAME = "opentee";
    public static final String OPENTEE_BIN_DIR = "bin";
    public static final String OPENTEE_TA_DIR = "ta";
    public static final String OPENTEE_TEE_DIR = "tee";

    // These describe where the files are located in the assets/
    public static final String OPENTEE_ENGINE_ASSET_BIN_NAME = "opentee-engine";
    public static final String STORAGE_TEST_ASSET_BIN_NAME = "storage_test";
    public static final String STORAGE_TEST_CA_ASSET_BIN_NAME = "storage_test_ca";
    public static final String PKCS11_TEST_ASSET_BIN_NAME = "pkcs11_test";
    public static final String LIB_TA_STORAGE_TEST_ASSET_TA_NAME = "libta_storage_test.so";
    public static final String LIB_TA_PKCS11_ASSET_TA_NAME = "libta_pkcs11_ta.so";
    public static final String LIB_TA_CONN_TEST_APP_ASSET_TA_NAME = "libta_conn_test_app.so";
    public static final String LIB_LAUNCHER_API_ASSET_TEE_NAME = "libLauncherApi.so";
    public static final String LIB_MANAGER_API_ASSET_TEE_NAME = "libManagerApi.so";

    public static final String OPENTEE_CONF_NAME = "opentee.conf.android";

    // Placeholder used in the assets/opentee.conf.android to be replaced at installation with the app data home dir
    public static final String OPENTEE_DIR_CONF_PLACEHOLDER = "OPENTEEDIR";

    // Fields used for passing around data in messages
    private static final int MSG_INSTALL_ASSET = 1;
    private static final int MSG_INSTALL_CONF = 2;
    private static final int MSG_INSTALL_ALL = 3;
    private static final int MSG_RUN_BIN = 4;
    private static final String MSG_ASSET_NAME = "MSG_ASSET_NAME";
    private static final String MSG_ASSET_SUBDIR = "MSG_ASSET_SUBDIR";
    private static final String MSG_CONF_NAME = "MSG_CONF_NAME";
    private static final String MSG_OVERWRITE = "MSG_OVERWRITE";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

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
                    installAssetToHomeDir(mContext.get(), data.getString(MSG_ASSET_NAME), data.getString(MSG_ASSET_SUBDIR), data.getBoolean(MSG_OVERWRITE));
                    break;
                case OpenTEEService.MSG_INSTALL_CONF:
                    installConfigToHomeDir(mContext.get(), data.getString(MSG_CONF_NAME));
                    break;
                case OpenTEEService.MSG_RUN_BIN:
                    // Setup the environment variable HOME to point to data home directory
                    Map<String, String> environmentVars = new HashMap<>();
                    environmentVars.put("HOME", Utils.getFullFileDataPath(mContext.get()));
                    execBinaryFromHomeDir(mContext.get(), data.getString(MSG_ASSET_NAME), environmentVars);
                    break;
                case OpenTEEService.MSG_INSTALL_ALL:
                    installConfigToHomeDir(mContext.get(), OPENTEE_CONF_NAME);
                    boolean overwrite = data.getBoolean(MSG_OVERWRITE);
                    installAssetToHomeDir(mContext.get(), OPENTEE_ENGINE_ASSET_BIN_NAME, OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), STORAGE_TEST_ASSET_BIN_NAME, OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), STORAGE_TEST_CA_ASSET_BIN_NAME, OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), PKCS11_TEST_ASSET_BIN_NAME, OPENTEE_BIN_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), LIB_TA_STORAGE_TEST_ASSET_TA_NAME, OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), LIB_TA_PKCS11_ASSET_TA_NAME, OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), LIB_TA_CONN_TEST_APP_ASSET_TA_NAME, OPENTEE_TA_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), LIB_LAUNCHER_API_ASSET_TEE_NAME, OPENTEE_TEE_DIR, overwrite);
                    installAssetToHomeDir(mContext.get(), LIB_MANAGER_API_ASSET_TEE_NAME, OPENTEE_TEE_DIR, overwrite);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
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

        // For each start request, send a message to start a job
        if (true) {
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
            b.putString(MSG_ASSET_NAME, OPENTEE_BIN_DIR + File.separator + OPENTEE_ENGINE_ASSET_BIN_NAME + " -c "
                    + dataHomeDir + File.separator + OPENTEE_CONF_NAME
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
        Log.i(OPEN_TEE_SERVICE_TAG, "Service stopped");
    }

    private void installAssetToHomeDir(final Context context, final String assetName, final String subdir, final boolean overwrite) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                String output = "";
                String destPath = Utils.getFullFileDataPath(context);
                // if you have to install in a subdir, check and create it if necessary
                if (subdir != null) {
                    destPath += File.separator + subdir;
                    Utils.checkAndCreateDir(destPath);
                }
                destPath += File.separator + assetName;
                // Asset folder containing the binary based on the first
                // supported CPU architecture (ABI) (i.e. armeabi, armeabi-v7a, x86)
                String originAssetPath = Build.SUPPORTED_ABIS[0] + File.separator + assetName;
                Log.d(OPEN_TEE_SERVICE_TAG, "App Data home Dir: " + destPath);

                File outFile = new File(destPath);

                // If the file doesn't exist or if we don't care (overwrite mode)
                if (!outFile.exists() || overwrite) {
                    // Copy and chmod the new file
                    try (InputStream inFile = context.getAssets().open(originAssetPath)) {

                        Log.d(OPEN_TEE_SERVICE_TAG, "Copying " + originAssetPath + " TO " + destPath);
                        Utils.copyStream(context,
                                inFile,
                                new FileOutputStream(outFile, false)); // we don't want to append, just (over)write
                        output = Utils.execUnixCommand(("/system/bin/chmod 744 " + destPath).split(" "), null);
                        if (!output.isEmpty()) {
                            Log.d(OPEN_TEE_SERVICE_TAG, "Chmod returned: " + output);
                        }
                    } catch (IOException | InterruptedException e) {
                        Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }

            }
        });
        thread.start();
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
        Thread thread = new Thread(new Runnable() {
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
                                line = line.replaceAll("\\b" + OPENTEE_DIR_CONF_PLACEHOLDER + "\\b", Utils.getFullFileDataPath(context));
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
        thread.start();
    }

    private void execBinaryFromHomeDir(final Context context, final String binaryName, final Map<String, String> environmentVars) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String output = "";
                    String destPath = Utils.getFullFileDataPath(context) + File.separator + binaryName;
                    output = Utils.execUnixCommand(destPath.split(" "), environmentVars);
                    if (!output.isEmpty()) {
                        Log.d(OPEN_TEE_SERVICE_TAG, "Execution of binary " + destPath + " returned: " + output);
                    }
                } catch (InterruptedException | IOException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                }
            }
        });
        thread.start();
    }
}