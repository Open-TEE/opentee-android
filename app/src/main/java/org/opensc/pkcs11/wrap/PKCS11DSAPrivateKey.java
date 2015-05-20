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
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAParameterSpec;

/**
 * @author wglas
 *
 * A DSA private key stored on the token.
 */
public class PKCS11DSAPrivateKey extends PKCS11PrivateKey implements
		DSAPrivateKey
{
	/**
	 * To be changed upon class layout change.
	 */
	private static final long serialVersionUID = 7732884476852711287L;

	BigInteger x;
	DSAParams params;
	
	/**
	 * @param session The PKCS#11 session to which we belong.
	 * @param handle The object handle for this key.
	 * @throws PKCS11Exception Upon errors when retrieving the data from the token.
	 */
	protected PKCS11DSAPrivateKey(PKCS11Session session, long handle)
			throws PKCS11Exception
	{
		super(session, CKK_DSA, true, handle);
		
		byte [] raw_x = getRawAttribute(PKCS11Attribute.CKA_VALUE);
		this.x = new BigInteger(raw_x);
		
		raw_x = getRawAttribute(PKCS11Attribute.CKA_PRIME);
		BigInteger p = new BigInteger(raw_x);
			
		raw_x = getRawAttribute(PKCS11Attribute.CKA_SUBPRIME);
		BigInteger q = new BigInteger(raw_x);
			
		raw_x = getRawAttribute(PKCS11Attribute.CKA_BASE);
		BigInteger g = new BigInteger(raw_x);
			
		this.params = new DSAParameterSpec(p,q,g);
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.DSAPrivateKey#getX()
	 */
	public BigInteger getX()
	{
		return this.x;
	}

	/* (non-Javadoc)
	 * @see java.security.interfaces.DSAKey#getParams()
	 */
	public DSAParams getParams()
	{
		return this.params;
	}

}
