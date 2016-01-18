/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.engine;

import java.security.cert.X509Certificate;
import javax.xml.namespace.QName;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.validation.ValidatorSuite;
import org.sec2.saml.SAMLBaseConfig;
import org.sec2.saml.Sec2SAMLBootstrap;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Abstract utility class that forms the entry point to the SAML processing.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 18, 2012
 */
public abstract class SAMLEngine {

    /**
     * An x.509 certificate that is trusted.
     */
    private X509Certificate trustedRootCertificate;
    /**
     * Generator used to generate basic XML types.
     */
    private final XSElementGenerator xsGenerator;
    /**
     * Validator used to validate SAML.
     */
    private final ValidatorSuite samlCoreValidator;
    /**
     * Validator used to validate Sec2 messages.
     */
    private final ValidatorSuite sec2samlValidator;

    /*
     * make sure that everything gets bootstrapped
     */
    static {
        try {
            Sec2SAMLBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new Error("A serious misconfiguration of OpenSAML was found",
                    e);
        }
    }

    /**
     * Bootstraps SAMLEngine.
     */
    protected SAMLEngine() {
        xsGenerator = new XSElementGenerator();
        samlCoreValidator = Configuration.getValidatorSuite(
                "saml2-core-schema-validator");
        sec2samlValidator = Configuration.getValidatorSuite(
                SAMLBaseConfig.VALIDATOR_SUITE_ID);
    }

    /**
     * Returns a SAML builder for a specific SAML object.
     *
     * @param <T> A specific SAML object whose builder is returned
     * @param samlType The SAML element for which a builder is to be returned
     * @param elementName The name of the SAML element for which a builder is to
     * be returned
     * @return The element's builder
     */
    public static <T extends SAMLObject> SAMLObjectBuilder<T> getSAMLBuilder(
            final Class<T> samlType, final QName elementName) {
        return (SAMLObjectBuilder<T>) getXMLBuilder(samlType, elementName);
    }

    /**
     * Returns an XML builder for a specific XML object. Throws a
     * ClassCastException if xmlType and elementName do not fit together
     *
     * @param <T> A specific XML object whose builder is returned
     * @param xmlType The XML element for which a builder is to be returned
     * @param elementName The name of the XML element for which a builder is to
     * be returned
     * @return The element's builder
     */
    public static <T extends XMLObject> XMLObjectBuilder<T> getXMLBuilder(
            final Class<T> xmlType, final QName elementName) {
        return (XMLObjectBuilder<T>) Configuration.getBuilderFactory().
                getBuilder(elementName);
    }

    /**
     * Get a specific XMLObject with a default element name.
     *
     * @param <T> The interface of the XMLObject to return
     * @param type The class literal of the XMLObject to return
     * @return The new XMLObject
     * @throws SAMLEngineException if the default element name of the XMLObject
     * cannot be determined
     */
    public static <T extends XMLObject> T getXMLObject(final Class<T> type)
            throws SAMLEngineException {
        QName defaultElementName;
        T element;
        try {
            defaultElementName = (QName) type.getDeclaredField(
                    "DEFAULT_ELEMENT_NAME").get(null);
            element = (T) getXMLBuilder(type, defaultElementName).
                    buildObject(defaultElementName);
        } catch (IllegalAccessException e) {
            throw new SAMLEngineException("Field 'DEFAULT_ELEMENT_NAME' of "
                    + "class " + type.getName() + "is not public.");
        } catch (NoSuchFieldException e) {
            throw new SAMLEngineException("Class " + type.getName() + "has no "
                    + "field 'DEFAULT_ELEMENT_NAME'");
        }
        return element;
    }

    /**
     * @return the trustedRootCertificate
     */
    public final synchronized X509Certificate getTrustedRootCertificate() {
        return trustedRootCertificate;
    }

    /**
     * Set a trusted root certificate. Allows only a single call to set the root
     * certificate. A subclass should call this method in its constructor to
     * protect the root certificate before any references to the instance exist.
     *
     * @param newRootCert The root certificate to set
     * @throws SAMLEngineException if a root certificate has already been set
     */
    protected final synchronized void setTrustedRootCertificate(
            final X509Certificate newRootCert) throws SAMLEngineException {
        if (newRootCert == null) {
            throw new IllegalArgumentException("Root certificate may not be "
                    + "null");
        }
        if (this.trustedRootCertificate == null) {
            this.trustedRootCertificate = newRootCert;
        } else {
            throw new SAMLEngineException(
                    "Root certificate cannot be overwritten");
        }
    }

    /**
     * @return the xsGenerator
     */
    public final XSElementGenerator getXSElementGenerator() {
        return xsGenerator;
    }

    /**
     * @return the samlCoreValidator
     */
    public final ValidatorSuite getSamlCoreValidator() {
        return samlCoreValidator;
    }

    /**
     * @return the signatureEngine
     */
    public abstract SignatureEngine getSignatureEngine();

    /**
     * @return the cipherEngine
     */
    public abstract CipherEngine getCipherEngine();

    /**
     * @return PreparedElementGenerator
     */
    public abstract PreparedElementGenerator getPreparedElementGenerator();

    /**
     * @return The ID of the entity this SAMLEngine works for (the hash of its
     * public key); null if the ID of the entity cannot be determined
     */
    public abstract byte[] getEntityID();

    /**
     * @return The ID of the entity this SAMLEngine works for Base64 encoded
     * (the hash of its public key); null if the ID of the entity cannot be
     * determined
     */
    public abstract String getEntityIDBase64();

    /**
     * @return the sec2samlValidator
     */
    public final ValidatorSuite getSec2samlValidator() {
        return sec2samlValidator;
    }
}
