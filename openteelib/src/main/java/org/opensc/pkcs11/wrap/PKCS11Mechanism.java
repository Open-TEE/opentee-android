/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 18, 2006
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

/**
 * @author wglas
 *
 * This class wrap a PKCS11 mechanism info, which is retrieved for
 * individual objects.
 */
public class PKCS11Mechanism
{
	private int type;
	private int minKeySize;
	private int maxKeySize;
	private int flags;
	
	/* The flags are defined as follows:
	 *      Bit Flag               Mask        Meaning */
	public static final int CKF_HW                 = 0x00000001;  /* performed by HW */
	
	/* The flags CKF_ENCRYPT, CKF_DECRYPT, CKF_DIGEST, CKF_SIGN,
	 * CKG_SIGN_RECOVER, CKF_VERIFY, CKF_VERIFY_RECOVER,
	 * CKF_GENERATE, CKF_GENERATE_KEY_PAIR, CKF_WRAP, CKF_UNWRAP,
	 * and CKF_DERIVE are new for v2.0.  They specify whether or not
	 * a mechanism can be used for a particular task */
	public static final int CKF_ENCRYPT            = 0x00000100;
	public static final int CKF_DECRYPT            = 0x00000200;
	public static final int CKF_DIGEST             = 0x00000400;
	public static final int CKF_SIGN               = 0x00000800;
	public static final int CKF_SIGN_RECOVER       = 0x00001000;
	public static final int CKF_VERIFY             = 0x00002000;
	public static final int CKF_VERIFY_RECOVER     = 0x00004000;
	public static final int CKF_GENERATE           = 0x00008000;
	public static final int CKF_GENERATE_KEY_PAIR  = 0x00010000;
	public static final int CKF_WRAP               = 0x00020000;
	public static final int CKF_UNWRAP             = 0x00040000;
	public static final int CKF_DERIVE             = 0x00080000;
	
	/* CKF_EC_F_P, CKF_EC_F_2M, CKF_EC_ECPARAMETERS, CKF_EC_NAMEDCURVE,
	 * CKF_EC_UNCOMPRESS, and CKF_EC_COMPRESS are new for v2.11. They
	 * describe a token's EC capabilities not available in mechanism
	 * information. */
	public static final int CKF_EC_F_P	           = 0x00100000;
	public static final int CKF_EC_F_2M	           = 0x00200000;
	public static final int CKF_EC_ECPARAMETERS	   = 0x00400000;
	public static final int CKF_EC_NAMEDCURVE	   = 0x00800000;
	public static final int CKF_EC_UNCOMPRESS	   = 0x01000000;
	public static final int CKF_EC_COMPRESS	       = 0x02000000;
	
	
	/* the following mechanism types are defined: */
	public static final int CKM_RSA_PKCS_KEY_PAIR_GEN      = 0x00000000;
	public static final int CKM_RSA_PKCS                   = 0x00000001;
	public static final int CKM_RSA_9796                   = 0x00000002;
	public static final int CKM_RSA_X_509                  = 0x00000003;
	
	/* CKM_MD2_RSA_PKCS, CKM_MD5_RSA_PKCS, and CKM_SHA1_RSA_PKCS
	 * are new for v2.0.  They are mechanisms which hash and sign */
	public static final int CKM_MD2_RSA_PKCS               = 0x00000004;
	public static final int CKM_MD5_RSA_PKCS               = 0x00000005;
	public static final int CKM_SHA1_RSA_PKCS              = 0x00000006;
	
	/* CKM_RIPEMD128_RSA_PKCS, CKM_RIPEMD160_RSA_PKCS, and
	 * CKM_RSA_PKCS_OAEP are new for v2.10 */
	public static final int CKM_RIPEMD128_RSA_PKCS         = 0x00000007;
	public static final int CKM_RIPEMD160_RSA_PKCS         = 0x00000008;
	public static final int CKM_RSA_PKCS_OAEP              = 0x00000009;
	
	/* CKM_RSA_X9_31_KEY_PAIR_GEN, CKM_RSA_X9_31, CKM_SHA1_RSA_X9_31,
	 * CKM_RSA_PKCS_PSS, and CKM_SHA1_RSA_PKCS_PSS are new for v2.11 */
	public static final int CKM_RSA_X9_31_KEY_PAIR_GEN     = 0x0000000A;
	public static final int CKM_RSA_X9_31                  = 0x0000000B;
	public static final int CKM_SHA1_RSA_X9_31             = 0x0000000C;
	public static final int CKM_RSA_PKCS_PSS               = 0x0000000D;
	public static final int CKM_SHA1_RSA_PKCS_PSS          = 0x0000000E;
	
	public static final int CKM_DSA_KEY_PAIR_GEN           = 0x00000010;
	public static final int CKM_DSA                        = 0x00000011;
	public static final int CKM_DSA_SHA1                   = 0x00000012;
	public static final int CKM_DH_PKCS_KEY_PAIR_GEN       = 0x00000020;
	public static final int CKM_DH_PKCS_DERIVE             = 0x00000021;
	
