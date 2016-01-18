package org.sec2.nativecrypto;

import java.security.SecureRandom;

public final class NativeRSA {
	private static native byte[] NativeExponentiate(byte basis[], 
			byte exponent[], byte mod[]);
	private static final int SHA1_LENGTH_IN_BYTE = 20;
	private static final int SHA256_LENGTH_IN_BYTE= 32;
	/** DER-Signatur-String für die Signaturbildung benötigt (RFC 2313) **/
	private static final byte SHA256_DER_DIGESTINFO_VALUE [] = 
		{0x20, 0x04, 0x00, 0x05, 0x01, 0x02, 0x04,
		0x03, 0x65, 0x01, 0x48, (byte) 0x86, 0x60, 0x09, 0x06, 
		0x0D, 0x30, 0x31, 0x30};
	private PaddingType paddingType;
	private byte[] label = {};

	
	/**
	 * Erstellt ein neues Objekt
	 */
	public NativeRSA(){
		this.paddingType = PaddingType.PADDING_NONE;
	}

	/**
	 * Entschlüsselt die Base mit RSA - Byte Arrays müssen in LittleEndain sein
	 * @param base Ciphertext
	 * @param exponent Exponent des RSAs
	 * @param modulus Modulus des RSAs
	 * @return gibt den ursprünglichen Plaintext zurück
	 * @throws CryptoException gibt eine CryptoException zurück
	 */
	public byte[] decrypt(byte base[], byte exponent[], byte modulus[]) 
			throws CryptoException{
		if(this.paddingType == PaddingType.PADDING_NONE)
		{
			return NativeExponentiate(base, exponent, modulus);
		}else{
			byte temp[] = NativeExponentiate(base, exponent, modulus);
			byte em [] = new byte[modulus.length];
			temp = changeLittleToBigEndian(temp);
			System.arraycopy(temp,0 , em, modulus.length - temp.length, temp.length);
			return oaepDecoding(em, modulus);
		}
		
	}

	/**
	 * Verschlüsselt die Base mit RSA - Byte Arrays müssen in LittleEndain sein
	 * @param base Plaintext
	 * @param exponent Exponent des RSAs
	 * @param modulus Modulus des RSAs
	 * @return gibt den Ciphertext zurück
	 * @throws CryptoException falls Verschlüsselung nicht möglich
	 */
	public byte[] encrypt(byte base[], byte exponent[], byte modulus[]) 
			throws CryptoException{
		if(this.paddingType == PaddingType.PADDING_NONE)
		{
			return NativeExponentiate(base, exponent, modulus);
		}else{
			if(base.length > modulus.length - 2 * SHA1_LENGTH_IN_BYTE - 2){
				throw new CryptoException("Message too long");
			}
			byte em[] = oaepEncoding(base, modulus);
			em = changeBigToLittelEndian(em); /* Endian für Lib umstellen */
			return NativeExponentiate(em, exponent, modulus);
		}
	}
	
	/**
	 * Signiert die Übergebene Base - Byte Arrays müssen im LittleEndain 
	 * übergeben werden
	 * @param hash Hashwert über der die Signatur gebildet werden soll
	 * @param exponent Exponent des RSAs
	 * @param modulus Modulus des RSAs
	 * @return Gibt die Signatur zurück
	 * @throws CryptoException 
	 */
	public byte[] sign(byte hash[], byte exponent[], byte modulus[]) 
			throws CryptoException{
		if(hash.length != SHA256_LENGTH_IN_BYTE) 
			throw new CryptoException("Hash length too long!");
		if(this.paddingType == PaddingType.PADDING_NONE)
		{
			return NativeExponentiate(hash, exponent, modulus);
		}else{
			byte temp[] = encodePrivateKeyPKCS15(hash, modulus);
			return NativeExponentiate(temp, exponent, modulus);
		}
	}
	
