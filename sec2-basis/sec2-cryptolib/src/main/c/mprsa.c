/**
 * @file   mp.h
 * @author Dominik Preikschat
 * @date   Jan, 2013
 * @brief  Multi-precision-library
 *
 */

//#include <gmp.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <setjmp.h>
#include <math.h>
#include <time.h>
#include <stdint.h>
#include "mprsa.h"

const uint32_t one[1] = { 1 };
const uint32_t zero[1] = { 0 };

/**
 * Initialisiert eine neue MPZ
 * @param m MPZ die initialisiert werden soll
 */
void mp_init(MPZ *m) {
	m->allocated = 0;
	m->len = 0;
	m->sign = 0;
	m->array = NULL;
}

/**
 * Reserviert den Speicher für die MPZ m von der Länge len
 * @param m MPZ für den der Speicher reserviert werden soll
 * @param len Länge der MPZ
 */
void mp_allocate(MPZ * m, unsigned int len){
	if(m->allocated)
		mp_free(m);
	m->array = malloc(sizeof(uint32_t) * len);
	m->len = len;
	m->allocated = 1;
}
/**
 * Die Länge der MPZ wird angepasst. Führende Nullen werden somit vernachlässigt
 * @param m MPZ die angepasst werden soll
 */
void mp_truncate(MPZ *m){
	unsigned int mylen = m->len;
	while ((mylen > 0) && (m->array[mylen - 1] == 0)){
				mylen--;
		}
	m->len = mylen;
}

/**
 * Setzt die MPZ. Bevor das möglich ist, muss Speicher für die MPZ über
 * mp_allocate reserviert werden.
 * @param result Gibt die MPZ zurück.
 * @param array uint32_t Array mit den Zahlenwerten der MPZ
 * @param sign Vorzeichen der MPZ
 */
void mp_set(MPZ *result, uint32_t *array, BOOL sign){
	if(result->allocated != 1){
		return;
	}else{
		memcpy(result->array, array, result->len * sizeof(uint32_t));
		result->sign = sign;
	}
}

/**
 * Kopiert die src Variabel in die target Variabel
 * @param target Ziel-MPZ
 * @param src Quell-MPZ
 */
void mp_copy(MPZ * target, MPZ src) {
	if (target->array != src.array)
		mp_allocate(target, src.len);
		mp_set(target,src.array,src.sign);
}

/**
 * Gibt den Speicher der MPZ mp wieder frei
 * @param mp MPZ dessen Speicher freigegeben werden soll
 */
void mp_free(MPZ *m) {
	if (m->allocated == 1) {
		m->allocated = 0;
		free(m->array);
		m->array = NULL;
	}
}

/**
 * Vergleicht zwei MPZ Zahlen miteinander
 * @param a MPZ a
 * @param b MPZ b
 * @return int Gibt 1 für a > b, 2 b > a und 0 für a = b zurück
 */
unsigned int mp_compare(MPZ a, MPZ b){
	int i;
	int mxlen = a.len;
	if (a.sign > b.sign)
		return 1;
	if (b.sign > a.sign)
		return 2;
	if (a.len > b.len)
		return 2 - a.sign;
	if (a.len < b.len)
		return 1 + a.sign;

	for (i = mxlen - 1; i >= 0; i--) {
		if (a.array[i] > b.array[i])
			return 2 - a.sign;
		if (a.array[i] < b.array[i])
			return 1 + a.sign;
	}
	return 0;
}

//TODO löschen
void dump(MPZ a) {
	int i;
	if (a.sign == FALSE)
		printf("-");
	else
		printf("+");
	for (i = a.len - 1; i >= 0; i--)
		printf("%u ", a.array[i]);
}

/**
 * Addiert zwei MPZ miteinander
 * @param result beinhaltet das Ergebnis als MPZ
 * @param a MPZ a
 * @param b MPZ b
 */