	/* CKM_X9_42_DH_KEY_PAIR_GEN, CKM_X9_42_DH_DERIVE,
	 * CKM_X9_42_DH_HYBRID_DERIVE, and CKM_X9_42_MQV_DERIVE are new for
	 * v2.11 */
	public static final int CKM_X9_42_DH_KEY_PAIR_GEN      = 0x00000030;
	public static final int CKM_X9_42_DH_DERIVE            = 0x00000031;
	public static final int CKM_X9_42_DH_HYBRID_DERIVE     = 0x00000032;
	public static final int CKM_X9_42_MQV_DERIVE           = 0x00000033;
	
	public static final int CKM_SHA256_RSA_PKCS            = 0x00000040;
	public static final int CKM_SHA384_RSA_PKCS            = 0x00000041;
	public static final int CKM_SHA512_RSA_PKCS            = 0x00000042;
	public static final int CKM_SHA256_RSA_PKCS_PSS        = 0x00000043;
	public static final int CKM_SHA384_RSA_PKCS_PSS        = 0x00000044;
	public static final int CKM_SHA512_RSA_PKCS_PSS        = 0x00000045;

	public static final int CKM_RC2_KEY_GEN                = 0x00000100;
	public static final int CKM_RC2_ECB                    = 0x00000101;
	public static final int CKM_RC2_CBC                    = 0x00000102;
	public static final int CKM_RC2_MAC                    = 0x00000103;
	
	/* CKM_RC2_MAC_GENERAL and CKM_RC2_CBC_PAD are new for v2.0 */
	public static final int CKM_RC2_MAC_GENERAL            = 0x00000104;
	public static final int CKM_RC2_CBC_PAD                = 0x00000105;
	
	public static final int CKM_RC4_KEY_GEN                = 0x00000110;
	public static final int CKM_RC4                        = 0x00000111;
	public static final int CKM_DES_KEY_GEN                = 0x00000120;
	public static final int CKM_DES_ECB                    = 0x00000121;
	public static final int CKM_DES_CBC                    = 0x00000122;
	public static final int CKM_DES_MAC                    = 0x00000123;
	
	/* CKM_DES_MAC_GENERAL and CKM_DES_CBC_PAD are new for v2.0 */
	public static final int CKM_DES_MAC_GENERAL            = 0x00000124;
	public static final int CKM_DES_CBC_PAD                = 0x00000125;
	
	public static final int CKM_DES2_KEY_GEN               = 0x00000130;
	public static final int CKM_DES3_KEY_GEN               = 0x00000131;
	public static final int CKM_DES3_ECB                   = 0x00000132;
	public static final int CKM_DES3_CBC                   = 0x00000133;
	public static final int CKM_DES3_MAC                   = 0x00000134;
	
	/* CKM_DES3_MAC_GENERAL, CKM_DES3_CBC_PAD, CKM_CDMF_KEY_GEN,
	 * CKM_CDMF_ECB, CKM_CDMF_CBC, CKM_CDMF_MAC,
	 * CKM_CDMF_MAC_GENERAL, and CKM_CDMF_CBC_PAD are new for v2.0 */
	public static final int CKM_DES3_MAC_GENERAL           = 0x00000135;
	public static final int CKM_DES3_CBC_PAD               = 0x00000136;
	public static final int CKM_CDMF_KEY_GEN               = 0x00000140;
	public static final int CKM_CDMF_ECB                   = 0x00000141;
	public static final int CKM_CDMF_CBC                   = 0x00000142;
	public static final int CKM_CDMF_MAC                   = 0x00000143;
	public static final int CKM_CDMF_MAC_GENERAL           = 0x00000144;
	public static final int CKM_CDMF_CBC_PAD               = 0x00000145;
	
	public static final int CKM_MD2                        = 0x00000200;
	
	/* CKM_MD2_HMAC and CKM_MD2_HMAC_GENERAL are new for v2.0 */
	public static final int CKM_MD2_HMAC                   = 0x00000201;
	public static final int CKM_MD2_HMAC_GENERAL           = 0x00000202;
	
	public static final int CKM_MD5                        = 0x00000210;
	
	/* CKM_MD5_HMAC and CKM_MD5_HMAC_GENERAL are new for v2.0 */
	public static final int CKM_MD5_HMAC                   = 0x00000211;
	public static final int CKM_MD5_HMAC_GENERAL           = 0x00000212;
	
	public static final int CKM_SHA_1                      = 0x00000220;
	
	/* CKM_SHA_1_HMAC and CKM_SHA_1_HMAC_GENERAL are new for v2.0 */
	public static final int CKM_SHA_1_HMAC                 = 0x00000221;
	public static final int CKM_SHA_1_HMAC_GENERAL         = 0x00000222;
	
	/* CKM_RIPEMD128, CKM_RIPEMD128_HMAC, 
	 * CKM_RIPEMD128_HMAC_GENERAL, CKM_RIPEMD160, CKM_RIPEMD160_HMAC,
	 * and CKM_RIPEMD160_HMAC_GENERAL are new for v2.10 */
	public static final int CKM_RIPEMD128                  = 0x00000230;
	public static final int CKM_RIPEMD128_HMAC             = 0x00000231;
	public static final int CKM_RIPEMD128_HMAC_GENERAL     = 0x00000232;
	public static final int CKM_RIPEMD160                  = 0x00000240;
	public static final int CKM_RIPEMD160_HMAC             = 0x00000241;
	public static final int CKM_RIPEMD160_HMAC_GENERAL     = 0x00000242;
	
