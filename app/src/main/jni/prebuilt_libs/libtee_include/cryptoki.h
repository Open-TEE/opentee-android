/* cryptoki.h include file for PKCS #11. */
/* $Revision: 1.4+ $ */

/* License to copy and use this software is granted provided that it is
 * identified as "RSA Security Inc. PKCS #11 Cryptographic Token Interface
 * (Cryptoki)" in all material mentioning or referencing this software.

 * License is also granted to make and use derivative works provided that
 * such works are identified as "derived from the RSA Security Inc. PKCS #11
 * Cryptographic Token Interface (Cryptoki)" in all material mentioning or
 * referencing the derived work.

 * RSA Security Inc. makes no representations concerning either the
 * merchantability of this software or the suitability of this software for
 * any particular purpose. It is provided "as is" without express or implied
 * warranty of any kind.
 */

/*
 * Modified to support the TEE interface from *nix systems
 */

#ifndef ___CRYPTOKI_H_INC___
#define ___CRYPTOKI_H_INC___

/* Check possible alignment issues with Chaabi if packing, so disable packing by default */
#ifdef PACKCRYPTOKI
#pragma pack(push, cryptoki, 1)
#endif

#define CK_PTR *

#define CK_DEFINE_FUNCTION(returnType, name) \
	returnType name

#define CK_DECLARE_FUNCTION(returnType, name) \
	returnType name

#define CK_DECLARE_FUNCTION_POINTER(returnType, name) \
	returnType (CK_PTR name)

#define CK_CALLBACK_FUNCTION(returnType, name) \
	returnType (CK_PTR name)

#ifndef NULL_PTR
#define NULL_PTR 0
#endif

#include "pkcs11.h"

#ifdef PACKCRYPTOKI
#pragma pack(pop, cryptoki)
#endif

#endif /* ___CRYPTOKI_H_INC___ */

