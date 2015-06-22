package fi.aalto.opentee;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.opensc.pkcs11.PKCS11LoadStoreParameter;
import org.opensc.pkcs11.PKCS11Provider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Random;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        // Add provider "SunPKCS11-OpenSC"
        String pkcs11_path;

        if (System.getProperty("os.name").contains("Windows"))
            pkcs11_path = System.getenv("ProgramFiles")+"\\Smart Card Bundle\\opensc-pkcs11.dll";
        else
            pkcs11_path = "libtee_pkcs11.so"; // TODO PASS LOCAL BUNDLED .SO FILE

        this.provider = new PKCS11Provider(pkcs11_path);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