void mp_add(MPZ *result, MPZ a, MPZ b){
	int mxlen, minlen, i;
	uint64_t uber = 0;
	/*
	 * Falls die MPZ unterschiedliche Vorzeichen haben, muss eine
	 * Subtraktion durchgeführt werden
	 */
	if (a.sign != b.sign) {
		if (a.sign == TRUE) {
			b.sign = TRUE;
			mp_subtract(result, a, b);
			b.sign = FALSE;
		} else {
			a.sign = TRUE;
			mp_subtract(result, b, a);
			a.sign = FALSE;
		}
		return;
	}

	if (a.len >= b.len){
		mxlen = a.len;
		minlen = b.len;
	} else {
		mxlen = b.len;
		minlen = a.len;
	}
	uint32_t target[mxlen + 1];
	target[mxlen] = 0;
	uint64_t tmp = 0;
	for (i = 0; i < minlen; i++){
		tmp = uber + ((uint64_t) a.array[i] + b.array[i]);
		target[i] = tmp;
		uber = tmp >> BASE_BIT;
	}
	/* Arrayfelder kopieren und Übertrag verrechnen */
	for (i = minlen; i < mxlen; i++) {
		if (a.len == minlen) {
			tmp = (uint64_t) b.array[i] + uber;
			target[i] = tmp;
			uber = tmp >> BASE_BIT;
		} else {
			tmp = (uint64_t) a.array[i] + uber;
			target[i] = tmp;
			uber = tmp >> BASE_BIT;
		}
	}
	/* Falls ein Übertrag vorhanden ist, muss dieser noch addiert werden */
	if (uber == 1) {
		target[mxlen] = uber;
		mp_allocate(result,mxlen +1);
		mp_set(result, target, a.sign);
	} else {
		target[mxlen] = uber;
		mp_allocate(result, mxlen);
		mp_set(result, target, a.sign);
	}
}

/**
 * Verschiebt die MPZ nach links um den Faktor der Base.
 * Die neuen Stellen werden auf 0 gesetzt. -> [a][a][a][0][0]...
 * @param result gibt die verschobene MPZ zurück
 * @param a MPZ die verschoben werden soll
 * @param baseToShift Anzahl um welchen Faktor der Base die
 * 		  Zahl a verschoben werden soll
 */
void mp_shiftBase(MPZ *result, MPZ a, int baseToShift){
	if (baseToShift == 0) {
		/*	Falls sich result und a unterscheiden muss a Kopiert werden */
		if (result->array != a.array)
			mp_copy(result, a);
		return;
	}
	if (baseToShift > 0) {
		/* Feld muss um den baseToShift-Wert vergrößert werden */
		uint32_t target[a.len + baseToShift];
		int i = 0;
		/* Setze alle niedrigen Stellen auf 0 */
		for (i = 0; i < baseToShift; i++) {
			target[i] = 0;
		}
		/* Kopiere a in target [a1][a0][0][0] */
		memcpy(&target[i], a.array, sizeof(uint32_t) * a.len);
		mp_allocate(result, a.len + baseToShift);
		mp_set(result, target, TRUE);
	} else if (baseToShift < 0) {
		uint32_t target[a.len + baseToShift];
		int i;
		for (i = 0; i <= a.len + baseToShift; i++) {
			target[i] = a.array[i + (-1) * baseToShift];
		}
		mp_allocate(result, a.len + baseToShift);
		mp_set(result, target, TRUE);
	} else {
		mp_allocate(result, a.len);
		mp_set(result, a.array,TRUE);
	}
}

/**
 * Schneidet die MPZ nach der Bitstelle n ab
 * (Die Stelle n bleibt im result vorhanden).
 * @param result gibt die abgeschnittene MPZ zurück
 * @param a MPZ die abgeschnitten werden soll
 * @param n An der Stelle n die MPZ abschneiden
 */
void mp_cutBit(MPZ * result, MPZ a, int n) {
	mp_copy(result, a);
	int newlen = n / BASE_BIT;
	if (n % BASE_BIT != 0) {
		newlen++;
		result->array[newlen - 1] = result->array[newlen - 1]
				% (uint32_t) (pow(2, n % BASE_BIT));
	}
	result->array = realloc(result->array, newlen * sizeof(uint32_t));
	result->len = newlen;
}

/**
 * Verschiebt die MPZ a um n bit nach rechts.
 * @param result gibt die verschobene MPZ zurück
 * @param a MPZ die verschoben werden soll
 * @param n Bit-Länge um die die Zahl a verschoben werden soll
 */
void mp_shiftRight(MPZ * result, MPZ a, int n) {
	int byteShiftRight = n >> BASE_EXPO_SIZE;
	int bitShiftRight = n & BASE_BIT_MOD;
	int len = 0, i = 0;
	uint32_t *array;
	if (bitShiftRight == 0) {
		len = a.len - byteShiftRight;
		array = malloc(len * sizeof(uint32_t));
		for (i = 0; i < len; i++) {
			array[i] = a.array[i + byteShiftRight];
		}
	} else {
		i = a.len - 1 - byteShiftRight;
		uint32_t highbit;
		highbit = a.array[a.len - 1] >> bitShiftRight;
		if (highbit != 0) {
			len = a.len - byteShiftRight;
			array = malloc(len * sizeof(uint32_t));
			array[i--] = highbit;
		} else {
			len = a.len - 1 - byteShiftRight;
			array = malloc(len * sizeof(uint32_t));
			i--;
		}
		int bitshift2 = BASE_BIT - bitShiftRight;
		int j = a.len - 1;
		while (j > byteShiftRight) {
			uint32_t t1 = a.array[j--] << bitshift2;
			uint32_t t2 = a.array[j] >> bitShiftRight;
			uint32_t t3 = t1 | t2;
			array[i--] = t3;
		}
	}
	mp_allocate(result, len);
	mp_set(result, array, a.sign);
	free(array);
}

