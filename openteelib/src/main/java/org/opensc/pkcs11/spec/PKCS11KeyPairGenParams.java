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

import java.security.KeyStore.LoadStoreParameter;
import java.security.spec.DSAParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.spec.DHGenParameterSpec;

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
public interface PKCS11KeyPairGenParams extends PKCS11PrivateKeyGenParams, PKCS11PublicKeyGenParams
{
    /**
     * @param loadStoreParameter An optional {@link LoadStoreParameter} setting for
     *             opening a PKCS11 session in contexts, which require opening
     *             a new session is required.
     */
    void setLoadStoreParameter(LoadStoreParameter loadStoreParameter);

    /**
     * @return The optional LoadStoreParameter settings.
     */
    LoadStoreParameter getLoadStoreParameter();
}
