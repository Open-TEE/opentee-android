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

import java.security.spec.DSAParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.spec.DHGenParameterSpec;

import org.opensc.pkcs11.spi.PKCS11KeyPairGeneratorSpi;

/**
 * This interface is implemented by all subclasses of
 * {@link RSAKeyGenParameterSpec}, {@link DSAParameterSpec} or
 * {@link DHGenParameterSpec} which are used to initialize a key
 * pair generator.
 * 
 * @see PKCS11KeyPairGeneratorSpi
 * 
 * @author wglas
 */
public interface PKCS11PrivateKeyGenParams
{
    /**
     * The standard id for the authentication key as specified by PKCS#15.
     */
    static public byte[] ID45 = new byte[] { 0x45 };
    
    /**
     * The standard id for the non-repudiation key as specified by PKCS#15.
     */
    static public byte[] ID46 = new byte[] { 0x46 };
    
    /**
     * Default value: {@link PKCS11PrivateKeyGenParams#ID45}.
     * 
     * @return The ID of the generated private key on the token.
     * 
     * @see PKCS11PrivateKeyGenParams#ID45
     * @see PKCS11PrivateKeyGenParams#ID46
     */
    byte[] getId();
    
    /**
     * @param id Set the ID of the generated private key on the token.
     */
    void setId(byte[] id);
    
    /**
     * Get the sensitive flag.
     * Default value: <code>true</code> 
     * 
     * @return Whether this private key is sensitive.
     */
    boolean isSensitive();
    
    /**
     * @param sensitive Set whether this private key is sensitive.
     */
    void setSensitive(boolean sensitive);
    
    /**
     * Get the extraction flag.
     * Default value: <code>false</code> 
     * 
     * @return Whether this private key is extractable.
     */
    boolean isExtractable();
    
    /**
     * @param extractable Set whether this private key is extractable.
     */
    void setExtractable(boolean extractable);
    
    /**
     * Get the wrap with trusted flag.
     * Default value: <code>false</code> 
     * 
     * @return Whether this private key may only be wrapped by trusted public keys.
     * 
     * @see PKCS11PublicKeyGenParams#isTrusted()
     */
    boolean isWrapWithTrusted();
    
    /**
     * @param extractable Set whether this private key may only be wrapped by trusted public keys.
     */
    void setWrapWithTrusted(boolean wrapWithTrusted);
    
    /**
     * Get the decryption flag.
     * Default value: <code>false</code> 
     *
     * @return Whether the generated public key my be used for decryption.
     */
    public boolean isDecrypt();
    
    /**
     * @param decrypt Set whether the generated public key my be used for decryption.
     */
    public void setDecrypt(boolean decrypt);
    
    /**
     * Get the signing flag.
     * Default value: <code>true</code> 
     *
     * @return Whether the generated public key my be used for signing.
     */
    public boolean isSigning();
    
    /**
     * @param signing Set whether the generated public key my be used for signing.
     */
    public void setSigning(boolean signing);
    
    /**
     * Get the unwrap flag.
     * Default value: <code>true</code> 
     *
     * @return Whether the generated public key may be used to unwrap keys.
     */
    public boolean isUnwrap();
    
    /**
     * @param unwrap Set whether the generated public key may be used to unwrap keys.
     */
    public void setUnwrap(boolean unwrap);
}