/**
 * Multipliziert zwei MPZ-Zahlen miteinander und gibt das Ergebnis in result
 * zurück
 * @param result MPZ Ergebnis der Berechnung
 * @param a MPZ a
 * @param b MPZ b
 */
void mp_multiply(MPZ* result, MPZ a, MPZ b) {
	int i, j;
	uint64_t tmp;
	uint32_t w[a.len + b.len], c, u, v;

	for (i = 0; i < a.len + b.len; i++) {
		w[i] = 0;
	}

	for (i = 0; i < b.len; i++) {
		c = 0;
		for (j = 0; j < a.len; j++) {
			tmp = (uint64_t) a.array[j] * b.array[i];
			tmp += w[i + j];
			tmp += c;
			u = tmp >> BASE_BIT;
			v = tmp & BASE_MOD;
			w[i + j] = v;
			c = u;
		}
		w[i + a.len] = c;
	}
	if (a.sign == b.sign) {
		mp_allocate(result, a.len + b.len);
		mp_set(result, w, TRUE);
		mp_truncate(result);
	} else {
		mp_allocate(result, a.len + b.len);
		mp_set(result, w, FALSE);
		mp_truncate(result);
	}
}

/**
 * Subtrahiert b von a und gibt das Ergebnis in result zurück (a - b)
 * @param result Ergebnis der Subtraktion
 * @param a Minuend a
 * @param b Subtrahend b
 */
void mp_subtract(MPZ * result, MPZ a, MPZ b) {
	MPZ x, y;
	mp_init(&x);
	mp_init(&y);
	int sign;
	/* (Wenn b > a und (a oder b negativ)) oder
	 * (Wenn a >= b und a und b negativ) */
	if ((mp_compare(a, b) == 2 && (a.sign != FALSE || b.sign != FALSE))
			|| (a.sign == FALSE && b.sign == FALSE && mp_compare(a, b) != 2)) {
		mp_copy(&x, b);
		mp_copy(&y, a);
		/* Wenn beide Vorzeichen negativ sind, ist das Ergebnis positiv */
		if (a.sign == FALSE && b.sign == FALSE) {
			sign = TRUE;
		} else {
			sign = FALSE;
		}
	} else {
		mp_copy(&x, a);
		mp_copy(&y, b);
		/* Wenn beide Zahlen negativ sind, ist die Zahl auch negativ,
		 * da a > b */
		if (a.sign == FALSE && b.sign == FALSE) {
			sign = FALSE;
		} else {
			sign = TRUE;
		}
	}
	/* Bei (a - b) und b ist negativ ->  a + b */
	if (x.sign == TRUE && y.sign == FALSE) {
		y.sign = TRUE;
		mp_add(result, x, y);
		result->sign = sign;
		mp_free(&x);
		mp_free(&y);
		return;
	}

	uint32_t c = 0, i = 0;
	uint32_t w[x.len];

	for (i = 0; i < y.len; i++) {
		w[i] = (x.array[i] - y.array[i] - c);
		if ((uint64_t) x.array[i] >= (uint64_t) y.array[i] + c)
			c = 0;
		else
			c = 1;
	}
	for (; i < x.len; i++) {
		w[i] = (x.array[i] - c);
		if (x.array[i] >= c)
			c = 0;
		else
			c = 1;
	}
	mp_allocate(result, x.len);
	mp_set(result, w, sign);
	mp_free(&x);
	mp_free(&y);
}

/**
 * Verschiebt die MPZ a um n bit nach Links. Neue Bits werden 0 gesetzt.
 * @param result Ergebnis der Verschiebung von a um n Bits
 * @param a MPZ die verschoben werden soll
 * @param n Anzahl der Bit um die die Zahl a verschoben werden soll
 */
