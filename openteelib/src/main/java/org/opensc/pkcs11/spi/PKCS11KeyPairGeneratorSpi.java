/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jan 24, 2007
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 * 
 ***********************************************************/

package org.opensc.pkcs11.spi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.KeyStore.LoadStoreParameter;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.PKCS11SessionStore;
import org.opensc.pkcs11.spec.PKCS11DSAKeyPairGenParameterSpec;
import org.opensc.pkcs11.spec.PKCS11RSAKeyPairGenParameterSpec;
import org.opensc.pkcs11.wrap.PKCS11DSAKeyPairGenerator;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11KeyPairGenerator;
import org.opensc.pkcs11.wrap.PKCS11RSAKeyPairGenerator;

/**
 * This is the PKCS11 implementation of the JCE KeyPairGenerator
 * facility.
 * 
 * @author wglas
 */
public class PKCS11KeyPairGeneratorSpi extends KeyPairGeneratorSpi
{
    private static final String TAG = "PKCS11KeyPairGeneratorSpi";

    private PKCS11Provider provider;
    private String algorithm;
    private PKCS11SessionStore sessionStore;
    private boolean needToCloseSesionStore;
    private PKCS11KeyPairGenerator generator;
    
    /**
     * @param provider
     * @param algorithm
     */
    public PKCS11KeyPairGeneratorSpi(PKCS11Provider provider, String algorithm)
    {
        this.provider = provider;
        this.algorithm = algorithm;
        this.generator = null;
        this.sessionStore = null;
        this.needToCloseSesionStore = false;
    }
    
    /* (non-Javadoc)
     * @see java.security.KeyPairGeneratorSpi#initialize(int, java.security.SecureRandom)
     */
    @Override
    public void initialize(int arg0, SecureRandom arg1)
    {
        throw new UnsupportedOperationException("PKCS11KeyPairGeneratorSpi.initialize(int,SecureRandeom) is not supported.");
    }

    /* (non-Javadoc)
     * @see java.security.KeyPairGeneratorSpi#initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom)
     */
    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException
    {
        LoadStoreParameter loadStoreParameter;
        
        if ("RSA".equals(this.algorithm))
        {
            if (!(params instanceof PKCS11RSAKeyPairGenParameterSpec))
                throw new InvalidAlgorithmParameterException("RSA AlgorithmParameterSpec must be of type PKCS11RSAKeyPairGenParameterSpec.");
     
            PKCS11RSAKeyPairGenParameterSpec rsaSpec =(PKCS11RSAKeyPairGenParameterSpec)params;
            
            this.generator = new PKCS11RSAKeyPairGenerator(rsaSpec);
            loadStoreParameter = rsaSpec.getLoadStoreParameter();
        }
        else if ("DSA".equals(this.algorithm))
        {
            if (!(params instanceof PKCS11DSAKeyPairGenParameterSpec))
                throw new InvalidAlgorithmParameterException("DSA AlgorithmParameterSpec must be of type PKCS11DSAKeyPairGenParameterSpec.");
                   
            PKCS11DSAKeyPairGenParameterSpec dsaSpec = (PKCS11DSAKeyPairGenParameterSpec)params;
            this.generator = new PKCS11DSAKeyPairGenerator(dsaSpec);
            loadStoreParameter = dsaSpec.getLoadStoreParameter();
        }
        else
            throw new InvalidAlgorithmParameterException("Algorithm "+this.algorithm+" is not supported.");
        
        try{
            if (this.sessionStore != null)
            {
                if (this.needToCloseSesionStore)
                    this.sessionStore.close();
            }
                
            if (loadStoreParameter instanceof PKCS11SessionStore)
            {
                this.sessionStore = (PKCS11SessionStore)loadStoreParameter;
                this.needToCloseSesionStore = false;
            }
            else
            {
                this.sessionStore = new PKCS11SessionStore();
                this.needToCloseSesionStore = true;
                this.sessionStore.open(this.provider, loadStoreParameter);
            }

        } catch (PKCS11Exception e) {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
     }

    /* (non-Javadoc)
     * @see java.security.KeyPairGeneratorSpi#generateKeyPair()
     */
    @Override
    public KeyPair generateKeyPair()
    {
        try
        {
            return this.generator.generateKeyPair(this.sessionStore.getSession());
            
        } catch (PKCS11Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
