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

#include <jniP11private.h>
#include <stdlib.h>

#ifdef DEBUG
# define DEBUG_PKCS11_SLOT
#endif

pkcs11_slot_t *new_pkcs11_slot(JNIEnv *env,  pkcs11_module_t *mod, CK_SLOT_ID id)
{
  int rv;
  pkcs11_slot_t *slot = (pkcs11_slot_t *) malloc(sizeof(pkcs11_slot_t));

  if (!slot)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Out of memory allocating PKCS11 slot.");
      return 0;
    }

  memset(slot, 0, sizeof(pkcs11_slot_t));

  slot->_magic = PKCS11_SLOT_MAGIC;
  slot->id = id;

  rv = mod->method->C_GetSlotInfo(id,&slot->ck_slot_info);
  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetSlotInfo for PKCS11 slot %d failed.",(int)id);
      goto failed;
    }

#ifdef DEBUG_PKCS11_SLOT
  fprintf(stderr,"Loaded slot: %d.\n",(int)id);
  fprintf(stderr,"handle= %p.\n",slot);
  fprintf(stderr,"description= %.64s.\n",slot->ck_slot_info.slotDescription);
  fprintf(stderr,"manufacturer= %.32s.\n",slot->ck_slot_info.manufacturerID);
  fprintf(stderr,"flags= %x.\n",(unsigned)slot->ck_slot_info.flags);
  fprintf(stderr,"hardwareVersion= %d.%d.\n",
          (int)slot->ck_slot_info.hardwareVersion.major,
          (int)slot->ck_slot_info.hardwareVersion.minor );
  fprintf(stderr,"firmwareVersion= %d.%d.\n",
          (int)slot->ck_slot_info.firmwareVersion.major,
          (int)slot->ck_slot_info.firmwareVersion.minor );
#endif

  if (slot->ck_slot_info.flags & CKF_TOKEN_PRESENT)
    {
      rv = mod->method->C_GetTokenInfo(id,&slot->ck_token_info);
      if (rv != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_GetTokenInfo for PKCS11 slot %d failed.",(int)id);
          goto failed;
        }

#ifdef DEBUG_PKCS11_SLOT
      fprintf(stderr,"token.label= %.32s.\n",slot->ck_token_info.label);
      fprintf(stderr,"token.manufacturer= %.32s.\n",slot->ck_token_info.manufacturerID);
      fprintf(stderr,"token.model= %.16s.\n",slot->ck_token_info.model);
      fprintf(stderr,"token.serialNumber= %.16s.\n",slot->ck_token_info.serialNumber);
      fprintf(stderr,"token.flags= %x.\n",(unsigned)slot->ck_token_info.flags);
      fprintf(stderr,"token.ulMaxSessionCount= %u.\n",
              (unsigned)slot->ck_token_info.ulMaxSessionCount);
      fprintf(stderr,"token.ulMaxPinLen= %u.\n",
              (unsigned)slot->ck_token_info.ulMaxPinLen);
      fprintf(stderr,"token.ulMinPinLen= %u.\n",
              (unsigned)slot->ck_token_info.ulMinPinLen);
#endif
    }
    
  return slot;

failed:
  free(slot);
  return 0;
}

jlong pkcs11_slot_to_jhandle(JNIEnv *env, pkcs11_slot_t *slot)
{
  return (jlong)(size_t)slot;
}

pkcs11_slot_t *pkcs11_slot_from_jhandle(JNIEnv *env, jlong handle)
{
  pkcs11_slot_t *slot = (pkcs11_slot_t *)(size_t)handle;

  if (!slot || slot->_magic != PKCS11_SLOT_MAGIC)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid PKCS11 slot handle %p.",(void*)slot);
      return 0;
    }
  
  return slot;
}

void destroy_pkcs11_slot(JNIEnv *env, pkcs11_module_t *mod, pkcs11_slot_t *slot)
{ 
#ifdef DEBUG_PKCS11_SLOT
  fprintf(stderr,"Unloading slot: %d.\n",(int)slot->id);
  fprintf(stderr,"handle= %p.\n",slot);
#endif

  memset(slot, 0, sizeof(pkcs11_slot_t));
  free(slot);
}

jobjectArray pkcs11_slot_make_jmechanisms(JNIEnv *env, pkcs11_module_t *mod, pkcs11_slot_t *slot,
                                          CK_MECHANISM_TYPE_PTR mechanisms, CK_ULONG n_mechanisms)
{
  CK_ULONG i;
  jclass clazz;
  jmethodID ctorID;
  jobjectArray ret;
  int rv;

  clazz = (*env)->FindClass(env,"org/opensc/pkcs11/wrap/PKCS11Mechanism");

  if (!clazz) return 0;

  ctorID = (*env)->GetMethodID(env,clazz,"<init>","(IIII)V");

  if (!ctorID) return 0;

  ret = (*env)->NewObjectArray(env,n_mechanisms,clazz,NULL /* initialElement */);

  if (!ret) return 0;

  for (i=0;i<n_mechanisms;++i)
    {
      jobject m;
      CK_MECHANISM_INFO mechanismInfo;

      rv = mod->method->C_GetMechanismInfo(slot->id,mechanisms[i], &mechanismInfo);

      if (rv  != CKR_OK)
        {
          jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                              "C_GetMechanismInfo failed for mechanism %u.",
                              (unsigned)mechanisms[i]);
          return 0;
        }

      m = (*env)->NewObject(env,clazz,ctorID,
                            (jint)mechanisms[i],
                            (jint)mechanismInfo.ulMinKeySize,
                            (jint)mechanismInfo.ulMaxKeySize,
                            (jint)mechanismInfo.flags  );

      if (!m) return 0;

      (*env)->SetObjectArrayElement(env,ret,i,m);
    }

  return ret;
}

