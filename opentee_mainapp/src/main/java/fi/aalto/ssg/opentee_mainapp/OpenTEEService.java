package fi.aalto.ssg.opentee_mainapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class OpenTEEService extends Service {

    public static final String OPEN_TEE_SERVICE_TAG = "OpenTEEService";

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
            Log.i(OPEN_TEE_SERVICE_TAG, "CPU ABI " + Arrays.toString(Build.SUPPORTED_ABIS) + " " + System.getProperty("os.arch") );
            installBinaryToHomeDir(mContext.get(), "opentee-engine");

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
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
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
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

    private void output(final String str) {
        Runnable proc = new Runnable() {
            public void run() {
                Log.i(OPEN_TEE_SERVICE_TAG, str);
            }
        };
        mServiceHandler.post(proc);
    }

    private void installBinaryToHomeDir(final Context context, final String binaryName) {
        final String destPath = context.getApplicationInfo().dataDir + File.separator + binaryName;
        final String originAssetPath = Build.SUPPORTED_ABIS[0] + File.separator + binaryName;
        Log.i(OPEN_TEE_SERVICE_TAG, "Home Dir: " + destPath);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String output = "";
                    Log.i(OPEN_TEE_SERVICE_TAG, "Copying " + originAssetPath + " TO " + destPath);
                    Utils.copyFromAssetsToAppDir(context, originAssetPath, destPath);
                    output = Utils.execUnixCommand("/system/bin/chmod 744 " + destPath);
                    Log.i(OPEN_TEE_SERVICE_TAG, "Chmod returned: " + output);
                    //output = Utils.execUnixCommand(destPath);
                    //Log.i(OPEN_TEE_SERVICE_TAG, "Execution of binary returned: " + output);
                } catch (InterruptedException | IOException e) {
                    Log.e(OPEN_TEE_SERVICE_TAG, e.getMessage());
                }
            }
        });
        thread.start();
    }
}