/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 21, 2006
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

import java.security.Key;

/**
 * @author wglas
 *
 * The base class for all PKCS11 key types.
 */
public class PKCS11Key extends PKCS11Object implements Key
{
	/**
	 * To be changed upon class layout change.
	 */
	private static final long serialVersionUID = -2438490448592590194L;

	private int keyType;

	private int keyBits;
	
	/* key types as defined by pkcs11t.h */
	public static final int CKK_RSA            = 0x00000000;
    public static final int CKK_DSA            = 0x00000001;
    public static final int CKK_DH             = 0x00000002;

	/* CKK_ECDSA and CKK_KEA are new for v2.0 */
	/* CKK_ECDSA is deprecated in v2.11, CKK_EC is preferred. */
    public static final int CKK_ECDSA          = 0x00000003;
    public static final int CKK_EC             = 0x00000003;
    public static final int CKK_X9_42_DH       = 0x00000004;
    public static final int CKK_KEA            = 0x00000005;

    public static final int CKK_GENERIC_SECRET = 0x00000010;
    public static final int CKK_RC2            = 0x00000011;
    public static final int CKK_RC4            = 0x00000012;
    public static final int CKK_DES            = 0x00000013;
    public static final int CKK_DES2           = 0x00000014;
    public static final int CKK_DES3           = 0x00000015;

	/* all these key types are new for v2.0 */
    public static final int CKK_CAST           = 0x00000016;
    public static final int CKK_CAST3          = 0x00000017;
	/* CKK_CAST5 is deprecated in v2.11, CKK_CAST128 is preferred. */
    public static final int CKK_CAST5          = 0x00000018;
    public static final int CKK_CAST128        = 0x00000018;
    public static final int CKK_RC5            = 0x00000019;
    public static final int CKK_IDEA           = 0x0000001A;
    public static final int CKK_SKIPJACK       = 0x0000001B;
    public static final int CKK_BATON          = 0x0000001C;
    public static final int CKK_JUNIPER        = 0x0000001D;
    public static final int CKK_CDMF           = 0x0000001E;
    public static final int CKK_AES            = 0x0000001F;

	/**
	 * @param session The session to which this key belongs.
	 * @param type The key type as returned by the CKR_KEY_TYPE attribute.
	 * @param handle The key handle as returned be the specific static
	 *               enumeration function of the subclass.
	 * @throws PKCS11Exception
	 */
	protected PKCS11Key(PKCS11Session session, int type, long handle) throws PKCS11Exception
	{
		super(session, handle);
		this.keyType = super.getULongAttribute(PKCS11Attribute.CKA_KEY_TYPE);
        // TODO move keyBits to corresponding RSA public key class
        this.keyBits = 0;
        try {
            this.keyBits = super.getULongAttribute(PKCS11Attribute.CKA_MODULUS_BITS);
        }
        catch(PKCS11Exception e) {
        }
	}
	
	/**
	 * @return Returns the keyType as defined by the CKK_* contants.
	 */
	public int getKeyType()
	{
		return this.keyType;
	}

	/**
	 * @return Returns the number of bits of the key.
	 */
	public int getKeyBits()
	{
		return this.keyBits;
	}

	/* (non-Javadoc)
	 * @see java.security.Key#getAlgorithm()
	 */
	public String getAlgorithm()
	{
		switch (this.keyType)
		{
		case CKK_RSA: return "RSA";
		case CKK_DSA: return "DSA";
		case CKK_DH: return "DiffieHellman";
		case CKK_DES: return "DES";
		case CKK_DES2: return "DESede";
		case CKK_DES3: return "DESede";
		case CKK_AES: return "AES";
		case CKK_RC2: return "RC2";
		case CKK_RC4: return "RC4";
		case CKK_RC5: return "RC5";
		case CKK_ECDSA: return "ECDSA";
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see java.security.Key#getFormat()
	 */
	public String getFormat()
	{
		return "X.509";
	}

	/* (non-Javadoc)
	 * @see java.security.Key#getEncoded()
	 */
	public byte[] getEncoded()
	{
		throw new SecurityException("Cannot get encoded version of a cryptographic key resident on a hardware token.");
	}

}
