package org.sec2.backend.init;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;

import junit.framework.TestCase;

import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.statictestdata.TestKeyProvider;

import CryptoServerCXI.CryptoServerCXI;
import CryptoServerJCE.CryptoServerProvider;

/**
 * 
 * @author Thorsten Schreiber, NDS
 * @author Utimaco Safeware
 */
public class InitKryptoServerTest extends TestCase {

    
    private static TestKeyProvider keyProvider;
    
    final static String CONFIGURATION_FILE = "configuration.xml";

    private Properties loadProperties() {
        Properties properties = new Properties();

        try {
            properties.loadFromXML(getClass().getClassLoader()
                    .getResourceAsStream(CONFIGURATION_FILE));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        return properties;
    }
    
    
    public void testInitializeKeys() throws Exception {
        keyProvider = TestKeyProvider.getInstance();
        Properties configuration = ConfigurationFactory.createDefault();
        System.out.println("Connecting to HSM '"
                + configuration.getProperty("hsm.host") + "'");

        CryptoServerCXI cxi = new CryptoServerCXI(configuration
                .getProperty("hsm.host"), Integer.parseInt(configuration
                .getProperty("hsm.timeout")));

        cxi.logonPassword(configuration.getProperty("hsm.username"),
                configuration.getProperty("hsm.password"));

        /** import encapsulation key **/

        
        PrivateKey encapsulationKey = keyProvider.getKeyserverEncKey().getPrivate();
        PKCS8EncodedKeySpec encapsulationKeyPKCS8 = new PKCS8EncodedKeySpec(encapsulationKey.getEncoded());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey encapsulationCrtKey = (RSAPrivateCrtKey) kf.generatePrivate(encapsulationKeyPKCS8);        

        CryptoServerCXI.KeyAttributes attr = new CryptoServerCXI.KeyAttributes();
        attr.setAlgo(CryptoServerCXI.KEY_ALGO_RSA);
        attr.setSize(encapsulationCrtKey.getModulus().bitLength());
        attr.setExport(CryptoServerCXI.KEY_EXPORT_ALLOW_PLAIN);
        attr.setName(configuration
                .getProperty("keyserver.encapsulation.keyname"));

        System.out.println("Key should have the name '" + attr.getName() + "'");
        
        CryptoServerCXI.KeyComponents comp = new CryptoServerCXI.KeyComponents();
        comp.add(CryptoServerCXI.KeyComponents.TYPE_MOD, 
                encapsulationCrtKey.getModulus());
        System.out.println("Public modulus is " + encapsulationCrtKey.getModulus().toString(16));
        comp.add(CryptoServerCXI.KeyComponents.TYPE_PEXP, 
                encapsulationCrtKey.getPublicExponent());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_SEXP,
                encapsulationCrtKey.getPrivateExponent());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_P, 
                encapsulationCrtKey.getPrimeP());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_Q, 
                encapsulationCrtKey.getPrimeQ());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_DP, 
                encapsulationCrtKey.getPrimeExponentP());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_DQ, 
                encapsulationCrtKey.getPrimeExponentQ());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_U, 
                encapsulationCrtKey.getCrtCoefficient());
        cxi.importClearKey(CryptoServerCXI.FLAG_OVERWRITE,
                CryptoServerCXI.KEY_BLOB_SIMPLE, attr, comp);

        /** import signature key **/
        
       
        PrivateKey signatureKey = keyProvider.getKeyserverSignKey().getPrivate();
        PKCS8EncodedKeySpec signatureKeyPKCS8 = new PKCS8EncodedKeySpec(signatureKey.getEncoded());
        RSAPrivateCrtKey signatureCrtKey = (RSAPrivateCrtKey) kf.generatePrivate(signatureKeyPKCS8);
        attr = new CryptoServerCXI.KeyAttributes();
        attr.setAlgo(CryptoServerCXI.KEY_ALGO_RSA);
        attr.setSize(signatureCrtKey.getModulus().bitLength());
        attr.setExport(CryptoServerCXI.KEY_EXPORT_ALLOW_PLAIN);
        attr.setName(configuration
                .getProperty("keyserver.saml.signature.keyname"));

        System.out.println("Key should have the name '" + attr.getName() + "'");
        
        comp = new CryptoServerCXI.KeyComponents();
        comp.add(CryptoServerCXI.KeyComponents.TYPE_MOD, 
                signatureCrtKey.getModulus());
        System.out.println("Public modulus is " + signatureCrtKey.getModulus().toString(16));
        comp.add(CryptoServerCXI.KeyComponents.TYPE_PEXP, 
                signatureCrtKey.getPublicExponent());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_SEXP,
                signatureCrtKey.getPrivateExponent());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_P, 
                signatureCrtKey.getPrimeP());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_Q, 
                signatureCrtKey.getPrimeQ());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_DP, 
                signatureCrtKey.getPrimeExponentP());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_DQ, 
                signatureCrtKey.getPrimeExponentQ());
        comp.add(CryptoServerCXI.KeyComponents.TYPE_U, 
                signatureCrtKey.getCrtCoefficient());
        cxi.importClearKey(CryptoServerCXI.FLAG_OVERWRITE,
                CryptoServerCXI.KEY_BLOB_SIMPLE, attr, comp);

        System.out.println("All Keys should be imported to the HSM!");

        


    }

    private CryptoServerProvider getAuthenticatedSession(Properties p) {
        CryptoServerProvider cs = null;
        try {
            cs = new CryptoServerProvider(p.getProperty("hsm.host"), 5000);

            cs.loginPassword(p.getProperty("hsm.username"), p
                    .getProperty("hsm.password"));
            System.out.println("Opened Session" + cs.getName());

        }
        catch (Exception ex) {

            ex.printStackTrace();

            fail(ex.getMessage());

        }
        return cs;
    }

    public void testCryptoServerLogin() {

        System.out.println("Begin testCryptoServerLogin");
        CryptoServerProvider cs = getAuthenticatedSession(loadProperties());
        assertNotNull(cs);
        System.out.println("An Info from the Krypto Server" + cs.getInfo());
        try {
            cs.logoff();

        }
        catch (Exception ex) {

            ex.printStackTrace();

            fail(ex.getMessage());
        }

        System.out.println("End testCryptoServerLogin");

    };

//    /*
//     * This Test try to generate A Key on the Krypto Server.
//     */
//    public void testInitSignKey() {
//        Properties p = loadProperties();
//        CryptoServerProvider csp = getAuthenticatedSession(p);
//        try {
//
//            // Get The Content of KryptoSever as KeyStore
//            KeyStore ks = KeyStore.getInstance(p
//                    .getProperty("keyserver.provider"), csp);
//            ks.load(null, null);
//
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", csp);
//            kpg.initialize(2048, null);
//            KeyPair k = kpg.generateKeyPair();
//            ks.setKeyEntry(p.getProperty("keyserver.saml.signature.keyname"), k
//                    .getPrivate(), null, csp.getDumyCertificateChain());
//
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//
//            fail(ex.getMessage());
//        }
//        finally {
//            try {
//                csp.logoff();
//            }
//            catch (Exception ex) {
//                ex.printStackTrace();
//
//                fail(ex.getMessage());
//            }
//
//        }
//
//    }
}
