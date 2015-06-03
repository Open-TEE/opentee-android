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
#include <org_opensc_pkcs11_wrap_PKCS11Object.h>

#include <jniP11private.h>
#include <stdlib.h>

#define ENUM_HANDLES_BLOCK_SZ 10

typedef struct myAttribute {
  CK_ATTRIBUTE_TYPE type;
  CK_VOID_PTR       pValue;

  /* ulValueLen went from CK_USHORT to CK_ULONG for v2.0 */
  CK_ULONG          ulValueLen;  /* in bytes */
} myAttribute;

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    enumObjectsNative
 * Signature: (JJJ[Lorg/opensc/pkcs11/wrap/PKCS11Attribute;)[J
 */
JNIEXPORT jlongArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_enumObjectsNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jobjectArray attrs)
{
  jclass clazz;
  jmethodID getKindID,getDataID;
  CK_ULONG ulAttributeCount;
  CK_ATTRIBUTE_PTR pAttributes;
  CK_ULONG i,count;
  int nobjs,rv;
  CK_ULONG obj_ids[ENUM_HANDLES_BLOCK_SZ];
  size_t ret_obj_ids_sz;
  jlong *ret_obj_ids;
  jlongArray ret;
  pkcs11_slot_t *slot;

#ifndef ANDROID
  fprintf(stderr, "----------::Session ID: Login %lu\n", hsession);
#endif
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

  ulAttributeCount = (*env)->GetArrayLength(env,attrs);
  pAttributes = alloca(ulAttributeCount * sizeof(CK_ATTRIBUTE));

  CK_OBJECT_CLASS obj_class = CKO_PUBLIC_KEY;
  
  //fprintf(stderr, "(pAttributes[1].pValue:%d\n", );
  myAttribute aAttribute[1] = {
    {CKA_CLASS, &obj_class, sizeof(obj_class) }
  };

  for (i=0;i<ulAttributeCount;++i)
    {
      jbyteArray data;
      jobject jattr = (*env)->GetObjectArrayElement(env,attrs,i);
      if (!jattr) return 0;

      pAttributes[i].type = (*env)->CallIntMethod(env,jattr,getKindID);

      data = (jbyteArray)(*env)->CallObjectMethod(env,jattr,getDataID);

      allocaCArrayFromJByteArray(pAttributes[i].pValue,pAttributes[i].ulValueLen,env,data);

      // TODO HACK
      pAttributes[i].ulValueLen = 8;
      //TODO: look at allocaCArrayFromJByteArray function.
    }

  ret_obj_ids_sz = ENUM_HANDLES_BLOCK_SZ;
  nobjs = 0;
  ret_obj_ids=(jlong*)malloc(ret_obj_ids_sz * sizeof(jlong));

  if (!ret_obj_ids)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Out of memory during object enumeration for slot number %d.",
                         (int)slot->id);
      goto failed;
    }

  rv = mod->method->C_FindObjectsInit(hsession,pAttributes,1);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_FindObjectsInit failed for slot number %d.",
                          (int)slot->id);
      goto failed;
    }

  count = 0;

  rv = mod->method->C_FindObjects(hsession,obj_ids, ENUM_HANDLES_BLOCK_SZ, &count);
#ifndef ANDROID
  fprintf(stderr, "enumObjectsNative::count: %d\n", count);
#endif
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_FindObjects failed for slot number %d.",
                          (int)slot->id);
      goto failed;
    }

  for (i=0; i<count ;++i)
    {
      jlong id = (jlong)obj_ids[i];
      ret_obj_ids[nobjs] = id;
      ++nobjs;
    }

  while (count == ENUM_HANDLES_BLOCK_SZ)
    {
      jlong *new_obj_ids;
      ret_obj_ids_sz += ENUM_HANDLES_BLOCK_SZ;

      new_obj_ids=(jlong*)realloc(ret_obj_ids,ret_obj_ids_sz * sizeof(jlong));
     
      if (!new_obj_ids)
        {
          jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                             "Out of memory during object enumeration for slot number %d.",
                             (int)slot->id);
          goto failed;
        }

      ret_obj_ids=new_obj_ids;

      rv = mod->method->C_FindObjects(hsession,obj_ids,ENUM_HANDLES_BLOCK_SZ, &count);
      
      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_FindObjects failed for slot number %d.",
                              (int)slot->id);
          goto failed;
        }

      for (i=0; i<count ;++i)
        {
          jlong id = (jlong)obj_ids[i];
          ret_obj_ids[nobjs] = id;
          ++nobjs;
        }
    }

  rv = mod->method->C_FindObjectsFinal(hsession);
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_FindObjectsFinal failed for slot number %d.",
                          (int)slot->id);
      goto failed;
    }

  /* OK, akc it into JAVA's realm. */
  ret = (*env)->NewLongArray(env,nobjs);
  (*env)->SetLongArrayRegion(env,ret,0,nobjs,ret_obj_ids);

#ifndef ANDROID
  fprintf(stderr, "enumObjectsNative finished\n");
