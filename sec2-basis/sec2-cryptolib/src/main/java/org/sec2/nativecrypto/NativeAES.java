package org.sec2.nativecrypto;
public final class NativeAES {
	private static native byte[] aesECBEnc(byte k[], byte data[]);
	private static native byte[] aesECBDec(byte k[], byte data[]);
	private static native byte[] aesCBCEnc(byte k[], byte iv[], byte data[]);
	private static native byte[] aesCBCDec(byte k[], byte iv[], byte data[]);
	private static native byte[] aesECBEncFile(byte k[], byte data[]);
	private native void gcmEncrypt(byte[] result, byte[] t, byte[] key, 
			byte[] data, byte[] iv, byte[] aad);
	private native boolean gcmDecrypt(byte[] result, byte[] t, byte[] key, 
			byte[] data, byte[] iv, byte[] aad);
	
	private transient byte key[];
	private byte iv[];
	private byte aad[];
	private byte t[];
	private AESCipherMode cipherMode = AESCipherMode.CIPHER_MODE_ECB;
	
	/**
	 * Erstellt ein neues Objekt
	 */
	public NativeAES(){	
	}
	
	/**
	 * Legt den Schlüssel fest
	 * @param key Schlüssel
	 * @throws Exception 
	 */
	public void setKey(byte key[]) throws Exception{
		if(key.length != 32){
			throw new Exception("Key Länge != 32.");
		}
		this.key = key;
	}
	
	/**
	 * Entschlüsselt Data - Achtung: Es muss vorher ein key angegeben werden
	 * @param data - Ciphertext
	 * @return Plaintext
	 */
	public byte[] decrypt(byte data[]){
		switch (this.cipherMode) {
		case CIPHER_MODE_ECB:
			return aesECBDec(this.key, data);
		case CIPHER_MODE_CBC:
			return aesCBCDec(this.key, this.iv, data);
		case CIPHER_MODE_GCM:
			byte[] result = new byte[data.length];
			boolean fail = gcmDecrypt(result, t, this.key, data, this.iv, this.aad);
			if(fail == true){
				//TODO fehler erstellen
				//throw new Exception("Token stimmt nicht überein. Entschlüsselung" +
				//		"nicht möglich");
				return null;
			}		
		return result;
		default:
			break;
		}
		return null;		
	}
	
	/**
	 * Verschlüsselt data - Achtung: Es muss vorher ein key gesetzt werden.
	 * @param data - Plaintext
	 * @return ciphertext
	 */
	public byte[] encrypt(byte data[]){
		switch (this.cipherMode) {
		case CIPHER_MODE_ECB:
			return aesECBEnc(this.key, data);
		case CIPHER_MODE_CBC:
			return aesCBCEnc(this.key, this.iv, data);
		case CIPHER_MODE_GCM:
			if(key == null){
				//TODO anpassen
				//throw new Exception("Es wurde kein Key angegeben!");
			}
			if(iv == null){
				//throw new Exception("Es wurde kein IV angegeben!");
			}
			byte[] result = new byte [data.length];
			t = new byte[16];
			gcmEncrypt(result, t, this.key, data, this.iv, this.aad);
			
			return result;
		case CIPHER_MODE_ECB_FILE:
			return aesECBEncFile(this.key, data);
		default:
			break;
		}
		return null;	
	}
	
	/**
	 * Legt den entsprechenden Verschlüsselungsmodus fest. Für jeden Modi gibt
	 * es unterschiedliche Parameter die gesetzt werden müssen
	 * @param cipherMode Verschlüsselungsmodus
	 */
	public void setMode(AESCipherMode cipherMode){
		this.cipherMode = cipherMode;
	}
		
	/**
	 * Legt den Initialisierungsvektor fest
	 * @param iv Initialisierungsvektor
	 */
	public void setIV(byte iv[]){
		this.iv = iv;
	}
	
	/**
	 * Legt den Authentifikationstoken fest. Ist nur für die Entschlüsselung
	 * von GCM erforderlich
	 * @param t
	 */
	public void setT(byte t[]){
		this.t = t;
	}
	
	/**
	 * Gibt den Authentifizierungstoken zurück. Wird nur bei GCM-Modus gesetzt.
	 * @return
	 */
	public byte[] getT(){
		return this.t;
	}
	
	/**
	 * Gibt die "Additional Authenticated Data" zurück. Ist nur für den Modi
	 * GCM erforderlich
	 * @return Additional Authenticated Data
	 */
	public byte[] getAAD(){
		return this.aad;
	}
	
	/**
	 * Setzt die Additional Authenticated Data. Ist nur für den Modi GCM 
	 * erforderlich.
	 * @param aad
	 */
	public void setAAD(byte[] aad){
		this.aad = aad;
	}
	
	static {
		System.loadLibrary("MPZ");
	}
}