void mp_shiftLeft(MPZ* result, MPZ a, int n) {
	if (n == 0) {
		mp_copy(result, a);
		return;
	}
	int byteShiftLeft = n >> BASE_EXPO_SIZE;
	int bitShiftLeft = n & BASE_BIT_MOD;
	int len = 0, i = 0;
	uint32_t *array;
	if (bitShiftLeft == 0) {
		len = a.len + byteShiftLeft;
		array = malloc(len * sizeof(uint32_t));
		for (i = 0; i < a.len; i++) {
			array[i + byteShiftLeft] = a.array[i];
		}
	} else {
		i = a.len + byteShiftLeft;
		int bitshift2 = BASE_BIT - bitShiftLeft;
		uint32_t highBits = a.array[a.len - 1] >> bitShiftLeft;
		if (highBits != 0) {
			len = a.len + byteShiftLeft + 1;
			i = len - 1;
			array = malloc(len * sizeof(uint32_t));
			array[i--] = highBits;
		} else {
			len = a.len + byteShiftLeft;
			i = len - 1;
			array = malloc(len * sizeof(uint32_t));
		}
		int j = a.len - 1;
		while (j > 0) {
			uint32_t t1 = a.array[j--] << bitShiftLeft;
			uint32_t t2 = a.array[j] >> bitshift2;
			uint32_t t3 = t1 | t2;
			array[i--] = t3;
		}
		array[i] = a.array[j] << bitShiftLeft;
	}
	mp_allocate(result, len);
	mp_set(result, array, a.sign);
	free(array);
}

/**
 * Ermittelt die höchste Bit Position von t
 * @param t Zahl vo der die höchste Bit-Position ermittelt werden soll
 * @return gibt die Höchste Bit-Position von t zurück
 */
uint32_t getHighesBitPos(uint32_t t) {
	uint32_t tmp = t;
	uint32_t bits = 0;
	while (tmp != 0) {
		bits++;
		tmp = tmp >> 1;
	}
	return bits;
}

/**
 * Berechnet a modulus mod
 * @param result gibt das Ergebnis der Modulus-Rechnung als MPZ zurück
 * @param a MPZ die modulo gerechnet werden soll
 * @param mod MPZ modulus
 */
void mp_modulus(MPZ * result, MPZ a, MPZ mod) {
	MPZ tmp;
	mp_init(&tmp);
	mp_division(result, &tmp, a, mod);
	mp_free(&tmp);
}

/**
 * Dividiert die Zahl a durch b und gibt den Rest und den Faktor zurück.
 * @param rest gibt den Rest der Division als MPZ zurück
 * @param factor gibt den Faktor der Division als MPZ zurück
 * @param a Dividend MPZ
 * @param b Divisor MPZ
 */
