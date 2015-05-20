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

#include <org_opensc_pkcs11_spi_PKCS11SignatureSpi.h>

#include <jniP11private.h>

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    initSignNative
 * Signature: (JJJJI)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_initSignNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jlong hkey, jint alg)
{
  int rv;
  CK_MECHANISM mechanism;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  memset(&mechanism, 0, sizeof(mechanism));
  mechanism.mechanism = alg;

  rv = mod->method->C_SignInit(hsession,&mechanism,hkey);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_SignInit failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    updateSignNative
 * Signature: (JJJ[BII)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_updateSignNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jbyteArray ba, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pPart;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);

  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return;
    }

  if (ba == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return;
    }

  allocaCArrayFromJByteArrayOffLen(pPart,env,ba,off,len);

  rv = mod->method->C_SignUpdate(hsession,pPart,len);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_SignUpdate failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    updateSignNative1
 * Signature: (JJJB)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_updateSignNative1)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jbyte b)
{
  int rv;
  CK_BYTE bb;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  bb = (CK_BYTE)b;

  rv = mod->method->C_SignUpdate(hsession,&bb,1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_SignUpdate failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    signNative
 * Signature: (JJJ)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_signNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession)
{
  int rv;
  CK_BYTE_PTR pSignature = NULL;
  CK_ULONG    ulSignatureLen = 0;
  jbyteArray ret;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  rv = mod->method->C_SignFinal(hsession,pSignature,&ulSignatureLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_SignFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pSignature = (CK_BYTE_PTR)alloca(ulSignatureLen);

  rv = mod->method->C_SignFinal(hsession,pSignature,&ulSignatureLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_SignFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ret = (*env)->NewByteArray(env,ulSignatureLen);
  if (ret)
    (*env)->SetByteArrayRegion(env,ret,0,ulSignatureLen,(jbyte*)pSignature);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    initVerifyNative
 * Signature: (JJJJI)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_initVerifyNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jlong hkey, jint alg)
{
  int rv;
  CK_MECHANISM mechanism;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  memset(&mechanism, 0, sizeof(mechanism));
  mechanism.mechanism = alg;

  rv = mod->method->C_VerifyInit(hsession,&mechanism,hkey);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_VerifyInit failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    updateVerifyNative
 * Signature: (JJJ[BII)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_updateVerifyNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jbyteArray data, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pPart;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return;
    }

  if (data == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return;
    }


  allocaCArrayFromJByteArrayOffLen(pPart,env,data,off,len);

  rv = mod->method->C_VerifyUpdate(hsession,pPart,len);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_VerifyUpdate failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    updateVerifyNative1
 * Signature: (JJJB)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_updateVerifyNative1)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jbyte b)
{ 
  int rv;
  CK_BYTE bb = (CK_BYTE)b;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return;

  rv = mod->method->C_VerifyUpdate(hsession,&bb,1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_VerifyUpdate failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11SignatureSpi
 * Method:    verifyNative
 * Signature: (JJJ[B)Z
 */
JNIEXPORT jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11SignatureSpi_verifyNative)
  (JNIEnv *env, jobject jsig, jlong mh, jlong shandle, jlong hsession, jbyteArray data)
{
  int rv;
  CK_BYTE_PTR pSignature;
  CK_ULONG    ulSignatureLen;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return JNI_FALSE;

  if (data == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return JNI_FALSE;
    }

  allocaCArrayFromJByteArray(pSignature,ulSignatureLen,env,data);

  rv = mod->method->C_VerifyFinal(hsession,pSignature,ulSignatureLen);

  switch (rv)
    {
    case CKR_SIGNATURE_INVALID:
      return JNI_FALSE;

    case CKR_OK:
      return JNI_TRUE;

    default:
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_VerifyFinal failed for slot %d.",
                          (int)slot->id);
      return JNI_FALSE;
    }
}
