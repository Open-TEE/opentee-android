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

package org.opensc.pkcs11.wrap;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.opensc.pkcs11.spec.PKCS11PrivateKeyGenParams;
import org.opensc.pkcs11.spec.PKCS11PublicKeyGenParams;

/**
 * This is the base class for generating PKCS11 key pairs.
 * 
 * @author wglas
 */
public abstract class PKCS11KeyPairGenerator
{
    static protected final int N_STATIC_PRIVATE_ATTRIBUTES = 9;
    static protected final int N_STATIC_PUBLIC_ATTRIBUTES = 4;
    
    private int algorithm;
    protected PKCS11Attribute[] pubKeyAttributes;
    protected PKCS11Attribute[] privKeyAttributes;
    
    
    /**
     * @param session The PKCS11 session.
     * @param algorithm An CKA_* algorithm constant.
     */
    protected PKCS11KeyPairGenerator(int algorithm)
    {
        this.algorithm = algorithm;
    }

    protected void initStaticPrivateAttrs(PKCS11PrivateKeyGenParams params, int extraAttrs)
    {
        this.privKeyAttributes = new PKCS11Attribute[N_STATIC_PRIVATE_ATTRIBUTES+extraAttrs];
        
        this.privKeyAttributes[0] = new PKCS11Attribute(PKCS11Attribute.CKA_SIGN,
                                                        params.isSigning());
        this.privKeyAttributes[1] = new PKCS11Attribute(PKCS11Attribute.CKA_DECRYPT,
                                                        params.isDecrypt());
        this.privKeyAttributes[2] = new PKCS11Attribute(PKCS11Attribute.CKA_UNWRAP,
                                                        params.isUnwrap());
        this.privKeyAttributes[3] = new PKCS11Attribute(PKCS11Attribute.CKA_WRAP_WITH_TRUSTED,
                                                        params.isWrapWithTrusted());
        this.privKeyAttributes[4] = new PKCS11Attribute(PKCS11Attribute.CKA_SENSITIVE,
                                                        params.isSensitive());
        this.privKeyAttributes[5] = new PKCS11Attribute(PKCS11Attribute.CKA_EXTRACTABLE,
                                                        params.isExtractable());
        this.privKeyAttributes[6] = new PKCS11Attribute(PKCS11Attribute.CKA_ID,
                                                        params.getId());
        this.privKeyAttributes[7] = new PKCS11Attribute(PKCS11Attribute.CKA_PRIVATE,
                                                        true);
        this.privKeyAttributes[8] = new PKCS11Attribute(PKCS11Attribute.CKA_TOKEN,
                                                        true);
        // if you add more static atributes to the list above,
        // increase the constant N_STATIC_PRIVATE_ATTRIBUTES likewise.
    }
    
    protected void initStaticPublicAttrs(PKCS11PublicKeyGenParams params, int extraAttrs)
    {
        this.pubKeyAttributes = new PKCS11Attribute[N_STATIC_PUBLIC_ATTRIBUTES+extraAttrs];
        
        this.pubKeyAttributes[0] = new PKCS11Attribute(PKCS11Attribute.CKA_VERIFY,
                                                       params.isVerify());
        this.pubKeyAttributes[1] = new PKCS11Attribute(PKCS11Attribute.CKA_ENCRYPT,
                                                       params.isEncrypt());
        this.pubKeyAttributes[2] = new PKCS11Attribute(PKCS11Attribute.CKA_WRAP,
                                                       params.isWrap());
        this.pubKeyAttributes[3] = new PKCS11Attribute(PKCS11Attribute.CKA_TRUSTED,
                                                       params.isTrusted());
        // if you add more static atributes to the list above,
        // increase the constant N_STATIC_PUBLIC_ATTRIBUTES likewise.
    }
    
    /**
     * Build the private key for the given C handle returned by the native function.
     * 
     * @param session The session for which to create the private key.
     * @param handle The handel returned by the native funtion of the newly created private key.
     * @return The appropriate private key object for the chosen algorithm.
     * @throws PKCS11Exception Upon errors.
     */
    protected abstract PrivateKey makePrivateKey(PKCS11Session session, long handle) throws PKCS11Exception;
    
    /**
     * Build the public key for the given C handle returned by the native function.
     * 
     * @param session The session for which to create the private key.
     * @param handle The handel returned by the native funtion of the newly created public key.
     * @return The appropriate private key object for the chosen algorithm.
     * @throws PKCS11Exception Upon errors.
     */
    protected abstract PublicKey makePublicKey(PKCS11Session session, long handle) throws PKCS11Exception;
     
    
    private native long[] generateKeyPairNative(long pvh, long shandle, long hsession,
                                                int algo,
                                                PKCS11Attribute[] pubAttrs,
                                                PKCS11Attribute[] privAttrs) throws PKCS11Exception;
  
    /**
     * Generate a key pair using the supplied algorithm parameters.
     * 
     * @param session The session, which is used to generate the key pair.
     *                This session must be opened in read/write mode.
     * 
     * @return The KeyPair containing the newly created keys.
     * @throws PKCS11Exception upon error of the underlying native functions.
     */
    public KeyPair generateKeyPair(PKCS11Session session) throws PKCS11Exception
    {
        long keyHandlePair[] =
            this.generateKeyPairNative(session.getPvh(), session.getSlotHandle(),
                                       session.getHandle(),this.algorithm,
                                       this.pubKeyAttributes, this.privKeyAttributes);
        
        return new KeyPair(makePublicKey(session,keyHandlePair[0]),
                           makePrivateKey(session,keyHandlePair[1]));
    }
    
}
