package org.sec2.nativecrypto;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;


public class NativeAesTest extends TestCase{
	private NativeAES nativeAes;
	private Random random;
	
	public void testNativeGcmRandomValues() throws Exception{
		//1MB
		byte data[] = new byte[1048576];
		byte result[];
		byte testresult[];
		byte t[] = new byte[16];
		byte aad[] = new byte[45];
		byte iv[] = new byte [16];
		byte key[] = new byte [32];
		random.nextBytes(data);
		random.nextBytes(aad);
		random.nextBytes(iv);
		random.nextBytes(key);
		
		nativeAes.setMode(AESCipherMode.CIPHER_MODE_GCM);
		nativeAes.setAAD(aad);
		nativeAes.setIV(iv);
		nativeAes.setKey(key);
		result = nativeAes.encrypt(data);
		t = nativeAes.getT();
		nativeAes.setT(t);
		testresult = nativeAes.decrypt(result);
		
		for (int i = 0; i < testresult.length; i++){
			if(testresult[i] != data[i]){
				throw new Exception("Entschlüsselter Wert stimmt nicht " +
						"mit ursprünglichen überein!");
			}
		}
		
		
	}

	public void testNativeGcmTestCase() throws Exception{
		byte[] key = {-2, -1, -23, -110, -122, 101, 115, 28, 109, 106, -113, 
				-108, 103, 48, -125, 8, -2, -1, -23, -110, -122, 101, 115, 
				28, 109, 106, -113, -108, 103, 48, -125, 8};
		byte[] aad = {};
		byte[] data = {-39, 49, 50, 37, -8, -124, 6, -27, -91, 89, 9, -59,
				-81, -11, 38, -102, -122, -89, -87, 83, 21, 52, -9, -38, 46, 
				76, 48, 61, -118, 49, -118, 114, 28, 60, 12, -107, -107, 104, 
				9, 83, 47, -49, 14, 36, 73, -90, -75, 37, -79, 106, -19, -11, 
				-86, 13, -26, 87, -70, 99, 123, 57, 26, -81, -46, 85};
		byte[] iv = {-54, -2, -70, -66, -6, -50, -37, -83, -34, -54, -8, -120}; 
		byte[] enc = {82, 45, -63, -16, -103, 86, 125, 7, -12, 127, 55, -93,
				42, -124, 66, 125, 100, 58, -116, -36, -65, -27, -64, -55, 117,
				-104, -94, -67, 37, 85, -47, -86, -116, -80, -114, 72, 89, 13, 
				-69, 61, -89, -80, -117, 16, 86, -126, -120, 56, -59, -10, 30,
				99, -109, -70, 122, 10, -68, -55, -10, 98, -119, -128, 21,
				-83 };
		byte[] t = {-80, -108, -38, -59, -39, 52, 113, -67, -20, 26, 80, 34,
				112, -29, -52, 108}; ;
		byte[] cipher, cipherT, plain;
		nativeAes.setMode(AESCipherMode.CIPHER_MODE_GCM);
		nativeAes.setAAD(aad);
		nativeAes.setIV(iv);
		nativeAes.setKey(key);
		cipher = nativeAes.encrypt(data);
		cipherT = nativeAes.getT();
		for(int i = 0; i < cipher.length; i++){
			if(cipher[i] != enc[i]){
				throw new Exception("Verschlüsselung ist falsch!");
			}
		}
		for(int i = 0; i < cipherT.length; i++){
			if(cipherT[i] != t[i]){
				throw new Exception("Security-Token T stimmt nicht überein!");
			}
		}
		nativeAes.setT(cipherT);
		plain = nativeAes.decrypt(cipher);
		for(int i = 0; i < data.length; i++){
			if(plain[i] != data[i]){
				throw new Exception("Entschlüsselte Daten stimmen nicht" +
						" überein");
			}
		}
		
	}
	
	
	
	public void setUp(){
		if(this.nativeAes == null){
			 this.nativeAes =  new NativeAES();
		}
		if(this.random == null){
			this.random = new Random();
		}
	}
	
