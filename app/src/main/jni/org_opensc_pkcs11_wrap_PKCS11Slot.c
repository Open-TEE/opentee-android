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

#include <org_opensc_pkcs11_wrap_PKCS11Slot.h>

#include <jniP11private.h>

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    initSlotNative
 * Signature: (J)J
 */
jlong JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_initSlotNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong id)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;
  
  slot = new_pkcs11_slot(env,mod,id);
  if (!slot) return 0;

  return pkcs11_slot_to_jhandle(env,slot);
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    destroySlotNative
 * Signature: (J)
 */
void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_destroySlotNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return;

  destroy_pkcs11_slot(env,mod,slot);
}


/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    enumerateSlotsNative
 * Signature: (J)[J
 */
jlongArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_enumerateSlotsNative)
  (JNIEnv *env, jclass clazz, jlong mh)
{
  CK_ULONG nslots=0;
  CK_ULONG i;
  CK_SLOT_ID *slot_ids;
  jlong *long_slot_ids;
  jlongArray ret;
  int rv;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  rv = mod->method->C_GetSlotList(FALSE /* tokenPresent */,(CK_SLOT_ID_PTR)0,&nslots);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetSlotList failed for module " PKCS11_MOD_NAME_FMT ".",
                          mod->name);
      return 0;
    }

  slot_ids = (CK_SLOT_ID *)alloca(sizeof(CK_SLOT_ID)*nslots);

  rv = mod->method->C_GetSlotList(FALSE /* tokenPresent */,slot_ids,&nslots);

  if (rv  != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetSlotList failed for module " PKCS11_MOD_NAME_FMT ".",mod->name);
      return 0;
    }
  
  long_slot_ids = (jlong *)alloca(sizeof(jlong)*nslots);

  for (i=0;i<nslots;++i)
    long_slot_ids[i] = slot_ids[i];

  ret = (*env)->NewLongArray(env,nslots);
  (*env)->SetLongArrayRegion(env,ret,0,nslots,long_slot_ids);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    waitForSlotNative
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_waitForSlotNative)
  (JNIEnv *env, jclass jslot, jlong mh)
{
  CK_ULONG slotId;
  int rv;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  /* wait in blocking mode. */
  rv = mod->method->C_WaitForSlotEvent(0,&slotId,NULL);

  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_WaitForSlotEvent failed.");
      return 0;
    }

  return slotId;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    isTokenPresentNative
 * Signature: (J)Z
 */
jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_isTokenPresentNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  return (slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) != 0;
}


/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    isRemovableDeviceNative
 * Signature: (J)Z
 */
jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_isRemovableDeviceNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  return (slot->ck_slot_info.flags & CKF_REMOVABLE_DEVICE) != 0;
}


/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    isHardwareDeviceNative
 * Signature: (J)Z
 */
jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_isHardwareDeviceNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  return (slot->ck_slot_info.flags & CKF_HW_SLOT) != 0;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getManufacturerNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getManufacturerNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  l = 32;

  while (l > 0 && slot->ck_slot_info.manufacturerID[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_slot_info.manufacturerID);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getDescriptionNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getDescriptionNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  l = 64;

  while (l > 0 && slot->ck_slot_info.slotDescription[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_slot_info.slotDescription);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getHardwareVersionNative
 * Signature: (JJ)D
 */
jdouble JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getHardwareVersionNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  return (jdouble)slot->ck_slot_info.hardwareVersion.major + 
    0.01 * (jdouble)slot->ck_slot_info.hardwareVersion.minor;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getFirmwareVersionNative
 * Signature: (JJ)D
 */
jdouble JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getFirmwareVersionNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  return (jdouble)slot->ck_slot_info.firmwareVersion.major + 
    0.01 * (jdouble)slot->ck_slot_info.firmwareVersion.minor;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getMechanismsNative
 * Signature: (JJ)[Lorg/opensc/pkcs11/wrap/PKCS11Mechanism;
 */
jobjectArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getMechanismsNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int rv;
  CK_ULONG n_mechanisms = 0;
  CK_MECHANISM_TYPE_PTR mechanisms;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return 0;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return 0;

  rv = mod->method->C_GetMechanismList(slot->id,NULL,&n_mechanisms);

  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetMechanismList for PKCS11 slot %d failed.",
                          (int)slot->id);
      return 0;
    }

  mechanisms =
    (CK_MECHANISM_TYPE_PTR)alloca(n_mechanisms*sizeof(CK_MECHANISM_TYPE));

  rv = mod->method->C_GetMechanismList(slot->id,mechanisms,&n_mechanisms);

  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetMechanismList for PKCS11 slot %d failed.",
                          (int)slot->id);
      return 0;
    }

  return pkcs11_slot_make_jmechanisms(env,mod,slot,mechanisms,n_mechanisms);
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenlabelNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenLabelNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  pkcs11_slot_t *slot;
  jbyteArray ret;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  l = sizeof(slot->ck_token_info.label)/sizeof(CK_UTF8CHAR);

  while (l > 0 && slot->ck_token_info.label[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_token_info.label);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenManufacturerNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenManufacturerNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  jbyteArray ret;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  l = sizeof(slot->ck_token_info.manufacturerID)/sizeof(CK_UTF8CHAR);

  while (l > 0 && slot->ck_token_info.manufacturerID[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_token_info.manufacturerID);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenModelNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenModelNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  jbyteArray ret;
  pkcs11_slot_t *slot;

  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  l = sizeof(slot->ck_token_info.model)/sizeof(CK_UTF8CHAR);

  while (l > 0 && slot->ck_token_info.model[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_token_info.model);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenSerialNumberNative
 * Signature: (JJ)[B
 */
jbyteArray JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenSerialNumberNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  int l;
  jbyteArray ret;
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  l = sizeof(slot->ck_token_info.serialNumber)/sizeof(CK_UTF8CHAR);

  while (l > 0 && slot->ck_token_info.serialNumber[l-1] == ' ')
    --l;

  ret = (*env)->NewByteArray(env,l);
  (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)slot->ck_token_info.serialNumber);

  return ret;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenMinPinLenNative
 * Signature: (JJ)I
 */
jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenMinPinLenNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);
 
  if (slot->ck_token_info.ulMinPinLen > 0x7fffffff ||
      slot->ck_token_info.ulMinPinLen > slot->ck_token_info.ulMaxPinLen )
    jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                       "Invalid value %u for ulMinPinLen of token in slot %d.",
                       (unsigned)slot->ck_token_info.ulMinPinLen,(int)slot->id);

  return slot->ck_token_info.ulMinPinLen;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    getTokenMaxPinLenNative
 * Signature: (JJ)I
 */
jint JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_getTokenMaxPinLenNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  if (slot->ck_token_info.ulMaxPinLen > 0x7fffffff ||
      slot->ck_token_info.ulMinPinLen > slot->ck_token_info.ulMaxPinLen )
    jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                       "Invalid value %u for ulMaxPinLen of token in slot %d.",
                       (unsigned)slot->ck_token_info.ulMaxPinLen,(int)slot->id);

  return slot->ck_token_info.ulMaxPinLen;
}

/*
 * Class:     org_opensc_pkcs11_wrap_PKCS11Slot
 * Method:    hasTokenProtectedAuthPathNative
 * Signature: (JJ)Z
 */
jboolean JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_wrap_PKCS11Slot_hasTokenProtectedAuthPathNative)
  (JNIEnv *env, jobject jslot, jlong mh, jlong handle)
{
  pkcs11_slot_t *slot;
  pkcs11_module_t *mod =  pkcs11_module_from_jhandle(env,mh);
  if (!mod) return JNI_FALSE;

  slot = pkcs11_slot_from_jhandle(env,handle);
  if (!slot) return JNI_FALSE;

  if ((slot->ck_slot_info.flags & CKF_TOKEN_PRESENT) == 0)
    jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",CKR_TOKEN_NOT_PRESENT,
                        "No token present in slot %d.",
                        (int)slot->id);

  return (slot->ck_token_info.flags & CKF_PROTECTED_AUTHENTICATION_PATH) != 0;
}
