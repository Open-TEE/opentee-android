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
#include <org_opensc_pkcs11_wrap_PKCS11KeyPairGenerator.h>

#include <jniP11private.h>
#include <stdlib.h>

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11KeyPairGenerator
 * Method:    generateKeyPairNative
 * Signature: (JJJI[Lorg/opensc/pkcs11/wrap/PKCS11Attribute;[Lorg/opensc/pkcs11/wrap/PKCS11Attribute;)[J
 */
JNIEXPORT jlongArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11KeyPairGenerator_generateKeyPairNative)
  (JNIEnv *env, jobject jp11kpg, jlong mh, jlong shandle, jlong hsession,
   jint algo, jobjectArray pubAttrs, jobjectArray privAttrs)
{
  int rv;
  CK_ULONG i;
  CK_ULONG ulPublicKeyAttributeCount;
  CK_ATTRIBUTE_PTR pPublicKeyTemplate;
  CK_ULONG ulPrivateKeyAttributeCount;
  CK_ATTRIBUTE_PTR pPrivateKeyTemplate;
  CK_MECHANISM keyPairMechanism;
  CK_OBJECT_HANDLE hPublicKey, hPrivateKey;
  jclass clazz;
  jmethodID getKindID;
  jmethodID getDataID;
  jlong buf[2];
  jlongArray ret;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;
  
  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  clazz = (*env)->FindClass(env,"org/opensc/pkcs11/wrap/PKCS11Attribute");

  if (!clazz) return 0;

  getKindID = (*env)->GetMethodID(env,clazz,"getKind","()I");

  if (!getKindID) return 0;

  getDataID = (*env)->GetMethodID(env,clazz,"getData","()[B");

  if (!getDataID) return 0;

  ulPublicKeyAttributeCount = (*env)->GetArrayLength(env,pubAttrs);
  pPublicKeyTemplate = alloca(ulPublicKeyAttributeCount * sizeof(CK_ATTRIBUTE));

  for (i=0;i<ulPublicKeyAttributeCount;++i)
    {
      jbyteArray data;
      jobject jattr = (*env)->GetObjectArrayElement(env,pubAttrs,i);
      if (!jattr) return 0;

      pPublicKeyTemplate[i].type = (*env)->CallIntMethod(env,jattr,getKindID);

      data = (jbyteArray)(*env)->CallObjectMethod(env,jattr,getDataID);

      allocaCArrayFromJByteArray(pPublicKeyTemplate[i].pValue,pPublicKeyTemplate[i].ulValueLen,env,data);
    }

  ulPrivateKeyAttributeCount = (*env)->GetArrayLength(env,privAttrs);
  pPrivateKeyTemplate = alloca(ulPrivateKeyAttributeCount * sizeof(CK_ATTRIBUTE));

  for (i=0;i<ulPrivateKeyAttributeCount;++i)
    {
      jbyteArray data;
      jobject jattr = (*env)->GetObjectArrayElement(env,privAttrs,i);
      if (!jattr) return 0;

      pPrivateKeyTemplate[i].type = (*env)->CallIntMethod(env,jattr,getKindID);

      data = (jbyteArray)(*env)->CallObjectMethod(env,jattr,getDataID);

      allocaCArrayFromJByteArray(pPrivateKeyTemplate[i].pValue,pPrivateKeyTemplate[i].ulValueLen,env,data);
    }

  keyPairMechanism.mechanism      = algo;
  keyPairMechanism.pParameter     = NULL_PTR;
  keyPairMechanism.ulParameterLen = 0;

  rv = mod->method->C_GenerateKeyPair(hsession, &keyPairMechanism,
                                      pPublicKeyTemplate, ulPublicKeyAttributeCount,
                                      pPrivateKeyTemplate, ulPrivateKeyAttributeCount,
                                      &hPublicKey, &hPrivateKey);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GenerateKeyPair failed.");
      return 0;
    }

  ret = (*env)->NewLongArray(env,2);

  if (!ret) return 0;

  buf[0] = hPublicKey;
  buf[1] = hPrivateKey;
  (*env)->SetLongArrayRegion(env,ret,0,2,buf);

  return ret;
}
