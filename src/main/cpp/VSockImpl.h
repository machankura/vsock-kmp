/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class machankura_vsockk_VSockImpl */

#ifndef _Included_machankura_vsockk_VSockImpl
#define _Included_machankura_vsockk_VSockImpl
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    socketCreate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_socketCreate
(JNIEnv *, jobject);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    connect
 * Signature: (Lfr/acinq/lightning/vsock/VSockAddress;)V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeConnect
(JNIEnv *, jobject, jobject);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeClose
(JNIEnv *, jobject);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    write
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeWrite
(JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    read
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_machankura_vsockk_VSockImpl_nativeRead
        (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    bind
 * Signature: (Lfr/acinq/lightning/vsock/VSockAddress;)V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeBind
(JNIEnv *, jobject, jobject);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    listen
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeListen
(JNIEnv *, jobject, jint);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    accept
 * Signature: (Lfr/acinq/lightning/vsock/native/VSockImpl;)V
 */
JNIEXPORT void JNICALL Java_machankura_vsockk_VSockImpl_nativeAccept
(JNIEnv *, jobject, jobject);

/*
 * Class:     machankura_vsockk_VSockImpl
 * Method:    getLocalCid
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_machankura_vsockk_VSockImpl_nativeGetLocalCid
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
