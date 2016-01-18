/**
 * @file   aesGcm.c
 * @author Dominik Preikschat
 * @date   Jun, 2013
 * @brief  AES-GCM Implementierung
 * Quelle für GCM-Impelentierung
 * http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf
 */
#include "aes256.h"
#include "aesGcm.h"
#include <string.h>
#include <math.h>
#include <stdio.h>


//#include <inttypes.h>
//ToDo "unsigned long long int" ersetzen durch uint32_t
aes256_context ctx;

/**
 * Wandelt einen uint64 in ein uint8 Array um
 * @param a uint8 Array
 * @param b uint64 Wert
 */
void convertUint64ToUint8(uint8_t *a, unsigned long long int b){
	a[0] = (b >> 56) & 0xFF;
	a[1] = (b >> 48) & 0xFF;
	a[2] = (b >> 40) & 0xFF;
	a[3] = (b >> 32) & 0xFF;
	a[4] = (b >> 24) & 0xFF;
	a[5] = (b >> 16) & 0xFF;
	a[6] = (b >> 8) & 0xFF;
	a[7] = b & 0xFF;
}

/**
 * Kopiert ein 16 Byte Array
 * @param result Kopie des 16 Byte Arrays
 * @param a Quelle
 */
void copy16ByteArray(uint8_t *result, uint8_t *a){
	for(int i = 0; i < 16; i++){
		result[i] = a[i];
	}
}
//TODO delete me
void printHex(uint8_t * a, int len){
	for(int i = 0;i < len; i++){
		printf("%02X", a[i]);
	}
	printf("\n");
}
//ToDo delete me
void printArray21(uint8_t *a, int len){
	printf("\n{");
	for(int i = 0; i < len; i++){
		printf("%u,", a[i]);
	}
}

/**
 * Shiftet ein 16 Byte-Array um 1 Bit nach Rechts
 * @param result Ergebnis Array
 * @param a	Quell Array
 */
void shiftRight(uint8_t result[16], uint8_t a[16]){
	uint8_t tmp = 0;
	uint8_t r[16];
	for(int i = 0; i < 16 ; i++){
		r[i] = a[i] >> 1;
		r[i] = r[i] | (tmp << 7);
		tmp = a[i] & 0x1;
	}
	for(int i = 0; i < 16; i++){
		result[i] = r[i];
	}
}

/**
 * Führt eine XOR Operation von Array a und b durch.
 * @param result gibt das Ergebnis zurück
 * @param a Array das mit b Array XORT wird
 * @param b Array das mit a Array XORT wird
 * @param byteLen Länge der beiden Arrays. Array's müssen die gleiche
 * Länge haben
 */
void xor(uint8_t * result ,uint8_t *a, uint8_t *b, int byteLen){
	for(int i = 0; i < byteLen; i++){
		result[i] = a[i] ^ b[i];
	}
}

/**
 * Multipliziert 2 Arrays miteinander.
 * @param result gibt das Ergebnis zurück
 * @param x Array das mit y multipliziert werden soll
 * @param y Array das mit x multipliziert werden soll
 */
void multiply(uint8_t *result,uint8_t * x, uint8_t * y){
	uint8_t Z[16];
	for(int i = 0; i < 16; i++) Z[i] = 0; // result muss mit 0 Initialisiert werden
	uint8_t V[16];
	uint8_t R = 0xE1; //R-Block 1110 0001 MSB-Block
	memcpy(V, y, sizeof(uint8_t) * 16);
	for (int i = 0; i < 128; i++){
		// xi == 1? TODO: Evt. verbessern
		if(((x[i/8] >> (7 -(i & 0x7))) & 0x1) == 1){
			xor(Z, Z, V, 16);
		}
		if(V[15] & 0x1){
			shiftRight(V,V);
			V[0] = V[0] ^ R;
		}else{
			shiftRight(V,V);
		}

	}
	memcpy(result, Z,sizeof(uint8_t) * 16);
}

/**
 * Berechnet den Ghash Wert von X. Durch den Parameter Y ist es möglich einen
 * bereits angefangen Hashwert weiter zu Berechnen. Das ist z.B. für die
 * Funktion S = GHASH(AAD || 0^v || C || 0^u || len(A) || len(C)) erforderlich.
 * Dazu ist es nicht nötig den gesamten String erneut zusammen zu bauen sondern
 * es wird einfach nurl Y = ghash(AAD || 0^v) und ghash(C || 0^u, Y) usw.
 * aufgerufen. Y = null -> es wird einfach nur der ghast von X berechenet.
 * @param result Hashergebnis 16 Byte groß
 * @param H H = CIPH(0 - Vektor)
 * @param X Blöcke die Gehashed werden sollen. Müssen abgeschlossene Blöcke sein
 * @param byteLenX Länge der Blöcke in Byte (gesamt)
 * @param Y Fall != NULL -> Wird der entsprechende Hashwert mit in die
 * Berechnung genommen. Es wird also der Hashwert weiterberechnet
 */
