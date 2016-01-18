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
package org.sec2.securityprovider.mobileclient;

import java.io.*;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import org.sec2.securityprovider.IServiceImpl;
import org.sec2.securityprovider.exceptions.CertificateBadMatchExeption;
import org.sec2.securityprovider.exceptions.CertificateValidationException;
import org.sec2.securityprovider.exceptions.KeyStoreStateException;
import org.sec2.securityprovider.serviceparameter.IServiceParameter;
import org.sec2.securityprovider.serviceparameter.PIN;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.IToken;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.GroupKeyId;
import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;
import org.sec2.token.swtoken.SoftwareToken;
import org.sec2.token.swtoken.SoftwareTokenTestUser1;
import org.sec2.token.swtoken.SoftwareTokenTestUser2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the key store functionality.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @author Jan Temme - Jan.temme@rub.de
 * @author Thorsten Schreiber - thorsten.schreiber@rub.de
 * @version 0.1
 *
 */
final class KeyStoreImpl extends KeyStoreSpi implements IServiceImpl {

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(KeyStoreImpl.class);
    }
    /**
     * Initialized flag used to prevent finalizer attacks.
     */
    private boolean initialized = false;
    /**
     * Selector for Software or Hardware token.
     */
    private TokenType mType;
    /**
     * Card Interface.
     */
    private final IToken tokenInterface;
    /**
     * PIN for Card Access.
     */
    private PIN mPin;
    /**
     * HashMap for internal storage of document Keys.
     */
    private HashMap<String, DocumentKey> mDocumentKeys;
    /**
     * HashMap for internal storage of group Keys.
     */
    private HashMap<String, GroupKey> mGroupKeys;
    //TODO: Hopefully WrappedGroupKeys
    /**
     * ArrayList for internal storage of Key Identifier.
     */
    private ArrayList<String> mKeyIdentifyers;
    /**
     * Internal certificate repository.
     */
    private HashMap<String, Certificate> mCertificates;
    /**
     * UserKey for signing from the Card.
     */
    private RSAPublicKey mPublicKeyClientSignature;
    /**
     * UserKey for CLIENT_ENCRYPTION from the Card.
     */
    private RSAPublicKey mPublicKeyClientEncryption;
    /**
     * PublicKey of the SERVER_SIGNATURE
     */
    private RSAPublicKey mPublicKeyServerSignature;
    /**
     * PublicKey of the SERVER_ENCRYPTION type
     */
    private RSAPublicKey mPublicKeyServerEncryption;

    /**
     * Custom constructor for initialization through parameters.
     *
     * @param parameters Service parameters
     */
    public KeyStoreImpl(final List<IServiceParameter> parameters) {
        // do the intialization

        if (parameters != null) {
            for (IServiceParameter param : parameters) {
                //Debugging:        System.out.println("The Param:");
                //Debugging:        System.out.println(param.toString());
                if (param instanceof PIN) {
                    /*
                     * To avoid multiple storage of the sensible PIN don't copy
                     * or clone the object/value. The PIN class is final and
                     * wraps final fields thus realizing some kind of
                     * immutability.
                     */
                    this.mPin = (PIN) param;
                }
                /*
                 * This Parameter carries the Token Type. If no Type is given,
                 * the default should be HARDWARE_TOKEN
                 */
                if (param instanceof TokenType) {
                    this.mType = (TokenType) param;
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Parameter list must not be NULL!");
        }

        if (this.mPin == null) {
            throw new IllegalArgumentException(
                    "Setting the PIN is manadatory!");
        }

        // This prevents the null pointer exception if the Keystoretype is 
        // not set
        if (this.mType == null) {
            throw new IllegalArgumentException(
                    "Setting the TokenType is manadatory!");
        }

        // get a card interface
        switch (this.mType) {
            case SOFTWARE_TOKEN:
                tokenInterface = SoftwareToken.getInstance();
                break;
            case SOFTWARE_TEST_TOKEN_USER_1:
                tokenInterface = SoftwareTokenTestUser1.getInstance();
                break;
            case SOFTWARE_TEST_TOKEN_USER_2:
                tokenInterface = SoftwareTokenTestUser2.getInstance();
                break;
            case HARDWARE_TOKEN:
            tokenInterface = new HardwareToken();
                break;
            default:
                tokenInterface = null;
                throw new IllegalStateException("Desired TokenType is not available");
        }

        //System.out.println("DEBUG: INIT KEYSTORE: " + mType.name());
        // finalize the initialization by setting the marker
        this.initialized = true;
    }

    /**
     * Returns the Key associated with the given alias. In case the alias
     * belongs to a groupkey an empty Sec2GroupKey with identifier set to alias
     * is returned.
     *
     * @return Key associated with alias
     *
     * @param alias alias, aka Keyidentifier
     * @param password not used here because we use a pin protected token
     *
     * {@inheritDoc}
     *
     * @throws UnrecoverableKeyException Thrown if Key for the given alias was
     * not found
     * @throws NoSuchAlgorithmException Thrown if Algorithm not available
     */
    @Override
    public Key engineGetKey(final String alias, final char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!this.initialized) {
            throw new NoSuchAlgorithmException("Keystore not initialized!");
        }

        Key requestedKey = null;

        /*
         * Search the requested Key.
         */

        if (mDocumentKeys.containsKey(alias)) {
            requestedKey = (DocumentKey) mDocumentKeys.get(alias);
        } else if (mGroupKeys.containsKey(alias)) {
            requestedKey = (GroupKey) mGroupKeys.get(alias);
        } else if (PublicKeyType.CLIENT_SIGNATURE.name().equals(alias)) {
            requestedKey = mPublicKeyClientSignature;
        } else if (PublicKeyType.CLIENT_ENCRYPTION.name().equals(alias)) {
            requestedKey = mPublicKeyClientEncryption;
        } else if (PublicKeyType.SERVER_SIGNATURE.name().equals(alias)) {
            requestedKey = mPublicKeyServerSignature;
        }


        if (requestedKey == null) {
            throw new UnrecoverableKeyException("Key not found");
        } else {
            return requestedKey;
        }

    }

    @Override
    public Certificate[] engineGetCertificateChain(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return new Certificate[0];
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Certificate engineGetCertificate(final String alias) {

        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        if (mCertificates.containsKey(alias)) {
            return mCertificates.get(alias);
        } else {
            return null;
        }
    }

    @Override
    public Date engineGetCreationDate(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Assigns the given key to the given alias. If Key is a Groupkey it is
     * stored to the token. If Key is a Documentkey it is safed in memory. If
     * alias is already associated with a Key, <s>the old Value will be
     * overwritten.</s> an exepction is thrown.
     *
     * @param alias The alias name. For Groupkey use: key.getId().toString
     * @param key The Key to be associated with the alias
     * @param password not used here because we use a Token
     * @param chain not needed (yet)
     *
     * @throws KeyStoreException if key is no Sec2Key, or
     */
    @Override
    public void engineSetKeyEntry(final String alias, final Key key,
            final char[] password, final Certificate[] chain)
            throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreStateException("Keystore not initialized!");
        }

        if (alias == null || key == null) {
            throw new KeyStoreException("Alias or Key must not be null");
        }

        if (mKeyIdentifyers.contains(alias)) {
            throw new KeyStoreException("Key already stored");
        }

        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(mPin.getPin());

                if (key instanceof GroupKey) {
                    mKeyIdentifyers.add(alias);
                    GroupKey groupkeyToStore = new GroupKey(new byte[0],
                            ((GroupKey) key).getId());
                    mGroupKeys.put(alias, groupkeyToStore);
                    tokenInterface.importGroupKey((GroupKey) key);

                } else if (key instanceof DocumentKey) {
                    mKeyIdentifyers.add(alias);
                    mDocumentKeys.put(alias, (DocumentKey) key);
                } else {
                    // only sec2keys GroupKey, DocumentKEy are supported.
                    throw new KeyStoreException("No valid Sec2Key");
                }
            } catch (TokenException ex) {
                throw new KeyStoreException(ex);
            } finally {
                try {
                    tokenInterface.logoutPIN();
                    tokenInterface.disconnect();
                } catch (TokenException ex) {
                    throw new KeyStoreException(ex);
                }
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void engineSetKeyEntry(final String alias, final byte[] key,
            final Certificate[] chain)
            throws KeyStoreException {

        if (!this.initialized) {
            throw new KeyStoreStateException("Keystore not initialized!");
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Compares two Public Keys. Tests on Alogrithm and Bytes.
     *
     * @param pk1
     * @param pk2
     *
     * @return True, if the keys differ.
     */
    private boolean areDifferent(PublicKey pk1, PublicKey pk2) {
        return !((pk1.getAlgorithm().compareTo(pk2.getAlgorithm()) == 0)
                && pk1.getEncoded().equals(pk2.getEncoded()));
    }

    /**
     * If Cerficate matches a Public Key, it is put in to internal certificate
     * list. If not, an Exception is thrown.
     *
     * @param alias Name
     * @param cert
     * @param matchingPublicKey
     *
     * @throws CertificateBadMatchExeption
     */
    private void putCertificateOnMatch(final String alias,
            final Certificate cert, final PublicKey matchingPublicKey)
            throws CertificateBadMatchExeption {
        if (areDifferent(matchingPublicKey, cert.getPublicKey())) {
            throw new CertificateBadMatchExeption(matchingPublicKey, cert);
        }
        mCertificates.put(alias, cert);
    }

    /**
     * Adds a certificate to the keystore. There are some reserved aliases
     * called slots. The aliases are defined in Enum PublicKeyType. Each Type
     * can be stored only once. These slots are checked because they are in a
     * certifiate chain and three publickey are stored on the card. This are
     * client_signature and client_encryption.
     *
     * @param alias The must be the String representation of Enum
     * org.sec2.securityprovider.
     * @param cert
     *
     * @throws KeyStoreException
     */
    @Override
    public void engineSetCertificateEntry(final String alias,
            final Certificate cert)
            throws KeyStoreException {

        if (!this.initialized) {
            throw new KeyStoreStateException("Keystore not initialized!");
        }

        if (alias == null) {
            throw new NullPointerException("Argument alias is unexpectly null");
        }
        if (cert == null) {
            throw new NullPointerException("Argument cert is unexpectly null");
        }

        getLogger().trace("Try to set new ertificate Entry alias:" + alias
                + "cert: " + cert);

        // Is Alias a reserved Name?
        PublicKeyType t = PublicKeyType.findConstant(alias);
        if (t == null) //No reserved name was found, so we just put it in.
        {
            mCertificates.put(alias, cert);
        } else {
            switch (t) {
                //The following PK are stored on token
                case CLIENT_SIGNATURE:
                    putCertificateOnMatch(alias, cert, mPublicKeyClientSignature);
                    break;
                case CLIENT_ENCRYPTION:
                    putCertificateOnMatch(alias, cert, mPublicKeyClientEncryption);
                    break;
                case SERVER_SIGNATURE:
                    putCertificateOnMatch(alias, cert, mPublicKeyClientEncryption);
                    break;
                /*
                 * This PK is not stored on token to save tons of kb. So it must
                 * be imported on Keystore init time. It is validated against
                 * Server_Signature.
                 */
                case SERVER_ENCRYPTON:
                    try {
                        cert.verify(mPublicKeyServerSignature);
                    } catch (Exception ex) {
                        CertificateValidationException e = new CertificateValidationException("Validation of Server Encryption"
                                + " Certificate was not successfull"
                                + ex.getMessage());

                        throw e;
                    }
                    mCertificates.put(alias, cert);
                    break;

            }
        }


    }

    @Override
    public void engineDeleteEntry(final String alias) throws KeyStoreException {
        if (!this.initialized) {
            throw new KeyStoreStateException("Keystore not initialized!");
        }

        // Is the key stored?
        if (!mKeyIdentifyers.contains(alias)) {
            throw new KeyStoreException("Key not found");
        }

        // Is the Key in Groupkkeys?
        if (mGroupKeys.containsKey(alias)) {
            if (tokenInterface != null) {
                try {
                    GroupKey key = (GroupKey) mGroupKeys.get(alias);
                    tokenInterface.connect();
                    tokenInterface.loginPIN(mPin.getPin());
                    tokenInterface.removeGroupKey(key.getId());
                } catch (TokenException ex) {
                    throw new KeyStoreException(ex);
                } finally {
                    try {
                        tokenInterface.logoutPIN();
                        tokenInterface.disconnect();
                    } catch (TokenException ex) {
                        throw new KeyStoreException(ex);
                    }
                }
            }
            mKeyIdentifyers.remove(alias);
            mGroupKeys.remove(alias);

        } else if (mDocumentKeys.containsKey(alias)) {
            mKeyIdentifyers.remove(alias);
            mDocumentKeys.remove(alias);
        }
    }

    @Override
    public Enumeration<String> engineAliases() {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        return Collections.enumeration(mKeyIdentifyers);
    }

    @Override
    public boolean engineContainsAlias(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return false;
        }
        return mKeyIdentifyers.contains(alias);
    }

    @Override
    public int engineSize() {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return -1;
        }
        int size = mDocumentKeys.size() + mGroupKeys.size();
        return size;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean engineIsKeyEntry(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return false;
        }
        return (mDocumentKeys.containsKey(alias)
                || mGroupKeys.containsKey(alias));
    }

    @Override
    public boolean engineIsCertificateEntry(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return false;
        }

        return (mCertificates.containsKey(alias));

    }

    @Override
    public String engineGetCertificateAlias(final Certificate cert) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void engineStore(final OutputStream stream, final char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }.
     *
     * @param stream not used
     * @param password not used
     */
    @Override
    public void engineLoad(final InputStream stream, final char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            throw new IOException("Keystore not initialized!!");
        }
        if (mDocumentKeys == null) {
            mDocumentKeys = new HashMap();
        }
        if (mGroupKeys == null) {
            mGroupKeys = new HashMap();
        }
        if (mKeyIdentifyers == null) {
            mKeyIdentifyers = new ArrayList();
        }
        if (mCertificates == null) {
            mCertificates = new HashMap();
        }

        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(mPin.getPin());
                GroupKeyId[] avaiGroupKeys = tokenInterface.getAvailableGroupKeys();
                for (int i = 0; i < avaiGroupKeys.length; i++) {
                    GroupKey key = new GroupKey(new byte[0],
                            avaiGroupKeys[i]);
                    mGroupKeys.put(key.getId().toString(), key);
                    mKeyIdentifyers.add(key.getId().toString());
                }
                UserKey pubSig = tokenInterface.getUserKeySig();

                if (pubSig == null) {
                    throw new IOException("Token returns null pointer "
                            + "instead of Signature Key");
                }

                BigInteger modInt = new BigInteger(1,
                        pubSig.getModulus().getBytes());
                BigInteger expInt = new BigInteger(
                        pubSig.getExponent().getBytes());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                mPublicKeyClientSignature = (RSAPublicKey) kf.generatePublic(
                        new RSAPublicKeySpec(modInt, expInt));

                UserKey pubEnc = tokenInterface.getUserKeyEnc();

                if (pubEnc == null) {
                    throw new IOException("Token returns null pointer "
                            + "instead of Encryption Key");
                }

                modInt = new BigInteger(1,
                        pubEnc.getModulus().getBytes());
                expInt = new BigInteger(
                        pubEnc.getExponent().getBytes());
                mPublicKeyClientEncryption = (RSAPublicKey) kf.generatePublic(
                        new RSAPublicKeySpec(modInt, expInt));

                ServerKey servKey = tokenInterface.getServerKey();
                modInt = new BigInteger(1,
                        servKey.getModulus().getBytes());
                expInt = new BigInteger(
                        servKey.getExponent().getBytes());
                mPublicKeyServerSignature = (RSAPublicKey) kf.generatePublic(
                        new RSAPublicKeySpec(modInt, expInt));
            } catch (TokenException ex) {
                ex.printStackTrace();
                throw new IOException(ex);
            } catch (InvalidKeySpecException ex) {
                ex.printStackTrace();
                throw new IOException(ex);
            } finally {
                try {
//                    tokenInterface.logoutPIN();
                    tokenInterface.disconnect();
                } catch (TokenException ex) {
                    ex.printStackTrace();
                    throw new IOException(ex);
                }
            }
        }
    }
}
