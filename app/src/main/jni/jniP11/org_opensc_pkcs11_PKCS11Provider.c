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

#include <org_opensc_pkcs11_PKCS11Provider.h>

#include <jniP11private.h>

/*
 * Class:     org_opensc_pkcs11_PKCS11Provider
 * Method:    loadPKCS11Module
 * Signature: ([B)J
 */
jlong JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_PKCS11Provider_loadNativePKCS11Module)
  (JNIEnv *env, jobject provider, jstring filename)
{
  pkcs11_module_t *mod =  new_pkcs11_module(env,filename);

  if (!mod) return 0;

  return pkcs11_module_to_jhandle(env,mod);
}

/*
 * Class:     org_opensc_pkcs11_PKCS11Provider
 * Method:    unloadPKCS11Module
 * Signature: (J)V
 */
void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_PKCS11Provider_unloadPKCS11Module)
  (JNIEnv *env, jobject provider, jlong handle)
{
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,handle);

  if (!mod) return;

  destroy_pkcs11_module(env,mod);
}