void ghash(
	uint8_t result[16],
	uint8_t H[16], uint8_t *X,
	unsigned long long int byteLenX,
	uint8_t Y[16]){

	if(Y != NULL){
		for(int i = 0; i < 16; i++) result[i] = Y[i];
	}else{
		for(int i = 0; i < 16; i++) result[i] = 0; //Init 0-Array
	}

	for(unsigned long long int i = 0; i < byteLenX >> 4; i++){
		if(byteLenX != 0){
			xor(result,result,X + (i << 4),16);
		}
		multiply(result,result,H);
	}

}

/**
 * Inkrementiert das Array
 * @param x Array das Inkrementiert werden soll
 */
void inc32(uint8_t x[16]){//15,14,13,12
	unsigned int tmp = x[15] | (x[14]<< 8) | (x[13] << 16) | (x[12] << 24);
	tmp++;
	x[15] = tmp;
	x[14] = tmp >> 8;
	x[13] = tmp >> 16;
	x[12] = tmp >> 24;
}

/**
 * GCTR Funktion
 * @param result verschlüsselte Daten
 * @param icb Initial Counter Block
 * @param X Datenarray
 * @param XByteLen Länger des X-Arrays in Byte
 */
void gctr(uint8_t *result ,uint8_t icb[16], uint8_t *X, unsigned long long int XByteLen){
	uint8_t tmp[16];
	if(XByteLen == 0 || X == NULL){
		result = NULL;
		return;
	}
	unsigned long long int n = 0;
	n = ceil(XByteLen/16.0); // len(X)/128 (bit len)
	uint8_t CB[16];
	copy16ByteArray(CB, icb);
	for(unsigned long long int i = 0; i < n-1; i++){
		copy16ByteArray(tmp, CB);
		aes256_encrypt_ecb(&ctx, tmp);
		xor(&result[i*16], &X[i*16], tmp, 16);
		inc32(CB);
	}

	copy16ByteArray(tmp, CB);
	aes256_encrypt_ecb(&ctx, tmp);
	int u = (16 -(XByteLen & 0xF)) & 0xF;
	if(u == 0){
		xor(&result[(n-1)*16], &X[(n-1)*16], tmp, 16);
	}else{
		xor(&result[(n-1)*16], &X[(n-1)*16], tmp, 16 - u);
	}

}

/**
 * Erstellt ein T-Token über den Ciphertext
 * @param T Authentifizierungs-Token
 * @param data Daten über das das Token gebildet werden soll (ciphertext)
 * @param dataLenByte Länge der Daten in Byte
 * @param aad zusätzliche authentifizierungsdaten
 * @param aadLenByte Länge des aad's in Byte
 * @param H Hash-Subkey
 * @param Y0 Pre-Counter-Block
 */
