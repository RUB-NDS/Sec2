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

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import org.sec2.securityprovider.IServiceImpl;
import org.sec2.securityprovider.serviceparameter.IServiceParameter;
import org.sec2.securityprovider.serviceparameter.PIN;
import org.sec2.securityprovider.serviceparameter.SignatureAlgorithm;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.IToken;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.UserKey;
import org.sec2.token.swtoken.SoftwareToken;

/**
 * Implementation of the signature functionality.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 10, 2011
 */
final class SignatureImpl extends SignatureSpi implements IServiceImpl {

    /**
     * Initialized flag used to prevent finalizer attacks.
     */
    private boolean initialized = false;
    /**
     * Internal buffer to be signed.
     */
    private byte[] localBuffer = null;
    /**
     * Selector for Software or Hardware token.
     */
    private TokenType mType;
    
    /**
     * Card Interface.
     */
    private final IToken tokenInterface;
    /**
     * PIN to access the crypto device.
     */
    private PIN pin;
    /**
     * Signature algorithm.
     */
    private SignatureAlgorithm signatureAlgorithm;
    /**
     * Key used in the offered operations.
     */
    private UserKey key = null;
    /**
     * Mode selector (sign/verify).
     */
    private boolean signMode = true;
    /**
     * Java specific Asn.1/DER encoded OID of Md5.
     */
    private byte[] md5Oid =
            new BigInteger("3020300c06082a864886f70d020505000410",
            16).toByteArray();
    /**
     * Java specific Asn.1/DER encoded OID of Sha1.
     */
    private byte[] sha1Oid =
            new BigInteger("3021300906052b0e03021a05000414", 16).toByteArray();
    /**
     * Java specific Asn.1/DER encoded OID of Sha256.
     */
    private byte[] sha256Oid =
            new BigInteger("3031300d060960864801650304020105000420",
            16).toByteArray();

