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
#ifndef __JNIX_H__
#define __JNIX_H__ 1

#include <jni.h>

#ifndef __GNUC__
#ifdef _MSC_VER
#define __inline__ __inline
#else
#define __inline__ inline
#endif
#endif

#ifdef WIN32
# define JNIX_INTERNAL_API
# ifdef __GNUC__
#  define JNIX_INTERNAL_PRINTF_API(i) __attribute__((format(printf,i,i+1)))
# else
#  define JNIX_INTERNAL_PRINTF_API(i)
# endif
#else
# ifdef __GNUC__
#  define JNIX_INTERNAL_API __attribute__((visibility("hidden")))
#  define JNIX_INTERNAL_PRINTF_API(i) __attribute__((visibility("hidden"),format(printf,i,i+1)))
# else
#  define JNIX_INTERNAL_API
#  define JNIX_INTERNAL_PRINTF_API(i)
# endif
#endif

#define JNIX_INTERNAL_PRINTF_API3 JNIX_INTERNAL_PRINTF_API(3)
#define JNIX_INTERNAL_PRINTF_API4 JNIX_INTERNAL_PRINTF_API(4)

#ifdef __MINGW32__
#define JNIX_FUNC_NAME(a) _ ## a

JNIEXPORT jint JNICALL
JNIX_FUNC_NAME(JNI_OnLoad)(JavaVM *vm, void *reserved);

JNIEXPORT void JNICALL
JNIX_FUNC_NAME(JNI_OnUnload)(JavaVM *vm, void *reserved);

#else
#define JNIX_FUNC_NAME(a) a
#endif

#ifdef WIN32
#include<malloc.h>
#else
#include<alloca.h>
#endif

#include<string.h>

/*! Throw an exception of the given fully qualified class name
    using the given format and arguments. The formatted string
    is passed to the one-argument constructor of the exception,
    which take a String object as argument.

    A valid class name is e.g. \c "java/io/IOException".
*/
int  JNIX_INTERNAL_PRINTF_API3 jnixThrowException(JNIEnv *env,
                                                  const char *class_name,
                                                  const char *fmt, ...);

/*! Throw an exception of the given fully qualified class name
    using the given format and argument list. The formatted string
    is passed to the one-argument constructor of the exception,
    which take a String object as argument.

    A valid class name is e.g. \c "java/io/IOException".
*/
int JNIX_INTERNAL_API jnixThrowExceptionV(JNIEnv *env,
                                          const char *class_name,
                                          const char *fmt, va_list ap);

/*! Throw an exception of the given fully qualified class name
    using the given format and arguments. The gieven error code
    and the formatted string are passed to the two-argument constructor
     of the exception, which takes an int and a String object
     as arguments.

    A valid class name is e.g. \c "java/io/IOException".
*/
int  JNIX_INTERNAL_PRINTF_API4 jnixThrowExceptionI(JNIEnv *env,
                                                   const char *class_name,
                                                   int err,
                                                   const char *fmt, ...);

/*! Throw an exception of the given fully qualified class name
    using the given format and arguments. The gieven error code
    and the formatted string are passed to the two-argument constructor
     of the exception, which takes an int and a String object
     as arguments.

    A valid class name is e.g. \c "java/io/IOException".
*/
int JNIX_INTERNAL_API jnixThrowExceptionIV(JNIEnv *env,
                                           const char *class_name,
                                           int err,
                                           const char *fmt, va_list ap);

/*! Allocate a zero-terminated copy of the jByteArray \c ba on the
    stack. The address is stored in the char * variable \c cstr.

\verbatim 
    JNICALL JNIX_FUNC_NAME(Java_com_test_TestClass_write)(JNIEnv *env,jByteArray ba)
    {
        char * cstr;
        allocaCStringFromJByteArray(cstr,env,ba);

        fprintf(stderr,"cstr=%s.\n",cstr),
        ...
    }
\endverbatim
 */
#define allocaCStringFromJByteArray(cstr,env,ba) \
   do { jsize sz = (*env)->GetArrayLength(env,ba); \
   cstr = alloca(sz+1); \
   (*env)->GetByteArrayRegion(env,ba,0,sz,(jbyte*)cstr); \
   cstr[sz] = '\0'; } while(0)

/*! Allocate a copy of the jByteArray \c ba on the
    stack. The address is stored in the char * variable \c carr,
    thelength of the array in the jsize variable \c csz.

\verbatim 
    JNICALL JNIX_FUNC_NAME(Java_com_test_TestClass_write)(JNIEnv *env,jByteArray ba)
    {
        char * carr;
        jsize  carr_sz;      

        allocaCArrayFromJByteArray(carr,carr_sz,env,ba);

        write(1,carr,carr_sz);
        ...
    }
\endverbatim
 */
#define allocaCArrayFromJByteArray(carr,csz,env,ba) \
   do { csz = (*env)->GetArrayLength(env,ba); \
   carr = alloca(csz); \
   (*env)->GetByteArrayRegion(env,ba,0,csz,(jbyte*)carr); \
   } while(0)

/*! Allocate a copy of a subset of the jByteArray \c ba on the
    stack. The address is stored in the char * variable \c carr.

\verbatim 
    JNICALL JNIX_FUNC_NAME(Java_com_test_TestClass_write)(JNIEnv *env,
                                              jByteArray ba, jint off,jint len)
    {
        char * carr;

        allocaCArrayFromJByteArrayOffLen(carr,env,ba,off,len);

        write(1,carr,len);
        ...
    }
\endverbatim
 */
#define allocaCArrayFromJByteArrayOffLen(carr,env,ba,off,len) \
   do { carr = alloca(len); \
   (*env)->GetByteArrayRegion(env,ba,off,len,(jbyte*)carr); \
   } while(0)

/*! Allocate a new jByteArray, which is a copy of the given zero-terminated
    C String.
*/
static jbyteArray newJByteArrayFromCString(JNIEnv *env, const char *str);

__inline__ jbyteArray newJByteArrayFromCString(JNIEnv *env, const char *str)
{
    size_t l = strlen(str);
    jbyteArray ret = (*env)->NewByteArray(env,l);
   (*env)->SetByteArrayRegion(env,ret,0,l,(jbyte*)str);
    return ret;
}

/*! Copy the given zero-terminated C String (including the zero) to the given jbyteArray.
*/
static void copyCStringToJByteArray(JNIEnv *env, const char *str, jbyteArray ba);

__inline__ void copyCStringToJByteArray(JNIEnv *env, const char *str, jbyteArray ba)
{
    size_t l = strlen(str)+1;
    jsize ba_length = (*env)->GetArrayLength(env, ba);
    (*env)->SetByteArrayRegion(env,ba,0,(l <= ba_length-1 ? l : ba_length-1),(jbyte*)str);
}

#endif
