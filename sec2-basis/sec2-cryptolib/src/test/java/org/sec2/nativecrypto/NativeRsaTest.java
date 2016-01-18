package org.sec2.nativecrypto;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import javax.crypto.Cipher;
import javax.swing.text.MaskFormatter;

import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.encodings.OAEPEncoding;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import junit.framework.TestCase;

public class NativeRsaTest extends TestCase {

	private NativeRSA nativeRSA;
	
	public void setUp(){
		Security.addProvider(new BouncyCastleProvider());
		if(this.nativeRSA == null){
			this.nativeRSA = new NativeRSA();
		}
	}
	
	public void testPKCS15SignatureWithSHA256() throws Exception{
		//Security.addProvider(new BouncyCastleProvider());
		/* Generate random massage */
		Random rnd = new Random();
		byte[] message = new byte[rnd.nextInt(1000)];
		rnd.nextBytes(message);
		
		Signature signer = Signature.getInstance("SHA256WithRSA", "BC");
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA" , "BC");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		PrivateKey pk = kp.getPrivate();
		PublicKey pubk = kp.getPublic();
		signer.initSign(pk);
		signer.update(message);
		byte sig []= signer.sign();
		RSAPublicKey rsaPub = (RSAPublicKey) pubk;
		RSAPrivateKey rsaPk = (RSAPrivateKey) pk;
		
		byte mod[] = convertBigIntegerToByte(rsaPub.getModulus(), true);
		byte expoPub [] = convertBigIntegerToByte(rsaPub.getPublicExponent(), true);
		byte expoPk [] = convertBigIntegerToByte(rsaPk.getPrivateExponent(), true);
		
		MessageDigest hasher = MessageDigest.getInstance("SHA-256","BC"); 
		hasher.update(message);
		byte[] hash = hasher.digest();
		hash = NativeRSA.changeBigToLittelEndian(hash);
		
		this.nativeRSA.setPadding(PaddingType.PADDING_OAEP);
		byte sig1[] = this.nativeRSA.sign(hash, expoPk, mod);
		
		sig = NativeRSA.changeBigToLittelEndian(sig);
		if (sig.length != sig1.length) fail("Signaturenlänge ist" +
				" unterschiedlich");
		for(int i = 0; i < sig1.length; i++){
			if(sig[i] != sig1[i]) fail("Signatur ist unterschiedlich !");
		}
		
		/* Verifizieren */
		if (this.nativeRSA.verify(sig1, hash, expoPub, mod) == false){
			fail("Signatur falsch!");
		}
	}
	
	public void testOAEPExponentation() throws Exception{
		byte message[] = new byte[32];
		this.nativeRSA.setPadding(PaddingType.PADDING_OAEP);
		
		Random rnd = new Random();
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA" , "BC");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		PrivateKey pk = kp.getPrivate();
		PublicKey pubk = kp.getPublic();
		RSAPublicKey rsaPub = (RSAPublicKey) pubk;
		RSAPrivateKey rsaPk = (RSAPrivateKey) pk;
		byte mod[] = convertBigIntegerToByte(rsaPub.getModulus(), true);
		byte expoPub [] = convertBigIntegerToByte(rsaPub.getPublicExponent(), true);
		byte expoPk [] = convertBigIntegerToByte(rsaPk.getPrivateExponent(), true);
		
		/* Erstelle aus der Nachricht einen OAEP-Cipherblock */
		Cipher rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
		rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPub);
		
		rnd.nextBytes(message);
		byte ciphertext[] = rsaCipher.doFinal(message);
		/* Konvertiere Ciphertext zu Little-Endian */
		byte testDecrypt[] = NativeRSA.changeBigToLittelEndian(ciphertext);	
		byte decResult [] = this.nativeRSA.decrypt(testDecrypt, expoPk,
				mod);
		/* Überprüfe ob die Entschlüsselte Nachricht, mit der orginal Nachricht
		 * übereinstimmt */
		if(message.length != decResult.length)
		for(int j = 0; j < message.length; j++){
			if(message[j] != decResult[j]) fail("Entschlüsselte Nachricht" +
					"stimmt nicht mit Orginal Nachricht überein ");
		}
		