void aesGcm_create_T(
	uint8_t T[16],
	uint8_t *data,
	unsigned long long int dataLenByte,
	uint8_t *aad,
	unsigned long long int aadLenByte,
	uint8_t H[16],
	uint8_t Y0[16]){

	uint8_t S[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t temp[16];
	int v = 0;
	v = (16 - (aadLenByte & 0xF)) & 0xF; // v = fehlende Bytes für vollständigen letzten Block  von AAD

	if(v > 0){
		//AAD ist nicht vielfaches von Blocklänge
		/* Erst alle vollständigen Blöcke von ADD Hashen */
		ghash(S,H,aad, aadLenByte - (16 - v), NULL);
		/* ADD mit 0 Padden */
		for(int i = 0; i < (aadLenByte & 0xF); i++){
			temp[i] = aad[aadLenByte - (16 - v) + i];
		}
		for(int i = aadLenByte & 0xF; i < 16; i++){
			temp[i] = 0;
		}
		ghash(S,H,temp,16,S); //Hash weiterberechnen mit Padding
	}else{
		//AAD ist vielfaches von Blocklänge
		ghash(S,H,aad, aadLenByte, NULL);
	}

	int u = (16 -(dataLenByte & 0xF)) & 0xF; // u = fehlende Bytes für vollständigen letzten Block von C
		if(u > 0){
			/* Alle vollständigen Blöcke von C hashen (Weiterhashen) */
			ghash(S,H,data,dataLenByte - (16 - u), S); //Hash weiterberechnen
			/* Letzen Block padden um auf Blocklänge zu kommen */
			for(int i = 0; i < (dataLenByte & 0xF); i++){
				temp[i] = data[dataLenByte - (16 - u) + i];
			}
			for(int i = dataLenByte & 0xF; i < 16; i++){
				temp[i] = 0;
			}
			ghash(S,H,temp, 16, S); // Weiterhashen
		}else{
			ghash(S,H,data, dataLenByte, S);
		}
		uint8_t len[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		convertUint64ToUint8(len, aadLenByte << 3);
		convertUint64ToUint8(len + 8, dataLenByte << 3);
		ghash(S,H,len, 16, S);
		gctr(T,Y0, S,16);

}

/**
 * Initialisiert Y, Y0
 * @param H - Hash-Subkey
 * @param key - Key Block 32 Byte
 * @param Y Counter-Block
 * @param Y0 Pre-Counter-Block
 * @param iv Initialisierungsvektor
 * @param ivlenByte - Länge des Initialisierungsvektors
 */
void aesGcm_init(uint8_t H[16], uint8_t key[32], uint8_t Y[16], uint8_t Y0[16], uint8_t *iv, unsigned long long int ivlenByte){
	aes256_init(&ctx,key);
	aes256_encrypt_ecb(&ctx, H); // H = ciph(0^128)

	//Berechnung von Y
	int i = 0;
		if(ivlenByte == 12){
			for(i = 0; i < 12; i++){
				Y[i] = iv[i];
			}
			Y[15] = 1;
		}else{
			int sbyte = (128 * (ceil(((float)ivlenByte*8)/128))- ivlenByte*8)/8;

			uint8_t tmp[ivlenByte + sbyte + 16];
			for(i = 0; i < ivlenByte; i++){
				tmp[i] = iv[i];
			}
			for(i = ivlenByte; i < ivlenByte + sbyte + 8; i++){
				tmp[i] = 0;
			}
			unsigned long long int ivlenbit = ivlenByte * 8;
			tmp[ivlenByte + sbyte + 8] = ivlenbit >> 56;
			tmp[ivlenByte + sbyte + 9] = ivlenbit >> 48;
			tmp[ivlenByte + sbyte + 10] = ivlenbit >> 40;
			tmp[ivlenByte + sbyte + 11] = ivlenbit >> 32;
			tmp[ivlenByte + sbyte + 12] = ivlenbit >> 24;
			tmp[ivlenByte + sbyte + 13] = ivlenbit >> 16;
			tmp[ivlenByte + sbyte + 14] = ivlenbit >> 8;
			tmp[ivlenByte + sbyte + 15] = ivlenbit;
			ghash(Y, H,tmp,ivlenByte + sbyte + 16, NULL);

		}
		copy16ByteArray(Y0,Y);
}

/**
 * Verschlüsselt das Data-Array. Gibt den Ciphertext in result und das
 * Authentifizierungstoken in T zurück
 * @param result Ciphertext (hat die selbe Länge wie Data)
 * @param T Authentifizierungstoken 16 Byte
 * @param key Schlüssel für die Verschlüsselung
 * @param data Datenblöcke, die verschlüsselt werden sollen
 * @param dataLenByte Länge der Datenblöcke in Byte
 * @param iv Initialisierungsvektor
 * @param ivlenByte Länge des Initialisierungsvektors
 * @param aad zusätzliches Authentifizierungstoken
 * @param aadLenByte Länge des zusätzlichen Authentifizierungstokens
 */
void aesGcm_encrypt(
	uint8_t *result,
	uint8_t T[16],
	uint8_t key[32],
	uint8_t *data,
	unsigned long long int dataLenByte,
	uint8_t *iv,
	unsigned long long int ivlenByte,
	uint8_t *aad,
	unsigned long long int aadLenByte){
	uint8_t H[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t Y[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t Y0[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	//Initialisieren
	aesGcm_init(H, key, Y, Y0, iv, ivlenByte);
	//Daten Verschlüsseln
	inc32(Y);
	gctr(result,Y, data, dataLenByte);
	//Authenticated Token T erstellen
	aesGcm_create_T(T, result, dataLenByte, aad, aadLenByte, H, Y0);
	//Schritt 6 Berechnung des T-Tokens

}

/**
 *
 * @param result Plaintext (hat die selbe Länge wie cipher)
 * @param T Authentifizierungstoken (bei keiner Übereinstimmung wird fail = 1
 * gesetzt)
 * @param key Schlüssel zum Entschlüsseln
 * @param cipher Ciphertext
 * @param cipherLenByte Länge des Ciphertexts
 * @param iv Initialisierungsvektor
 * @param ivlenByte Länge des Initialisierungsvektors
 * @param aad erweiteres Authentifizierungstoken
 * @param aadLenByte Länge des aad's
 * @param fail 0 = erfolgreiches, 1 = fehlerhafts Token
 */
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
	short *fail){

	uint8_t H[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t Y[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t Y0[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	uint8_t tVector[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	*fail = 0;
	//Initialisieren
	aesGcm_init(H,key,Y,Y0,iv,ivlenByte);
	inc32(Y);
	gctr(result,Y, cipher, cipherLenByte);
	//Token erstellen
	aesGcm_create_T(tVector, cipher, cipherLenByte, aad,aadLenByte,H,Y0);
	//Token überprüfen
	for(int i = 0; i < 16; i++){
		if(tVector[i] != T[i]){
			*fail = 1;
		}
	}
}