	/**
	 * Überprüft die Signatur und liefert den Hashwert zurück - Werte müssen
	 * im Little Endian übergeben werden
	 * @param sig Signatur 
	 * @param hash Der Hashwert der mit der Signatur verglichen werden soll
	 * @param exponent Exponent des RSAs
	 * @param modulus Modulus des RSAs
	 * @return true - Signatur gültig, false -Signatur ungültig
	 * @throws CryptoException 
	 */
	public boolean verify(
		byte sig[],
		byte hash[] ,
		byte exponent[], 
		byte modulus[]) throws CryptoException{
		
		byte temp[];
		byte hashvalue [];
		if(this.paddingType == PaddingType.PADDING_NONE)
		{
			hashvalue = NativeExponentiate(sig, exponent, modulus);
			
		}else{
			temp = NativeExponentiate(sig, exponent, modulus);
			hashvalue = decodePublicKeyPKSC15(temp, modulus);
		}
		for(int i = 0; i < hashvalue.length; i++){
			if(hashvalue[i] != hash[i])
				return false;
		}
		return true;
	}
	
	/**
	 * Legt den Padding-Typ fest
	 * @param paddingType Padding-Typ
	 */
	public void setPadding(PaddingType paddingType){
		this.paddingType = paddingType;
	}
	
	/**
	 * Konvertiert Big zu Littel-Endian Format
	 * @param a Array das konvertiert werden soll
	 * @return konvertiertes Array
	 */
	public static byte[] changeBigToLittelEndian(byte[] a){
		int i = 0, j = a.length -1, blub = 0;
		byte tmpa[] = new byte[a.length-blub];
		System.arraycopy(a, blub, tmpa, 0, a.length-blub);
		byte result[] = new byte[tmpa.length];
		j = tmpa.length -1;
		byte tmp;
		for(i = 0; i <= j; i++){
			tmp = tmpa[i];
			result[i] = tmpa[j];
			result[j] = tmp;
			j--;
		}
		return result;
	}
	
	/**
	 * Konvertiert Little-Endian zu Big-Endian-Format
	 * @param a Array das konvertiert werden soll
	 * @return Konvertiertes Array
	 */
	public static byte[] changeLittleToBigEndian(byte[] a){
		int i =0, j = 0, blub = 0;
		for(i = a.length -1; i > 0; i--){
			if(a[i] == 0){
				blub++;
			}else{
					break;
			}
		}
		byte tmpa[] = new byte[a.length-blub];
		System.arraycopy(a, 0, tmpa, 0, a.length-blub);
		byte result[] = new byte[tmpa.length];
		j = 0;
		byte tmp;
		for(i = a.length-1 - blub; i >= j; i--){
			tmp = tmpa[i];
			result[i] = tmpa[j];
			result[j] = tmp;
			j++;
		}
		return result;
	}

	/**
	 * Dekodiert für die Signaturverifikation den Hash-String und gibt diesen
	 * zurück - Alle Arrays müssen in LittleEndian sein
	 * @param value - Entschlüsselter Wert der Dekodiert werden soll
	 * @param modulus - Modulus des RSA's
	 * @return Gibt den Hashwert der Signatur zurück
	 * @throws CryptoException 
	 */
	private byte[] decodePublicKeyPKSC15 (
		byte [] value, 
		byte [] modulus) throws CryptoException{
		
		byte result[] = new byte[SHA256_LENGTH_IN_BYTE];
		if(value[value.length - 1] != 0x00) throw new CryptoException("Wrong " +
				"Format");
		if(value[value.length - 2] != 0x01) throw new CryptoException("Wrong " +
				"Format");
		int i = 3;
		/* Suche den Anfang der Nachricht */
		while(value[value.length- i] == (byte) 0xFF){
			i++;
		}
		if(value[value.length - i] != 0x00) throw new CryptoException("Wrong " +
				"Format");
		i++;
		/* DER-Padding prüfen */
		for(int j = 0; j < SHA256_DER_DIGESTINFO_VALUE.length; j++){
			if(SHA256_DER_DIGESTINFO_VALUE[j] != 
						value[SHA256_LENGTH_IN_BYTE + j]){
				throw new CryptoException("Wrong Format");
			}
		}
		System.arraycopy(value, 0, result, 0, SHA256_LENGTH_IN_BYTE);
		return result;
	}
	
