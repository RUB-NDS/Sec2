/*
 * NativeGCM.c
 *
 *  Created on: 10.06.2013
 *      Author: Dominik
 */

#include "NativeGCM.h"


JNIEXPORT void JNICALL Java_cipher_NativeGCM_gcmEncrypt(
	JNIEnv* env,
	jclass jcls,
	jbyteArray jresult,
	jbyteArray jT,
	jbyteArray jkey,
	jbyteArray jdata,
	jbyteArray jiv,
	jbyteArray jaad){

	 jbyte * myresult = (*env)->GetByteArrayElements(env,jresult,0);
	 jbyte * mykey = (*env)->GetByteArrayElements(env, jkey, 0);
	 jbyte * mydata = (*env)->GetByteArrayElements(env, jdata, 0);
	 jbyte * myiv = (*env)->GetByteArrayElements(env, jiv,0);
	 jbyte * myaad = (*env)->GetByteArrayElements(env, jaad, 0);
	 jbyte * myT = (*env)->GetByteArrayElements(env, jT,0);
	 unsigned long long int dataLen = (*env)->GetArrayLength(env, jdata);
	 unsigned long long int ivLen = (*env)->GetArrayLength(env, jiv);
	 unsigned long long int aadLen = (*env)->GetArrayLength(env, jaad);
	 aesGcm_encrypt(myresult, myT, mykey, mydata, dataLen, myiv, ivLen, myaad, aadLen);
	 printf("MYT: ");
	 printHex(myT, 16);
	 //Ergebnisse zurück schreiben
	 (*env)->ReleaseByteArrayElements(env,jresult, myresult,0);
	 (*env)->ReleaseByteArrayElements(env,jT, myT, 0);
	 //Alle anderen Eingaben Verwerfen
	 (*env)->ReleaseByteArrayElements(env,jkey, mykey, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jdata, mydata, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jiv, myiv, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jaad, myaad, JNI_ABORT);
}


JNIEXPORT jboolean JNICALL Java_cipher_NativeGCM_gcmDecrypt(
	JNIEnv* env,
	jclass jcls,
	jbyteArray jresult,
	jbyteArray jT,
	jbyteArray jkey,
	jbyteArray jdata,
	jbyteArray jiv,
	jbyteArray jaad){

	 jbyte * myresult = (*env)->GetByteArrayElements(env,jresult,0);
	 jbyte * mykey = (*env)->GetByteArrayElements(env, jkey, 0);
	 jbyte * mydata = (*env)->GetByteArrayElements(env, jdata, 0);
	 jbyte * myiv = (*env)->GetByteArrayElements(env, jiv,0);
	 jbyte * myaad = (*env)->GetByteArrayElements(env, jaad, 0);
	 jbyte * myT = (*env)->GetByteArrayElements(env, jT,0);
	 unsigned long long int dataLen = (*env)->GetArrayLength(env, jdata);
	 unsigned long long int ivLen = (*env)->GetArrayLength(env, jiv);
	 unsigned long long int aadLen = (*env)->GetArrayLength(env, jaad);
	 short fail = 0;
	 aesGcm_decrypt(myresult, myT, mykey, mydata, dataLen, myiv, ivLen, myaad, aadLen, &fail);
	 printf("\nFail %u\n", fail);
	 //Ergebnisse zurück schreiben
	 (*env)->ReleaseByteArrayElements(env,jresult, myresult,0);
	 //Alle anderen Eingaben Verwerfen
	 (*env)->ReleaseByteArrayElements(env,jT, myT, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jkey, mykey, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jdata, mydata, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jiv, myiv, JNI_ABORT);
	 (*env)->ReleaseByteArrayElements(env,jaad, myaad, JNI_ABORT);
	 printf("fail: %u", fail);
	 jboolean jfail = fail;
	 return jfail;
}
