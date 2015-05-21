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

#include <org_opensc_pkcs11_spi_PKCS11CipherSpi.h>

#include <jniP11private.h>

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    initEncryptNative
 * Signature: (JJJJI)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_initEncryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jint alg)
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

  rv = mod->method->C_EncryptInit(hsession,&mechanism,hkey);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptInit failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    initDecryptNative
 * Signature: (JJJJI)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_initDecryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jint alg)
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

  rv = mod->method->C_DecryptInit(hsession,&mechanism,hkey);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptInit failed for slot %d.",
                          (int)slot->id);
      return;
    }
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateDecryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_updateDecryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart = 0;
  CK_ULONG ulOutputLen = 0;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart=alloca(ulOutputLen);

  rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ret = (*env)->NewByteArray(env,ulOutputLen);
  if (ret)
    (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen,(jbyte*)pOutputPart);
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateEncryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_updateEncryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart = 0;
  CK_ULONG ulOutputLen = 0;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart=alloca(ulOutputLen);

  rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ret = (*env)->NewByteArray(env,ulOutputLen);
  if (ret)
    (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen,(jbyte*)pOutputPart);
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateDecryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_updateDecryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_BYTE_PTR pOutputPart,pInputPart;
  CK_ULONG ulOutputLen;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  if (output == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL output data.");
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                         "C_DecryptUpdate failed for slot %d.",
                         (int)slot->id);
      return 0;
    }

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateEncryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_updateEncryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_ULONG ulOutputLen;
  CK_BYTE_PTR pOutputPart,pInputPart;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  if (output == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL output data.");
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    doFinalDecryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doFinalDecryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart0 = 0;
  CK_ULONG ulOutputLen0 = 0;
  CK_BYTE_PTR pOutputPart1 = 0;
  CK_ULONG ulOutputLen1 = 0;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart0,&ulOutputLen0);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart0=alloca(ulOutputLen0);

  if (ulOutputLen0)
    {
      rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart0,&ulOutputLen0);

      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_DecryptUpdate failed for slot %d.",
                              (int)slot->id);
          return 0;
        }
    }

  rv = mod->method->C_DecryptFinal(hsession,pOutputPart1,&ulOutputLen1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  if (ulOutputLen1)
    {
      pOutputPart1=alloca(ulOutputLen1);

      rv = mod->method->C_DecryptFinal(hsession,pOutputPart1,&ulOutputLen1);

      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                             "C_DecryptFinal failed for slot %d.",
                             (int)slot->id);
          return 0;
        }
    }

  ret = (*env)->NewByteArray(env,ulOutputLen0+ulOutputLen1);
  if (ret)
    {
      if (ulOutputLen0)
        (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen0,(jbyte*)pOutputPart0);
      if (ulOutputLen1)
        (*env)->SetByteArrayRegion(env,ret,ulOutputLen0,ulOutputLen1,(jbyte*)pOutputPart1);
    }
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    doFinalEncryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doFinalEncryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart0 = 0;
  CK_ULONG ulOutputLen0 = 0;
  CK_BYTE_PTR pOutputPart1 = 0;
  CK_ULONG ulOutputLen1 = 0;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart0,&ulOutputLen0);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart0=alloca(ulOutputLen0);

  if (ulOutputLen0)
    {
      rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart0,&ulOutputLen0);

      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_EncryptUpdate failed for slot %d.",
                              (int)slot->id);
          return 0;
        }
    }

  rv = mod->method->C_EncryptFinal(hsession,pOutputPart1,&ulOutputLen1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  if (ulOutputLen1)
    {
      pOutputPart1=alloca(ulOutputLen1);

      rv = mod->method->C_EncryptFinal(hsession,pOutputPart1,&ulOutputLen1);

      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_EncryptFinal failed for slot %d.",
                              (int)slot->id);
          return 0;
        }
    }

  ret = (*env)->NewByteArray(env,ulOutputLen0+ulOutputLen1);
  if (ret)
    {
      if (ulOutputLen0)
        (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen0,(jbyte*)pOutputPart0);
      if (ulOutputLen1)
        (*env)->SetByteArrayRegion(env,ret,ulOutputLen0,ulOutputLen1,(jbyte*)pOutputPart1);
    }
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    doFinalDecryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doFinalDecryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_BYTE_PTR pOutputPart;
  CK_ULONG ulOutputLen0;
  CK_BYTE_PTR pInputPart;
  CK_ULONG ulOutputLen,ulOutputLen1;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);
  ulOutputLen0 = ulOutputLen;

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_DecryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen0);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ulOutputLen1 = ulOutputLen - ulOutputLen0;

  rv = mod->method->C_DecryptFinal(hsession,pOutputPart+ulOutputLen0,&ulOutputLen1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_DecryptFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ulOutputLen = ulOutputLen0+ulOutputLen1;

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    doFinalEncryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doFinalEncryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_BYTE_PTR pOutputPart;
  CK_ULONG ulOutputLen,ulOutputLen0,ulOutputLen1;
  CK_BYTE_PTR pInputPart;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);

  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);
  ulOutputLen0 = ulOutputLen;

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_EncryptUpdate(hsession,pInputPart,len,pOutputPart,&ulOutputLen0);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptUpdate failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ulOutputLen1 = ulOutputLen - ulOutputLen0;

  rv = mod->method->C_EncryptFinal(hsession,pOutputPart+ulOutputLen0,&ulOutputLen1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_EncryptFinal failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ulOutputLen = ulOutputLen0+ulOutputLen1;

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}

