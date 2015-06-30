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

public class OpenTEEService extends Service {

    public static final String OPEN_TEE_SERVICE_TAG = "OpenTEEService";

    public static final String OPENTEE_ENGINE_BIN_NAME = "opentee-engine";
    public static final String OPENTEE_ENGINE_CONF_NAME = "opentee.conf.android";

    // Fields used for passing around data in messages
    private static final int MSG_INSTALL_BIN = 1;
    private static final int MSG_INSTALL_CONF = 2;
    private static final int MSG_RUN_BIN = 3;
    private static final String MSG_BINARY_NAME = "MSG_BINARY_NAME";
    private static final String MSG_CONF_NAME = "MSG_CONF_NAME";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private static final String OPENTEE_DIR_CONF_PLACEHOLDER = "OPENTEEDIR";
    ;

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
                case OpenTEEService.MSG_INSTALL_BIN:
                    installBinaryToHomeDir(mContext.get(), data.getString(MSG_BINARY_NAME));
                    break;
                case OpenTEEService.MSG_INSTALL_CONF:
                    installConfigToHomeDir(mContext.get(), data.getString(MSG_CONF_NAME));
                    break;
                case OpenTEEService.MSG_RUN_BIN:
                    execBinaryFromHomeDir(mContext.get(), data.getString(MSG_BINARY_NAME));
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

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage(MSG_INSTALL_CONF);
        msg.arg1 = startId;
        Bundle b = new Bundle();
        b.putString(MSG_CONF_NAME, OPENTEE_ENGINE_CONF_NAME);
        msg.setData(b);
        mServiceHandler.sendMessage(msg);

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

    private void installBinaryToHomeDir(final Context context, final String binaryName) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                String output = "";
                String destPath = getFullFileDataPath(context) + File.separator + binaryName;

                // Asset folder containing the binary based on the first
                // supported CPU architecture (ABI) (i.e. armeabi, armeabi-v7a, x86)
                String originAssetPath = Build.SUPPORTED_ABIS[0] + File.separator + binaryName;
                Log.d(OPEN_TEE_SERVICE_TAG, "App Data home Dir: " + destPath);

                File outBinFile = new File(destPath);

                // If the file doesn't exist
                if (!outBinFile.exists()) {
                    // Copy and chmod the new file
                    try (InputStream inBinFile = context.getAssets().open(originAssetPath)) {

                        Log.d(OPEN_TEE_SERVICE_TAG, "Copying " + originAssetPath + " TO " + destPath);
                        Utils.copyStream(context,
                                inBinFile,
                                new FileOutputStream(outBinFile, false)); // we don't want to append, just (over)write
                        output = Utils.execUnixCommand("/system/bin/chmod 744 " + destPath);
                        Log.d(OPEN_TEE_SERVICE_TAG, "Chmod returned: " + output);
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
                String destPath = getFullFileDataPath(context) + File.separator + confFileName;

                // Asset folder containing the binary based on the first
                // supported CPU architecture (ABI) (i.e. armeabi, armeabi-v7a, x86)
                Log.d(OPEN_TEE_SERVICE_TAG, "App Data home Dir: " + destPath);

                File outConfFile = new File(destPath);

                // If the file doesn't exist
                if (!outConfFile.exists()) {
                    try (InputStream inConfFile = context.getAssets().open(confFileName)) {
                        // Copy and chmod the new file
                        Log.d(OPEN_TEE_SERVICE_TAG, "Copying " + destPath + " TO " + destPath);

                        BufferedReader inReader = new BufferedReader(new InputStreamReader(inConfFile, "UTF-8"));
                        try (PrintWriter outWriter = new PrintWriter(outConfFile, "UTF-8")) {

                            String line = null;
                            while ((line = inReader.readLine()) != null) {
                                line = line.replaceAll("\\b" + OPENTEE_DIR_CONF_PLACEHOLDER + "\\b", getFullFileDataPath(context));
                                System.out.println(line);
                                outWriter.println(line);
                            }

                        } catch (IOException e) {
                            Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        output = Utils.execUnixCommand("/system/bin/chmod 640 " + destPath);
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

    // TODO make this return a directory under /opentee/ ...
    private static String getFullFileDataPath(Context context) {
        return context.getApplicationInfo().dataDir;
    }

    private void execBinaryFromHomeDir(final Context context, final String binaryName) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String output = "";
                    String destPath = getFullFileDataPath(context) + File.separator + binaryName;
                    output = Utils.execUnixCommand(destPath);
                    Log.d(OPEN_TEE_SERVICE_TAG, "Execution of binary returned: " + output);
                } catch (InterruptedException | IOException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                }
            }
        });
        thread.start();
    }
}