#endif

  // jlong* ids_ptr = ret_obj_ids;
  // while (*ids_ptr) {
  //     fprintf(stderr, "ret: %02X \n", *ids_ptr);
  //     ids_ptr++;
  // }

  free(ret_obj_ids);
  return ret;

 failed:
  free(ret_obj_ids);
  return 0;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    getAttributeNative
 * Signature: (JJJI)[B
 */
JNIEXPORT jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_getAttributeNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jlong ohandle, jint att)
{
  int rv;
  CK_ATTRIBUTE templ;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  templ.type = att;
  templ.pValue = NULL;
  templ.ulValueLen = 0;
      
  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute %u.",
                          (unsigned)att);
      return 0;
    }

  if (templ.ulValueLen == ~((CK_ULONG)0))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen -1 for attribute %u but returned CKR_OK. The underlying PKCS#11 module seems to be broken.",
                          (unsigned)att);
      return 0;
    }

  templ.pValue = alloca(templ.ulValueLen);

  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute %u.",
                          (unsigned)att);
      return 0;
    }
  
  if (templ.ulValueLen == ~((CK_ULONG)0))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen -1 for attribute %u but returned CKR_OK. The underlying PKCS#11 module seems to be broken.",
                          (unsigned)att);
      return 0;
    }

  ret = (*env)->NewByteArray(env,templ.ulValueLen);
  (*env)->SetByteArrayRegion(env,ret,0,templ.ulValueLen,(jbyte*)templ.pValue);
  
  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    getMechanismsNative
 * Signature: (JJJJ)[Lorg/opensc/pkcs11/wrap/PKCS11Mechanism;
 */
jobjectArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_getAllowedMechanismsNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jlong ohandle)
{
  int rv;
  CK_ATTRIBUTE templ;
  CK_MECHANISM_TYPE_PTR mechanisms;
  CK_ULONG n_mechanisms;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  templ.type = CKA_ALLOWED_MECHANISMS;
  templ.pValue = NULL;
  templ.ulValueLen = 0;
      
  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute CKA_ALLOWED_MECHANISMS.");
      return 0;
    }

  if (templ.ulValueLen == ~((CK_ULONG)0))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen -1 for attribute CKA_ALLOWED_MECHANISMS but returned CKR_OK. The underlying PKCS#11 module seems to be broken.");
      return 0;
    }

  templ.pValue = alloca(templ.ulValueLen);

  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute CKA_ALLOWED_MECHANISMS.");
      return 0;
    }

  if (templ.ulValueLen == ~((CK_ULONG)0))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen -1 for attribute CKA_ALLOWED_MECHANISMS but returned CKR_OK. The underlying PKCS#11 module seems to be broken.");
      return 0;
    }

  mechanisms = (CK_MECHANISM_TYPE_PTR)templ.pValue;
  n_mechanisms = templ.ulValueLen/sizeof(CK_MECHANISM_TYPE);

  return pkcs11_slot_make_jmechanisms(env,mod,slot,mechanisms,n_mechanisms);
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    getULongAttributeNative
 * Signature: (JJJJI)I
 */
jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_getULongAttributeNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jlong ohandle, jint att)
{
  int rv;
  CK_ATTRIBUTE templ;
  /* default return value */
  CK_ULONG ret=~((CK_ULONG)0);
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  templ.type = att;
  templ.pValue = &ret;
  templ.ulValueLen = sizeof(CK_ULONG);
      
  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute %u.",
                          (unsigned)att);
      return 0;
    }

  if (templ.ulValueLen != sizeof(CK_ULONG))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen==%lu, which is not equal to sizeof(CK_ULONG) for attribute %u of type CK_ULONG but returned CKR_OK. The underlying PKCS#11 module seems to be broken.",
                          (unsigned long)templ.ulValueLen,(unsigned)att);
      return 0;
    }

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    getBooleanAttributeNative
 * Signature: (JJJJI)Z
 */
jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_getBooleanAttributeNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jlong ohandle, jint att)
{
  int rv;
  CK_ATTRIBUTE templ;
  CK_BBOOL ret = 0;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,shandle);
  if (!slot) return 0;

  templ.type = att;
  templ.pValue = &ret;
  templ.ulValueLen = sizeof(CK_BBOOL);
      
  rv = mod->method->C_GetAttributeValue(hsession,ohandle,&templ,1);
  
  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetAttributeValue failed for attribute %u.",
                          (unsigned)att);
      return 0;
    }

  if (templ.ulValueLen != sizeof(CK_BBOOL))
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_FUNCTION_FAILED,
                          "C_GetAttributeValue returned ulValueLen==%lu, which is not equal to sizeof(CK_BBOOL) for attribute %u of type CK_BBOOL but returned CKR_OK. The underlying PKCS#11 module seems to be broken.",
                          (unsigned long)templ.ulValueLen,(unsigned)att);
      return 0;
    }

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Object
 * Method:    createObjectNative
 * Signature: (JJJ[Lorg/opensc/pkcs11/wrap/PKCS11Attribute;)J
 */
JNIEXPORT jlong JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Object_createObjectNative)
  (JNIEnv *env, jclass jp11obj, jlong mh, jlong shandle, jlong hsession, jobjectArray attrs)
{
  int rv;
  CK_ULONG i;
  CK_ULONG ulAttributeCount;
  CK_ATTRIBUTE_PTR pAttributes;
  CK_OBJECT_HANDLE hObject;
  jclass clazz;
  jmethodID getKindID,getDataID;
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

  ulAttributeCount = (*env)->GetArrayLength(env,attrs);
  pAttributes = alloca(ulAttributeCount * sizeof(CK_ATTRIBUTE));

  for (i=0;i<ulAttributeCount;++i)
    {
      jbyteArray data;
      jobject jattr = (*env)->GetObjectArrayElement(env,attrs,i);
      if (!jattr) return 0;

      pAttributes[i].type = (*env)->CallIntMethod(env,jattr,getKindID);

      data = (jbyteArray)(*env)->CallObjectMethod(env,jattr,getDataID);

      allocaCArrayFromJByteArray(pAttributes[i].pValue,pAttributes[i].ulValueLen,env,data);
    }


  rv = mod->method->C_CreateObject(hsession, pAttributes, ulAttributeCount, &hObject);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_CreateObject failed.");
      return 0;
    }

  return hObject;
}
