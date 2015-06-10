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
import java.security.interfaces.RSAPublicKey;

/**
 * @author wglas
 *
 * A RSA public key stored on the token.
 */
public class PKCS11RSAPublicKey extends PKCS11PublicKey implements RSAPublicKey
{
	/**
	 * To be changed upon class layout change.
	 */
	private static final long serialVersionUID = -3378514468395316553L;
	
	private BigInteger publicExponent;
	private BigInteger modulus;
	
	/**
	 * @param session
	 * @param type
	 * @param handle
	 * @throws PKCS11Exception
	 */
	protected PKCS11RSAPublicKey(PKCS11Session session, long handle)
			throws PKCS11Exception
	{
		super(session, CKK_RSA, handle);
		
		byte [] raw_modulus = getRawAttribute(PKCS11Attribute.CKA_MODULUS);
		this.modulus = new BigInteger(raw_modulus);
		
		byte [] raw_exp = getRawAttribute(PKCS11Attribute.CKA_PUBLIC_EXPONENT);
		this.publicExponent = new BigInteger(raw_exp);
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.RSAPublicKey#getPublicExponent()
	 */
	public BigInteger getPublicExponent()
	{
		return this.publicExponent;
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.RSAKey#getModulus()
	 */
	public BigInteger getModulus()
	{
		return this.modulus;
	}

}