	/**
	 * Encodiert für die PKCS#1 v. 1.5 den Hashwert und gibt diesen zurück. 
	 * Alle Werte müssen im Little-Endian sein
	 * @param hash SHA256-Wert für den das Padding durchgeführt werden soll
	 * @param modulus Mod vom Key
	 * @return Gibt encodierten Wert zurück, der nun verschlüsselt werden muss.
	 */
	private byte[] encodePrivateKeyPKCS15(byte [] hash, byte [] modulus){
		
		byte[] eb = new byte[modulus.length];
		eb[modulus.length - 1] = 0; // Höchstes Byte auf 00 setzen
		/* Laut RFC 2313 - Sollte hier 0x01 nicht 0x00 verwendet werden */
		eb[modulus.length - 2] = 0x01;
		/* Padding mit 0xFF ausfüllen */
		for(int i = modulus.length -3; i > hash.length + 19 ; i--){
			eb[i] = (byte)0xFF;
		}
		//DER-Formatierung hinzufügen
		System.arraycopy(SHA256_DER_DIGESTINFO_VALUE, 0, eb, hash.length, 
				SHA256_DER_DIGESTINFO_VALUE.length);
		//Hashwert an das Ende der DER-Formatierung kopieren
		System.arraycopy(hash, 0, eb, 0, hash.length);
		return eb;
	}

	/**
	 * Padded die Base mit OAEP v2.1 - Werte müssen im Little-Endian übergeben
	 * werden
	 * @param base Wert der mit OAEP gepaddet werden soll
	 * @param modulus Modulus des RSAs
	 * @return gibt gepaddetes Array zurück
	 */
	private byte[] oaepEncoding(byte base[], byte modulus[]){
		// Schritt a - L Hashen
		NativeSHA1 sha = new NativeSHA1();	
		sha.initialize();	
		sha.inputEntrieData(label);
		sha.finish();
		byte[] shaValue = sha.getHashValue();
		// Schritt b - PS erstellen
		byte[] ps;
		int pslen = modulus.length - base.length - 2* SHA1_LENGTH_IN_BYTE - 2;
		//Schritt c - db erstellen
		byte db[] = new byte[modulus.length - SHA1_LENGTH_IN_BYTE -1];
		
		System.arraycopy(shaValue, 0, db, 0, SHA1_LENGTH_IN_BYTE);
		if (pslen >= 0){ 
			ps = new byte[pslen];
			System.arraycopy(ps, 0, db, SHA1_LENGTH_IN_BYTE, pslen);
		}
		db[20+pslen] = 0x01;
		System.arraycopy(base, 0, db, SHA1_LENGTH_IN_BYTE + pslen + 1, 
				base.length);
		//Schritt d - zufälligen octet String seed erstellen
		SecureRandom rnd = new SecureRandom();
		byte seed[] = new byte[SHA1_LENGTH_IN_BYTE]; 
		rnd.nextBytes(seed);		
		//Schritt e - dbMask
		byte dbMask[] = maskGenerationFunction(seed, modulus.length - 
				SHA1_LENGTH_IN_BYTE +1);
		//Schritt f - maskedDB
		byte maskedDB[] = byteXorArray(db, dbMask);
		//Schritt g - seedMask
		byte seedMask[] = maskGenerationFunction(maskedDB, SHA1_LENGTH_IN_BYTE);
		//Schritt h - maskedSeed
		byte maskedSeed[] = byteXorArray(seed, seedMask);
		//Schritt i - create EM
		byte em[] = new byte[1 + maskedSeed.length + maskedDB.length];
		em[0] = 0x00;
		System.arraycopy(maskedSeed, 0, em, 1, maskedSeed.length);
		System.arraycopy(maskedDB, 0, em, SHA1_LENGTH_IN_BYTE + 1,
				maskedDB.length);
		return em;
	}
	