		byte encResult[] = this.nativeRSA.encrypt(message, expoPub, mod);
		encResult = NativeRSA.changeLittleToBigEndian(encResult);
		rsaCipher.init(Cipher.DECRYPT_MODE, pk);
		byte plaintext [] = rsaCipher.doFinal(encResult);
		for(int j = 0; j < message.length; j++){
			if(message[j] != plaintext[j]) fail("Nachricht wurde nicht " +
					"richtig verschlüsselt!");
		}
		
	}
	
	public void testExponentation(){
		try {
			//Generierung von 2 prime mit 1024 bit
			BigInteger p = BigInteger.probablePrime(1023, new Random());
			BigInteger q = BigInteger.probablePrime(1023, new Random());
			//Berechnen des Modulos p*q
			BigInteger bMod = p.multiply(q);
			//Zufällige Base
			BigInteger bBase = new BigInteger(2048, new Random());
			//Zufälliger Exponent
			BigInteger bExp = new BigInteger(2048, new Random());
			//Mit BigInteger die Exponentation berechnen
			BigInteger r = bBase.modPow(bExp, bMod);
			
			byte bigIntegerResultByte[] =r.toByteArray();
			
			int cutlen = 0;
			/*Abschneiden der führenden "0"en, da diese nicht benötigt werden */
			for(int i = 0; i < bigIntegerResultByte.length;){
				if(bigIntegerResultByte[i] == 0){
					cutlen++;
					break;
				}else{
					break;
				}
			}
			/* Kopieren des bigInteger-Arrays abzüglich der führenden "0"en */
			byte[] byteresult = new byte[bigIntegerResultByte.length - cutlen];
			System.arraycopy(bigIntegerResultByte, cutlen, byteresult,
					0 , byteresult.length);
			/* Mit C-Lib berechen. Hier ist LittleEndian gefragt. Daher müssen
			 * alle Werte erst in LittelEndian umgewandelt werden*/
			this.nativeRSA.setPadding(PaddingType.PADDING_NONE);
			byte cresult[] = this.nativeRSA.decrypt(
					NativeRSA.changeBigToLittelEndian(bBase.toByteArray()),
					NativeRSA.changeBigToLittelEndian(bExp.toByteArray()), 
					NativeRSA.changeBigToLittelEndian(bMod.toByteArray()));
			/* Ergebnis aus der C-Lib in BigEndian umwandeln, damit das Ergebnis
			 * mit dem Ergebnis aus BigInteger verglichen werden kann */
			byte cresultBigEndian[] = NativeRSA.changeLittleToBigEndian(cresult);

			/* Vergleich der einzelnen Bytes */
			for(int i = 0; i < byteresult.length; i++){
				if(cresultBigEndian[i] != byteresult[i]){
					String errorMsg = "";
					fail("Exponentation nicht erfolgreich");
				}
			}
		} catch (Exception e) {
			fail(e.toString());
		}	
	}
	
	private byte[] convertBigIntegerToByte(
		BigInteger b, 
		boolean changeToLittleEndian){
		
		byte[] temp = b.toByteArray();
		int cutlen = 0;
		/*Abschneiden der führenden "0"en, da diese nicht benötigt werden */
		for(int i = 0; i < temp.length;){
			if(temp[i] == 0){
				cutlen++;
				break;
			}else{
				break;
			}
		}
		byte [] result = new byte[temp.length - cutlen];
		System.arraycopy(temp, cutlen, result,
				0 , result.length);
		if(changeToLittleEndian) 
			result =  NativeRSA.changeBigToLittelEndian(result);
		return result;
	}
}
