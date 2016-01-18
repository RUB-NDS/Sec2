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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.opensaml.Configuration;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.BasicKeyInfoGeneratorFactory;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.KeyNameCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.saml.SAMLBaseConfig;
import org.sec2.saml.exceptions.NoSignatureException;
import org.sec2.saml.exceptions.SignatureEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base engine that supports typical XML signature operations.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
public abstract class SignatureEngine extends CryptoEngine {

    /**
     * Keysize of the dummy key. This is only used to make sure that the
     * KeyGenerator does not whine about a small keysize.
     */
    private static final int DUMMY_KEYSIZE = 512;

    /**
     * Keypair that OpenSAML uses for signing. May be a dummy credential that is
     * not used in reality.
     */
    private Credential signCredential;

    /**
     * A trust engine that checks XML signatures and tries to establish trust
     * to the credentials used.
     */
    private SignatureTrustEngine signatureTrustEngine;

    /**
     * Generates KeyInfo elements from a credential.
     */
    private final KeyInfoGenerator keyInfoGenerator;

    /**
     * Validator that checks non cryptographic requirements of signatures.
     */
    private final Sec2SignatureProfileValidator profileValidator;

    /**
     * Constructor.
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     * @throws SignatureEngineException if the signature algorithm is not
     *          supported
     */
    public SignatureEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws SignatureEngineException {
        super(rootCert, newSAMLEngine);
        BasicKeyInfoGeneratorFactory keyInfoGeneratorFactory =
                new BasicKeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityIDAsKeyName(true);
        keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
        profileValidator = new Sec2SignatureProfileValidator();
    }

    /**
     * Create dummy keypair.
     * @param identifier The identifier that is used als keyName in keyInfo
     *          elements of signatures this engine generates
     * @return A dummy keypair
     * @throws SignatureEngineException if the signature algorithm is not
     *          supported
     */
    protected final Credential createDummyCredential(final String identifier)
            throws SignatureEngineException {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance(
                    SAMLBaseConfig.XML_SIGNATURE_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureEngineException(
                    "Signature algorithm not supported", e);
        }
        keyGen.initialize(DUMMY_KEYSIZE);
        KeyPair pair = keyGen.generateKeyPair();
        BasicCredential dummy = new BasicCredential();
        dummy.setPrivateKey(pair.getPrivate());
        dummy.setPublicKey(pair.getPublic());
        dummy.setEntityId(identifier);
        dummy.setUsageType(UsageType.SIGNING);
        return dummy;
    }

    /**
     * @return the dummyCredential
     */
    protected final Credential getSignCredential() {
        return signCredential;
    }

    /**
     * @param pSignCredential the signCredential to set
     */
    protected final void setSignCredential(final Credential pSignCredential) {
        this.signCredential = pSignCredential;
    }

    /**
     * Signs a given signable SAML object.
     *
     * @param signableXMLObject The SignableXMLObject to sign.
     * @throws MarshallingException if the SignableXMLObject could not be
     * marshalled.
     * @throws SignatureEngineException if the SignableXMLObject could not be
     *          signed or if it is already signed.
     */
    public final void signXMLObject(final SignableXMLObject signableXMLObject)
            throws MarshallingException, SignatureEngineException {
        if (signableXMLObject.isSigned()) {
            throw new SignatureEngineException(
                    "SAML element is already signed");
        }
        // Generate <ds:Signature> element
        SignatureBuilder signatureElementBuilder = (SignatureBuilder)
                    Configuration.getBuilderFactory().getBuilder(
                    Signature.DEFAULT_ELEMENT_NAME);
        Signature signatureElement =
                (Signature) signatureElementBuilder.buildObject();
        signableXMLObject.setSignature(signatureElement);
        signatureElement.setSignatureAlgorithm(
                SAMLBaseConfig.XML_SIGNATURE_ALGORITHM_NS);
        signatureElement.setCanonicalizationAlgorithm(
                SAMLBaseConfig.XML_SIGNATURE_C14N_ALGORITHM_NS);
        // Attach a dummy key that has key identifier needed for <ds:keyInfo>
        signatureElement.setSigningCredential(getSignCredential());

        // generate <ds:keyInfo>
        try {
            signatureElement.setKeyInfo(keyInfoGenerator.generate(
                    signatureElement.getSigningCredential()));
        } catch (org.opensaml.xml.security.SecurityException e) {
            throw new SignatureEngineException(
                    "KeyInfo element could not be generated", e);
        }

        // Marshall the assertion. This creates the <ds:Signature> element
        // in the corresponding DOM tree.
        Configuration.getMarshallerFactory().
                getMarshaller(signableXMLObject).marshall(signableXMLObject);

        // Sign the XML element
        try {
            Signer.signObject(signatureElement);
        } catch (org.opensaml.xml.signature.SignatureException e) {
            throw new SignatureEngineException(
                    "SAML element could not be signed", e);
        }
    }

