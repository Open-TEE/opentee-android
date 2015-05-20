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

package org.opensc.pkcs11.wrap;

import org.opensc.util.PKCS11Id;

/**
 * A PKCS11 attribute, which may be passed from JAVA to the native interface.
 * 
 * @author wglas
 */
public class PKCS11Attribute
{
    /*
     * PKCS11 Attribute constants used for attribute fetching imported from pkcs11t.h
     */
    public static final int CKA_CLASS              = 0x00000000;
    public static final int CKA_TOKEN              = 0x00000001;
    public static final int CKA_PRIVATE            = 0x00000002;
    public static final int CKA_LABEL              = 0x00000003;
    public static final int CKA_APPLICATION        = 0x00000010;
    public static final int CKA_VALUE              = 0x00000011;
    
    /* CKA_OBJECT_ID is new for v2.10 */
    public static final int CKA_OBJECT_ID          = 0x00000012;
    
    public static final int CKA_CERTIFICATE_TYPE   = 0x00000080;
    public static final int CKA_ISSUER             = 0x00000081;
    public static final int CKA_SERIAL_NUMBER      = 0x00000082;
    
    /* CKA_AC_ISSUER, CKA_OWNER, and CKA_ATTR_TYPES are new 
     * for v2.10 */
    public static final int CKA_AC_ISSUER          = 0x00000083;
    public static final int CKA_OWNER              = 0x00000084;
    public static final int CKA_ATTR_TYPES         = 0x00000085;
    
    /* CKA_TRUSTED is new for v2.11 */
    public static final int CKA_TRUSTED            = 0x00000086;
    
    public static final int CKA_CERTIFICATE_CATEGORY       = 0x00000087;
    public static final int CKA_JAVA_MIDP_SECURITY_DOMAIN  = 0x00000088;
    public static final int CKA_URL                        = 0x00000089;
    public static final int CKA_HASH_OF_SUBJECT_PUBLIC_KEY = 0x0000008a;
    public static final int CKA_HASH_OF_ISSUER_PUBLIC_KEY  = 0x0000008b;
    public static final int CKA_CHECK_VALUE                = 0x00000090;
    public static final int CKA_KEY_TYPE           = 0x00000100;
    public static final int CKA_SUBJECT            = 0x00000101;
    public static final int CKA_ID                 = 0x00000102;
    public static final int CKA_SENSITIVE          = 0x00000103;
    public static final int CKA_ENCRYPT            = 0x00000104;
    public static final int CKA_DECRYPT            = 0x00000105;
    public static final int CKA_WRAP               = 0x00000106;
    public static final int CKA_UNWRAP             = 0x00000107;
    public static final int CKA_SIGN               = 0x00000108;
    public static final int CKA_SIGN_RECOVER       = 0x00000109;
    public static final int CKA_VERIFY             = 0x0000010A;
    public static final int CKA_VERIFY_RECOVER     = 0x0000010B;
    public static final int CKA_DERIVE             = 0x0000010C;
    public static final int CKA_START_DATE         = 0x00000110;
    public static final int CKA_END_DATE           = 0x00000111;
    public static final int CKA_MODULUS            = 0x00000120;
    public static final int CKA_MODULUS_BITS       = 0x00000121;
    public static final int CKA_PUBLIC_EXPONENT    = 0x00000122;
    public static final int CKA_PRIVATE_EXPONENT   = 0x00000123;
    public static final int CKA_PRIME_1            = 0x00000124;
    public static final int CKA_PRIME_2            = 0x00000125;
    public static final int CKA_EXPONENT_1         = 0x00000126;
    public static final int CKA_EXPONENT_2         = 0x00000127;
    public static final int CKA_COEFFICIENT        = 0x00000128;
    public static final int CKA_PRIME              = 0x00000130;
    public static final int CKA_SUBPRIME           = 0x00000131;
    public static final int CKA_BASE               = 0x00000132;
    
    /* CKA_PRIME_BITS and CKA_SUB_PRIME_BITS are new for v2.11 */
    public static final int CKA_PRIME_BITS         = 0x00000133;
    public static final int CKA_SUB_PRIME_BITS     = 0x00000134;
    
    public static final int CKA_VALUE_BITS         = 0x00000160;
    public static final int CKA_VALUE_LEN          = 0x00000161;

    public static final int CKA_EXTRACTABLE        = 0x00000162;
    public static final int CKA_LOCAL              = 0x00000163;
    public static final int CKA_NEVER_EXTRACTABLE  = 0x00000164;
    public static final int CKA_ALWAYS_SENSITIVE   = 0x00000165;

    public static final int CKA_KEY_GEN_MECHANISM  = 0x00000166;

    public static final int CKA_MODIFIABLE         = 0x00000170;
    public static final int CKA_ECDSA_PARAMS       = 0x00000180;
    public static final int CKA_EC_PARAMS          = 0x00000180;
    public static final int CKA_EC_POINT           = 0x00000181;

    public static final int CKA_SECONDARY_AUTH     = 0x00000200;
    public static final int CKA_AUTH_PIN_FLAGS     = 0x00000201;
    public static final int CKA_ALWAYS_AUTHENTICATE= 0x00000202;
    public static final int CKA_WRAP_WITH_TRUSTED  = 0x00000210;

    private static final int LITTLE_ENDIAN = 0;
    private static final int BIG_ENDIAN    = 1;
    
    private static int endianness;
    
    static {
        endianness =
            "little".equalsIgnoreCase(System.getProperty("sun.cpu.endian")) ?
            LITTLE_ENDIAN : BIG_ENDIAN;
    }
 
    private int kind;
    private byte[] data;
    
    /**
     * Contruct a byte array attribute.
     * 
     * @param kind The kind of the attribute, which is one oof the CKA_* contants.
     * @param data The byte array data of the attrribute.
     */
    public PKCS11Attribute(int kind, byte[] data)
    {
        this.kind = kind;
        this.data = data;
    }

    /**
     * Contruct an Id attribute.
     * 
     * @param kind The kind of the attribute, which is one oof the CKA_* contants.
     * @param data The PKCS11Id data of the attrribute.
     */
    public PKCS11Attribute(int kind, PKCS11Id id)
    {
        this.kind = kind;
        this.data = id.getData();
    }

    /**
     * Contruct a DWORD attribute.
     * 
     * @param kind The kind of the attribute, which is one oof the CKA_* contants.
     * @param dword The 32 bit unsigned value stored as a JAVA int.
     */
    public PKCS11Attribute(int kind, int dword)
    {
        this.kind = kind;
        this.data = new byte[4];
        
        if (endianness == LITTLE_ENDIAN)
        {
            this.data[0] = (byte)(dword);
            this.data[1] = (byte)(dword >> 8);
            this.data[2] = (byte)(dword >> 16);
            this.data[3] = (byte)(dword >> 24);
        }
        else
        {
            this.data[0] = (byte)(dword >> 24);
            this.data[1] = (byte)(dword >> 16);
            this.data[2] = (byte)(dword >> 8);
            this.data[3] = (byte)(dword);
        }
    }

    /**
     * Contruct a boolean attribute.
     * 
     * @param kind The kind of the attribute, which is one oof the CKA_* contants.
     * @param v The boolean value.
     */
    public PKCS11Attribute(int kind, boolean v)
    {
        this.kind = kind;
        this.data = new byte[1];
        this.data[0] = v ? (byte)1 : (byte)0;
    }

    /**
     * @return Returns the data of the atribute.
     */
    public byte[] getData()
    {
        return this.data;
    }

    /**
     * @return Returns the kind of the atribute.
     */
    public int getKind()
    {
        return this.kind;
    }
    
}