	/* All of the following mechanisms are new for v2.0 */
	/* Note that CAST128 and CAST5 are the same algorithm */
	public static final int CKM_CAST_KEY_GEN               = 0x00000300;
	public static final int CKM_CAST_ECB                   = 0x00000301;
	public static final int CKM_CAST_CBC                   = 0x00000302;
	public static final int CKM_CAST_MAC                   = 0x00000303;
	public static final int CKM_CAST_MAC_GENERAL           = 0x00000304;
	public static final int CKM_CAST_CBC_PAD               = 0x00000305;
	public static final int CKM_CAST3_KEY_GEN              = 0x00000310;
	public static final int CKM_CAST3_ECB                  = 0x00000311;
	public static final int CKM_CAST3_CBC                  = 0x00000312;
	public static final int CKM_CAST3_MAC                  = 0x00000313;
	public static final int CKM_CAST3_MAC_GENERAL          = 0x00000314;
	public static final int CKM_CAST3_CBC_PAD              = 0x00000315;
	public static final int CKM_CAST5_KEY_GEN              = 0x00000320;
	public static final int CKM_CAST128_KEY_GEN            = 0x00000320;
	public static final int CKM_CAST5_ECB                  = 0x00000321;
	public static final int CKM_CAST128_ECB                = 0x00000321;
	public static final int CKM_CAST5_CBC                  = 0x00000322;
	public static final int CKM_CAST128_CBC                = 0x00000322;
	public static final int CKM_CAST5_MAC                  = 0x00000323;
	public static final int CKM_CAST128_MAC                = 0x00000323;
	public static final int CKM_CAST5_MAC_GENERAL          = 0x00000324;
	public static final int CKM_CAST128_MAC_GENERAL        = 0x00000324;
	public static final int CKM_CAST5_CBC_PAD              = 0x00000325;
	public static final int CKM_CAST128_CBC_PAD            = 0x00000325;
	public static final int CKM_RC5_KEY_GEN                = 0x00000330;
	public static final int CKM_RC5_ECB                    = 0x00000331;
	public static final int CKM_RC5_CBC                    = 0x00000332;
	public static final int CKM_RC5_MAC                    = 0x00000333;
	public static final int CKM_RC5_MAC_GENERAL            = 0x00000334;
	public static final int CKM_RC5_CBC_PAD                = 0x00000335;
	public static final int CKM_IDEA_KEY_GEN               = 0x00000340;
	public static final int CKM_IDEA_ECB                   = 0x00000341;
	public static final int CKM_IDEA_CBC                   = 0x00000342;
	public static final int CKM_IDEA_MAC                   = 0x00000343;
	public static final int CKM_IDEA_MAC_GENERAL           = 0x00000344;
	public static final int CKM_IDEA_CBC_PAD               = 0x00000345;
	public static final int CKM_GENERIC_SECRET_KEY_GEN     = 0x00000350;
	public static final int CKM_CONCATENATE_BASE_AND_KEY   = 0x00000360;
	public static final int CKM_CONCATENATE_BASE_AND_DATA  = 0x00000362;
	public static final int CKM_CONCATENATE_DATA_AND_BASE  = 0x00000363;
	public static final int CKM_XOR_BASE_AND_DATA          = 0x00000364;
	public static final int CKM_EXTRACT_KEY_FROM_KEY       = 0x00000365;
	public static final int CKM_SSL3_PRE_MASTER_KEY_GEN    = 0x00000370;
	public static final int CKM_SSL3_MASTER_KEY_DERIVE     = 0x00000371;
	public static final int CKM_SSL3_KEY_AND_MAC_DERIVE    = 0x00000372;
	
	/* CKM_SSL3_MASTER_KEY_DERIVE_DH, CKM_TLS_PRE_MASTER_KEY_GEN,
	 * CKM_TLS_MASTER_KEY_DERIVE, CKM_TLS_KEY_AND_MAC_DERIVE, and
	 * CKM_TLS_MASTER_KEY_DERIVE_DH are new for v2.11 */
	public static final int CKM_SSL3_MASTER_KEY_DERIVE_DH  = 0x00000373;
	public static final int CKM_TLS_PRE_MASTER_KEY_GEN     = 0x00000374;
	public static final int CKM_TLS_MASTER_KEY_DERIVE      = 0x00000375;
	public static final int CKM_TLS_KEY_AND_MAC_DERIVE     = 0x00000376;
	public static final int CKM_TLS_MASTER_KEY_DERIVE_DH   = 0x00000377;
	
