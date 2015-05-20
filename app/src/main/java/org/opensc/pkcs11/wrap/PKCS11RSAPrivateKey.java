/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 29, 2006
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

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

/**
 * @author wglas
 *
 * A RSA private key stored on the token.
 */
public class PKCS11RSAPrivateKey extends PKCS11PrivateKey implements RSAPrivateKey
{
	/**
	 * To be changed upon class layout change.
	 */
	private static final long serialVersionUID = 7992312719342457459L;
	
	BigInteger privateExponent;
	BigInteger modulus;
	
	/**
	 * @param session The PKCS#11 session to which we belong.
	 * @param handle The object handle for this key.
	 * @throws PKCS11Exception Upon errors when retrieving the data from the token.
	 */
	protected PKCS11RSAPrivateKey(PKCS11Session session, long handle)
			throws PKCS11Exception
	{
		super(session, CKK_RSA, true, handle);
		
		byte [] raw_modulus = getRawAttribute(PKCS11Attribute.CKA_MODULUS);
		this.modulus = new BigInteger(raw_modulus);
		
		byte [] raw_exp = getRawAttribute(PKCS11Attribute.CKA_PRIVATE_EXPONENT);
		this.privateExponent = new BigInteger(raw_exp);
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.RSAPrivateKey#getPrivateExponent()
	 */
	public BigInteger getPrivateExponent()
	{
		return this.privateExponent;
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.RSAKey#getModulus()
	 */
	public BigInteger getModulus()
	{
		return this.modulus;
	}

}