    /**
     * Custom constructor for initialization through parameters.
     *
     * @param parameter Service parameters
     */
    public SignatureImpl(final List<IServiceParameter> parameter) {
        // do the intialization

        if (parameter != null) {
            for (IServiceParameter param : parameter) {
                if (param instanceof PIN) {
                    /*
                     * To avoid multiple storage of the sensible PIN don't copy
                     * or clone the object/value. The PIN class is final and
                     * wraps final fields thus realizing some kind of
                     * immutability.
                     */
                    this.pin = (PIN) param;
                }

                if (param instanceof SignatureAlgorithm) {
                    this.signatureAlgorithm = (SignatureAlgorithm) param;
                }
                /*
                 *  This Parameter carries the Token Type.
                 *  If no Type is given, the default should be HARDWARE_TOKEN
                 */
                if (param instanceof TokenType){
                    this.mType = (TokenType) param;
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Parameter list must not be NULL!");
        }

        if (this.pin == null || this.signatureAlgorithm == null) {
            throw new IllegalArgumentException(
                    "Setting the PIN, Signature algorithm is manadatory!");
        }

            if (this.mType ==null ){
            //Hardware Token should be standard,
            // only if explicty given, the Type should be changed.
            mType = TokenType.HARDWARE_TOKEN;
        }
            

        // get a card interface
        switch (this.mType) {
            case SOFTWARE_TOKEN:
                tokenInterface = new SoftwareToken(); break;
            case HARDWARE_TOKEN:
            default:
                tokenInterface = new HardwareToken();
       }

        this.initialized = true;
    }

    @Override
    protected void engineInitVerify(final PublicKey publicKey)
            throws InvalidKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        if (publicKey instanceof UserKey) {
            this.key = (UserKey) publicKey;
        } else if (publicKey == null) {
            if (tokenInterface != null) {
                try {
                    tokenInterface.connect();
                    tokenInterface.loginPIN(pin.getPin());
                    this.key = tokenInterface.getUserKeySig();
                } catch (TokenException ex) {
                    throw new InvalidKeyException(
                            "Problems with the crypto device occured.", ex);
                } finally {
                    try {
                        tokenInterface.logoutPIN();
                        tokenInterface.disconnect();
                    } catch (TokenException e) {
                        throw new InvalidKeyException(
                                "Problems during crypto device diconnect.");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Only NULL or UserKey objects are allowed.");
        }

        signMode = false;
    }

    /**
     * @{inheritDoc}
     *
     * !DIFFERENT BEHAVIOUR!
     *
     * @param privateKey No private key muss be handled - parameter may be NULL
     * @throws InvalidKeyException Not used here
     */
    @Override
    protected void engineInitSign(final PrivateKey privateKey)
            throws InvalidKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }
        signMode = true;
    }

    @Override
    protected void engineUpdate(final byte b) throws SignatureException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        try {
            if (localBuffer == null) {
                localBuffer = new byte[]{b};
            } else {
                byte[] toAdd = new byte[]{b};
                localBuffer = concat(localBuffer, toAdd);
            }
        } catch (NullPointerException e) {
            // something went somewhere terribly wrong :-(
            throw new SignatureException("No internal buffer instance.", e);
        } catch (IndexOutOfBoundsException e) {
            // damn it - blame me, i was to dumb to compute the right size
            throw new SignatureException("Buffer capacity exceeded.", e);
        } catch (ArrayStoreException e) {
            // type mismatch should never happen due to type convention
            throw new SignatureException("Buffering data failed.", e);
        }
    }

    @Override
    protected void engineUpdate(final byte[] input,
            final int off, final int len) throws SignatureException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        try {
            if (localBuffer == null) {
                localBuffer = new byte[len - off];
                System.arraycopy(input, off, localBuffer, 0, len);
            } else {
                byte[] temp = new byte[len - off];
                System.arraycopy(input, off, temp, 0, len);
                localBuffer = concat(localBuffer, temp);
            }
        } catch (NullPointerException e) {
            // something went somewhere terribly wrong :-(
            throw new SignatureException("No internal buffer instance.", e);
        } catch (IndexOutOfBoundsException e) {
            // damn it - blame me, i was to dumb to compute the right size
            throw new SignatureException("Buffer capacity exceeded.", e);
        } catch (ArrayStoreException e) {
            // type mismatch should never happen due to type convention
            throw new SignatureException("Buffering data failed.", e);
        }
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        byte[] result = new byte[0];

        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return result;
        }

        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(pin.getPin());
                this.localBuffer = preparePayload(this.localBuffer);
                result = tokenInterface.sign(localBuffer);
                localBuffer = null;
            } catch (NoSuchAlgorithmException ex) {
                throw new SignatureException(ex);
            } catch (TokenException ex) {
                throw new SignatureException(ex.toString(), ex);
            } finally {
                if (tokenInterface != null) {
                    try {
                        tokenInterface.disconnect();
                    } catch (TokenException e) {
                        throw new SignatureException(
                                "Problems during crypto device diconnect.");
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected boolean engineVerify(final byte[] sigBytes)
            throws SignatureException {
        boolean result = false;

        if (!this.initialized) {
            // class not yet initialized, leave immediately
            result = false;
        } else {
            if (this.key == null || signMode) {
                throw new SignatureException(
                        "Engine not initialized for verification");
            }

            try {
                // utilize JCA to find suiteable signature verifiers....
                Signature signatureVerifier = Signature.getInstance(
                        signatureAlgorithm.name());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                RSAPublicKey pub = (RSAPublicKey) kf.generatePublic(
                        new RSAPublicKeySpec(key.getModulus().toBigInteger(),
                        key.getExponent().toBigInteger()));
                signatureVerifier.initVerify(pub);
                signatureVerifier.update(localBuffer);
                result = signatureVerifier.verify(sigBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new SignatureException(e);
            } catch (InvalidKeyException e) {
                throw new SignatureException(e);
            } catch (InvalidKeySpecException e) {
                throw new SignatureException(e);
            } catch (NullPointerException ex) {
                throw new SignatureException(ex);
            }
        }
        localBuffer = null;
        return result;
    }

    @Override
    protected void engineSetParameter(final String param, final Object value) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object engineGetParameter(final String param) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Used to prepare the data for signing. This means hashing and concat with
     * the Prefix.
     *
     * @param data Data to be prepared for Signing
     * @return data as it can be signed by the Card to create a valid Signature
     */
    private byte[] preparePayload(final byte[] data)
            throws NoSuchAlgorithmException {
        MessageDigest md = null;
        byte[] result = null;
        byte[] hashed = null;
        try {
            String digestAlgo =
                    this.signatureAlgorithm.getDigestAlgorithm().toString();
            if (digestAlgo.equals("MD5")) {
                md = MessageDigest.getInstance(digestAlgo);
                hashed = md.digest(data);
                result = concat(md5Oid, hashed);
            } else if (digestAlgo.equals("SHA1")) {
                md = MessageDigest.getInstance("SHA1");
                hashed = md.digest(data);
                result = concat(sha1Oid, hashed);
            } else if (digestAlgo.equals("SHA256")) {
                md = MessageDigest.getInstance("SHA-256");
                hashed = md.digest(data);
                result = concat(sha256Oid, hashed);
            } else {
                throw new NoSuchAlgorithmException("Algorithm: " + digestAlgo
                        + " not available");
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException(ex);
        }

        return result;
    }

    /**
     * Helping Method to concatenate two byte arrays.
     *
     * @param first First Array
     * @param second Second Array
     * @return first|second
     */
    private byte[] concat(final byte[] first, final byte[] second) {
        byte[] res = new byte[first.length + second.length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(second, 0, res, first.length, second.length);
        return res;
    }
}