	public static final int CKM_SSL3_MD5_MAC               = 0x00000380;
	public static final int CKM_SSL3_SHA1_MAC              = 0x00000381;
	public static final int CKM_MD5_KEY_DERIVATION         = 0x00000390;
	public static final int CKM_MD2_KEY_DERIVATION         = 0x00000391;
	public static final int CKM_SHA1_KEY_DERIVATION        = 0x00000392;
	public static final int CKM_PBE_MD2_DES_CBC            = 0x000003A0;
	public static final int CKM_PBE_MD5_DES_CBC            = 0x000003A1;
	public static final int CKM_PBE_MD5_CAST_CBC           = 0x000003A2;
	public static final int CKM_PBE_MD5_CAST3_CBC          = 0x000003A3;
	public static final int CKM_PBE_MD5_CAST5_CBC          = 0x000003A4;
	public static final int CKM_PBE_MD5_CAST128_CBC        = 0x000003A4;
	public static final int CKM_PBE_SHA1_CAST5_CBC         = 0x000003A5;
	public static final int CKM_PBE_SHA1_CAST128_CBC       = 0x000003A5;
	public static final int CKM_PBE_SHA1_RC4_128           = 0x000003A6;
	public static final int CKM_PBE_SHA1_RC4_40            = 0x000003A7;
	public static final int CKM_PBE_SHA1_DES3_EDE_CBC      = 0x000003A8;
	public static final int CKM_PBE_SHA1_DES2_EDE_CBC      = 0x000003A9;
	public static final int CKM_PBE_SHA1_RC2_128_CBC       = 0x000003AA;
	public static final int CKM_PBE_SHA1_RC2_40_CBC        = 0x000003AB;
	
	/* CKM_PKCS5_PBKD2 is new for v2.10 */
	public static final int CKM_PKCS5_PBKD2                = 0x000003B0;
	
	public static final int CKM_PBA_SHA1_WITH_SHA1_HMAC    = 0x000003C0;
	public static final int CKM_KEY_WRAP_LYNKS             = 0x00000400;
	public static final int CKM_KEY_WRAP_SET_OAEP          = 0x00000401;
	
	/* Fortezza mechanisms */
	public static final int CKM_SKIPJACK_KEY_GEN           = 0x00001000;
	public static final int CKM_SKIPJACK_ECB64             = 0x00001001;
	public static final int CKM_SKIPJACK_CBC64             = 0x00001002;
	public static final int CKM_SKIPJACK_OFB64             = 0x00001003;
	public static final int CKM_SKIPJACK_CFB64             = 0x00001004;
	public static final int CKM_SKIPJACK_CFB32             = 0x00001005;
	public static final int CKM_SKIPJACK_CFB16             = 0x00001006;
	public static final int CKM_SKIPJACK_CFB8              = 0x00001007;
	public static final int CKM_SKIPJACK_WRAP              = 0x00001008;
	public static final int CKM_SKIPJACK_PRIVATE_WRAP      = 0x00001009;
	public static final int CKM_SKIPJACK_RELAYX            = 0x0000100a;
	public static final int CKM_KEA_KEY_PAIR_GEN           = 0x00001010;
	public static final int CKM_KEA_KEY_DERIVE             = 0x00001011;
	public static final int CKM_FORTEZZA_TIMESTAMP         = 0x00001020;
	public static final int CKM_BATON_KEY_GEN              = 0x00001030;
	public static final int CKM_BATON_ECB128               = 0x00001031;
	public static final int CKM_BATON_ECB96                = 0x00001032;
	public static final int CKM_BATON_CBC128               = 0x00001033;
	public static final int CKM_BATON_COUNTER              = 0x00001034;
	public static final int CKM_BATON_SHUFFLE              = 0x00001035;
	public static final int CKM_BATON_WRAP                 = 0x00001036;
	
	/* CKM_ECDSA_KEY_PAIR_GEN is deprecated in v2.11,
	 * CKM_EC_KEY_PAIR_GEN is preferred */
	public static final int CKM_ECDSA_KEY_PAIR_GEN         = 0x00001040;
	public static final int CKM_EC_KEY_PAIR_GEN            = 0x00001040;
	
	public static final int CKM_ECDSA                      = 0x00001041;
	public static final int CKM_ECDSA_SHA1                 = 0x00001042;
	
	/* CKM_ECDH1_DERIVE, CKM_ECDH1_COFACTOR_DERIVE, and CKM_ECMQV_DERIVE
	 * are new for v2.11 */
	public static final int CKM_ECDH1_DERIVE               = 0x00001050;
	public static final int CKM_ECDH1_COFACTOR_DERIVE      = 0x00001051;
	public static final int CKM_ECMQV_DERIVE               = 0x00001052;
	
	public static final int CKM_JUNIPER_KEY_GEN            = 0x00001060;
	public static final int CKM_JUNIPER_ECB128             = 0x00001061;
	public static final int CKM_JUNIPER_CBC128             = 0x00001062;
	public static final int CKM_JUNIPER_COUNTER            = 0x00001063;
	public static final int CKM_JUNIPER_SHUFFLE            = 0x00001064;
	public static final int CKM_JUNIPER_WRAP               = 0x00001065;
	public static final int CKM_FASTHASH                   = 0x00001070;
	
