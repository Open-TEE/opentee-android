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

package fi.aalto.ssg.opentee;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read setting from "ot_rt_setting.properties" ().
 */
public class Setting {
    final String TAG = "Setting";
    String mPropFilePath;
    Properties mProp;

    public Setting(Context context){
        if(context == null){
            Log.e(TAG, "invalid application context.");
            return;
        }

        try {
            mPropFilePath = OTUtils.getFullPath(context) +
                            File.separator +
                            OTConstants.OPENTEE_RUNTIME_SETTING;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        InputStream inputStream;
        try {
            inputStream = OTUtils.fileToInputStream(mPropFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, mPropFilePath + " not found");
            return;
        }

        mProp = new Properties();
        try {
            mProp.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "unable to load input stream from setting file.");
        }
    }

    public synchronized void updateSetting(final String settingKey, final String newSetting){
        if(mProp == null){
            Log.e(TAG, "update setting failed: invalid property setting");
            return;
        }
        mProp.setProperty(settingKey, newSetting);
    }

    public synchronized String getSetting(final String settingKey){
        if(mProp == null){
            Log.e(TAG, "get setting failed: invalid property setting");
            return null;
        }
        return mProp.getProperty(settingKey);
    }

    public synchronized void saveSetting(){
        if(mProp == null){
            Log.e(TAG, "save setting failed: invalid property setting");
            return;
        }

        try {
            mProp.store(OTUtils.fileToOutputStream(mPropFilePath), null);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "save setting failed.");
        }

        Log.i(TAG, "setting saved.");
    }
}