void mp_division(MPZ * rest, MPZ * factor, MPZ a, MPZ b) {
	int compare = mp_compare(a, b);
	/* Es ist keine Berechnung erforderlich */
	if (compare == 0) {
		mp_allocate(rest, 1);
		mp_allocate(factor, 1);
		mp_set(rest, zero, TRUE);
		mp_set(factor, one, TRUE);
		return;
	}
	/* Wenn b > a keine Berechnung erforderlich */
	if (compare == 2) {
		mp_copy(rest, a);
		mp_allocate(factor, 1);
		mp_set(factor, zero,TRUE);
		return;
	}
	/* Wenn die Länge beider Zahlen = 1 ist kann die normale
	 * Division verwendet werden */
	if (a.len == 1 && b.len == 1) {
		uint32_t atmp[1],btmp[1];
		atmp[0] = a.array[0] % b.array[0];
		btmp[0] = a.array[0] / b.array[0];
		mp_allocate(rest, 1);
		mp_allocate(factor, 1);
		mp_set(rest, atmp, TRUE);
		mp_set(factor, btmp, TRUE);
		return;
	}

	MPZ x, y;
	mp_init(&x);
	mp_init(&y);
	int byteshifted = 0;
	/* a und b kopieren, damit original-Werte nicht geändert werden */
	mp_copy(&x, a);
	mp_copy(&y, b);
	/* Für die Division sind für x min 3 Stellen erforderlich */
	if (x.len < 3) {
		byteshifted = 3 - x.len;
		mp_shiftBase(&y, y, 3 - x.len);
		mp_shiftBase(&x, x, 3 - x.len);
	}
	/* Für die Division sind für y min 2 Stellen erforderlich */
	if (y.len < 2) {
		byteshifted += 2 - y.len;
		mp_shiftBase(&x, x, 2 - y.len);
		mp_shiftBase(&y, y, 2 - y.len);
	}

	MPZ tmp, tmp2;
	mp_init(&tmp);
	mp_init(&tmp2);

	int n = x.len - 1,
		t = y.len - 1,
		len_q = n - t + 1;
	uint32_t uint_q[len_q];
	int i;
	/* Alle Werte auf 0 setzen*/
	for (i = 0; i <= n - t; i++) {
		uint_q[i] = 0;
	}

	mp_copy(&tmp, y);
	mp_shiftBase(&tmp, tmp, n - t);
	while (mp_compare(x, tmp) != 2) {
		uint_q[n - t] += 1;
		mp_subtract(&x, x, tmp);
	}
	uint64_t temp, s1;
	uint32_t x2, x1, x0, s0, s2;
	for (i = n; i >= t + 1; i--) {
		/* Wenn x.len kleiner sein sollte als 3, müssen die X-Werte auf 0
		 * gesetzt werden */
		x2 = x.len > i ? x.array[i] : 0;
		x1 = x.len > i - 1 ? x.array[i - 1] : 0;
		x0 = x.len > i - 2 ? x.array[i - 2] : 0;

		if (x.array[i] == y.array[t]) {
			/* Base - 1 = BASE_MOD */
			uint_q[i - t - 1] = BASE_MOD;
		} else {
			uint_q[i - t - 1] = (((uint64_t) x2 << BASE_BIT) + x1) / y.array[t];
		}
		/* Berechnen von Zwischenwerten
		 * q*(yi * Base + y_(i-1))
		 * Werte werden nach und nach ermittelt, da hier eine 3 stellige
		 * Base-Zahl benötigt wird */
		temp = (uint64_t) uint_q[i - t - 1] * y.array[t - 1];
		s0 = temp & BASE_MOD;
		s1 = temp >> BASE_BIT;
		temp = (uint64_t) uint_q[i - t - 1] * y.array[t];
		s1 = s1 + (temp & BASE_MOD);
		s2 = (temp >> BASE_BIT) + (s1 >> BASE_BIT);
		s1 = s1 & BASE_MOD;
		/* q muss reduziert werden, falls s2 > x2 oder
		 * wenn s2 = x2 und (1. ->  s1 > x1 oder
		 * 2. s1 = x1 und s0 > x0)
		 * Eine Reduktion ist immer erforderlich wenn der zusammengesetzt y
		 * Wert größer als der zusammengesetzt x Wert ist.
		 */
		while ((s2 > x2)
				|| (s2 == x2 && ((s1 > x1) || ((s1 == x1) && (s0 > x0))))) {
			uint_q[i - t - 1]--;
			//Zwischenergebnisse neu berechnen
			temp = (uint64_t) uint_q[i - t - 1] * y.array[t - 1];
			s0 = temp & BASE_MOD;
			s1 = temp >> BASE_BIT;
			temp = (uint64_t) uint_q[i - t - 1] * y.array[t];
			s1 = s1 + (temp & BASE_MOD);
			s2 = (temp >> BASE_BIT) + (s1 >> BASE_BIT);
			s1 = s1 & BASE_MOD;
		}
		mp_copy(&tmp, y);
		uint32_t temp_q[1] = { uint_q[i - t - 1] };
		/* Faktor setzen */
		mp_allocate(&tmp2,  1);
		mp_set(&tmp2, temp_q, TRUE);
		/* Faktor * y nehmen */
		mp_multiply(&tmp, tmp2, tmp);
		/* An die Länge von x anpassen */
		mp_shiftBase(&tmp, tmp, i - t - 1);
		/* y von x Abziehen */
		mp_subtract(&x, x, tmp);
		/* Falls nach der Subtraktion der Wert negativ sein sollte muss der
		 * Wert angepasst werden  */
		if (x.sign == FALSE) {
			mp_copy(&tmp, y);
			mp_shiftBase(&tmp, tmp, i - t - 1);
			mp_add(&x, x, tmp);
			uint_q[i - t - 1]--;
		}
	}
	/* Um den richtigen Rest zu bekommen müssen die Shifts
	 * hier wieder rückgängig gemacht werden */
	if (byteshifted != 0)
		mp_shiftBase(&x, x, -1 * byteshifted);
	mp_copy(rest, x);
	mp_allocate(factor, len_q);
	mp_set(factor, uint_q, TRUE);
	mp_truncate(factor);
	mp_truncate(rest);
	mp_free(&x);
	mp_free(&y);
	mp_free(&tmp);
	mp_free(&tmp2);
}

