/*
 * NativeRSA.c
 *
 *  Created on: 12.03.2013
 *      Author: Dominik Preikschat
 */
#include "NativeRSA.h"
#include "mprsa.h"

void convertCharToMPZ(MPZ * result,int charlen,unsigned char *a);
void convertMPZtoChar(signed char *result,MPZ* a);

JNIEXPORT jbyteArray JNICALL Java_cipher_NativeRSA_NativeExponentiate
	(JNIEnv * env,
	jclass clas,
	jbyteArray jbasis,
	jbyteArray jexpo,
	jbyteArray jmod){

	jbyteArray jresult;
	/* MPZ -Parameter*/
	MPZ basis, mod, expo, result, R, minverse;
	mp_init(&basis);
	mp_init(&mod);
	mp_init(&expo);
	mp_init(&result);
	mp_init(&minverse);
	mp_init(&R);

	/* Länge der Byte-Arrays */
	int jbasisLen = (*env)->GetArrayLength(env,jbasis);
	int jexpoLen = (*env)->GetArrayLength(env, jexpo);
	int jmodLen = (*env)->GetArrayLength(env, jmod);
	/* char Arrays für die Umwandlung von jbyte -> char */
	unsigned char basisCharArray[jbasisLen];
	unsigned char expoCharArray[jexpoLen];
	unsigned char modCharArray[jmodLen];
	/* Auslesen der jByteArrays und speichern in CharArrays */
	(*env)->GetByteArrayRegion(env, jbasis, 0 , jbasisLen, basisCharArray);
	(*env)->GetByteArrayRegion(env, jexpo, 0 , jexpoLen, expoCharArray);
	(*env)->GetByteArrayRegion(env, jmod, 0 , jmodLen, modCharArray);
	/* Char-Array in MPZ-Zahlen umwandeln*/
	convertCharToMPZ(&basis, jbasisLen, basisCharArray);
	convertCharToMPZ(&mod, jmodLen, modCharArray);
	convertCharToMPZ(&expo, jexpoLen, expoCharArray);

	/* Berechnen der Werte in MPZ */
	mp_calculate_montgomery_reduction_parameter(&R, &minverse, mod);
	mp_exponentiate(&result,basis, expo, mod, 4, minverse, R);
	jresult = (*env)->NewByteArray(env, 4 * result.len);
	signed char charResultArray[result.len * 4];
	convertMPZtoChar(charResultArray, &result);
	(*env)->SetByteArrayRegion(env, jresult, 0, result.len * 4, charResultArray);

	return jresult;
}

void convertCharToMPZ(MPZ * result,int charlen,unsigned char *a){
	 int i = 0, uebertrag = 0;
	 int lenInt = charlen / 4;
	 if (charlen % 4 != 0) uebertrag++;
	 uint32_t array[lenInt + uebertrag];
	 for(i = 0; i < lenInt + uebertrag; i++) array[i] = 0;
	 mp_allocate(result, lenInt +uebertrag);
	 for(i = 0; i < lenInt; i++){
		 array[i] = a[i*4];
		 array[i] = array[i] | (a[i*4 + 1] << 8);
		 array[i] = array[i] | (a[i*4 + 2] << 16);
		 array[i] = array[i] | (a[i*4 + 3] << 24);
	 }
	 if(uebertrag == 1){
		 int tmp = charlen % 4;
		 for(i = 0; i < tmp; i++){
			 array[lenInt] = array[lenInt] | (a[(lenInt)*4 +i] << 8*i);
		 }
	 }
	 mp_set(result, array, TRUE);
}

void convertMPZtoChar(signed char *result,MPZ* a){
	int i = 0;
	for(i = 0; i < a->len; i++) result[i] = 0;
	for(i = 0; i < a->len; i++){
		result[i*4] = (a->array[i] & BYTEMOD);
		result[i*4 + 1] = (a->array[i] >> 8) & BYTEMOD;
		result[i*4 + 2] = (a->array[i] >> 16) & BYTEMOD;
		result[i*4 + 3] = (a->array[i] >> 24) & BYTEMOD;
	}
}


