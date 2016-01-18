#include "NativeAES.h"
#include <stdlib.h>
#include "aes256.h"

JNIEXPORT jbyteArray JNICALL Java_cipher_NativeAES_aesCBCEnc(
	JNIEnv* env,
	jclass jcls,
	jbyteArray jkey,
	jbyteArray jiv,
	jbyteArray jdata)
	{
    aes256_context ctx;
    int i;
    jbyte * mykey = (*env)->GetByteArrayElements(env,jkey,0);
    jbyte * mydat = (*env)->GetByteArrayElements(env,jdata,0);
    jbyte * myiv  = (*env)->GetByteArrayElements(env,jiv,0);
    //perform data xor iv
    for(i=0; i< 16; i++)
    mydat[i]=mydat[i]^myiv[i];
    aes256_init(&ctx, mykey);
    aes256_encrypt_ecb(&ctx, mydat);
    jbyteArray jb =(*env)->NewByteArray(env, 16);
    (*env)->SetByteArrayRegion(env, jb, 0, 16, mydat);
    (*env)->ReleaseByteArrayElements(env,jkey, mykey, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,jiv, myiv, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,jdata, mydat, JNI_ABORT);
    return jb;
}

JNIEXPORT jbyteArray JNICALL Java_cipher_NativeAES_aesCBCDec (
    JNIEnv* env,
    jclass jcls,
    jbyteArray jkey,
    jbyteArray jiv,
    jbyteArray jdata)
{
    aes256_context ctx;
    int i = 0;
    jbyte * mykey = (*env)->GetByteArrayElements(env,jkey,0);
    jbyte * mydat = (*env)->GetByteArrayElements(env,jdata,0);
    jbyte * myiv  = (*env)->GetByteArrayElements(env,jiv,0);
    aes256_init(&ctx, mykey);
    aes256_decrypt_ecb(&ctx, mydat);
    for(i=0; i< 16; i++)
    mydat[i]=mydat[i]^myiv[i];
    jbyteArray jb =(*env)->NewByteArray(env, 16);
    (*env)->SetByteArrayRegion(env, jb, 0, 16, mydat);
    (*env)->ReleaseByteArrayElements(env,jkey, mykey, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,jiv, myiv, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env,jdata, mydat, JNI_ABORT);
    return jb;
}

JNIEXPORT jbyteArray JNICALL Java_cipher_NativeAES_aesECBEnc(
	JNIEnv* env,
	jclass jcls,
	jbyteArray jkey,
	jbyteArray jdata){

	aes256_context ctx;
	jbyte * key = (*env)->GetByteArrayElements(env, jkey,0);
	jbyte * data = (*env)->GetByteArrayElements(env, jdata, 0);

	//uint8_t key[32];

	aes256_init(&ctx,key);
	aes256_encrypt_ecb(&ctx, data);

	jbyteArray jresult = (*env)->NewByteArray(env, 16);
	(*env)->SetByteArrayRegion(env, jresult, 0, 16, data);
	(*env)->ReleaseByteArrayElements(env,jkey, key, JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,jdata, data, JNI_ABORT);
	return jresult;
}
JNIEXPORT jbyteArray JNICALL Java_cipher_NativeAES_aesECBDec(
	JNIEnv* env,
	jclass jcls,
	jbyteArray jkey,
	jbyteArray jdata){

	aes256_context ctx;

	jbyte * key = (*env)->GetByteArrayElements(env, jkey,0);
	jbyte * data = (*env)->GetByteArrayElements(env, jdata, 0);

	aes256_init(&ctx,key);
	aes256_decrypt_ecb(&ctx, data);

	jbyteArray jresult = (*env)->NewByteArray(env, 16);
	(*env)->SetByteArrayRegion(env, jresult, 0, 16, data);
	(*env)->ReleaseByteArrayElements(env,jkey, key, JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,jdata, data, JNI_ABORT);
	return jresult;

}

void printArray1(jbyte *z){
	for(int i = 0; i<16; i++){
		printf("%u ", z[i]);
	}
	printf("\n");
}

JNIEXPORT jbyteArray JNICALL Java_cipher_NativeAES_aesECBEncFile (
		JNIEnv* env,
		jclass jcls,
		jbyteArray jkey,
		jbyteArray jdata){
		aes256_context ctx;

		jbyte * key = (*env)->GetByteArrayElements(env, jkey,0);
		jbyte * data = (*env)->GetByteArrayElements(env, jdata, 0);
		aes256_init(&ctx,key);
		int datalen = (*env)->GetArrayLength(env, jdata);
		jbyte result[datalen];
		jbyte currentData[16];
		printf("%u ", datalen);
		for(int i = 0; i < datalen/16; i++){
			//memcpy(currentData, data + i*16, sizeof(char) * 16);
			aes256_encrypt_ecb(&ctx, data + i*16);
			//memcpy(result + i*16, currentData, sizeof(char) * 16);
			if(i == 42500){
				printf("%u \n", i);
			}
		}

		jbyteArray jresult = (*env)->NewByteArray(env, datalen);
		(*env)->SetByteArrayRegion(env, jresult, 0, datalen, data);
		(*env)->ReleaseByteArrayElements(env,jkey, key, JNI_ABORT);
		(*env)->ReleaseByteArrayElements(env,jdata, data, JNI_ABORT);
		return jresult;

}