	public void testNativeAesECB() {
		byte key[] = new byte[32];
		byte ciphertext[] = {};
		byte plaintext[] = new byte[16];
		byte tmp[] = {};
		this.random.nextBytes(key);
		this.random.nextBytes(plaintext);
		try {
			this.nativeAes.setKey(key);
			this.nativeAes.setMode(AESCipherMode.CIPHER_MODE_ECB);
			ciphertext = this.nativeAes.encrypt(plaintext);
			tmp = this.nativeAes.decrypt(ciphertext);
			if(Arrays.equals(plaintext, tmp) == false){
				fail("Plaintexte stimmen nach Ver- Entschlüsselung nicht mehr"
						+" überein");
			}
			
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testNativeAesCBC(){
		byte key[] = new byte[32];
		byte iv[] = new byte[16];
		byte plaintext []= new byte[16];
		byte ciphertext[] = {};
		byte tmp[] = {};
		this.random.nextBytes(key);
		this.random.nextBytes(iv);
		this.random.nextBytes(plaintext);
		try {
			this.nativeAes.setKey(key);
			this.nativeAes.setMode(AESCipherMode.CIPHER_MODE_CBC);
			this.nativeAes.setIV(iv);
			ciphertext = this.nativeAes.encrypt(plaintext);
			tmp = this.nativeAes.decrypt(ciphertext);
			if(Arrays.equals(tmp, plaintext) == false){
				fail("Plaintexte stimmen nach Ver- Entschlüsselung nicht mehr"
						+" überein");
			}
			
		} catch (Exception e) {
			fail(e.toString());	
		}
	}
	
	public void testNativeAesCBCMonteCarlo(){
		byte iv[] = new byte[16];
		byte plaintext[] = new byte[16];
		byte key[] = new byte[32];
		byte ciphertext [] = {};
		byte tmpciphertext [] = {};
		byte resultIV[] = {-85,105,87,-62,-13,-45,96,89,62,-112,-106,-13,-93,
				-110,-89,1};
		byte resultCiphertext[] = {-64,-2,-1,-16,117,6,-96,-76,-51,123,-117,
				12,-14,93,54,100};
		byte resultPlaintext[] = {-91,-116,109,-58,49,37,13,122,-97,14,49,55,
				-82,86,64,42};
		byte resultKey[] = {61,-14,-65,19,-73,-1,-105,-54,19,86,122,-119,14,
				17,-55,121,111,-65,-42,-114,74,38,82,80,-82,87,27,4,112,15,
				33,59};
		byte tmpKey[] = {};
		byte tmpiv[] = {};
		byte tmpPlaintext[] = {};
		try{
			this.nativeAes.setMode(AESCipherMode.CIPHER_MODE_CBC);
			this.nativeAes.setKey(key);
			for (int i = 0; i < 400; i++){
				tmpKey = key;
				tmpiv = iv;
				tmpPlaintext = plaintext;
				for(int j = 0; j < 10000; j++){
					this.nativeAes.setIV(iv);
					tmpciphertext = ciphertext;
					ciphertext = this.nativeAes.encrypt(plaintext);
					if (j != 0){
						plaintext = tmpciphertext;
					}else{
						plaintext = iv;
					}
					iv = ciphertext;
				}
				key = generateKeyForMonteCarlo(key, tmpciphertext, ciphertext);
				plaintext = tmpciphertext;
				this.nativeAes.setKey(key);
			}
			if(Arrays.equals(tmpPlaintext, resultPlaintext) == false){ 
				fail("Plaintexte stimmen in der letzten Runde " +
						"nicht überein") ;
			}
			
			if(Arrays.equals(tmpKey, resultKey) == false){
				fail("Keys stimmen in der letzten Runde " +
						"nicht überein");
			}
			
			if(Arrays.equals(tmpiv, resultIV) == false){
				fail("IVs stimmen in der letzten Runde " +
						"nicht überein");
			}
			
			if(Arrays.equals(ciphertext, resultCiphertext) == false){
				fail("Ciphertexte stimmen in der letzten Runde " +
						"nicht überein");
			}
			
		}catch(Exception e){
			fail(e.toString());
		}
		
	}
	
	public void testNatvieAesECBMonteCarlo(){
		/*Test-Vektoren von 
		 * http://www.ntua.gr/cryptix/old/cryptix/aes/docs/katmct.html
		 */
		byte key [] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0};
		byte plaintext[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		byte ciphertext[] = {};
		byte inputBlock[] = {};
		byte tmpPlaintext[] = {};
		byte tmpKey[] = {};
		byte resultKey[] = {-104,45,97,122,15,115,115,66,-23,-111,35,-91,-91,
				115,-46,102,-12,-106,25,21,-77,45,-54,65,24,-83,92,-15,-36,-74,
				-19,0};
		byte resultPlaintext[] = {111,-122,6,-69,-90,-52,3,-91,-48,-90,79,
				-30,30,39,123,96};
		byte resultCiphertext[] = {31,103,99,-33,-128,122,126,112,
				-106,13,76,-45,17,-114,96,26};
		try {
			this.nativeAes.setMode(AESCipherMode.CIPHER_MODE_ECB);
			for (int i = 0; i < 400; i++){
				this.nativeAes.setKey(key);
				tmpPlaintext = plaintext;
				tmpKey = key;
				for(int j = 0; j < 10000; j++){
					inputBlock = plaintext;
					ciphertext = this.nativeAes.encrypt(inputBlock);
					plaintext =  ciphertext;
				}
				key = generateKeyForMonteCarlo(key, inputBlock, ciphertext);
			}
			if(Arrays.equals(tmpPlaintext, resultPlaintext) == false){ 
				fail("Plaintexte stimmen in der letzten Runde " +
						"nicht überein") ;
			}
			
			if(Arrays.equals(tmpKey, resultKey) == false){
				fail("Keys stimmen in der letzten Runde " +
						"nicht überein");
			}
			
			if(Arrays.equals(ciphertext, resultCiphertext) == false){
				fail("Ciphertexte stimmen in der letzten Runde " +
						"nicht überein");
			}
			
		} catch (Exception e) {
			fail(e.toString());	
		}
	}
	

	
	/**
	 * Berechnet den Key für die neue Runde mit dem AltenKey Xor ct9998||ct9999
	 * @param oldKey Key aus der vorherigen Runde
	 * @param ct9998 ciphertext aus der 9998 Runde
	 * @param ct9999 ciphertext aus der 9999 Runde
	 * @return neuer Key
	 */
	private byte[] generateKeyForMonteCarlo(byte oldKey[],
			byte ct9998[],
			byte ct9999[]){
		byte [] result = new byte[oldKey.length];
		for(int i = 0; i < 16; i++){
			result[i] = (byte) (oldKey[i] ^ ct9998[i]); 
		}
		for(int i = 16; i < 32; i++){
			result[i] = (byte) (oldKey[i] ^ ct9999[i - 16]); 
		}
		return result;
	}

}
