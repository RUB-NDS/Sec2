package org.sec2.backend.provider;

import CryptoServerAPI.CryptoServerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import CryptoServerJCE.CryptoServerProvider;

/**
 * 
 * 
 *
 */
public final class Sec2Provider extends Provider {

    private static final Logger LOGGER = Logger.getLogger(Sec2Provider.class
            .getName());
    {
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -3353889674967235920L;

    /**
	 * 
	 */
    private static String info = "JCA/JCE provider for Sec2 signatures and key encapsulation";

    /**
	 * 
	 */
    public static String PROVIDER_NAME = "Sec2";

    /**
	 * 
	 */
    private Properties configuration;

    /**
	 * 
	 */
    public Sec2Provider(Properties configuration) {
        super(PROVIDER_NAME, 1.0, info);
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.entering(Sec2Provider.class.getName(), "Sec2Provider");
        this.configuration = configuration;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                setup();
                return null;
            }
        });
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.exiting(Sec2Provider.class.getName(), "Sec2Provider");
    }

    /**
     * 
     */
    private void setup() {
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.entering(Sec2Provider.class.getName(), "setup");
        CryptoServerProvider cs = (CryptoServerProvider) Security
                .getProvider("CryptoServer");
        if (cs == null) {
            LOGGER
                    .fine("No CryptoServer provider found, generating new one...");
            try {
                String host = configuration.getProperty("hsm.host");
                int timeout = Integer.parseInt(configuration
                        .getProperty("hsm.timeout"));
                String username = configuration.getProperty("hsm.username");
                LOGGER.fine("Connecting to HSM '" + host + "' with a "
                        + timeout + "ms timeout...");
                cs = new CryptoServerProvider(host, timeout);
                LOGGER.fine("...connected successfully!");
                LOGGER.fine("Logging in as user '" + username + "'...");
                cs.loginPassword(username, configuration
                        .getProperty("hsm.password"));
                LOGGER.fine("...Login successful!");
                // keeps the session alive (polls HSM every once in a while)
                cs.getCryptoServer().setKeepSessionAlive(true);
                LOGGER.fine("Adding CryptoServer provider...");
                Security.addProvider(cs);
                LOGGER.fine("... provider added successful!");
            }
            catch (CryptoServerException e) {
                e.printStackTrace();
                throw new RuntimeException(
                        "CryptoServerException", e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(
                        "IOException", e);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new RuntimeException(
                        "NumberFormatException", e);
            }
        }

        putService(new SignatureService(this, "Signature", configuration
                .getProperty("keyserver.saml.signature.type"),
                "org.sec2.provider.Sec2Provider$Sec2SignatureSpi"));

        putService(new CipherService(this, "Cipher", configuration
                .getProperty("keyserver.encapsulation.type"),
                "org.sec2.provider.Sec2Provider$Sec2CipherSpi"));
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.exiting(Sec2Provider.class.getName(), "setup");
    }

    public Properties getConfiguration() {
        return this.configuration;
    }

    /**
     * Signature service
     */
    private static class CipherService extends Service {

        private Sec2Provider provider;

        /**
         * 
         * @param provider
         * @param type
         * @param algorithm
         * @param className
         */
        CipherService(Provider provider, String type, String algorithm,
                String className) {
            super(provider, type, algorithm, className, null, null);
            this.provider = (Sec2Provider) provider;
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(CipherService.class.getName(), "CipherService",
                        new Object[] { provider, type, algorithm, className });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(CipherService.class.getName(), "CipherService");
        }

        /**
		 * 
		 */
        public Object newInstance(Object param) throws NoSuchAlgorithmException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(CipherService.class.getName(), "newInstance",
                        param);
            Sec2CipherSpi sec2CipherSpi = new Sec2CipherSpi(provider
                    .getConfiguration());
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(CipherService.class.getName(), "newInstance",
                        sec2CipherSpi);
            return sec2CipherSpi;
        }

        /**
		 * 
		 */
        public boolean supportsParameter(Object obj) {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(CipherService.class.getName(),
                        "supportsParameter", obj);
            if (obj == null) {
                LOGGER.finer("Null parameter passed");
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.exiting(CipherService.class.getName(),
                            "supportsParameter", true);
                return true;
            }
            //System.out.println("supportsParameter: " + obj.toString());
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(CipherService.class.getName(),
                        "supportsParameter", true);
            return true;
        }
    }

    private static class Sec2CipherSpi extends CipherSpi {

        private Properties configuration;
        private Provider bouncyCastleProvider;
        private Key initKey;
        private int opmode;

        public Sec2CipherSpi(Properties configuration) {
            this.configuration = configuration;
            this.bouncyCastleProvider = new BouncyCastleProvider();
        }

        @Override
        protected byte[] engineDoFinal(byte[] input, int inputOffset,
                int inputLen) throws IllegalBlockSizeException,
                BadPaddingException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineDofinal");
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineDofinal");
            return null;
        }

        @Override
        protected int engineDoFinal(byte[] input, int inputOffset,
                int inputLen, byte[] output, int outputOffset)
                throws ShortBufferException, IllegalBlockSizeException,
                BadPaddingException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineDofinal");
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineDofinal");
            return 0;
        }

        @Override
        protected int engineGetBlockSize() {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(),
                        "engineGetBlockSize");
            // TODO Auto-generated method stub
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(),
                        "engineGetBlockSize");
            return 0;
        }

        @Override
        protected byte[] engineGetIV() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected int engineGetOutputSize(int inputLen) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        protected AlgorithmParameters engineGetParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineInit",
                        new Object[] { opmode, key, random });
            this.initKey = key;
            this.opmode = opmode;
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineInit");
        }

        @Override
        protected void engineInit(int opmode, Key key,
                AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineInit",
                        new Object[] { opmode, key, params, random });

            if (opmode != Cipher.UNWRAP_MODE && opmode != Cipher.WRAP_MODE) {
                // TODO: throw better exception
                throw new InvalidAlgorithmParameterException(
                        "Sec2 Cipher only supports WRAP and UNWRAP mode.");
            }

            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineInit");
        }

        @Override
        protected void engineInit(int opmode, Key key,
                AlgorithmParameters params, SecureRandom random)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineInit",
                        new Object[] { opmode, key, params, random });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineInit");
        }

        @Override
        protected void engineSetMode(String mode)
                throws NoSuchAlgorithmException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(), "engineSetMode",
                        new Object[] { mode });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(), "engineSetMode");
        }

        @Override
        protected void engineSetPadding(String padding)
                throws NoSuchPaddingException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2CipherSpi.class.getName(),
                        "engineSetPadding", new Object[] { padding });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2CipherSpi.class.getName(),
                        "engineSetPadding");
        }

        @Override
        protected byte[] engineUpdate(byte[] input, int inputOffset,
                int inputLen) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
                byte[] output, int outputOffset) throws ShortBufferException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        protected byte[] engineWrap(Key key) throws IllegalBlockSizeException,
                InvalidKeyException {
            byte[] wrappedKey = null;
            try {
                Cipher wrapCipher = Cipher.getInstance(configuration
                        .getProperty("keyserver.encapsulation.type"),
                        bouncyCastleProvider);
                wrapCipher.init(opmode, initKey);
                wrappedKey = wrapCipher.wrap(key);

            }
            catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return wrappedKey;
        }

        @Override
        protected Key engineUnwrap(byte[] wrappedKey,
                String wrappedKeyAlgorithm, int wrappedKeyType)
                throws InvalidKeyException, NoSuchAlgorithmException {
            byte[] unwrappedKeyRaw = null;
            Key unwrappedKey = null;
            try {

                    KeyStore ks = KeyStore.getInstance("CryptoServer", "CryptoServer");
                    ks.load(null, null);

                    PrivateKey keyserverKey = (PrivateKey) ks.getKey(configuration
                            .getProperty("keyserver.encapsulation.keyname"), null);
                    Cipher decapsulator = Cipher.getInstance(configuration
                            .getProperty("keyserver.encapsulation.type.internal"), "CryptoServer");
                    decapsulator.init(Cipher.DECRYPT_MODE, keyserverKey,
                            OAEPParameterSpec.DEFAULT);
                    unwrappedKeyRaw = decapsulator.doFinal(wrappedKey);
                    SecretKeySpec sks = new SecretKeySpec(unwrappedKeyRaw, "AES");
                    SecretKeyFactory sksf = SecretKeyFactory.getInstance("AES");
                    unwrappedKey = sksf.generateSecret(sks);

            }
            catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (NoSuchProviderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InvalidAlgorithmParameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (CertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InvalidKeySpecException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return unwrappedKey;

        }
    }

    /**
     * Signature service
     */
    private static class SignatureService extends Service {

        private Sec2Provider provider;

        /**
         * 
         * @param provider
         * @param type
         * @param algorithm
         * @param className
         */
        SignatureService(Provider provider, String type, String algorithm,
                String className) {
            super(provider, type, algorithm, className, null, null);
            this.provider = (Sec2Provider) provider;
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(SignatureService.class.getName(),
                        "SignatureService", new Object[] { provider, type,
                                algorithm, className });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(SignatureService.class.getName(),
                        "SignatureService");
        }

        /**
		 * 
		 */
        public Object newInstance(Object param) throws NoSuchAlgorithmException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(SignatureService.class.getName(),
                        "newInstance", param);
            Sec2SignatureSpi sec2SignatureSpi = new Sec2SignatureSpi(provider
                    .getConfiguration());
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(SignatureService.class.getName(), "newInstance",
                        sec2SignatureSpi);
            return sec2SignatureSpi;
        }

        /**
		 * 
		 */
        public boolean supportsParameter(Object obj) {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(SignatureService.class.getName(),
                        "supportsParameter", obj);
            if (obj == null) {
                LOGGER.finer("Null parameter passed");
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.exiting(SignatureService.class.getName(),
                            "supportsParameter", true);
                return true;
            }
            //System.out.println("supportsParameter: " + obj.toString());
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(SignatureService.class.getName(),
                        "supportsParameter", true);
            return true;
        }
    }

    /**
	 * 
	 */
    private static class Sec2SignatureSpi extends SignatureSpi {

        public static final int MODE_SIGN = 1;
        public static final int MODE_VERIFY = 2;
        public static final int MODE_UNKNOWN = 0;

        private ByteArrayOutputStream bos;
        private int mode;
        private PublicKey userSignPublicKey;
        private Provider bouncyCastleProvider;
        private Properties configuration;

        /**
         * @param properties
         * 
         */
        public Sec2SignatureSpi(Properties configuration) {
            this.configuration = configuration;
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "Sec2SignatureSpi");
            this.bos = new ByteArrayOutputStream();
            this.mode = MODE_UNKNOWN;
            this.bouncyCastleProvider = new BouncyCastleProvider();
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                        "Sec2SignatureSpi");
        }

        @Override
        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineGetParameter", param);
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                        "engineGetParameter", null);
            return null;
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineInitSign", privateKey);
            this.mode = MODE_SIGN;
            this.bos.reset();
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                        "engineInitSign");
        }

        @Override
        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineInitVerify", publicKey);
            this.mode = MODE_VERIFY;
            this.bos.reset();
            this.userSignPublicKey = publicKey;
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                        "engineInitVerify");
        }

        @Override
        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineSetParameter", new Object[] { param, value });
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                        "engineSetParameter");
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(), "engineSign");
            if (mode == MODE_UNKNOWN || mode == MODE_VERIFY)
                throw new SignatureException("Not initialized for signing.");
            byte[] data = bos.toByteArray();
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Signing: " + DatatypeConverter.printHexBinary(data));
            }
            byte[] signature = null;
            // Start signing with key in HSM
            try {
                KeyStore ks = KeyStore.getInstance("CryptoServer",
                        "CryptoServer");
                ks.load(null, null);
                PrivateKey privateKey = (PrivateKey) ks.getKey(configuration
                        .getProperty("keyserver.saml.signature.keyname"), null);
                
                
                Signature signer = Signature.getInstance(configuration
                        .getProperty("keyserver.saml.signature.type"), 
                        "CryptoServer");
                signer.initSign(privateKey);
                signer.update(data);
                signature = signer.sign();
                
            }
            catch (Exception e) {
                throw new SignatureException(
                        "Error during signature creation.", e);
            }
            finally {
                bos.reset();
            }
            // End signing with key in HSM
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.exiting(Sec2SignatureSpi.class.getName(), "engineSign", DatatypeConverter.printHexBinary(signature));
            return signature;
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineUpdate", b);
            bos.write(new byte[] { b }, 0, 1);
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER
                        .exiting(Sec2SignatureSpi.class.getName(),
                                "engineUpdate");
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineUpdate", new Object[] { b, off, len });
            // TODO: input validation
            bos.write(b, off, len);
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER
                        .exiting(Sec2SignatureSpi.class.getName(),
                                "engineUpdate");
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.entering(Sec2SignatureSpi.class.getName(),
                        "engineVerify", DatatypeConverter.printHexBinary(sigBytes));
            }
            if (mode == MODE_UNKNOWN || mode == MODE_SIGN) {
                throw new SignatureException(
                        "Not initialized for verification.");
            }
            byte[] data = bos.toByteArray();
            boolean verified = false;
            try {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Verifying: " 
                            + DatatypeConverter.printHexBinary(data));
                }
                Signature verifier = Signature.getInstance(configuration
                        .getProperty("keyserver.saml.signature.type"),
                        this.bouncyCastleProvider);
                verifier.initVerify(userSignPublicKey);
                verifier.update(data);
                verified = verifier.verify(sigBytes);
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.exiting(Sec2SignatureSpi.class.getName(),
                            "engineVerify", verified);
                return verified;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new SignatureException(
                        "Error during signature verification.", e);
            }
            finally {
                bos.reset();
            }
        }

    }
}