/******************************/

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateDecryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doDecryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart;
  CK_ULONG ulOutputLen;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  pOutputPart = 0;
  ulOutputLen = 0;

  rv = mod->method->C_Decrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Decrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart=alloca(ulOutputLen);

  rv = mod->method->C_Decrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Decrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ret = (*env)->NewByteArray(env,ulOutputLen);
  if (ret)
    (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen,(jbyte*)pOutputPart);
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateEncryptNative
 * Signature: (JJJJ[BII)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doEncryptNative)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len)
{
  int rv;
  CK_BYTE_PTR pInputPart;
  CK_BYTE_PTR pOutputPart;
  CK_ULONG ulOutputLen;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  pOutputPart = 0;
  ulOutputLen = 0;

  rv = mod->method->C_Encrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Encrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  pOutputPart=alloca(ulOutputLen);

  rv = mod->method->C_Encrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Encrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  ret = (*env)->NewByteArray(env,ulOutputLen);
  if (ret)
    (*env)->SetByteArrayRegion(env,ret,0,ulOutputLen,(jbyte*)pOutputPart);
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateDecryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doDecryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_BYTE_PTR pOutputPart,pInputPart;
  CK_ULONG ulOutputLen;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  if (output == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL output data.");
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_Decrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Decrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}

/*
 * Class:     org_opensc_pkcs11_spi_PKCS11CipherSpi
 * Method:    updateEncryptNativeOff
 * Signature: (JJJJ[BII[BI)I
 */
JNIEXPORT jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_spi_PKCS11CipherSpi_doEncryptNativeOff)
  (JNIEnv *env, jobject jciph, jlong mh, jlong shandle, jlong hsession, jlong hkey, jbyteArray input, jint off, jint len, jbyteArray output, jint output_off)
{
  int rv;
  CK_BYTE_PTR pOutputPart,pInputPart;
  CK_ULONG ulOutputLen;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  if (len < 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid data length %d.",(int)len);
      return 0;
    }

  if (input == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL input data.");
      return 0;
    }

  if (off < 0 || off > len)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid input offset %d.",(int)off);
      return 0;
    }

  if (output == NULL)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "NULL output data.");
      return 0;
    }

  ulOutputLen = (*env)->GetArrayLength(env,output);
  
  if (output_off < 0 || output_off > ulOutputLen)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid output offset %d.",(int)output_off);
      return 0;
    }
 
  ulOutputLen -= output_off;
  pOutputPart = alloca(ulOutputLen);

  allocaCArrayFromJByteArrayOffLen(pInputPart,env,input,off,len);

  rv = mod->method->C_Encrypt(hsession,pInputPart,len,pOutputPart,&ulOutputLen);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Encrypt failed for slot %d.",
                          (int)slot->id);
      return 0;
    }

  (*env)->SetByteArrayRegion(env,output,output_off,ulOutputLen,(jbyte*)pOutputPart);
  
  return ulOutputLen;
}