	/* CKM_AES_KEY_GEN, CKM_AES_ECB, CKM_AES_CBC, CKM_AES_MAC,
	 * CKM_AES_MAC_GENERAL, CKM_AES_CBC_PAD, CKM_DSA_PARAMETER_GEN,
	 * CKM_DH_PKCS_PARAMETER_GEN, and CKM_X9_42_DH_PARAMETER_GEN are
	 * new for v2.11 */
	public static final int CKM_AES_KEY_GEN                = 0x00001080;
	public static final int CKM_AES_ECB                    = 0x00001081;
	public static final int CKM_AES_CBC                    = 0x00001082;
	public static final int CKM_AES_MAC                    = 0x00001083;
	public static final int CKM_AES_MAC_GENERAL            = 0x00001084;
	public static final int CKM_AES_CBC_PAD                = 0x00001085;
	public static final int CKM_DSA_PARAMETER_GEN          = 0x00002000;
	public static final int CKM_DH_PKCS_PARAMETER_GEN      = 0x00002001;
	public static final int CKM_X9_42_DH_PARAMETER_GEN     = 0x00002002;

	/**
	 * @param type The mechanism type, which is one of the CKM_* constants.
	 * @return The name of this algotihm as defined by the PKCS11 C header file.
	 */
	static String getTypeName(int type)
	{
		switch (type)
		{
		case CKM_RSA_PKCS_KEY_PAIR_GEN: return "CKM_RSA_PKCS_KEY_PAIR_GEN";
		case CKM_RSA_PKCS: return "CKM_RSA_PKCS";
		case CKM_RSA_9796: return "CKM_RSA_9796";
		case CKM_RSA_X_509: return "CKM_RSA_X_509";
		case CKM_MD2_RSA_PKCS: return "CKM_MD2_RSA_PKCS";
		case CKM_MD5_RSA_PKCS: return "CKM_MD5_RSA_PKCS";
		case CKM_SHA1_RSA_PKCS: return "CKM_SHA1_RSA_PKCS";
		case CKM_RIPEMD128_RSA_PKCS: return "CKM_RIPEMD128_RSA_PKCS";
		case CKM_RIPEMD160_RSA_PKCS: return "CKM_RIPEMD160_RSA_PKCS";
		case CKM_RSA_PKCS_OAEP: return "CKM_RSA_PKCS_OAEP";
		case CKM_RSA_X9_31_KEY_PAIR_GEN: return "CKM_RSA_X9_31_KEY_PAIR_GEN";
		case CKM_RSA_X9_31: return "CKM_RSA_X9_31";
		case CKM_SHA1_RSA_X9_31: return "CKM_SHA1_RSA_X9_31";
		case CKM_RSA_PKCS_PSS: return "CKM_RSA_PKCS_PSS";
		case CKM_SHA1_RSA_PKCS_PSS: return "CKM_SHA1_RSA_PKCS_PSS";
		case CKM_DSA_KEY_PAIR_GEN: return "CKM_DSA_KEY_PAIR_GEN";
		case CKM_DSA: return "CKM_DSA";
		case CKM_DSA_SHA1: return "CKM_DSA_SHA1";
		case CKM_DH_PKCS_KEY_PAIR_GEN: return "CKM_DH_PKCS_KEY_PAIR_GEN";
		case CKM_DH_PKCS_DERIVE: return "CKM_DH_PKCS_DERIVE";
		case CKM_X9_42_DH_KEY_PAIR_GEN: return "CKM_X9_42_DH_KEY_PAIR_GEN";
		case CKM_X9_42_DH_DERIVE: return "CKM_X9_42_DH_DERIVE";
		case CKM_X9_42_DH_HYBRID_DERIVE: return "CKM_X9_42_DH_HYBRID_DERIVE";
		case CKM_X9_42_MQV_DERIVE: return "CKM_X9_42_MQV_DERIVE";
		case CKM_SHA256_RSA_PKCS: return "CKM_SHA256_RSA_PKCS";
		case CKM_SHA384_RSA_PKCS: return "CKM_SHA384_RSA_PKCS";
		case CKM_SHA512_RSA_PKCS: return "CKM_SHA512_RSA_PKCS";
		case CKM_SHA256_RSA_PKCS_PSS: return "CKM_SHA256_RSA_PKCS_PSS";
		case CKM_SHA384_RSA_PKCS_PSS: return "CKM_SHA384_RSA_PKCS_PSS";
		case CKM_SHA512_RSA_PKCS_PSS: return "CKM_SHA512_RSA_PKCS_PSS";
		case CKM_RC2_KEY_GEN: return "CKM_RC2_KEY_GEN";
		case CKM_RC2_ECB: return "CKM_RC2_ECB";
		case CKM_RC2_CBC: return "CKM_RC2_CBC";
		case CKM_RC2_MAC: return "CKM_RC2_MAC";
		case CKM_RC2_MAC_GENERAL: return "CKM_RC2_MAC_GENERAL";
		case CKM_RC2_CBC_PAD: return "CKM_RC2_CBC_PAD";
		case CKM_RC4_KEY_GEN: return "CKM_RC4_KEY_GEN";
		case CKM_RC4: return "CKM_RC4";
		case CKM_DES_KEY_GEN: return "CKM_DES_KEY_GEN";
		case CKM_DES_ECB: return "CKM_DES_ECB";
		case CKM_DES_CBC: return "CKM_DES_CBC";
		case CKM_DES_MAC: return "CKM_DES_MAC";
		case CKM_DES_MAC_GENERAL: return "CKM_DES_MAC_GENERAL";
		case CKM_DES_CBC_PAD: return "CKM_DES_CBC_PAD";
		case CKM_DES2_KEY_GEN: return "CKM_DES2_KEY_GEN";
		case CKM_DES3_KEY_GEN: return "CKM_DES3_KEY_GEN";
		case CKM_DES3_ECB: return "CKM_DES3_ECB";
		case CKM_DES3_CBC: return "CKM_DES3_CBC";
		case CKM_DES3_MAC: return "CKM_DES3_MAC";
		case CKM_DES3_MAC_GENERAL: return "CKM_DES3_MAC_GENERAL";
		case CKM_DES3_CBC_PAD: return "CKM_DES3_CBC_PAD";
		case CKM_CDMF_KEY_GEN: return "CKM_CDMF_KEY_GEN";
		case CKM_CDMF_ECB: return "CKM_CDMF_ECB";
		case CKM_CDMF_CBC: return "CKM_CDMF_CBC";
		case CKM_CDMF_MAC: return "CKM_CDMF_MAC";
		case CKM_CDMF_MAC_GENERAL: return "CKM_CDMF_MAC_GENERAL";
		case CKM_CDMF_CBC_PAD: return "CKM_CDMF_CBC_PAD";
		case CKM_MD2: return "CKM_MD2";
		case CKM_MD2_HMAC: return "CKM_MD2_HMAC";
		case CKM_MD2_HMAC_GENERAL: return "CKM_MD2_HMAC_GENERAL";
		case CKM_MD5: return "CKM_MD5";
		case CKM_MD5_HMAC: return "CKM_MD5_HMAC";
		case CKM_MD5_HMAC_GENERAL: return "CKM_MD5_HMAC_GENERAL";
		case CKM_SHA_1: return "CKM_SHA_1";
		case CKM_SHA_1_HMAC: return "CKM_SHA_1_HMAC";
		case CKM_SHA_1_HMAC_GENERAL: return "CKM_SHA_1_HMAC_GENERAL";
		case CKM_RIPEMD128: return "CKM_RIPEMD128";
		case CKM_RIPEMD128_HMAC: return "CKM_RIPEMD128_HMAC";
		case CKM_RIPEMD128_HMAC_GENERAL: return "CKM_RIPEMD128_HMAC_GENERAL";
		case CKM_RIPEMD160: return "CKM_RIPEMD160";
		case CKM_RIPEMD160_HMAC: return "CKM_RIPEMD160_HMAC";
		case CKM_RIPEMD160_HMAC_GENERAL: return "CKM_RIPEMD160_HMAC_GENERAL";
		case CKM_CAST_KEY_GEN: return "CKM_CAST_KEY_GEN";
		case CKM_CAST_ECB: return "CKM_CAST_ECB";
		case CKM_CAST_CBC: return "CKM_CAST_CBC";
		case CKM_CAST_MAC: return "CKM_CAST_MAC";
		case CKM_CAST_MAC_GENERAL: return "CKM_CAST_MAC_GENERAL";
		case CKM_CAST_CBC_PAD: return "CKM_CAST_CBC_PAD";
		case CKM_CAST3_KEY_GEN: return "CKM_CAST3_KEY_GEN";
		case CKM_CAST3_ECB: return "CKM_CAST3_ECB";
		case CKM_CAST3_CBC: return "CKM_CAST3_CBC";
		case CKM_CAST3_MAC: return "CKM_CAST3_MAC";
		case CKM_CAST3_MAC_GENERAL: return "CKM_CAST3_MAC_GENERAL";
		case CKM_CAST3_CBC_PAD: return "CKM_CAST3_CBC_PAD";
		case CKM_CAST5_KEY_GEN: return "CKM_CAST5_KEY_GEN";
		case CKM_CAST5_ECB: return "CKM_CAST5_ECB";
		case CKM_CAST5_CBC: return "CKM_CAST5_CBC";
		case CKM_CAST5_MAC: return "CKM_CAST5_MAC";
		case CKM_CAST5_MAC_GENERAL: return "CKM_CAST5_MAC_GENERAL";
		case CKM_CAST5_CBC_PAD: return "CKM_CAST5_CBC_PAD";
		case CKM_RC5_KEY_GEN: return "CKM_RC5_KEY_GEN";
		case CKM_RC5_ECB: return "CKM_RC5_ECB";
		case CKM_RC5_CBC: return "CKM_RC5_CBC";
		case CKM_RC5_MAC: return "CKM_RC5_MAC";
		case CKM_RC5_MAC_GENERAL: return "CKM_RC5_MAC_GENERAL";
		case CKM_RC5_CBC_PAD: return "CKM_RC5_CBC_PAD";
		case CKM_IDEA_KEY_GEN: return "CKM_IDEA_KEY_GEN";
		case CKM_IDEA_ECB: return "CKM_IDEA_ECB";
		case CKM_IDEA_CBC: return "CKM_IDEA_CBC";
		case CKM_IDEA_MAC: return "CKM_IDEA_MAC";
		case CKM_IDEA_MAC_GENERAL: return "CKM_IDEA_MAC_GENERAL";
		case CKM_IDEA_CBC_PAD: return "CKM_IDEA_CBC_PAD";
		case CKM_GENERIC_SECRET_KEY_GEN: return "CKM_GENERIC_SECRET_KEY_GEN";
		case CKM_CONCATENATE_BASE_AND_KEY: return "CKM_CONCATENATE_BASE_AND_KEY";
		case CKM_CONCATENATE_BASE_AND_DATA: return "CKM_CONCATENATE_BASE_AND_DATA";
		case CKM_CONCATENATE_DATA_AND_BASE: return "CKM_CONCATENATE_DATA_AND_BASE";
		case CKM_XOR_BASE_AND_DATA: return "CKM_XOR_BASE_AND_DATA";
		case CKM_EXTRACT_KEY_FROM_KEY: return "CKM_EXTRACT_KEY_FROM_KEY";
		case CKM_SSL3_PRE_MASTER_KEY_GEN: return "CKM_SSL3_PRE_MASTER_KEY_GEN";
		case CKM_SSL3_MASTER_KEY_DERIVE: return "CKM_SSL3_MASTER_KEY_DERIVE";
		case CKM_SSL3_KEY_AND_MAC_DERIVE: return "CKM_SSL3_KEY_AND_MAC_DERIVE";
		case CKM_SSL3_MASTER_KEY_DERIVE_DH: return "CKM_SSL3_MASTER_KEY_DERIVE_DH";
		case CKM_TLS_PRE_MASTER_KEY_GEN: return "CKM_TLS_PRE_MASTER_KEY_GEN";
		case CKM_TLS_MASTER_KEY_DERIVE: return "CKM_TLS_MASTER_KEY_DERIVE";
		case CKM_TLS_KEY_AND_MAC_DERIVE: return "CKM_TLS_KEY_AND_MAC_DERIVE";
		case CKM_TLS_MASTER_KEY_DERIVE_DH: return "CKM_TLS_MASTER_KEY_DERIVE_DH";
		case CKM_SSL3_MD5_MAC: return "CKM_SSL3_MD5_MAC";
		case CKM_SSL3_SHA1_MAC: return "CKM_SSL3_SHA1_MAC";
		case CKM_MD5_KEY_DERIVATION: return "CKM_MD5_KEY_DERIVATION";
		case CKM_MD2_KEY_DERIVATION: return "CKM_MD2_KEY_DERIVATION";
		case CKM_SHA1_KEY_DERIVATION: return "CKM_SHA1_KEY_DERIVATION";
		case CKM_PBE_MD2_DES_CBC: return "CKM_PBE_MD2_DES_CBC";
		case CKM_PBE_MD5_DES_CBC: return "CKM_PBE_MD5_DES_CBC";
		case CKM_PBE_MD5_CAST_CBC: return "CKM_PBE_MD5_CAST_CBC";
		case CKM_PBE_MD5_CAST3_CBC: return "CKM_PBE_MD5_CAST3_CBC";
		case CKM_PBE_MD5_CAST5_CBC: return "CKM_PBE_MD5_CAST5_CBC";
		case CKM_PBE_SHA1_CAST5_CBC: return "CKM_PBE_SHA1_CAST5_CBC";
		case CKM_PBE_SHA1_RC4_128: return "CKM_PBE_SHA1_RC4_128";
		case CKM_PBE_SHA1_RC4_40: return "CKM_PBE_SHA1_RC4_40";
		case CKM_PBE_SHA1_DES3_EDE_CBC: return "CKM_PBE_SHA1_DES3_EDE_CBC";
		case CKM_PBE_SHA1_DES2_EDE_CBC: return "CKM_PBE_SHA1_DES2_EDE_CBC";
		case CKM_PBE_SHA1_RC2_128_CBC: return "CKM_PBE_SHA1_RC2_128_CBC";
		case CKM_PBE_SHA1_RC2_40_CBC: return "CKM_PBE_SHA1_RC2_40_CBC";
		case CKM_PKCS5_PBKD2: return "CKM_PKCS5_PBKD2";
		case CKM_PBA_SHA1_WITH_SHA1_HMAC: return "CKM_PBA_SHA1_WITH_SHA1_HMAC";
		case CKM_KEY_WRAP_LYNKS: return "CKM_KEY_WRAP_LYNKS";
		case CKM_KEY_WRAP_SET_OAEP: return "CKM_KEY_WRAP_SET_OAEP";
		case CKM_SKIPJACK_KEY_GEN: return "CKM_SKIPJACK_KEY_GEN";
		case CKM_SKIPJACK_ECB64: return "CKM_SKIPJACK_ECB64";
		case CKM_SKIPJACK_CBC64: return "CKM_SKIPJACK_CBC64";
		case CKM_SKIPJACK_OFB64: return "CKM_SKIPJACK_OFB64";
		case CKM_SKIPJACK_CFB64: return "CKM_SKIPJACK_CFB64";
		case CKM_SKIPJACK_CFB32: return "CKM_SKIPJACK_CFB32";
		case CKM_SKIPJACK_CFB16: return "CKM_SKIPJACK_CFB16";
		case CKM_SKIPJACK_CFB8: return "CKM_SKIPJACK_CFB8";
		case CKM_SKIPJACK_WRAP: return "CKM_SKIPJACK_WRAP";
		case CKM_SKIPJACK_PRIVATE_WRAP: return "CKM_SKIPJACK_PRIVATE_WRAP";
		case CKM_SKIPJACK_RELAYX: return "CKM_SKIPJACK_RELAYX";
		case CKM_KEA_KEY_PAIR_GEN: return "CKM_KEA_KEY_PAIR_GEN";
		case CKM_KEA_KEY_DERIVE: return "CKM_KEA_KEY_DERIVE";
		case CKM_FORTEZZA_TIMESTAMP: return "CKM_FORTEZZA_TIMESTAMP";
		case CKM_BATON_KEY_GEN: return "CKM_BATON_KEY_GEN";
		case CKM_BATON_ECB128: return "CKM_BATON_ECB128";
		case CKM_BATON_ECB96: return "CKM_BATON_ECB96";
		case CKM_BATON_CBC128: return "CKM_BATON_CBC128";
		case CKM_BATON_COUNTER: return "CKM_BATON_COUNTER";
		case CKM_BATON_SHUFFLE: return "CKM_BATON_SHUFFLE";
		case CKM_BATON_WRAP: return "CKM_BATON_WRAP";
		case CKM_ECDSA_KEY_PAIR_GEN: return "CKM_ECDSA_KEY_PAIR_GEN";
		case CKM_ECDSA: return "CKM_ECDSA";
		case CKM_ECDSA_SHA1: return "CKM_ECDSA_SHA1";
		case CKM_ECDH1_DERIVE: return "CKM_ECDH1_DERIVE";
		case CKM_ECDH1_COFACTOR_DERIVE: return "CKM_ECDH1_COFACTOR_DERIVE";
		case CKM_ECMQV_DERIVE: return "CKM_ECMQV_DERIVE";
		case CKM_JUNIPER_KEY_GEN: return "CKM_JUNIPER_KEY_GEN";
		case CKM_JUNIPER_ECB128: return "CKM_JUNIPER_ECB128";
		case CKM_JUNIPER_CBC128: return "CKM_JUNIPER_CBC128";
		case CKM_JUNIPER_COUNTER: return "CKM_JUNIPER_COUNTER";
		case CKM_JUNIPER_SHUFFLE: return "CKM_JUNIPER_SHUFFLE";
		case CKM_JUNIPER_WRAP: return "CKM_JUNIPER_WRAP";
		case CKM_FASTHASH: return "CKM_FASTHASH";
		case CKM_AES_KEY_GEN: return "CKM_AES_KEY_GEN";
		case CKM_AES_ECB: return "CKM_AES_ECB";
		case CKM_AES_CBC: return "CKM_AES_CBC";
		case CKM_AES_MAC: return "CKM_AES_MAC";
		case CKM_AES_MAC_GENERAL: return "CKM_AES_MAC_GENERAL";
		case CKM_AES_CBC_PAD: return "CKM_AES_CBC_PAD";
		case CKM_DSA_PARAMETER_GEN: return "CKM_DSA_PARAMETER_GEN";
		case CKM_DH_PKCS_PARAMETER_GEN: return "CKM_DH_PKCS_PARAMETER_GEN";
		case CKM_X9_42_DH_PARAMETER_GEN: return "CKM_X9_42_DH_PARAMETER_GEN";
		
		default: return null;
		}
	}

	
	/**
	 * Construct a PKCS11Mechnism.
	 * @param type The type of the mechanism.
	 * @param minKeySize The minimal key size.
	 * @param maxKeySize The maximal key size.
	 * @param flags The flags.
	 */
	public PKCS11Mechanism(int type, int minKeySize, int maxKeySize, int flags)
	{
		super();
		this.type = type;
		this.minKeySize = minKeySize;
		this.maxKeySize = maxKeySize;
		this.flags = flags;
	}

	/**
	 * @return Returns the flags, which is a bitwise or some CKF_* contants.
	 */
	public int getFlags()
	{
		return this.flags;
	}

	/**
	 * @return Returns the maxKeySize.
	 */
	public int getMaxKeySize()
	{
		return this.maxKeySize;
	}

	/**
	 * @return Returns the minKeySize.
	 */
	public int getMinKeySize()
	{
		return this.minKeySize;
	}

	/**
	 * @return Returns the type, which is one of the CKM_* contants.
	 */
	public int getType()
	{
		return this.type;
	}

	/**
	 * @return Returns the name of the mechnism type as defined by the PKCS11 C header files.
	 */
	public String getTypeName()
	{
		return getTypeName(this.type);
	}

}
