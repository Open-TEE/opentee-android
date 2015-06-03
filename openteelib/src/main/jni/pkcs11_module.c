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
# define DEBUG_PKCS11_MODULE
#endif

#ifdef WIN32
#include <windows.h>

static int throwWin32Error(JNIEnv *env, const char *msg, const wchar_t *module)
{
  wchar_t *strerr=0;
  FormatMessageW(FORMAT_MESSAGE_ALLOCATE_BUFFER |
                 FORMAT_MESSAGE_FROM_SYSTEM,
                 NULL,
                 GetLastError(),
                 MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                 (LPWSTR)&strerr,
                 0, NULL );

  jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception","%s %S: %S",
                     msg,module,strerr);

  LocalFree(strerr);
  return -1;
}

static CK_RV pkcs11_create_mutex(CK_VOID_PTR_PTR ppMutex)
{
  LPCRITICAL_SECTION cs;

  if (ppMutex == NULL) return CKR_ARGUMENTS_BAD;

  cs = (LPCRITICAL_SECTION)malloc(sizeof(CRITICAL_SECTION));

  if (cs == NULL) return CKR_HOST_MEMORY;

  InitializeCriticalSection(cs);

  *ppMutex = (CK_VOID_PTR)cs;

  return CKR_OK;
}

static CK_RV pkcs11_destroy_mutex(CK_VOID_PTR pMutex)
{
  LPCRITICAL_SECTION cs = (LPCRITICAL_SECTION)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  DeleteCriticalSection(cs);
  free(cs);

  return CKR_OK;
}

static CK_RV pkcs11_lock_mutex(CK_VOID_PTR pMutex)
{
  LPCRITICAL_SECTION cs = (LPCRITICAL_SECTION)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  EnterCriticalSection(cs);

  return CKR_OK;
}

static CK_RV pkcs11_unlock_mutex(CK_VOID_PTR pMutex)
{
  LPCRITICAL_SECTION cs = (LPCRITICAL_SECTION)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  LeaveCriticalSection(cs);

  return CKR_OK;
}
#else

#include <pthread.h>
#ifdef ANDROID
#include <dlfcn.h>
#else
#include <ltdl.h>
#endif /* ANDROID */

static CK_RV pkcs11_create_mutex(CK_VOID_PTR_PTR ppMutex)
{
  pthread_mutex_t *mutex;
  if (ppMutex == NULL) return CKR_ARGUMENTS_BAD;

  mutex = (pthread_mutex_t *)malloc(sizeof(pthread_mutex_t));

  if (mutex == NULL) return CKR_HOST_MEMORY;

  if (pthread_mutex_init(mutex,NULL))
    {
      free(mutex);
      return CKR_GENERAL_ERROR;
    }

  *ppMutex = (CK_VOID_PTR)mutex;

  return CKR_OK;
}

static CK_RV pkcs11_destroy_mutex(CK_VOID_PTR pMutex)
{
  pthread_mutex_t *mutex = (pthread_mutex_t *)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  pthread_mutex_destroy(mutex);
  free(mutex);

  return CKR_OK;
}

static CK_RV pkcs11_lock_mutex(CK_VOID_PTR pMutex)
{
  pthread_mutex_t * mutex = (pthread_mutex_t *)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  if (pthread_mutex_lock(mutex))
    return CKR_GENERAL_ERROR;

  return CKR_OK;
}

static CK_RV pkcs11_unlock_mutex(CK_VOID_PTR pMutex)
{
  pthread_mutex_t *mutex = (pthread_mutex_t *)pMutex;

  if (pMutex == NULL) return CKR_ARGUMENTS_BAD;

  if (pthread_mutex_unlock(mutex))
    return CKR_GENERAL_ERROR;

  return CKR_OK;
}

#endif

