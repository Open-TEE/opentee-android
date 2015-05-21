/* jniP11, a JCE cryptographic povider in top of PKCS#11 API
 *
 * Copyright (C) 2006 by ev-i Informationstechnologie GmbH www.ev-i.at
 *
 * Many code-snippets imported from libp11, which is
 *
 * Copyright (C) 2005 Olaf Kirch <okir@lst.de>
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
 */
#ifndef __JNI_P11_PRIVATE_H__
#define __JNI_P11_PRIVATE_H__ 1

#include <jnix.h>
#include <opensc/pkcs11.h>

typedef struct pkcs11_module_st pkcs11_module_t;

#define PKCS11_MODULE_MAGIC 0xd0bed0be

#ifdef WIN32
#define PKCS11_MOD_NAME_FMT "%S"
#else
#define PKCS11_MOD_NAME_FMT "%s"
#endif

struct pkcs11_module_st
{
  int _magic;
#ifdef WIN32
  wchar_t *name;
#else
  char *name;
#endif
  CK_INFO ck_info;
  CK_FUNCTION_LIST_PTR method;
  void *handle;

};

typedef struct pkcs11_slot_st pkcs11_slot_t;

#define PKCS11_SLOT_MAGIC 0x0bed0bed

struct pkcs11_slot_st
{
  int _magic;
  CK_SLOT_ID id;
  CK_SLOT_INFO ck_slot_info;
  CK_TOKEN_INFO ck_token_info;
};

/* functions in pkcs11_error.c */
const char JNIX_INTERNAL_API * pkcs11_strerror(int rv);

/* functions in pkcs11_module.c */
pkcs11_module_t JNIX_INTERNAL_API * new_pkcs11_module(JNIEnv *env, jstring filename);

jlong JNIX_INTERNAL_API pkcs11_module_to_jhandle(JNIEnv *env, pkcs11_module_t *mod);
pkcs11_module_t JNIX_INTERNAL_API * pkcs11_module_from_jhandle(JNIEnv *env, jlong handle);

void JNIX_INTERNAL_API destroy_pkcs11_module(JNIEnv *env, pkcs11_module_t *mod);

/* functions in pkcs11_slot.c */
pkcs11_slot_t JNIX_INTERNAL_API * new_pkcs11_slot(JNIEnv *env, pkcs11_module_t *mod, CK_SLOT_ID id);

jlong JNIX_INTERNAL_API pkcs11_slot_to_jhandle(JNIEnv *env, pkcs11_slot_t *mod);
pkcs11_slot_t JNIX_INTERNAL_API * pkcs11_slot_from_jhandle(JNIEnv *env, jlong handle);

void JNIX_INTERNAL_API destroy_pkcs11_slot(JNIEnv *env, pkcs11_module_t *mod, pkcs11_slot_t *slot);

jobjectArray JNIX_INTERNAL_API pkcs11_slot_make_jmechanisms(JNIEnv *env, pkcs11_module_t *mod, pkcs11_slot_t *slot,
                                                            CK_MECHANISM_TYPE_PTR mechanisms, CK_ULONG n_mechanisms);

#endif