/**
 * Berechnet das Inverse. Das Inverse kann auch Negativ sein.
 * @param s gibt das Inverse von a = 1 mod b als MPZ zurück
 * @param t gibt das Inverse von b = 1 mod a als MPZ zurück
 * @param a MPZ a
 * @param b MPZ b
 */
void mp_invert(MPZ * s, MPZ * t, MPZ a, MPZ b) {

	MPZ s1, t1, r, q, a1, b1;
	MPZ temp1, temp2, sresult, tresult, zz;
	mp_init(&s1);
	mp_init(&t1);
	mp_init(&r);
	mp_init(&q);
	mp_init(&a1);
	mp_init(&b1);
	mp_init(&temp1);
	mp_init(&temp2);
	mp_init(&sresult);
	mp_init(&tresult);
	mp_init(&zz);
	uint32_t z[1] = { 0 };
	uint32_t o[1] = { 1 };
	mp_copy(&a1, a);
	mp_copy(&b1, b);
	mp_allocate(&zz,1);
	mp_allocate(&s1, 1);
	mp_allocate(&t1,1);
	mp_allocate(&r, 1);
	mp_allocate(&q, 1);
	mp_allocate(&temp1, 1);
	mp_allocate(&temp2, 1);
	mp_allocate(&sresult, 1);
	mp_allocate(&tresult, 1);
	mp_set(&zz, z, TRUE);
	mp_set(&s1, o, TRUE);
	mp_set(&t1, z, TRUE);
	mp_set(&r, z, TRUE);
	mp_set(&q, z, TRUE);
	mp_set(&temp1, z, TRUE);
	mp_set(&temp2, z, TRUE);
	mp_set(&sresult, z, TRUE);
	mp_set(&tresult, o, TRUE);

	mp_division(&r, &q, a1, b1);
	int i = 0;
	do {
		i++;
		mp_multiply(&temp2, sresult, q);
		mp_subtract(&temp1, s1, temp2);
		mp_copy(&s1, sresult);
		mp_copy(&sresult, temp1);

		mp_multiply(&temp2, tresult, q);
		mp_subtract(&temp1, t1, temp2);
		mp_copy(&t1, tresult);
		mp_copy(&tresult, temp1);

		mp_copy(&a1, b1);
		mp_copy(&b1, r);
		mp_division(&r, &q, a1, b1);
	} while (mp_compare(r, zz) != 0);
	mp_copy(t, tresult);
	mp_copy(s, sresult);
	mp_free(&s1);
	mp_free(&t1);
	mp_free(&r);
	mp_free(&q);
	mp_free(&temp1);
	mp_free(&temp2);
	mp_free(&zz);
	mp_free(&a1);
	mp_free(&b1);
	mp_free(&tresult);
	mp_free(&sresult);
}

/**
 * Berechnet die Anzahl der Bits einer MPZ
 * @param m MPZ von der die Bit-Länge bestimmt werden soll
 * @return Bit-Länge der MPZ als Integer
 */
unsigned int mp_countBit(MPZ m) {
	int r = (m.len - 1) * BASE_BIT;
	r += getHighesBitPos(m.array[m.len - 1]);
	return r;
}

/**
 * Berechnet Werte für Montgomery. Hier wird das R sowie das Inverse von
 * dem Modulus berechnet, die für die Montgomery Reduktion benötigt werden
 * @param R Radix R
 * @param minverse ist das Inverse von -mod Modulus R
 * @param mod ist der Modulus
 */
void mp_calculate_montgomery_reduction_parameter(
	MPZ *R,
	MPZ *minverse,
	MPZ mod) {


	int n;
	MPZ mtemp, tmp;
	mp_init(&mtemp);
	mp_init(&tmp);
	// set R
	uint32_t r[1] = { 1 };
	mp_allocate(R, 1);
	mp_set(R, r, TRUE);
	n = mp_countBit(mod);
	mp_shiftBase(R, *R, n >> BASE_EXPO_SIZE);
	mp_shiftLeft(R, *R, n & BASE_BIT_MOD);

	//Inverse von m in R bilden (m') -> -m^-1 mod R
	mp_copy(&mtemp, mod);
	mtemp.sign = FALSE; // -m
	mp_add(&mtemp, mtemp, *R); // + R
	mp_invert(&tmp, minverse, *R, mtemp); // Berechne m'
	if (minverse->sign == FALSE)
		mp_add(minverse, *minverse, *R);
	mp_free(&mtemp);
	mp_free(&tmp);
}

/**
 * Führt einen Montgomery-Reduktion durch.
 * @param result Gibt den reduzierten Wert zurück
 * @param z Produkt der Multiplikation
 * @param R Radix R
 * @param m Modulus m
 * @param md das Inverse von -m mod R
 */
