package fi.aalto.ssg.opentee.bundletest;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import fi.aalto.ssg.opentee.OT;
import fi.aalto.ssg.opentee.OTConstants;
import fi.aalto.ssg.opentee.OTInstallTA;
import fi.aalto.ssg.opentee.OTUtils;
import fi.aalto.ssg.opentee.Worker;

public class MainActivity extends AppCompatActivity {
    class InstallAndStartCABin{
        String mCaName;

        public InstallAndStartCABin(String caName){
            this.mCaName = caName;
        }

        public Runnable task = new Runnable() {
            @Override
            public void run() {
                /* install conn_test CA into $app/opentee/bin */
                OTUtils.installAssetToHomeDir(getApplicationContext(), mCaName, OTConstants.OT_BIN_DIR, true);

                /* start the test */
                Map<String, String> envVars;
                try {
                    envVars = OTUtils.getOTEnvVars(getApplicationContext());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();

                    Log.e(TAG, "Test failed! Unable to get environment variables for Open-TEE");

                    return;
                }

                Worker worker = new Worker();
                worker.execBinaryInHomeDir(getApplicationContext(), mCaName, envVars);
                worker.stopExecutor();
            }
        };
    }

    final String TAG = "BundleTest";

    final String CONN_TA_NAME = "libta_conn_test_app.so";
    final String CONN_CA_NAME = "conn_test_app";

    HandlerThread mHandlerThread;
    Handler mHandler;
    OT mOT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* create another thread to avoid too much work in main thread */
        mHandlerThread = new HandlerThread("tough worker"); // create a separate working thread.
        mHandlerThread.start(); // start the thread.
        mHandler = new Handler(mHandlerThread.getLooper()); // get the handle to this working thread to post task to it later.

        /* install and run Open-TEE */
        this.mOT = new OT(getApplication());
        mHandler.post(mOT.installationTask);

        /* install conn_test TA */
        byte[] connTaInBytes = Utils.getFileFromLib(getApplicationContext(), CONN_TA_NAME);  // read the TA to raw byte array under app lib directory.

        if(connTaInBytes == null){
            Log.e(TAG, "Test failed! Unable to get TA:" + CONN_TA_NAME);
            return;
        }

        // create a new install TA task. The TA will be stored into $APP_DATA_PATH/opentee/ta with the name STORAGE_TA_NAME. Open-TEE will automatically start the TA.
        OTInstallTA installConnTA = new OTInstallTA(getApplicationContext(),    // application context
                CONN_TA_NAME,   // the name to be stored.
                connTaInBytes,  // TA in raw bytes.
                true);          // overwrite previous TA if exists. A quick note in here.
                                // If there is an older version of this TA running and this parameter set to true, Open-TEE will not load this new version util Open-TEE restarts.

        // post this task to the working thread.
        mHandler.post(installConnTA.installTATask);

        /* install and run conn_test CA */
        mHandler.post(new InstallAndStartCABin(CONN_CA_NAME).task);
    }

    @Override
    protected void onDestroy() {
        /* stop open-tee engine */
        mHandler.post(mOT.stopEngineTask);

        Log.d(TAG, "Stopping the engine");

        if ( mHandlerThread != null ){
            mHandlerThread.quitSafely();
        }

        super.onDestroy();
    }
}
