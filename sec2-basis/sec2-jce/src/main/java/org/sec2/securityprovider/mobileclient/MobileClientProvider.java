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
     * Selectore for Token.
     */
    private TokenType mType = TokenType.HARDWARE_TOKEN;
    
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
     */
    private MobileClientProvider(final byte[] pinCode, final TokenType type) {
        super(PROVIDER_NAME, PROVIDER_VERSION, PROVIDER_INFO);
       
        // set the PIN
        this.pin = new PIN(pinCode);
        
        // set the Token
        this.mType = type;
        
        // Tell the outside what kind of token is running
        this.setProperty(TokenType.TOKEN_TYPE_IDENTIFIER, type.name());


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
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.MD5withRSA);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.SHA1withRSA);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(SignatureAlgorithm.SHA256withRSA);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SIGNATURE);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.RSA);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.CIPHER);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.AES);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.CIPHER);
        putService(service);
        
        //KeyStore
        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(KeyStoreType.Standard);
        parameter.add(this.mType);
        parameter.add(this.pin);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.KEYSTORE);
        putService(service);
        
        //SoftwareToken
  /*      parameter = new ArrayList<IServiceParameter>(2);
        parameter.add(TokenType.SOFTWARE_TOKEN);
        parameter.add(this.pin);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.KEYSTORE);
        putService(service);*/


        parameter = new ArrayList<IServiceParameter>(3);
        parameter.add(CipherAlgorithm.AES);
        parameter.add(this.pin);
        parameter.add(this.mType);
        service = new ServiceLoader(this, parameter,
                ServiceDescriptor.SECRETKEY_FACTORY);
        putService(service);

        parameter = new ArrayList<IServiceParameter>(2);
        parameter.add(CipherAlgorithm.RSA);
        parameter.add(this.pin);
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
     * Getter for singleton Instance of MobileClientProvider, used instead of
     * Constructor.
     *
     * @param pinCode PinCode used for Login on Card
     * @return MobileClientProvider Instance
     */
    public static MobileClientProvider getInstance(final byte[] pinCode) {
        if (selfInstance == null) {
            selfInstance = new MobileClientProvider(pinCode, TokenType.HARDWARE_TOKEN);
        
        }

        return selfInstance;
    }
    
       /**
     * Getter for singleton Instance of MobileClientProvider, used instead of
     * Constructor.
     *
     * @param pinCode PinCode used for Login on Card
     * @return MobileClientProvider Instance
     */
    public static MobileClientProvider getInstance(final byte[] pinCode,
                                                      final String type) {
     if (selfInstance == null) {
        selfInstance = new MobileClientProvider(pinCode, TokenType.valueOf(type));
         
        }
               

        return selfInstance;
    }
    
    
    public static void forTestDestructInstance(){
        selfInstance.initialized = false;
        while (!selfInstance.getServices().isEmpty())
            selfInstance.removeService(selfInstance.getServices().iterator().next());
        selfInstance = null;
    }
}