void mp_reduce_montgomery(MPZ * result, MPZ z, MPZ R, MPZ m, MPZ md) {
	MPZ U;
	mp_init(&U);
	mp_multiply(&U, z, md);

	MPZ t, tmp;
	mp_init(&t);
	mp_init(&tmp);
	mp_cutBit(&U, U, mp_countBit(R) - 1);

	mp_multiply(&U, U, m);
	mp_add(&t, z, U);

	mp_shiftRight(&t, t, mp_countBit(R) - 1);
	if (mp_compare(t, m) < 2) {
		mp_subtract(result, t, m);
	} else {
		mp_copy(result, t);
	}
	mp_free(&tmp);
	mp_free(&U);
	mp_free(&t);
}

//TODO löschen
void sdump(MPZ t, char * s) {
	printf("%s", s);
	dump(t);
	printf("\n");
}

/**
 * Erstellt eine Teil-MPZ aus der MPZ a. Es muss der Anfang und das Ende
 * angegeben werden.
 * @param result Ist das Ergebnis der Teil MPZ als MPZ. Besteht aus der MPZ a
 * von begin bis end.
 * @param a MPZ die unterteilt werden soll
 * @param begin Gibt den Anfang an, ab dem die MPZ kopiert werden soll
 * @param end Gibt das Ende an bis wohin die MPZ kopiert werden soll
 */
void mp_subMPZ(MPZ *result, MPZ a, int begin, int end) {
	if(begin > end || begin > a.len){
		result = NULL;
		return;
	}
	if(end >= a.len)
		end = a.len -1;
	uint32_t r[end - begin + 1];
	int i = 0;
	for (i = 0; i <= end - begin; i++) {
		r[i] = a.array[begin + i];
	}
	mp_allocate(result, end - begin + 1);
	mp_set(result, r, TRUE);
}

/**
 * Quadriert die MPZ a und gibt das Ergebnis in result zurück
 * @param result Ergebnis der Quadrierung als MPZ
 * @param a MPZ die Quadriert werden soll
 */
void mp_square(MPZ * result, MPZ a) {
	uint32_t c1 = 0, c2 = 0, u = 0, v = 0, q = 0, r = 0, s = 0;
	uint32_t w[2 * a.len + 1];
	int i = 0, j = 0;
	uint64_t tmp = 0, tmp1 = 0;
	for (i = 0; i < 2 * a.len; i++) {
		w[i] = 0;
	}
	for (i = 0; i < a.len; i++) {
		tmp = w[2 * i] + (uint64_t) a.array[i] * a.array[i];
		u = tmp >> BASE_BIT;
		v = tmp & BASE_MOD;
		w[2 * i] = v;
		c1 = u;
		c2 = 0;
		for (j = i + 1; j < a.len; j++) {
			q = 0;
			tmp1 = (uint64_t) a.array[i] * a.array[j];
			/* Falls tmp1 > Carry_bit wird muss der Übertrag behalten werden */
			if (tmp1 >= CARRY_BIT)
				q = 1;
			tmp = w[i + j] + (tmp1 << 1) + c1;
			/* Falls tmp < tmp1 hat hier ein Übertrag stattgefunden */
			if (tmp < tmp1)
				q = 1;
			u = tmp >> BASE_BIT;
			v = tmp & BASE_MOD;
			w[i + j] = v;
			tmp = ((uint64_t)u + c2);
			r = tmp >> BASE_BIT;
			s = tmp & BASE_MOD;
			c1 = s;
			c2 = q + r;
		}
		tmp = (uint64_t) (w[i + a.len] + c1);
		u = tmp >> BASE_BIT;
		v = tmp & BASE_MOD;
		w[i + a.len] = v;
		w[i + a.len + 1] = c2 + u;
	}
	mp_allocate(result, a.len * 2 + 1);
	mp_set(result, w, a.sign);
	mp_truncate(result);
}

/**
 * Konvertiert eine MPZ in die Binärdarstellung und gibt diese als MPZ zurück
 * @param result MPZ mit der Base 2
 * @param a MPZ die in die Base 2 umgewandelt werden soll
 */
void mp_convert_baseToBin(MPZ *result, MPZ a) {
	MPZ temp;
	mp_init(&temp);
	mp_copy(&temp, a);
	int i, j, k;
	uint32_t tmp[a.len * BASE_BIT];
	for (i = a.len; i > 0; i--) {
		for (j = 0, k = BASE_BIT; j < BASE_BIT; j++, k--) {
			tmp[i * BASE_BIT - (k)] = (temp.array[i - 1] >> j) & 1;
		}
	}
	mp_allocate(result, a.len * BASE_BIT);
	mp_set(result, tmp, TRUE);
	mp_free(&temp);
}

