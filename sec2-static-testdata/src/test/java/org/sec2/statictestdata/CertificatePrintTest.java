/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.statictestdata;

import static junit.framework.Assert.assertFalse;
import junit.framework.TestCase;

/**
 * Prints all keys and certificates in this package.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 25 2013
 */
public class CertificatePrintTest extends TestCase {

    /**
     * Prints all keys and certificates in this package.
     */
    public void testPrint() {
        TestKeyProvider prov = TestKeyProvider.getInstance();
        
        System.out.println("******* Keyserver *******");
        System.out.println("ID: " + prov.getKeyserverIDBase64());
        System.out.println("Signature Cert: " + prov.getKeyserverSignCert());
        System.out.println("Signature PublicKey: " + prov.getKeyserverSignKey().getPublic());
        System.out.println("Signature PrivateKey: " + prov.getKeyserverSignKey().getPrivate());
        System.out.println("Encryption Cert: " + prov.getKeyserverEncCert());
        System.out.println("Encryption PublicKey: " + prov.getKeyserverEncKey().getPublic());
        System.out.println("Encryption PrivateKey: " + prov.getKeyserverEncKey().getPrivate());
        
        System.out.println("******** User 1 ********");
        System.out.println("ID: " + prov.getUserIDBase64());
        System.out.println("Signature Cert: " + prov.getUserSignCert());
        System.out.println("Signature PublicKey: " + prov.getUserSignKey().getPublic());
        System.out.println("Signature PrivateKey: " + prov.getUserSignKey().getPrivate());
        System.out.println("Encryption Cert: " + prov.getUserEncCert());
        System.out.println("Encryption PublicKey: " + prov.getUserEncKey().getPublic());
        System.out.println("Encryption PrivateKey: " + prov.getUserEncKey().getPrivate());

        System.out.println("******** User 2 ********");
        System.out.println("ID: " + prov.getUser2IDBase64());
        System.out.println("Signature Cert: " + prov.getUser2SignCert());
        System.out.println("Signature PublicKey: " + prov.getUser2SignKey().getPublic());
        System.out.println("Signature PrivateKey: " + prov.getUser2SignKey().getPrivate());
        System.out.println("Encryption Cert: " + prov.getUser2EncCert());
        System.out.println("Encryption PublicKey: " + prov.getUser2EncKey().getPublic());
        System.out.println("Encryption PrivateKey: " + prov.getUser2EncKey().getPrivate());
        
        System.out.println("******** User 3 ********");
        System.out.println("ID: " + prov.getUser3IDBase64());
        System.out.println("Signature Cert: " + prov.getUser3SignCert());
        System.out.println("Signature PublicKey: " + prov.getUser3SignKey().getPublic());
        System.out.println("Signature PrivateKey: " + prov.getUser3SignKey().getPrivate());
        System.out.println("Encryption Cert: " + prov.getUser3EncCert());
        System.out.println("Encryption PublicKey: " + prov.getUser3EncKey().getPublic());
        System.out.println("Encryption PrivateKey: " + prov.getUser3EncKey().getPrivate());

        assertFalse(prov.getKeyserverSignKey().equals(prov.getUserSignKey()));
        assertFalse(prov.getKeyserverSignKey().equals(prov.getUser2SignKey()));
        assertFalse(prov.getKeyserverSignKey().equals(prov.getUser3SignKey()));
        
        assertFalse(prov.getUserSignKey().equals(prov.getUser2SignKey()));
        assertFalse(prov.getUserSignKey().equals(prov.getUser3SignKey()));
        
        assertFalse(prov.getUser2SignKey().equals(prov.getUser3SignKey()));


        assertFalse(prov.getKeyserverEncKey().equals(prov.getUserEncKey()));
        assertFalse(prov.getKeyserverEncKey().equals(prov.getUser2EncKey()));
        assertFalse(prov.getKeyserverEncKey().equals(prov.getUser3EncKey()));
        
        assertFalse(prov.getUserEncKey().equals(prov.getUser2EncKey()));
        assertFalse(prov.getUserEncKey().equals(prov.getUser3EncKey()));
        
        assertFalse(prov.getUser2EncKey().equals(prov.getUser3EncKey()));
   }
}
