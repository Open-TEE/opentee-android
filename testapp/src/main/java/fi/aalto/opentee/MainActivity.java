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
package fi.aalto.opentee;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.aalto.ssg.opentee.OTCallback;
import fi.aalto.ssg.opentee.OTConstants;
import fi.aalto.ssg.opentee.OTUtils;
import fi.aalto.ssg.opentee.OpenTEEConnection;


public class MainActivity extends Activity {

    private OpenTEEConnection mOpenTEEConnection;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenTEEConnection = new OpenTEEConnection(this, new OTCallback() {
            @Override
            public void onConnectionEstablished() {
                runTests();
            }

            @Override
            public void onConnectionDestroyed() { }
        });

    }

    @Override
    protected void onDestroy() {
        mOpenTEEConnection.stopConnection();
        super.onDestroy();
    }

    /* opentee tests */

    private void runTests() {
        mOpenTEEConnection.installOpenTEEToHomeDir(true);

        mOpenTEEConnection.restartOTEngine(); // Also cleans up any remains from previous runs

        mOpenTEEConnection.runOTBinary(OTConstants.STORAGE_TEST_APP_ASSET_BIN_NAME);
        testInstallFileStream();
        //mOpenTEEConnection.stopOTEngine();
    }

    public void testInstallFileStream() {
        InputStream inFile = null;
        try {
            // test by installing the conf file (our supposed TA).
            String originPath = OpenTEEConnection.getOTLConfPath(getApplicationContext());
            inFile = new FileInputStream(originPath);
        } catch (IOException e) {
            Log.e("MAINACTIVITY", e.getMessage());
            e.printStackTrace();
        }
        byte[] inBytes;
        try {
            inBytes = OTUtils.readBytesFromStream(inFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mOpenTEEConnection.installByteStreamTA(inBytes, "testFile", OTConstants.OPENTEE_TA_DIR, true);
    }
}