/**
 * Konvertiert ein uint32_t array mit der Base 2 in einen Integer-Wert
 * @param b uint32_t array mit der Base 2
 * @param len Länge des uint32_t arrays
 * @return Gibt den im uint32_t gespeicherten 2 Base-Wert als Integer wieder
 */
unsigned int convert_binToInt(uint32_t *b, int len) {
	int i, result = 0;
	for (i = 0; i < len; i++) {
		result += b[i] << i;
	}
	return result;
}

/**
 * Führt eine Exponentiation mit Montgomery-Reduktion durch. Hierbei kommt
 * der Sliding-Window Algorithmus zum Einsatz.
 * @param result Gibt das Ergebnis der Exponentiation als MPZ zurück
 * @param g Gibt die Basis als MPZ an
 * @param e Gibt den Exponenten als MPZ an
 * @param mod Gibt den Modulus als MPZ an
 * @param k Sliding Window Parameter der die maximale Anzahl der Shifts für den
 * Exponenten an. Dieser Wert sollte nicht größer als 7 und nicht kleiner als 3
 * gewählt werden. Des Weiteren finden 2^k Vorberechnungen statt.
 * @param minverse Montgomery Parameter der für die Reduktion benötigt wird. Es
 * wir hier das Inverse von -mod modulus R erwartet.
 * @param R Montgomery Parameter der für die Reduktion benötigt wird. Dieser
 * Wert muss immer größer als mod sein und ein vielfaches von 2^n.
 */
void mp_exponentiate(
	MPZ *result,
	MPZ g,
	MPZ e,
	MPZ mod,
	int k,
	MPZ minverse,
	MPZ R) {

	int anzahlgn = (int) (pow(2, k)) - 1;
	MPZ A, x;
	mp_init(&A);
	mp_init(&x);
	MPZ gn[2 * anzahlgn + 2], binaerE;
	mp_init(&binaerE);
	uint32_t expo[k];

	/* Basis-Wert kopieren und für die Montgomery Reduktion transformieren */
	mp_copy(&x, g);
	mp_multiply(&x, x, R); //x' bilden
	mp_modulus(&x, x, mod); // x'= x * R mod m

	int i = 0;
	/* Werte für die Vorberechnungen initialisieren */
	for (i = 0; i <= 2 * anzahlgn + 1; i++)
		mp_init(&gn[i]);
	/* Vorberechnungen für den Sliding Window Algorithmus durchführen */
	mp_allocate(&gn[0], 1);
	mp_set(&gn[0], one, TRUE);
	mp_copy(&gn[1], x);
	mp_square(&gn[2], x);
	mp_reduce_montgomery(&gn[2], gn[2], R, mod, minverse);
	for (i = 1; i < anzahlgn; i++) {
		mp_multiply(&gn[2 * i + 1], gn[2 * i - 1], gn[2]);
		mp_reduce_montgomery(&gn[2 * i + 1], gn[2 * i + 1], R, mod, minverse);
	}

	/* A = 1 initialisieren und in den Montgomery-Raum transformieren */
	mp_allocate(&A, 1);
	mp_set(&A, one, TRUE);
	mp_multiply(&A, A, R);
	mp_modulus(&A, A, mod);

	/* Länge des Exponenten bestimmen und anschließend den Exponenten in
	 * Base 2 umwandeln.
	 */
	i = mp_countBit(e) - 1;
	mp_convert_baseToBin(&binaerE, e);

	while (i >= 0) {
		if (binaerE.array[i] == 0) {
			mp_square(&A, A);
			mp_reduce_montgomery(&A, A, R, mod, minverse);
			i--;
		} else {
			int j;
			/* Suche der rechten Eins innerhalb von j-k
			 * 101000111 für k = 5 -> 101  */
			for (j = i; (i - j + 1 <= k) && binaerE.array[j] == 1; j--) {
				expo[i - j] = binaerE.array[j];
				mp_square(&A, A);
				mp_reduce_montgomery(&A, A, R, mod, minverse);
			}
			mp_multiply(&A, A, gn[convert_binToInt(expo, i - j)]);
			mp_reduce_montgomery(&A, A, R, mod, minverse);
			i = j;
		}
	}

	mp_reduce_montgomery(&A, A, R, mod, minverse);
	mp_copy(result, A);
	mp_truncate(result);
}
