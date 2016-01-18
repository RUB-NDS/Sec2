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

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import org.sec2.securityprovider.IServiceImpl;
import org.sec2.securityprovider.keys.KeyType;
import org.sec2.securityprovider.keys.Sec2SecretKeySpec;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;
import org.sec2.securityprovider.serviceparameter.IServiceParameter;
import org.sec2.securityprovider.serviceparameter.PIN;
import org.sec2.token.IToken;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;

/**
 * Implementation of the secret key import functionality.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 10, 2011
 */
final class SecretKeyFactoryImpl extends SecretKeyFactorySpi
  implements IServiceImpl {

    /**
     * Initialized flag used to prevent finalizer attacks.
     */
    private boolean initialized = false;
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
     * Custom constructor for initialization through parameters.
     *
     * @param parameters Service parameters
     */
    public SecretKeyFactoryImpl(final List<IServiceParameter> parameters) {
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
            }
        } else {
            throw new IllegalArgumentException(
              "Parameter list must not be NULL!");
        }

        if (this.pin == null || this.cipherAlgorithm == null) {
            throw new IllegalArgumentException(
              "Setting the PIN and cipher algorithm is manadatory!");
        }

        // get a card interface
        tokenInterface = new HardwareToken();

        // finalize the initialization by setting the marker
        this.initialized = true;
    }

    @Override
    protected SecretKey engineGenerateSecret(final KeySpec keySpec)
      throws InvalidKeySpecException {
        final SecretKey result;
        final Sec2SecretKeySpec spec;
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        if (keySpec == null || !(keySpec instanceof Sec2SecretKeySpec)) {
            throw new InvalidKeySpecException(
              "The key specification must be of non NULL "
              + "Sec2SecretKeySpec type.");
        }
        spec = (Sec2SecretKeySpec) keySpec;
        if (spec.getKeyType() != KeyType.DOCUMENT) {
            throw new InvalidKeySpecException("Can only generate DocumentKeys");
        }
        if (tokenInterface != null) {
            try {
                tokenInterface.connect();
                tokenInterface.loginPIN(pin.getPin());
                DocumentKey key = tokenInterface.createDocumentKey();
                result = key;
            } catch (TokenException e) {
                /*
                 * thanks to to very flexible SPI - - wrapping and rethrowing
                 * excpetion
                 *
                 * the beauty of ugliness - incredible unpretty - absolutely
                 * confusing when handling the exception - non self-explanatory
                 *
                 * to make it short, simply crappy!
                 */
                throw new InvalidKeySpecException(e);
            } finally {
                try {
                    tokenInterface.logoutPIN();
                    tokenInterface.disconnect();
                } catch (TokenException e) {
                    throw new InvalidKeySpecException(e);
                }
            }
        } else {
            result = null;
        }

        return result;
    }

    @Override
    protected KeySpec engineGetKeySpec(final SecretKey key, final Class keySpec)
      throws InvalidKeySpecException {
        final Sec2SecretKeySpec result;
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        if (!keySpec.getSimpleName().equals(
          Sec2SecretKeySpec.class.getSimpleName())) {
            throw new InvalidKeySpecException(
              "Only Sec2SecretKeySpecs are supported.");
        }
        int length = 0;
        CipherAlgorithm algo = null;
        KeyType type = null;
        if (key instanceof DocumentKey) {
            DocumentKey dkey = (DocumentKey) key;
            length = dkey.getKey().getBytes().length;
            algo = CipherAlgorithm.valueOf(dkey.getAlgorithm());
            type = KeyType.DOCUMENT;
        } else if (key instanceof GroupKey) {
            GroupKey gkey = (GroupKey) key;
            length = gkey.getKey().getBytes().length;
            algo = CipherAlgorithm.valueOf(gkey.getAlgorithm());
            type = KeyType.GROUP;
        } else {
            throw new InvalidKeySpecException(
              "Only Sec2SecretKeys are supported.");
        }

        result = new Sec2SecretKeySpec(length, algo, type);

        return result;
    }

    @Override
    protected SecretKey engineTranslateKey(final SecretKey key)
      throws InvalidKeyException {
        if (!this.initialized) {
            // class not yet initialized, leave immediately
            return null;
        }

        throw new UnsupportedOperationException("Translation not supported.");
    }
}