static CK_C_INITIALIZE_ARGS pkcs11_init_args =
  {
    pkcs11_create_mutex,
    pkcs11_destroy_mutex,
    pkcs11_lock_mutex,
    pkcs11_unlock_mutex,
    CKF_OS_LOCKING_OK,
    NULL
  };


pkcs11_module_t *new_pkcs11_module(JNIEnv *env, jstring filename)
{
  int rv;
  jclass sc;
  CK_RV (*c_get_function_list)(CK_FUNCTION_LIST_PTR_PTR);
  jsize sz;
#ifdef WIN32
  jmethodID toCharArrayId;
  jcharArray ucs2;
#else
  jmethodID getBytesId;
  jbyteArray filename8;
#endif

  pkcs11_module_t *mod = (pkcs11_module_t *) malloc(sizeof(pkcs11_module_t));

  if (!mod)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Out of memory allocating PKCS11 context.");
      return 0;
    }

  mod->_magic = PKCS11_MODULE_MAGIC;
  mod->name = 0;
  mod->handle = 0;

  sc = (*env)->FindClass(env,"java/lang/String");

  if (!sc) goto failed;

#ifdef WIN32
  toCharArrayId = (*env)->GetMethodID(env,sc,"toCharArray","()[C");

  if (!toCharArrayId) goto failed;

  ucs2 = (*env)->CallObjectMethod(env,filename,toCharArrayId);

  if (!ucs2) goto failed;

  sz = (*env)->GetArrayLength(env,ucs2);
  mod->name = (wchar_t *)malloc(2*(sz+1));

  if (!mod->name)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Out of memory allocating PKCS11 module name.");
      goto failed;
    }

  (*env)->GetCharArrayRegion(env,ucs2,0,sz,(jchar*)mod->name);
  mod->name[sz] = 0;

  mod->handle = LoadLibraryW(mod->name);

  if (!mod->handle)
    {
      throwWin32Error(env,"Cannot open PKCS11 module",mod->name);
      goto failed;
    }
#else
#ifndef ANDROID
  if (lt_dlinit() != 0)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Unable ot initialize dynamic function loading.");
      return 0;
    }
#endif /* ANDROID */

  getBytesId = (*env)->GetMethodID(env,sc,"getBytes","()[B");

  if (!getBytesId) goto failed;

  filename8 = (*env)->CallObjectMethod(env,filename,getBytesId);

  if (!filename8) goto failed;

  sz = (*env)->GetArrayLength(env,filename8);
  mod->name = (char*)malloc(sz+1);

  if (!mod->name)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Out of memory allocating PKCS11 module name.");
      goto failed;
    }

  (*env)->GetByteArrayRegion(env,filename8,0,sz,(jbyte*)mod->name);
  mod->name[sz] = 0;

#ifdef ANDROID
  mod->handle = dlopen(mod->name, RTLD_NOW);
#else
  mod->handle = lt_dlopen(mod->name);
#endif /* ANDROID */

  if (mod->handle == NULL)
    {
#ifdef ANDROID
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Cannot open PKCS11 module %s: %s.",mod->name,dlerror());
#else
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Cannot open PKCS11 module %s: %s.",mod->name,lt_dlerror());
#endif /* ANDROID */
      goto failed;
    }
#endif

#ifdef WIN32
  c_get_function_list = (CK_RV (*)(CK_FUNCTION_LIST_PTR_PTR))
    GetProcAddress(mod->handle, "C_GetFunctionList");

  if (!c_get_function_list)
    {
    	throwWin32Error(env,"Cannot find function C_GetFunctionList in PKCS11 module",mod->name);
      goto failed;
    }

#else
#ifdef ANDROID
  c_get_function_list = (CK_RV (*)(CK_FUNCTION_LIST_PTR_PTR))
    dlsym(mod->handle, "C_GetFunctionList");
#else
  c_get_function_list = (CK_RV (*)(CK_FUNCTION_LIST_PTR_PTR))
    lt_dlsym(mod->handle, "C_GetFunctionList");
