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


import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import javax.crypto.*;
import org.sec2.securityprovider.IServiceImpl;
import org.sec2.securityprovider.exceptions.ExTokenLogin;
import org.sec2.securityprovider.serviceparameter.*;
import org.sec2.token.IToken;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.swtoken.SoftwareToken;

/**
 * Implementation of the symmetric encryption/decryption functionality.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 */
final class CipherImpl extends CipherSpi implements IServiceImpl {

    /**
     * Initialized flag used to prevent finalizer attacks.
     */
    private boolean initialized = false;
    /**
     * Internal buffer to be signed.
     */
    private byte[] localBuffer = null;
    /**
     *  Card Interface Selector.
     */
    private TokenType  mType;
    /**
     * Card Interface.
     */
    private final IToken tokenInterface;
    /**
     * PIN to access the crypto device.
     */
    private PIN pin;
    /**
     * Cipher algorithm.
     */
    private CipherAlgorithm cipherAlgorithm;
    /**
     * Operation mode (Encrypt, Decrypt, Wrap, Unwrap..).
     */
    private int operationMode = Cipher.WRAP_MODE;
    /**
    * Cipher operation mode - unused due to hard-coded algorithms in lib.
    */
    private CipherOperationMode cipherOperationMode;
    /**
     * En-/Decryption key.
     */
    private ClusterKey clusterKey;
    /**
     * Padding algorithm - default is no PKCS#7.
     */
    private PaddingAlgorithm paddingAlgorithm = PaddingAlgorithm.PKCS7;

    /**
     * Custom constructor for initialization through parameters.
     *
     * @param parameters Service parameters
     */
    public CipherImpl(final List<IServiceParameter> parameters) {
        // do the intialization

        if (parameters != null) {
            for (IServiceParameter param : parameters) {
                if (param instanceof PIN) {
                    /*
                     * To avoid multiple storage of the sensible PIN don't copy
                     * or clone the object/value. The PIN class is final and
                     * wraps final fields thus realizing some kind of
                     * immutability.
                     */
                    this.pin = (PIN) param;
                }

                if (param instanceof CipherAlgorithm) {
                    this.cipherAlgorithm = (CipherAlgorithm) param;
                }
                       /*
                 *  This Parameter carries the Keystore Type.
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

        if (this.pin == null || this.cipherAlgorithm == null) {
            throw new IllegalArgumentException(
                    "Setting the PIN and cipher algorithm is manadatory!");
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

        // finalize the initialization by setting the marker
        this.initialized = true;
    }

    @Override
    protected void engineSetMode(final String mode)
            throws NoSuchAlgorithmException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        try {
            this.cipherOperationMode = CipherOperationMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    @Override
    protected void engineSetPadding(final String padding)
            throws NoSuchPaddingException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        try {
            paddingAlgorithm = PaddingAlgorithm.valueOf(padding);
        } catch (IllegalArgumentException e) {
            throw new NoSuchPaddingException(
                    "Padding not supported by crypto library.");
        }
    }

    @Override
    protected int engineGetBlockSize() {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return -1;
        }

        return this.cipherAlgorithm.getBlockSize();
    }

    @Override
    protected int engineGetOutputSize(final int inputLen) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return -1;
        }

        //according to library spec input length = output length
        return inputLen;
    }

    @Override
    /**
     * TODO
     *
     * Mail Korrespondenz Norbert: Zu der Frage nach dem IV: Aus der Doku: AES,
     * DES, triple DES and Korean SEED algorithms in CBC mode will use 0 for
     * initial vector(IV) if this method is used. Alternativ können wir aber
     * auch noch einen IV angeben. Dies ist im Moment jedoch nicht vorgesehen.
     *
     * Klären, was machen wir hier letztendlich?
     */
    protected byte[] engineGetIV() {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return new byte[0];
        }

        // TODO
        throw new UnsupportedOperationException("IV not accessible.");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        throw new UnsupportedOperationException(
                "AlgorithmParameters objects are not supported.");
    }

    @Override
    protected void engineInit(final int opmode, final Key key,
            final SecureRandom random)
            throws InvalidKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }
        // only wrap and unwrap are supported
        if (opmode != Cipher.UNWRAP_MODE && opmode != Cipher.WRAP_MODE) {
            throw new IllegalArgumentException(
                    "Only wrapping and unwrapping keys is allowed.");
        }
        this.operationMode = opmode;

        // random is not used
        // check key
        if (key == null || !(key instanceof ClusterKey)) {
            throw new InvalidKeyException("Key must be of NON NULL type"
                    + " and ClusterKey");
        }

        this.clusterKey = (ClusterKey) key;
    }

    @Override
    protected void engineInit(final int opmode, final Key key,
            final AlgorithmParameterSpec params, final SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        try {
            engineInit(opmode, key, random);
        } catch (IllegalArgumentException e) {
            throw new InvalidAlgorithmParameterException(e);
        }
    }

    @Override
    protected void engineInit(final int opmode, final Key key,
            final AlgorithmParameters params, final SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return;
        }

        engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
    }

    /**
     * {@inheritDoc }
     *
     * This method will always return null, since we only process the data when
     * using the wrap methods.
     */
    @Override
    protected byte[] engineUpdate(final byte[] input,
            final int inputOffset, final int inputLen) {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return new byte[0];
        }

        byte[] addMe = new byte[inputLen - inputOffset];
        System.arraycopy(input, inputOffset, addMe, 0, inputLen);
        if (localBuffer == null) {
            localBuffer = new byte[inputLen];
            System.arraycopy(input, inputOffset, localBuffer, 0, inputLen);
        } else {
            byte[] tmp = new byte[localBuffer.length + inputLen];
            System.arraycopy(localBuffer, 0, tmp, 0, localBuffer.length);
            System.arraycopy(input, inputOffset, tmp,
                                localBuffer.length, inputLen);
            localBuffer = tmp;
        }
        // processing only takes place on final
        return null;
    }

    /**
     * {@inheritDoc }
     *
     * This method will always return 0, since we only process the data when
     * using the final methods.
     */
    @Override
    protected int engineUpdate(final byte[] input, final int inputOffset,
            final int inputLen, final byte[] output, final int outputOffset)
            throws ShortBufferException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return -1;
        }

        if (engineGetOutputSize(inputLen) > output.length - outputOffset) {
            throw new ShortBufferException("Output buffer too small.");
        }
        engineUpdate(input, inputOffset, inputLen);

        return 0;
    }

    @Override
    protected byte[] engineWrap(final Key key)
            throws IllegalBlockSizeException, InvalidKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return new byte[0];
        }

        if (!(key instanceof DocumentKey)) {
            throw new InvalidKeyException("Only DocumentKey allowed");
        }

        DocumentKey docKey = (DocumentKey) key;

        byte[] result = null;
        try {
            result = invokeCard(docKey.getKey().getBytes(), operationMode);
        } catch (ExTokenLogin ex) {
            throw new InvalidKeyException(ex);
        } catch (TokenException ex) {
            throw new InvalidKeyException(ex);
        }
        return result;
    }

    @Override
    protected Key engineUnwrap(final byte[] bytes, final String string,
            final int i) throws InvalidKeyException, NoSuchAlgorithmException {
        /*
         * byte[] tmpkey = new byte[bytes.length-2]; System.arraycopy(bytes, 2,
         * tmpkey, 0, bytes.length-2); engineUpdate(tmpkey,0,tmpkey.length);
         * byte[] result = this.payloadProcessor();
         */

        DocumentKey key = null;
        try {
            byte[] result = this.invokeCard(bytes, operationMode);
            key = new DocumentKey(result, false);
        } catch (ExTokenLogin ex) {
            throw new InvalidKeyException(ex);
        } catch (TokenException ex) {
            throw new InvalidKeyException(ex);
        }
        return key;

    }

    // Guess DoFinal is only for crypting and thus not needed here
    @Override
    protected byte[] engineDoFinal(final byte[] input, final int inputOffset,
            final int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return new byte[0];
        }
        throw new UnsupportedOperationException("Operation not supported yet");
    }

    @Override
    protected int engineDoFinal(final byte[] input, final int inputOffset,
            final int inputLen, final byte[] output, final int outputOffset)
            throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return -1;
        }
        throw new UnsupportedOperationException("Operation not supported yet");
    }

    /**
     * Wraps/Unwraps an encoded key.
     *
     * @param keyBytes Bytes of the encoded key
     * @param mode Operation mode
     *
     * @return (Un)Wrapped key bytes
     * @throws ExTokenLogin Thrown if login at the crypto device failed
     * @throws TokenException Thrown if problems with the crypto device occurred
     */
    private byte[] invokeCard(final byte[] keyBytes, final int mode)
            throws ExTokenLogin, TokenException {
        DocumentKey wrappedKey = null;
        byte[] result = new byte[0];
        DocumentKey deliveredKey = null;
        /*
         * At first check if the delivered bytes represent a valid DocumentKey
         */
        switch (mode) {
            case Cipher.WRAP_MODE:
                deliveredKey = new DocumentKey(keyBytes, false);
                break;
            case Cipher.UNWRAP_MODE:
                deliveredKey = new DocumentKey(keyBytes, true);
                break;
            default:
                break;
        }

        if (deliveredKey == null) {
            throw new IllegalArgumentException(
                    "Key bytes could not be parsed.");
        }

        if (tokenInterface != null) {
            tokenInterface.connect();
            tokenInterface.loginPIN(pin.getPin());
            try {
                /*
                 * TODO cipher operation modes are hardcoded at the token
                 * interface at the moment and thus noch configurable
                 */
                switch (mode) {
                    case Cipher.WRAP_MODE:
                        wrappedKey = tokenInterface.encryptDocumentKey(
                                this.clusterKey.getId(), deliveredKey);
                        break;
                    case Cipher.UNWRAP_MODE:
                        wrappedKey = tokenInterface.decryptDocumentKey(
                                this.clusterKey.getId(), deliveredKey);
                        break;
                    default:
                        break;
                }

                if (wrappedKey != null) {
                    result = wrappedKey.getKey().getBytes();
                }
            } finally {
                tokenInterface.logoutPIN();
                tokenInterface.disconnect();
            }
        }

        return result;
    }
}
