package fi.aalto.ssg.opentee;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Open-TEE engine utility wrapper.
 */
public class OT {
    final String TAG = "Open-TEE";

    public static final String TA_LIST = "TA_List";

    Application mApp;

    public OT(Application app){
        this.mApp = app;
    }

    /* Overall installation task after initial launch of the application */
    public Runnable installationTask = new Runnable() {
        @Override
        public void run() {

            /* prepare to start */
            Worker worker = new Worker();
            Context context = mApp.getApplicationContext();

            Log.d(TAG, "Ready files for opentee to run");

            /* fresh copy for open-tee and TAs */
            boolean overwrite = true;

            /* install configuration file */
            worker.installConfigToHomeDir(context, OTConstants.OPENTEE_CONF_NAME);

            /* install OT runtime setting file */
            worker.installConfigToHomeDir(context, OTConstants.OPENTEE_RUNTIME_SETTING);

            /* install open-tee */
            worker.installAssetToHomeDir(context, OTConstants.OPENTEE_ENGINE_ASSET_BIN_NAME, OTConstants.OT_BIN_DIR, overwrite);
            worker.installAssetToHomeDir(context, OTConstants.LIB_LAUNCHER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);
            worker.installAssetToHomeDir(context, OTConstants.LIB_MANAGER_API_ASSET_TEE_NAME, OTConstants.OT_TEE_DIR, overwrite);

            /* install TAs */
            Setting setting = new Setting(mApp.getApplicationContext());
            String propertiesStr = setting.getSetting(TA_LIST);
            if(propertiesStr == null){
                Log.e(TAG, "no TA to be deployed!");

                // still create TA folder. Otherwise, Open-TEE will report it as an error.
                try {
                    OTUtils.createDirInHome(context, OTConstants.OT_DIR_NAME + File.separator + OTConstants.OT_TA_DIR);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();

                    Log.e(TAG, "Unable to create TA folder, installation abort!");
                    return;
                }
            }
            else{
                Log.i(TAG, "-------- begin installing TAs -----------");

                String[] properties = propertiesStr.split(",");
                for(String taName: properties){
                    if(!taName.isEmpty()){
                        Log.i(TAG, "installing TA:" + taName);
                        worker.installAssetToHomeDir(context, taName, OTConstants.OT_TA_DIR, overwrite);
                    }
                }

                Log.i(TAG, "-----------------------------------------");
            }

            /* start the open-tee engine */
            try {
                worker.startOpenTEEEngine(context);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            worker.stopExecutor();

            Log.d(TAG, "Installation ready");

        }
    }; // end of installation task.

    public Runnable stopEngineTask = new Runnable() {
        @Override
        public void run() {
            Worker worker = new Worker();

            worker.stopOpenTEEEngine(mApp.getApplicationContext());
            worker.stopExecutor();
        }
    };
}