#endif /* ANDROID */

  if (!c_get_function_list)
    {
#ifdef ANDROID
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Cannot find function C_GetFunctionList in PKCS11 module %s: %s.",
                         mod->name,dlerror());
#else
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Cannot find function C_GetFunctionList in PKCS11 module %s: %s.",
                         mod->name,lt_dlerror());
#endif /* ANDROID */
      goto failed;
    }
#endif

  rv = c_get_function_list(&mod->method);
  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetFunctionList in PKCS11 module " PKCS11_MOD_NAME_FMT " failed.",
                          mod->name);
      goto failed;
    }

  rv = mod->method->C_Initialize(&pkcs11_init_args);
  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_Initialize in PKCS11 module " PKCS11_MOD_NAME_FMT " failed.",
                          mod->name);
      goto failed;
    }

  /* Get info on the library */
  rv = mod->method->C_GetInfo(&mod->ck_info);
  if (rv != CKR_OK)
    {
      jnixThrowExceptionI(env,"org/opensc/pkcs11/wrap/PKCS11Exception",rv,
                          "C_GetInfo in PKCS11 module " PKCS11_MOD_NAME_FMT " failed.",mod->name);
      goto failed;
    }

#ifdef DEBUG_PKCS11_MODULE
  fprintf(stderr,"Loaded module: " PKCS11_MOD_NAME_FMT ".\n",mod->name);
  fprintf(stderr,"handle= %p.\n",mod);
  fprintf(stderr,"version= %d.%d.\n",
          (int)mod->ck_info.cryptokiVersion.major,
          (int)mod->ck_info.cryptokiVersion.minor );
  fprintf(stderr,"manufacturer= %.32s.\n",mod->ck_info.manufacturerID);
  fprintf(stderr,"description= %.32s.\n",mod->ck_info.libraryDescription);
#endif

 return mod;

failed:
  if (mod->name) free(mod->name);

  if (mod->handle) {
#ifdef WIN32
    FreeLibrary(mod->handle);
#else
#ifdef ANDROID
    dlclose(mod->handle);
#else
    lt_dlclose(mod->handle);
#endif /* ANDROID */
#endif
  }

  free(mod);

#if !defined(WIN32) && !defined(ANDROID)
  lt_dlexit();
#endif
  return 0;
}

jlong pkcs11_module_to_jhandle(JNIEnv *env, pkcs11_module_t *mod)
{
  return (jlong)(size_t)mod;
}

pkcs11_module_t *pkcs11_module_from_jhandle(JNIEnv *env, jlong handle)
{
  pkcs11_module_t *mod = (pkcs11_module_t *)(size_t)handle;

  if (!mod || mod->_magic != PKCS11_MODULE_MAGIC)
    {
      jnixThrowException(env,"org/opensc/pkcs11/wrap/PKCS11Exception",
                         "Invalid PKCS 11 module handle %p.",(void*)mod);
      return 0;
    }

  return mod;
}

void destroy_pkcs11_module(JNIEnv *env, pkcs11_module_t *mod)
{

#ifdef DEBUG_PKCS11_MODULE
  fprintf(stderr,"Unloading module: " PKCS11_MOD_NAME_FMT ".\n",mod->name);
  fprintf(stderr,"handle= %p.\n",mod);
#endif

  /* Tell the PKCS11 library to shut down */
  mod->method->C_Finalize(NULL);

  if (mod->handle) {
#ifdef WIN32
    FreeLibrary(mod->handle);
#else
#ifdef ANDROID
    dlclose(mod->handle);
#else
    lt_dlclose(mod->handle);
#endif /* ANDROID */
#endif
  }

  if (mod->name) free(mod->name);

  memset(mod, 0, sizeof(pkcs11_module_t));
  free(mod);

#if !defined(WIN32) && !defined(ANDROID)
  lt_dlexit();
#endif
}
