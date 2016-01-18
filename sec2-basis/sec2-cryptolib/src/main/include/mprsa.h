/*
 * mp.h
 *
 *  Created on: 30.08.2012
 *      Author: Dominik
 */
#ifndef MP_H_
#define MP_H_
	#include <stdint.h>

/**
 * Gibt die Bit-Array-Größe der Base an (hier 32-bit)
 */
	#ifndef BASE_BIT
	#define BASE_BIT 32
	#endif
/**
 * Gibt den Modulo für das Array der Base an
 */
	#ifndef BASE_MOD
	#define BASE_MOD 0xFFFFFFFF
	#endif

/**
 * Gibt an ab wann ein Übertrag entsteht bei einer 64-bit Zahl, wenn diese
 * mit dem Faktor 2 multipliziert wird.
 */
	#ifndef CARRY_BIT
	#define CARRY_BIT 0x8000000000000000
	#endif

/**
 * Modulo für die Länge der Base (bei 32-bit -> ist das 31)
 */
	#ifndef BASE_BIT_MOD
	#define BASE_BIT_MOD 0x1f // Bit mod (2^8 -> Bit mod = 0x07)
	#endif

/**
 * Exponent der Base in der Darstellung 2^n (n = Exponent der Base). Bei
 * 32-bit = 5
 */
	#ifndef BASE_EXPO_SIZE
	#define BASE_EXPO_SIZE 5 // Length of the Exponent of the Base - 2^8 -> 8 = 2^3
	#endif

/**
 * Stellt den Wert "wahr" da.
 */
	#ifndef TRUE
	#define TRUE 1
	#endif
/**
 * Stellt den Wert "falsch" da.
 */
	#ifndef FALSE
	#define FALSE 0
	#endif

/**
 * Boolean definierenl
 */
	#define BOOL int

	/**
	 * MPZ Struktur.
	 */
	typedef struct mpz_struct{
		/** Länge der MPZ */
		int len;
		/** Zeigt an ob Speicher reserviert ist */
		int allocated;
		/** Unsigned Integer Array das die Zahlen der MPZ beinhaltet */
		uint32_t *array;
		/** Vorzeichen der MPZ */
		BOOL sign;
	}MPZ;

	void mp_allocate(MPZ * m, unsigned int len);
	void mp_add(MPZ *result, MPZ a, MPZ b);
	void mp_calculate_montgomery_reduction_parameter(MPZ *R, MPZ *minverse, MPZ mod);
	unsigned int mp_compare(MPZ a, MPZ b);
	void mp_copy(MPZ * target, MPZ src);
	void mp_division(MPZ * result, MPZ * factor, MPZ a, MPZ b);
	void mp_exponentiate(MPZ *result, MPZ g, MPZ e, MPZ mod, int k, MPZ minverse, MPZ R);
	void mp_free(MPZ *m);
	void mp_init(MPZ *m);
	void mp_modulus(MPZ * result, MPZ a, MPZ mod);
	void mp_multiply(MPZ* result, MPZ a, MPZ b);
	void mp_reduce_montgomery(MPZ * wt, MPZ z, MPZ R, MPZ m, MPZ md);
	void mp_set(MPZ *result, uint32_t *array, BOOL sign);
	void mp_square(MPZ * result, MPZ a);
	void mp_subtract(MPZ* result, MPZ a, MPZ b);
	void mp_truncate(MPZ *m);
	void dump(MPZ a);
	void sdump(MPZ t, char * s);

#endif /* MP_H_ */