	/**
	 *	Dekodiert die Value und gibt die ungepaddete Nachricht zurück
	 * @param value gepaddete Nachricht
	 * @param modulus Modulus des RSAs
	 * @return ungepaddete Nachricht
	 * @throws CryptoException gibt eine CryptoException zurück, falls die 
	 * Entschlüsselung nicht korrekt ist
	 */
	private byte[] oaepDecoding(byte value[], byte modulus[]) throws CryptoException{
		if(value.length != modulus.length)
			throw new CryptoException("decryption error");
		// Schritt a - L Hashen
		NativeSHA1 sha = new NativeSHA1();	
		sha.initialize();	
		sha.inputEntrieData(label);
		sha.finish();
		byte[] shaValue = sha.getHashValue();
		
		//Schritt B - Aufteilen der Nachricht
		byte y = value[0];
		byte maskedSeed[] = new byte[SHA1_LENGTH_IN_BYTE];
		byte maskedDB [] = new byte[modulus.length - SHA1_LENGTH_IN_BYTE - 1];
		System.arraycopy(value, 1, maskedSeed, 0, SHA1_LENGTH_IN_BYTE);
		System.arraycopy(value, SHA1_LENGTH_IN_BYTE +1 , maskedDB, 0, 
				modulus.length - (SHA1_LENGTH_IN_BYTE +1));
		//Schritt C - Seedmask erstellen
		byte seedMask[] = maskGenerationFunction(maskedDB, SHA1_LENGTH_IN_BYTE);
		//Schritt D - seed berechnen
		byte seed[] = byteXorArray(maskedSeed, seedMask);
		//Schritt E - dbMask berechnen
		byte dbMask[] = maskGenerationFunction(seed, 
				modulus.length - SHA1_LENGTH_IN_BYTE - 1);
		//Schritt F - DB berechnen
		byte DB[] = byteXorArray(maskedDB, dbMask);
		//Schritt G - DB aufteilen
		byte lHash [] = new byte[SHA1_LENGTH_IN_BYTE]; 
		System.arraycopy(DB, 0,lHash , 0, SHA1_LENGTH_IN_BYTE);
		//Bestimme die position an der die Message beginnt
		int pos = SHA1_LENGTH_IN_BYTE;
		while(DB[pos] != 0x01 && pos < DB.length) pos++;
		pos++;
		byte m[] = new byte[DB.length - pos];
		System.arraycopy(DB, pos, m, 0, DB.length - pos);
		boolean fail = false;
		boolean dummyfail = false;
		if (DB.length - pos == 0){
			fail = true;
		}else{
			dummyfail = false;
		}
		if(y != 0x00){
			fail = true;
		}else{
			dummyfail = false;
		}
		for(int i = 0; i < SHA1_LENGTH_IN_BYTE; i++){
			if(shaValue[i] != lHash[i]){
				fail = true;
			}else{
				dummyfail = false;
			}
		}
		
		if(fail == true){
			throw new CryptoException("decryption error");
		}
		return m;
	}
	
	private byte[] byteXorArray(byte[] a, byte[] b){
		byte result[] = new byte[a.length];
		for(int i = 0; i < a.length; i++){
			result[i] = (byte) (a[i] ^ b[i]);
		}
		return result;
	}
	
	/**
	 * maskGenerationFunction ist eine Mask Generation Funktion basierend auf 
	 * einer Hashfunktion (SHA1). Wird für OAEP-Padding benötigt
	 * @param mgfSeed seed von der die maske generiert wird
	 * @param maskLen Länge der Maske max. 2^32
	 * @return gibt den generierten Wert zurück
	 */
	private byte[] maskGenerationFunction(byte[] mgfSeed, long maskLen){
		int to = (int)Math.ceil((double)maskLen / 20);
		byte c[] = new byte[4];
		byte t[];
		if(maskLen % 20 == 0){
			t = new byte[to * 20 ];
		}else{
			t = new byte[(to - 1) * 20 + (int)(maskLen %20)];
		}
		
		byte temp[] = new byte[mgfSeed.length + 4];
		System.arraycopy(mgfSeed, 0, temp, 0, mgfSeed.length);
		NativeSHA1 sha1 = new NativeSHA1();
		for(int i = 0; i < to ; i++){
			c = I2OSP(i, 4);
			temp[mgfSeed.length] = c[3];
			temp[mgfSeed.length +1] = c[2];
			temp[mgfSeed.length +2] = c[1];
			temp[mgfSeed.length +3] = c[0];
			
			sha1.initialize();
			sha1.inputEntrieData(temp);
			sha1.finish();
			if(i < to -1 || (maskLen % 20 == 0)){
				System.arraycopy(sha1.getHashValue(), 0, t, 20*i, 20);
			}else{
				System.arraycopy(sha1.getHashValue(), 0, t, 20*i, 
						(int)(maskLen % 20));
			}
		}
		return t;
	}
	
	/**
	 * Konvertiert einen Integer-Wert in ein byte Array
	 * @param x Integerwert
	 * @param xLen Länge des Arrays, das zurückgegeben werden soll
	 * @return Gibt Byte Array mit dem Int-Wert x und mit der Länge xLen zurück
	 */
	private byte[] I2OSP(int x, int xLen){
		byte result [] = new byte[xLen];
		for (int i = 0; i < xLen; i++){
			result[i] = (byte) (x >> i*8) ;
		}
		return result;
	}
	
	static {
		System.loadLibrary("MPZ");
	}	
}
