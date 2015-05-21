/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jan 25, 2007
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

package org.opensc.pkcs11.spec;

import org.opensc.pkcs11.spi.PKCS11KeyPairGeneratorSpi;

import java.math.BigInteger;
import java.security.KeyStore.LoadStoreParameter;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * This extension of {@link RSAKeyGenParameterSpec} is used to initialize the
 * PKCS key pair generator.
 * 
 * @see PKCS11KeyPairGeneratorSpi
 *
 * @author wglas
 */
public class PKCS11RSAKeyPairGenParameterSpec extends RSAKeyGenParameterSpec implements
                                                                            PKCS11KeyPairGenParams
{
    private byte[] id;
    private boolean signing;
    private boolean verify;
    private boolean decrypt;
    private boolean encrypt;
    private boolean wrap;
    private boolean unwrap;
    private boolean trusted;
    private boolean wrapWithTrusted;
    private boolean sensitive;
    private boolean extractable;
    private LoadStoreParameter loadStoreParameter;
    
    /**
     * Generate a PKCS11DSAKeyPairGenParameterSpec instance using the given DSA
     * parameters.
     * 
     * @param keysize The size of the public RSA modulus in bits.
     * @param publicExponent The RSA public exponent.
     * 
     * @see RSAKeyGenParameterSpec#RSAKeyGenParameterSpec(int, BigInteger)
     */
    public PKCS11RSAKeyPairGenParameterSpec(int keysize, BigInteger publicExponent)
    {
        super(keysize, publicExponent);
        this.signing = true;
        this.verify = true;
        this.encrypt = false;
        this.decrypt = false;
        this.unwrap = true;
        this.wrap = true;
        this.trusted = true;
        this.wrapWithTrusted = true;
        this.extractable = false;
        this.sensitive = true;
        this.id = PKCS11PrivateKeyGenParams.ID45;
        this.loadStoreParameter = null;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PrivateKeyGenAttributes#isDecrypt()
     */
    public boolean isDecrypt()
    {
        return this.decrypt;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PrivateKeyGenAttributes#isSigning()
     */
    public boolean isSigning()
    {
        return this.signing;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PrivateKeyGenAttributes#setDecrypt(boolean)
     */
    public void setDecrypt(boolean decrypt)
    {
        this.decrypt = decrypt;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PrivateKeyGenAttributes#setSigning(boolean)
     */
    public void setSigning(boolean signing)
    {
        this.signing = signing;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PublicKeyGenAttributes#isEncrypt()
     */
    public boolean isEncrypt()
    {
        return this.encrypt;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PublicKeyGenAttributes#isVerify()
     */
    public boolean isVerify()
    {
        return this.verify;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PublicKeyGenAttributes#setEncrypt(boolean)
     */
    public void setEncrypt(boolean encrypt)
    {
        this.encrypt = encrypt;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11PublicKeyGenAttributes#setVerify(boolean)
     */
    public void setVerify(boolean verify)
    {
        this.verify = verify;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#isUnwrap()
     */
    public boolean isUnwrap()
    {
        return this.unwrap;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#setUnwrap(boolean)
     */
    public void setUnwrap(boolean unwrap)
    {
        this.unwrap = unwrap;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PublicKeyGenParams#isWrap()
     */
    public boolean isWrap()
    {
        return this.wrap;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PublicKeyGenParams#setWrap(boolean)
     */
    public void setWrap(boolean wrap)
    {
        this.wrap = wrap;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#isExtractable()
     */
    public boolean isExtractable()
    {
        return this.extractable;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#setExtractable(boolean)
     */
    public void setExtractable(boolean extractable)
    {
        this.extractable = extractable;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#getId()
     */
    public byte[] getId()
    {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#setId(byte[])
     */
    public void setId(byte[] id)
    {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#isSensitive()
     */
    public boolean isSensitive()
    {
        return this.sensitive;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#setSensitive(boolean)
     */
    public void setSensitive(boolean sensitive)
    {
        this.sensitive = sensitive;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PublicKeyGenParams#isTrusted()
     */
    public boolean isTrusted()
    {
        return this.trusted;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PublicKeyGenParams#setTrusted(boolean)
     */
    public void setTrusted(boolean trusted)
    {
        this.trusted = trusted;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#isWrapWithTrusted()
     */
    public boolean isWrapWithTrusted()
    {
        return this.wrapWithTrusted;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams#setWrapWithTrusted(boolean)
     */
    public void setWrapWithTrusted(boolean wrapWithTrusted)
    {
        this.wrapWithTrusted = wrapWithTrusted;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11KeyPairGenParams#getLoadStoreParameter()
     */
    public LoadStoreParameter getLoadStoreParameter()
    {
        return this.loadStoreParameter;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.spec.PKCS11KeyPairGenParams#setLoadStoreParameter(java.security.KeyStore.LoadStoreParameter)
     */
    public void setLoadStoreParameter(LoadStoreParameter loadStoreParameter)
    {
        this.loadStoreParameter = loadStoreParameter;
    }

}
