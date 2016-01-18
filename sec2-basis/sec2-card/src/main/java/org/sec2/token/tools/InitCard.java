/*
 * Copyright 2011 Sec2 Consortium
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://www.sec2.org
 */
package org.sec2.token.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.ServerKey;
import org.sec2.token.swtoken.SoftwareToken;

/**
 * Initialize Hardware Token.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Oct 2, 2013
 */
public class InitCard {

//    private static final String SERVER_CERT_PATH = "../../sec2-static-testdata/workspace/src/main/resources/certificates/sec2.server.sign.crt";
    private static final String SERVER_CERT_PATH = "../../sec2-static-testdata/src/main/resources/certificates/sec2.server.sign.crt";
    private static final byte[] PUK = TokenConstants.DEFAULT_PUK;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, CertificateException, TokenException {
        File keyFile = new File(SERVER_CERT_PATH);
        FileInputStream fileInputStream = new FileInputStream(keyFile);
        
        CertificateFactory certificateFactory = CertificateFactory.getInstance(
                "X509");
        X509Certificate cert = (X509Certificate) certificateFactory.
                generateCertificate(fileInputStream);

        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
        byte[] modulus = publicKey.getModulus().toByteArray();
        if(publicKey.getModulus().toByteArray().length % 8 == 1) {
            // strip off first byte
            System.out.println("Stripping off first byte: " + modulus[0]);
            modulus = Arrays.copyOfRange(modulus, 1, modulus.length);
            System.out.println("==> Modulus.length " + modulus.length);
        }
        
        ServerKey serverSignKey = new ServerKey(modulus,
                publicKey.getPublicExponent().toByteArray());
        
        SoftwareToken token = SoftwareToken.getInstance();               
//        token.connect();
        token.loginPUK(PUK);
        
        // load server key
        token.setServerKey(serverSignKey);
        
        // generate user keys
        token.generateUserKeys();
        
        token.logoutPUK();
//        token.save();
        token.disconnect();
    }
}
