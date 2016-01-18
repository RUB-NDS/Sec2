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
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import org.sec2.logging.LogLevel;
import org.sec2.logging.Logger;
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
import org.sec2.token.keys.*;
import org.sec2.token.swtoken.SoftwareToken;

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
     * HashMap for internal storage of cluster Keys.
     */
    private HashMap<String, ClusterKey> mClusterKeys;//TODO: Hopefully WrappedClusterKeys
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
                 * This Parameter carries the Token Type.
                 * If no Type is given, the default should be HARDWARE_TOKEN
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

        if (this.mType == null) {
            //Hardware Token should be standard,
            // only if explicty given, the Type should be changed.
            mType = TokenType.HARDWARE_TOKEN;
        }


        // get a card interface
        switch (this.mType) {
            case SOFTWARE_TOKEN:
                tokenInterface = new SoftwareToken();
                break;
            case HARDWARE_TOKEN:
            default:
                tokenInterface = new HardwareToken();
        }

        //System.out.println("DEBUG: INIT KEYSTORE: " + mType.name());



        // finalize the initialization by setting the marker
        this.initialized = true;
    }

    /**
     * Returns the Key associated with the given alias. In case the alias
     * belongs to a clusterkey an empty Sec2ClusterKey with identifier set to
     * alias is returned.
     *
     * @return Key associated with alias
     * @param alias alias, aka Keyidentifier
     * @param password not used here because we use a Token {@inheritDoc}
     * @throws UnrecoverableKeyException Thrown if Key for the given alias was
     * not found
     * @throws NoSuchAlgorithmException Thrown if Algorithm not available
     */
    @Override
    public Key engineGetKey(final String alias, final char[] password)
      throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }
        if (mDocumentKeys.containsKey(alias)) {
            return (DocumentKey) mDocumentKeys.get(alias);
        } else if (mClusterKeys.containsKey(alias)) {
            return (ClusterKey) mClusterKeys.get(alias);
        } else if (PublicKeyType.CLIENT_SIGNATURE.name().equals(alias)) {
            return mPublicKeyClientSignature;
        } else if (PublicKeyType.CLIENT_ENCRYPTION.name().equals(alias)) {
            return mPublicKeyClientEncryption;
        } else if (PublicKeyType.SERVER_SIGNATURE.name().equals(alias)) {
            return mPublicKeyServerSignature;
        } else {
            throw new UnrecoverableKeyException("Key not found");
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
     * Assigns the given key to the given alias. If Key is a Clusterkey it is
     * stored to the token. If Key is a Documentkey it is safed internal. If
     * alias is already associated with a Key, the old Value will be
     * overwritten.
     *
     * @param alias The alias name. For Clusterkey use: key.getId().toString
     * @param key The Key to be associated with the alias
     * @param password not used here because we use a Token
     * @param chain not needed (yet)
     * @throws KeyStoreException Thrown if key is no Sec2Key
     */
    @Override
    public void engineSetKeyEntry(final String alias, final Key key,
      final char[] password, final Certificate[] chain)
      throws KeyStoreException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }
        if (mKeyIdentifyers.contains(alias)) {
            throw new KeyStoreException("Key already stored");
        }
        if (alias == null || key == null) {
            throw new KeyStoreException("Alias and Key may not be null");
        }
        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(mPin.getPin());
                if (key instanceof ClusterKey) {
                    mKeyIdentifyers.add(alias);
                    ClusterKey temp = new ClusterKey(new byte[0],
                      ((ClusterKey) key).getId());
                    mClusterKeys.put(alias, temp);
                    tokenInterface.importClusterKey((ClusterKey) key);
                } else if (key instanceof DocumentKey) {
                    mDocumentKeys.put(alias, (DocumentKey) key);
                    mKeyIdentifyers.add(alias);
                } else {
                    //Maybe unnecessary
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
            // class not yet initialized, leave immediately
            return;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Compares to Public Keys.
     *
     * @param pk1
     * @param pk2
     * @return True, if the keys differ.
     */
    private boolean areDifferent(PublicKey pk1, PublicKey pk2) {
        return !((pk1.getAlgorithm().compareTo(pk2.getAlgorithm()) == 0)
          && pk1.getEncoded().equals(pk2.getEncoded()));

    }

    /**
     * If Cerficate matches a Public Key, it is put in to internal
     * certificate list
     *
     * @param alias Name
     * @param cert
     * @param matchingPublicKey
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
     *
     * @param alias The must be the String representation of Enum
     * org.sec2.securityprovider.
     * @param cert
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

        Logger.log("Try to set new ertificate Entry alias:" + alias
          + "cert: " + cert, LogLevel.TRACE);

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
                 * This PK is not stored on token to save tons of kb.
                 * So it must be imported on Keystore init time.
                 * It is validated against Server_Signature.
                 */
                case SERVER_ENCRYPTON:
                    try {
                        cert.verify(mPublicKeyServerSignature);
                    } catch (Exception ex) {
                        CertificateValidationException e =
                          new CertificateValidationException("Validation of Server Encryption"
                          + " Certificate was not successfull"
                          + ex.getMessage());

                        throw e;
                    }
                    mCertificates.put(alias, cert);
                    break;

            }
        }





        /*
         * if (!this.initialized) {
         * // class not yet initialized, leave immediately
         * return;
         * }
         *
         * throw new UnsupportedOperationException("Not supported yet.");
         */
    }

    @Override
    public void engineDeleteEntry(final String alias) throws KeyStoreException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }
        if (!mKeyIdentifyers.contains(alias)) {
            throw new KeyStoreException("Key not found");
        }
        if (mClusterKeys.containsKey(alias)) {
            if (tokenInterface != null) {
                try {
                    ClusterKey key = (ClusterKey) mClusterKeys.get(alias);
                    tokenInterface.connect();
                    tokenInterface.loginPIN(mPin.getPin());
                    tokenInterface.removeClusterKey(key.getId());
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
            mClusterKeys.remove(alias);

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
        int size = mDocumentKeys.size() + mClusterKeys.size();
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
          || mClusterKeys.containsKey(alias));
    }

    @Override
    public boolean engineIsCertificateEntry(final String alias) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return false;
        }

        throw new UnsupportedOperationException("Not supported yet.");
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
            return;
        }
        mDocumentKeys = new HashMap();
        mClusterKeys = new HashMap();
        mKeyIdentifyers = new ArrayList();
        mCertificates = new HashMap();
        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(mPin.getPin());
                ClusterKeyId[] avaiGroupKeys =
                  tokenInterface.getAvailableClusterKeys();
                for (int i = 0; i < avaiGroupKeys.length; i++) {
                    ClusterKey key = new ClusterKey(new byte[0],
                      avaiGroupKeys[i]);
                    mClusterKeys.put(key.getId().toString(), key);
                    mKeyIdentifyers.add(key.getId().toString());
                }
                UserKey pubSig = tokenInterface.getUserKeySig();
                BigInteger modInt = new BigInteger(1,
                  pubSig.getModulus().getBytes());
                BigInteger expInt = new BigInteger(
                  pubSig.getExponent().getBytes());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                mPublicKeyClientSignature = (RSAPublicKey) kf.generatePublic(
                  new RSAPublicKeySpec(modInt, expInt));

                UserKey pubEnc = tokenInterface.getUserKeyEnc();
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
                throw new IOException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new IOException(ex);
            } finally {
                try {
                    tokenInterface.logoutPIN();
                    tokenInterface.disconnect();
                } catch (TokenException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }
}
