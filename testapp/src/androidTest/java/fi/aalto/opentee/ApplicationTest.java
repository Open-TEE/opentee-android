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

import android.app.Application;
import android.test.ApplicationTestCase;

import org.opensc.pkcs11.PKCS11LoadStoreParameter;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
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
}