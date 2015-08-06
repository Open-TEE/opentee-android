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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.opensc.pkcs11.PKCS11LoadStoreParameter;
import org.opensc.pkcs11.PKCS11Provider;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Random;

import fi.aalto.ssg.opentee_mainapp.Constants;
import fi.aalto.ssg.opentee_mainapp.OpenTEEService;
import fi.aalto.ssg.opentee_mainapp.Utils;


public class MainActivity extends Activity {

    /** Messenger for communicating with the service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
            runTests();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    private void runTests() {
        testInstallationOfOpenTEEToHomeDir();
        testRestartOpenTEE(); // Also cleans up any remains from previous runs
        //testStopOpenTEE();
    }

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService();
    }

    @Override
    protected void onDestroy() {

        stopService();
        super.onDestroy();
    }
    /* opentee_mainapp tests */

    public void startService() {
        // Bind to the service
        bindService(new Intent(this, OpenTEEService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void stopService() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    public void testStartOpenTEE() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_START_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void testRestartOpenTEE() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_RESTART_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void testStopOpenTEE() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_STOP_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void testSELinuxToPermissive() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_SELINUX_TO_PERMISSIVE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void testInstallationOfOpenTEEToHomeDir() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_INSTALL_ALL, 0, 0);
        Bundle b = new Bundle();
        b.putBoolean(OpenTEEService.MSG_OVERWRITE, true);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs opentee binary from home dir bin/ folder
     * e.g. to run opentee provide the following as argument:
     * Constants.OPENTEE_BIN_DIR + File.separator + Constants.OPENTEE_ENGINE_ASSET_BIN_NAME
     */
    public void testRunBinary(String openteeBinary) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_RUN_BIN, 0, 0);
        Bundle b = new Bundle();
        String dataHomeDir = Utils.getFullFileDataPath(getApplicationContext());
        b.putString(OpenTEEService.MSG_ASSET_NAME, Constants.OPENTEE_BIN_DIR + File.separator + Constants.OPENTEE_ENGINE_ASSET_BIN_NAME + " -c "
                + dataHomeDir + File.separator + Constants.OPENTEE_CONF_NAME
                + " -p " + dataHomeDir);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