    /**
     * Returns a KeyInfoCredentialResolver that will be used by the
     * signature trust engine to resolve keys using the information
     * in a keyInfo element.
     *
     * @return the KeyInfoCredentialResolver to be used by the
     *          signature trust engine
     * @throws SignatureEngineException if the KeyInfoResolver cannot access
     *          the keys or if the credential is invalid in some way
     */
    protected abstract KeyInfoCredentialResolver getSignatureKeyInfoResolver()
            throws SignatureEngineException;

    /**
     * Returns a SignatureTrustEngine that checks XML signatures and tries to
     * establish trust to the credentials used. The SignatureTrustEngine is
     * created while the first call to this method. This is necessary
     * (and cannot be done in the constructor) since a subclass must set a
     * KeyInfoCredentialResolver first.
     *
     * @return a trust engine that checks XML signatures and tries to
     *          establish trust to the credentials used.
     * @throws SignatureEngineException if the KeyInfoResolver cannot access
     *          the keys
     */
    protected synchronized SignatureTrustEngine getSignatureTrustEngine()
            throws SignatureEngineException {
        if (this.signatureTrustEngine == null) {
            this.signatureTrustEngine = new PKIXSignatureTrustEngine(
                    this.getPKIXResolver(), this.getSignatureKeyInfoResolver());
        }
        return this.signatureTrustEngine;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkCertificateInfo(final X509Certificate cert) {
        boolean result = super.checkCertificateInfo(cert);
        Logger logger = getLogger();
        if (!cert.getKeyUsage()[KeyUsage.digitalSignature.index()]) {
            logger.warn("Certificate is not intended to be used for "
                    + "digital signatures");
            result = false;
        }
        if (cert.getKeyUsage()[KeyUsage.keyEncipherment.index()]
                || cert.getKeyUsage()[KeyUsage.dataEncipherment.index()]) {
            logger.warn("A certificate for digital signatures must not be "
                    + "used for encryption");
            result = false;
        }
        return result;
    }

    /**
     * Checks if an XML signature is valid and trusted.
     *
     * @param xml The signable XML to validate
     * @throws SignatureEngineException if the signature is invalid or the xml
     *          is unsigned
     */
    public final void validate(final SignableXMLObject xml)
            throws SignatureEngineException {
        if (xml.isSigned()) {
            try {
                profileValidator.validate(xml.getSignature());
            } catch (ValidationException e) {
                throw new SignatureEngineException("Signature did not conform "
                        + "to SAML Signature profile", e);
            }

            //TODO: test and extend CriteriaSet
            CriteriaSet criteria = new CriteriaSet();
            criteria.add(new UsageCriteria(UsageType.SIGNING));
            KeyInfo ki = xml.getSignature().getKeyInfo();
            if (ki != null && ki.getKeyNames() != null
                    && !ki.getKeyNames().isEmpty()) {
                for (KeyName kn : ki.getKeyNames()) {
                    criteria.add(new KeyNameCriteria(kn.getValue()));
                }
            } else {
                getLogger().warn("Signature contained no KeyInfo element or no "
                        + "KeyNames, could not resolve verification "
                        + "credentials");
            }

            try {
                if (!this.getSignatureTrustEngine().validate(
                        xml.getSignature(), criteria)) {
                    throw new SignatureEngineException(
                            "Signature verification failed");
                }
            } catch (org.opensaml.xml.security.SecurityException e) {
                throw new SignatureEngineException(e);
            }
        } else {
            throw new NoSignatureException("Element is unsigned, "
                    + "cannot validate");
        }
    }

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(SignatureEngine.class);
    }
}
