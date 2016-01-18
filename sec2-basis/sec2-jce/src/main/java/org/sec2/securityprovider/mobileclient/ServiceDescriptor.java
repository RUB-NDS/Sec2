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

import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;
import org.sec2.securityprovider.serviceparameter.IServiceParameter;
import org.sec2.securityprovider.serviceparameter.KeyStoreType;
import org.sec2.securityprovider.serviceparameter.SignatureAlgorithm;

/**
 * Service listing of this service provider.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Jul 25, 2011
 */
public enum ServiceDescriptor {

    /**
     * Cipher service descriptor.
     */
    CIPHER("Cipher",
    CipherAlgorithm.values(),
    CipherImpl.class),
    /**
     * Key store service descriptor.
     */
    KEYSTORE("KeyStore",
    KeyStoreType.values(),
    KeyStoreImpl.class),
    /**
     * Signature service descriptor.
     */
    SIGNATURE("Signature",
    SignatureAlgorithm.values(),
    SignatureImpl.class),
    /**
     * Secret key factory service descriptor.
     */
    SECRETKEY_FACTORY("SecretKeyFactory",
    CipherAlgorithm.values(),
    SecretKeyFactoryImpl.class);
    /**
     * String representation of this service type.
     */
    private String serviceType;
    /**
     * Associated service parameter.
     */
    private IServiceParameter[] serviceParameter;
    /**
     * Implementation class.
     */
    private Class implementingClass;

    /**
     * Private constructor for service description enums.
     *
     * @param type String representation of the type of this service.
     * @param parameter Parameters for this service.
     * @param implementation Clss implementing this service.
     */
    private ServiceDescriptor(final String type,
            final IServiceParameter[] parameter,
            final Class implementation) {
        this.serviceType = type;
        this.serviceParameter = parameter;
        this.implementingClass = implementation;
    }

    /**
     * Getter for the service type.
     *
     * @return Service type
     */
    String getServiceType() {
        return this.serviceType;
    }

    /**
     * Getter for the service parameters.
     *
     * @return Service parameter array
     */
    IServiceParameter[] getServiceParameter() {
        return this.serviceParameter;
    }

    /**
     * Getter for implementing class.
     *
     * @return Canonical name of the implementing class
     */
    Class getImplementingClass() {
        return this.implementingClass;
    }
}
