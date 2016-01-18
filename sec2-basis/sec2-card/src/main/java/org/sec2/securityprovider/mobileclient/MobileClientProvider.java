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

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import org.sec2.configuration.ConfigurationManager;
import org.sec2.configuration.exceptions.ExConfigurationInitializationFailure;
import org.sec2.configuration.exceptions.ExNoSuchProperty;
import org.sec2.configuration.exceptions.ExRestrictedPropertyAccess;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.serviceparameter.*;

/**
 * SecurityProvider to integrate EmSec CPP into the Java JCA.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 9, 2011
 */
public final class MobileClientProvider extends Provider {

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Description of the provider.
     */
    public static final String PROVIDER_INFO = "Sec2 Cryptographic Provider "
            + "at mobile client side";
    /**
     * Name of the provider.
     */
    public static final String PROVIDER_NAME = "Sec2CPProvider";
    /**
     * Version of the provider.
     */
    public static final double PROVIDER_VERSION = 0.1;
    /**
     * Current state of Tokentype. Set by standard to Hardware, should be only
     * changed once.
     */
    private static TokenType cTokenType = TokenType.UNSPECIFIED;
    /**
     * Fallback Token Type. If no statement is made from other sources, this
     * tokentype is used.
     */
    private static TokenType FALLBACK_TOKEN_TYPE = TokenType.HARDWARE_TOKEN;
    /**
     * ConfigurationManagers Key to Token Selector.
     */
    private final static String CONFIG_KEY_TOKEN_TYPE = "org.sec2"
            + ".securityprovider.tokentype";
    /**
     * Intialization flag, used to prevent finalizer attacks.
     */
    private boolean initialized = false;
    /**
     * PIN for the card.
     */
    private transient PIN pin = null;
    /**
     * Singleton ReferenceValue.
     */
    private static MobileClientProvider selfInstance = null;

    /**
     * Initialize the Client provider.
     *
     * @param pinCode PIN for the crypto device.
     * @param type specifies if the software simulator or the hardware token
     * should be used.
     */
    private MobileClientProvider(final byte[] pinCode) {
        super(PROVIDER_NAME, PROVIDER_VERSION, PROVIDER_INFO);

        // set the PIN
        this.pin = new PIN(pinCode);

        // Last check before set the Token
        if (cTokenType == TokenType.UNSPECIFIED)
            throw new IllegalStateException("Tokentype must be specified");
        

        // Tell the outside what kind of token is running
        this.setProperty(TokenType.TOKEN_TYPE_IDENTIFIER, cTokenType.name());


        // add services
        this.addServices();

        // mark provider as initialized
        this.initialized = true;
    }

    /**
     * Adds services to the service list.
     */
    private void addServices() {
        // put services
        Service service;
        // first entry of the parameters list MUST BE the algorithm!
        List<IServiceParameter> parameter = new ArrayList<IServiceParameter>(2);

        // dummy insertion -- will be overridden
        parameter.add(null);
        // add the PIN (due to convention earliest at second position)
        parameter.add(this.pin);

        // TODO fix new instantiation, temporary fix only!
        /**
         * Add the service parameters for signature.
         */
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.MD5withRSA);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);


        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.SHA1withRSA);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.SHA256withRSA);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);

        /**
         * Adds the service parameters for Encryption.
         */
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.RSA);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.CIPHER);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.AES);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.CIPHER);
        putService(service);

        /**
         * Adds the service parameter for the Keystore.
         */
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(KeyStoreType.STANDARD);
        parameter.add(this.cTokenType);
        parameter.add(this.pin);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.KEYSTORE);
        putService(service);


        /**
         * Adds the service parameter for the Key Generation.
         */
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.AES);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SECRETKEY_FACTORY);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.RSA);
        parameter.add(this.pin);
        parameter.add(this.cTokenType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SECRETKEY_FACTORY);
        putService(service);
    }

    /**
     * Checks if the provider was properly initizialized.
     *
     * @return True if the provider was properly initalized
     */
    public boolean isInitialized() {
        // looks silly but prevents object stealing from outer classes
        return this.initialized;
    }

    /**
     * To specify the tokentype manually, this method must be called before
     * instancing. If this method is never called, the central configuration is
     * consulted for choosing the Tokentype. If no configuration is available,
     * the Tokentypye Hardware is used.
     * 
     * Each value of the TokenType Enum is valid, the final check will be later.
     *
     * @param type
     * @throws IllegalStateException if the Mobile Security Provider is already
     * instantinated.
     */
    public static void setType(final TokenType type)
            throws IllegalPostInstantinationModificationException {
        if (selfInstance == null) {
            cTokenType = type;
        } else {
            throw new IllegalPostInstantinationModificationException(
                    "The Token type cannot be set after instantination.");
        }
    }

    /**
     * Getter for singleton Instance of MobileClientProvider, used instead of
     * Constructor. This constructor returns a provider communicating to the
     * hardware token. If the type is not set manually by
     *
     * @see setTokentype()
     *
     * the central configuration is consulted for choosing the Tokentype. If no
     * configuration is available, the HardwareToken is used.
     *
     * @param pinCode PinCode used for Login on Card
     * @return MobileClientProvider Instance
     */
    public static MobileClientProvider getInstance(final byte[] pinCode) {
        if (selfInstance == null) {
            /*
             *
             * Ask the configuration manger, wich tokentype should be used. If
             * no value is available or on any other Exception, the class
             * variable cTokentype will rest on unspecified.
             */
            if (cTokenType == TokenType.UNSPECIFIED) {
                try {
                    String tokenTypeConfig = ConfigurationManager.getInstance().
                            getConfigurationProperty(CONFIG_KEY_TOKEN_TYPE);
                    cTokenType = TokenType.valueOf(tokenTypeConfig);

                } catch (ExNoSuchProperty ex) {
                } catch (ExRestrictedPropertyAccess ex) {
                } catch (ExConfigurationInitializationFailure ex) {
                } catch (IllegalArgumentException ex) {
                }

            }

            if (cTokenType == TokenType.UNSPECIFIED) {
                cTokenType = FALLBACK_TOKEN_TYPE;
            }

            selfInstance = new MobileClientProvider(pinCode);
        }

        return selfInstance;
    }
    
    public static MobileClientProvider getInstance() {
        if (selfInstance != null && selfInstance.initialized) {
            return selfInstance;
        } else {
            throw new IllegalStateException("Mobile Client Provider not yet "
                    + "initialized, you need a PIN.");
        }
    }


    /**
     * TODO: WARNING: This method is used for tests and has to be removed for
     * release. By this method a test can destruct the provider to prevent the
     * use of a new instance of a jvm.
     *
     */
    @Deprecated
    public static void forTestDestructInstance() {

        /*
         * If not instantinated leave immidiatley
         */
        if (selfInstance == null) {
            return;
        }

        selfInstance.initialized = false;
        while (!selfInstance.getServices().isEmpty()) {
            selfInstance.removeService(
                    selfInstance.getServices().iterator().next());
        }
        selfInstance = null;
    }
}
