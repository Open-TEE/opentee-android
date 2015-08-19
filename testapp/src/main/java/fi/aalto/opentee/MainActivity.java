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

import org.opensc.pkcs11.PKCS11LoadStoreParameter;
import org.opensc.pkcs11.PKCS11Provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Random;

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

    /* openteelib tests */

    private void testPKCS11() {
        try {
            setUp();
            testKeyStore();
            tearDown();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    public void testKeyStore() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS11","OpenSC-PKCS11");

        PKCS11LoadStoreParameter params  = new PKCS11LoadStoreParameter();

        PinEntryUI pe = new PinEntryUI(PinEntryUI.PinType.USER_PIN, "12345678");
        pe.setPin(PinEntryUI.PinType.SO_PIN, "123456789");

        params.setWaitForSlot(true);
        params.setProtectionCallback(pe);
        params.setEventHandler(pe);

        ks.load(params);

        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();

            System.out.println("alias="+alias);

            System.out.println(" isKey="+ks.isKeyEntry(alias));
            System.out.println(" isCertificate="+ks.isCertificateEntry(alias));

            if (ks.isCertificateEntry(alias))
            {
                Certificate certificate = ks.getCertificate(alias);
                System.out.println(" certificate="+certificate);
                System.out.println(" certAlias="+ks.getCertificateAlias(certificate));

                Certificate [] chain = ks.getCertificateChain(alias);

                for (int i=0;i<chain.length;++i)
                {
                    X509Certificate x509 = (X509Certificate)chain[i];

                    System.out.println(" chain["+i+"].subject="+x509.getSubjectX500Principal());
                    System.out.println(" chain["+i+"].issuer="+x509.getSubjectX500Principal());
                    System.out.println(" chain["+i+"].serial="+x509.getSerialNumber());
                }
            }
        }
    }

    protected PKCS11Provider provider;
    protected byte[] testData;

    public void setUp() throws IOException {

        this.provider = new PKCS11Provider( "libtee_pkcs11.so");
        Security.addProvider(this.provider);

        Provider providers[] = Security.getProviders();
        for (Provider p : providers)
            System.out.println("Found provider: " + p.getName());

        this.testData = new byte[199];

        Random random = new Random(System.currentTimeMillis());

        random.nextBytes(this.testData);
    }

    public void tearDown() {
        this.provider.cleanup();
        this.provider = null;
        this.testData = null;
        Security.removeProvider("OpenSC-PKCS11");
    }
}
