/*
 * $Id$
 *
 * jnix, some amendemends to JNI for the programmer's convenience.
 *
 * Copyright (C) 2006 by ev-i Informationstechnologie GmbH www.ev-i.at
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
#include<jnix.h>

static void jnixThrowExceptionInternal(int *sz,
                                       JNIEnv *env,
                                       const char *class_name,
                                       const char *fmt, va_list ap)
{
  jclass clazz;
  int n;
  char *msg=alloca(*sz);

#ifdef WIN32
  n = _vsnprintf(msg,*sz,fmt,ap);
#else
  va_list aq;
  va_copy(aq,ap);
  n = vsnprintf(msg,*sz,fmt,aq);
  va_end(aq);
#endif

  if (n < -1)
    { *sz *= 2; return; }

  if (n >= *sz)
    { *sz = n+1; return; }

  *sz = 0;

  clazz = (*env)->FindClass(env,class_name);

  if (!clazz) return;

  (*env)->ThrowNew(env,clazz,msg);
}

int jnixThrowException(JNIEnv *env,
                       const char *class_name,
                       const char *fmt, ...)
{
  int sz = 1024;

  do
    {
      va_list ap;
      va_start(ap,fmt);
      jnixThrowExceptionInternal(&sz,env,class_name,fmt,ap);
      va_end(ap);
    }
  while (sz);

  return -1;
}

int jnixThrowExceptionV(JNIEnv *env,
                        const char *class_name,
                        const char *fmt, va_list ap)
{
  int sz = 1024;

  do
    {
#ifdef WIN32
      jnixThrowExceptionInternal(&sz,env,class_name,fmt,ap);
#else
      va_list aq;
      va_copy(aq,ap);
      jnixThrowExceptionInternal(&sz,env,class_name,fmt,aq);
      va_end(aq);
#endif
    }
  while (sz);

  return -1;
}


static void jnixThrowExceptionInternalI(int *sz,
                                        JNIEnv *env,
                                        const char *class_name,
                                        int err,
                                        const char *fmt, va_list ap)
{
  int n;
  jclass clazz;
  jstring jmsg;
  jmethodID ctorID;
  jobject e;
  
  char *msg=alloca(*sz);

#ifdef WIN32
  n = _vsnprintf(msg,*sz,fmt,ap);
#else
  va_list aq;
  va_copy(aq,ap);
  n = vsnprintf(msg,*sz,fmt,aq);
  va_end(aq);
#endif

  if (n < -1)
    { *sz *= 2; return; }

  if (n >= *sz)
    { *sz = n+1; return; }

  *sz = 0;

  clazz = (*env)->FindClass(env,class_name);

  if (!clazz) return;

  ctorID = (*env)->GetMethodID(env,clazz,"<init>","(ILjava/lang/String;)V");

  if (!ctorID) return;

  jmsg = (*env)->NewStringUTF(env,msg);

  if (!jmsg) return;

  e = (*env)->NewObject(env,clazz,ctorID,
                                (jint)err,jmsg);

  if (!e) return;

  (*env)->Throw(env,e);
}

int jnixThrowExceptionI(JNIEnv *env,
                        const char *class_name,
                        int err,
                        const char *fmt, ...)
{
  int sz = 1024;

  do
    {
      va_list ap;
      va_start(ap,fmt);
      jnixThrowExceptionInternalI(&sz,env,class_name,err,fmt,ap);
      va_end(ap);
    }
  while (sz);

  return -1;
}

int jnixThrowExceptionIV(JNIEnv *env,
                         const char *class_name,
                         int err,
                         const char *fmt, va_list ap)
{
  int sz = 1024;

  do
    {
#ifdef WIN32
      jnixThrowExceptionInternalI(&sz,env,class_name,err,fmt,ap);
#else
      va_list aq;
      va_copy(aq,ap);
      jnixThrowExceptionInternalI(&sz,env,class_name,err,fmt,aq);
      va_end(aq);
#endif
    }
  while (sz);

  return -1;
}
