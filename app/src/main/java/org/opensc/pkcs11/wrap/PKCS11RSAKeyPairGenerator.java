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

import java.security.PrivateKey;
import java.security.PublicKey;

import org.opensc.pkcs11.spec.PKCS11RSAKeyPairGenParameterSpec;

/**
 * @author wglas
 *
 */
public class PKCS11RSAKeyPairGenerator extends PKCS11KeyPairGenerator
{
    static private final int N_EXTRA_PRIVATE_ATTRIBUTES = 0;
    static private final int N_EXTRA_PUBLIC_ATTRIBUTES = 2;
    
    private PKCS11RSAKeyPairGenParameterSpec params;
    
    /**
     * Create a new PKCS11 key pair generator.
     * 
     * @param session The underlying PKCS11 session.
     * @param params The parameters for this initialization.
     */
    public PKCS11RSAKeyPairGenerator(PKCS11RSAKeyPairGenParameterSpec params)
    {
        super(PKCS11Mechanism.CKM_RSA_PKCS_KEY_PAIR_GEN);
        
        super.initStaticPublicAttrs(params, N_EXTRA_PUBLIC_ATTRIBUTES);
        super.pubKeyAttributes[N_STATIC_PUBLIC_ATTRIBUTES+0] =
            new PKCS11Attribute(PKCS11Attribute.CKA_MODULUS_BITS,
                                params.getKeysize());
        super.pubKeyAttributes[N_STATIC_PUBLIC_ATTRIBUTES+1] =
            new PKCS11Attribute(PKCS11Attribute.CKA_PUBLIC_EXPONENT,
                                params.getPublicExponent().toByteArray());

        super.initStaticPrivateAttrs(params, N_EXTRA_PRIVATE_ATTRIBUTES);
        
        this.params = params;
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11KeyPairGenerator#makePrivateKey(long)
     */
    @Override
    protected PrivateKey makePrivateKey(PKCS11Session session,
                                        long handle) throws PKCS11Exception
    {
        if (this.params.isExtractable() && ! this.params.isSensitive())
            return new PKCS11RSAPrivateKey(session,handle);
        else
            return new PKCS11NeRSAPrivateKey(session,handle);
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs11.wrap.PKCS11KeyPairGenerator#makePublicKey(long)
     */
    @Override
    protected PublicKey makePublicKey(PKCS11Session session,
                                      long handle) throws PKCS11Exception
    {
        return new PKCS11RSAPublicKey(session,handle);
    }

}
