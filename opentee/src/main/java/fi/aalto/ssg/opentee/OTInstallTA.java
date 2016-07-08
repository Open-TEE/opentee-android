package fi.aalto.ssg.opentee;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Open-TEE install TA task.
 */
public class OTInstallTA{
    final String TAG = "OTInstallTA";

    Context mContext;
    String mTAName;
    byte[] mTAinBytes;
    boolean mOverwrite = true;

    public OTInstallTA(Context context, String taName, byte[] ta, boolean overwrite){
        this.mContext = context;
        this.mTAName = taName;
        this.mTAinBytes = ta;
        this.mOverwrite = overwrite;
    }

    public Runnable installTATask = new Runnable() {
        @Override
        public void run() {
            Worker worker = new Worker();

            worker.installBytesToHomeDir(mContext,
                    mTAinBytes,
                    OTConstants.OT_TA_DIR,
                    mTAName,
                    mOverwrite);

            worker.stopExecutor();

            /* update setting */
            Setting setting = new Setting(mContext);

            // add new TA name to the old list if it is not existed.
            String propertiesStr = setting.getSetting(OT.TA_LIST);
            List<String> taList = Arrays.asList( propertiesStr.split(",") );

            if(!taList.contains(mTAName)) {
                propertiesStr += "," + mTAName;

                // update and save setting.
                setting.updateSetting(OT.TA_LIST, propertiesStr);
                setting.saveSetting();

                Log.i(TAG, mTAName + " added.");
            }else{
                Log.i(TAG, mTAName + " already existed. So will not be added and only update its binary.");
            }
        }
    };
}