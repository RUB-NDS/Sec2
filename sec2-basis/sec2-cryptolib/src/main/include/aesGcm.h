/*
 * aesGcm.h
 *
 *  Created on: 22.04.2013
 *      Author: farin
 */

#ifndef AESGCM_H_
#define AESGCM_H_
//#include <stdint.h>

void aesGcm_encrypt(
		uint8_t * result,
		uint8_t T[16],
		uint8_t key[32],
		uint8_t *data,
		unsigned long long int dataLenByte,
		uint8_t *iv,
		unsigned long long int ivlenByte,
		uint8_t *aad,
		unsigned long long int aadLenByte);
void aesGcm_decrypt(
		uint8_t *result,
		uint8_t T[16],
		uint8_t key[32],
		uint8_t *cipher,
		unsigned long long int cipherLenByte,
		uint8_t *iv,
		unsigned long long int ivlenByte,
		uint8_t *aad,
		unsigned long long int aadLenByte,
		short *fail);

void xor(uint8_t * result ,uint8_t *a, uint8_t *b, int byteLen);
void multiply(uint8_t *result,uint8_t * X, uint8_t * Y);
uint8_t getLSBlittleEndian(uint8_t *a);

#endif /* AESGCM_H_ */
