package org.sec2.nativecrypto;

import junit.framework.TestCase;
/**
 * TestVektors from 
 * http://www.di-mgt.com.au/sha_testvectors.html
 * @author Dominik
 *
 */
public class NativeSHA1Test extends TestCase{

	public void testHashValueBlock1() throws Exception{
		String testVektor =
			"abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmno";
		byte result[] = {119, -119, -16, -55, -17, 123, -4, 64, -39, 51,
				17, 20, 61, -5, -26, -98, 32, 23, -11, -110};
		byte tmp[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		tmp = testVektor.getBytes();
		for(int i = 0; i < 16777216; i++){
			nativeSHA1.inputBlock(tmp);
		}
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
	
	public void testHashValueBlock2() throws Exception{
		String testVektor =
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		byte result[] = {52, -86, -105, 60, -44, -60, -38, -92, -10, 30,
				-21, 43, -37, -83, 39, 49, 101, 52, 1, 111};
		byte tmp[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		tmp = testVektor.getBytes();
		for(int i = 0; i < 15625; i++){
			nativeSHA1.inputBlock(tmp);
		}
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
	
	public void testHashValueBlock3() throws Exception{
		String testVektor = "";
		byte result[] = {-38, 57, -93, -18, 94, 107, 75, 13, 50, 85, -65,
				-17, -107, 96, 24, -112, -81, -40, 7, 9};
		byte tmp[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		tmp = testVektor.getBytes();
		nativeSHA1.inputBlock(tmp);
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
	
	public void testHashValueBlock4() throws Exception{
		String testVektor = "abcdefghbcdefghicdefghijdefghijkefghijklfghij" +
				"klmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrs" +
				"tnopqrstu";
		byte result[] = {-92, -101, 36, 70, -96, 44, 100, 91, -12, 25, -7, 
				-107, -74, 112, -111, 37, 58, 4, -94, 89};
		byte testValue[], tmp[] = new byte[64];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		testValue = testVektor.getBytes();
		int Anzahlbloecke = (int) Math.ceil(((double)testVektor.length() / 64));
		for(int i = 0; i < Anzahlbloecke; i++){
			if(i == Anzahlbloecke - 1){
				tmp = new byte[testVektor.length() % 64];
				System.arraycopy(testValue, i*64, tmp, 0, testVektor.length() % 64);
			}else{
				System.arraycopy(testValue, i*64, tmp, 0, 64);
			}
			nativeSHA1.inputBlock(tmp);
		}
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
		
	}
	
	public void testHashValueEntireData() throws Exception{
		String testVektor = "abcdefghbcdefghicdefghijdefghijkefghijklfghij" +
				"klmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrs" +
				"tnopqrstu";
		byte result[] = {-92, -101, 36, 70, -96, 44, 100, 91, -12, 25, -7, 
				-107, -74, 112, -111, 37, 58, 4, -94, 89};
		byte testValue[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		testValue = testVektor.getBytes();
		nativeSHA1.inputEntrieData(testValue);
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
	
	public void testHashValue5() throws Exception{
		String testVektor = 
				"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
		byte result[] = {-124, -104, 62, 68, 28, 59, -46, 110, -70, -82, 74, 
				-95, -7, 81, 41, -27, -27, 70, 112, -15};
		byte tmp[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		tmp = testVektor.getBytes();
		nativeSHA1.inputBlock(tmp);
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
	
	public void testHashValue6() throws Exception{
		String testVektor = "abc";
		byte result[] = {-87, -103, 62, 54, 71, 6, -127, 106, -70, 62, 37,
				113, 120, 80, -62, 108, -100, -48, -40, -99};
		byte tmp[];
		NativeSHA1 nativeSHA1 = new NativeSHA1();
		nativeSHA1.initialize();
		tmp = testVektor.getBytes();
		nativeSHA1.inputBlock(tmp);
		nativeSHA1.finish();
		for(int i = 0; i < 20; i++){
			if(result[i] != nativeSHA1.getHashValue()[i]){
				throw new Exception("Berechneter Hashwert stimmt nicht mit" +
						"Testvektor überein");
			}
		}
	}
}
