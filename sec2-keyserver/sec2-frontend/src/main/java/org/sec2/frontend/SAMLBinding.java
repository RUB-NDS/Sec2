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
package org.sec2.frontend;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Pair;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.provider.Sec2Provider;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.frontend.exceptions.ErrorResponseException;
import org.sec2.frontend.exceptions.KeyserverSecurityException;
import org.sec2.frontend.exceptions.XMLProcessException;
import org.sec2.frontend.processors.BackendJob;
import org.sec2.frontend.processors.ISec2RequestMessageProcessor;
import org.sec2.frontend.processors.Sec2MessageProcessor;
import org.sec2.frontend.samlengine.KeyserverSAMLEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.saml.exceptions.NoSignatureException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.exceptions.SignatureEngineException;
import org.sec2.saml.xml.RegisterUser;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.sec2.saml.xml.Sec2ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Receives SAML-Queries and generates a SAML response.
 *
 * One word about SAMLEngineExceptions: This exception is only thrown on the
 * constructor. Then it is an unrecoverable error. All other
 * SAMLEngineExceptions are catched within this class to minimize exposing
 * internal processing details.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public final class SAMLBinding {

    /**
     * Class logger.
     */
    private final Logger log = LoggerFactory.getLogger(SAMLBinding.class);
    /**
     * The SAML engine used for all SAML operations.
     */
    private final KeyserverSAMLEngine engine;
    /**
     * The processor that connects the frontend to the backend.
     */
    private final ISec2RequestMessageProcessor processor;
    /**
     * Cache of recently used IDs. It would suffice to have only the keys, but
     * since this is a map, store the IssueInstant timestamp wo know when the
     * original request was sent (useful for logging).
     */
    private final Cache<String, DateTime> recentIDs;

    /**
     * Singleton Constructor.
     *
     * @throws SAMLEngineException if something went wrong instantiating the
     * SAMLEngine
     */
    private SAMLBinding() throws SAMLEngineException {
        engine = KeyserverSAMLEngine.getInstance();
        recentIDs = CacheBuilder.from(
                KeyserverFrontendConfig.RECENT_IDS_CACHE_CONFIG).build();
        processor = new Sec2MessageProcessor();
    }

    /**
     * Checks the request and dispatches generation of a response to the
     * corresponding processor.
     *
     * @param request The SAML-AttributeQuery as string
     * @param requestURL The URL the request was sent to
     * @return The SAML-response as string
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws KeyserverSecurityException if the keyserver aborted processing of
     * a request due to security considerations.
     * @throws XMLProcessException if the input could not be processed
     * @throws BackendProcessException if the keyserver fails to process a
     * request in the backend.
     * @throws ErrorResponseException if an error response could not be created
     */
    public String generateResponse(final String request,
            final String requestURL) throws MarshallingException,
            KeyserverSecurityException, XMLProcessException,
            BackendProcessException, ErrorResponseException {
        String response;
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter request must not be null or empty!");
        }
        if (requestURL == null || requestURL.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter requestURL must not be null or empty!");
        }

        Pair<BackendJob, Credential> jobKeyPair =
                parseContent(request, requestURL);
        Sec2ResponseMessage payload;
        payload = processor.process(jobKeyPair.getFirst());

        try {
            response = this.engine.createResponse(payload,
                    jobKeyPair.getFirst().getClientID(),
                    jobKeyPair.getFirst().getRequestID(),
                    jobKeyPair.getSecond());
        } catch (SAMLEngineException e) {
            e.log();
            throw new KeyserverSecurityException(e);
        }

        return response;
    }

    /**
     * Parses the request and checks all security pre-conditions.
     *
     * @param request The SAML-AttributeQuery as string
     * @param requestURL The URL the request was sent to
     * @return A container with the essential parts of the request and the key
     * used for encryption
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws KeyserverSecurityException if the keyserver aborted processing of
     * a request due to security considerations.
     * @throws XMLProcessException if the input could not be processed
     */
    private Pair<BackendJob, Credential> parseContent(final String request,
            final String requestURL)
            throws KeyserverSecurityException, XMLProcessException,
            MarshallingException {
        // parse the request
        AttributeQuery query;
        try {
            Element root = XMLHelper.parseXMLElement(request);
            query = engine.parseAttributeQuery(root);
        } catch (SAMLEngineException e) {
            e.log();
            throw new XMLProcessException(e.getLocalizedMessage(), e);
        }
        // check input
        boolean xmlIsSigned = validateRequest(query, requestURL);

        // decrypt the request
        XSAny attributeValue = (XSAny) query.getAttributes().get(0).
                getAttributeValues().get(0);
        EncryptedAttribute encContent = (EncryptedAttribute) attributeValue.
                getUnknownXMLObjects().get(0);
        Pair<Attribute, Credential> attributeKeyPair;
        try {
            attributeKeyPair = engine.getCipherEngine().decrypt(encContent);
        } catch (CipherEngineException e) {
            e.log();
            throw new KeyserverSecurityException(e);
        }
        Attribute content = attributeKeyPair.getFirst();
        if (log.isTraceEnabled()) { // saves performance if logging is disabled
            log.trace("Decrypted content: {}", XMLHelper.getXMLString(content));
        }

        // validate content
        try {
            engine.getSec2samlValidator().validate(content);
        } catch (ValidationException e) {
            throw new KeyserverSecurityException(e);
        }

        if (content.getAttributeValues().size() != 1) {
            throw new KeyserverSecurityException("Request does not contain "
                    + "exactly one AttributeValue in its EncryptedAttribute");
        }
        attributeValue = (XSAny) content.getAttributeValues().get(0);
        if (attributeValue.getUnknownXMLObjects().size() != 1) {
            throw new KeyserverSecurityException("Request does not contain "
                    + "exactly one child in its EncryptedAttribute's "
                    + "AttributeValue");
        }
        if (!(attributeValue.getUnknownXMLObjects().get(0)
                instanceof Sec2RequestMessage)) {
            throw new KeyserverSecurityException("Request does not contain "
                    + "a Sec2 message in its EncryptedAttribute's "
                    + "AttributeValue");
        }
        BackendJob job = new BackendJob((Sec2RequestMessage) attributeValue.
                getUnknownXMLObjects().get(0), query.getIssuer().getValue(),
                query.getID());
        if (!xmlIsSigned && !(attributeValue instanceof RegisterUser)) {
            throw new KeyserverSecurityException("Request is unsigned and "
                    + "contains a message which has to be signed");
        }
        return new Pair<BackendJob, Credential>(job,
                attributeKeyPair.getSecond());
    }

    /**
     * Validates XML, checks the signature (if present), checks if it's
     * trustworthy and verifies that the request meets all requirements needed
     * for processing.
     *
     * @param query The AttributeQuery to check
     * @param requestURL The URL the AttributeQuery was sent to
     * @return if the XML is signed at all
     * @throws KeyserverSecurityException if something prevents further
     * processing of the request
     */
    private boolean validateRequest(final AttributeQuery query,
            final String requestURL) throws KeyserverSecurityException {
        // validate XML
        try {
            engine.getSamlCoreValidator().validate(query);
        } catch (ValidationException e) {
            throw new KeyserverSecurityException(e);
        }

        boolean isSigned = true;
        // check the signature
        try {
            engine.getSignatureEngine().validate(query);
        } catch (SignatureEngineException e) {
            if (e instanceof NoSignatureException) {
                isSigned = false;
            } else {
                e.log();
                throw new KeyserverSecurityException(e);
            }
        }

        // verify non cryptographic requirements
        engine.getRequestVerifier().verify(query, requestURL);

        // check that the request is fresh
        if (recentIDs.getIfPresent(query.getID()) != null) {
            log.error("Request-ID has been used before ({}), "
                    + "assuming replay attack",
                    recentIDs.getIfPresent(query.getID()));
            throw new KeyserverSecurityException("Request-ID has been used "
                    + "before, assuming replay attack");
        } else {
            recentIDs.put(query.getID(), query.getIssueInstant());
        }
        return isSigned;
    }

    /**
     * Generates a SAML Response that indicates, that the keyserver does not
     * support the request.
     *
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @return The error SAML Response as String
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     */
    public String generateUnsupportedErrorResponse(final String message)
            throws MarshallingException, ErrorResponseException {
        return generateErrorResponse(StatusCode.REQUESTER_URI, null,
                message, null, null);
    }

    /**
     * Generates a SAML Response that indicates, that the keyserver denies
     * processing a request because of security considerations.
     *
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @return The error SAML Response as String
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     */
    public String generateDeniedErrorResponse(final String message)
            throws MarshallingException, ErrorResponseException {
        return generateErrorResponse(StatusCode.REQUESTER_URI, null,
                message, null, null);
    }

    /**
     * Generates a SAML Response that indicates, that the keyserver denies
     * processing a request because of security considerations.
     *
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @param clientID The ID of the client; will be used for the destination
     * attribute in form of 'http://<clientID>@sec2.org/'; may be null or empty
     * if the destination should not be present
     * @param requestID The ID of the request that this response answers; may be
     * null or empty if the InResponseTo-Attribute should not be present
     * @return The error SAML Response as String
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     */
    public String generateDeniedErrorResponse(
            final String message, final String clientID, final String requestID)
            throws MarshallingException, ErrorResponseException {
        return generateErrorResponse(StatusCode.REQUESTER_URI, null,
                message, clientID, requestID);
    }

    /**
     * Generates a SAML Response that indicates, that the keyserver cannot find
     * what the client requested.
     *
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @param clientID The ID of the client; will be used for the destination
     * attribute in form of 'http://<clientID>@sec2.org/'; may be null or empty
     * if the destination should not be present
     * @param requestID The ID of the request that this response answers; may be
     * null or empty if the InResponseTo-Attribute should not be present
     * @return The error SAML Response as String
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     */
    public String generateNotFoundResponse(
            final String message, final String clientID, final String requestID)
            throws MarshallingException, ErrorResponseException {
        return generateErrorResponse(StatusCode.RESPONDER_URI,
                StatusCode.RESOURCE_NOT_RECOGNIZED_URI, message, clientID,
                requestID);
    }

    /**
     * Generates a SAML Response that indicates an error.
     *
     * @param topLevelSAMLStatusCode The top-level statuscode to be used. Has to
     * match the SAML-standard; use one of the constants in
     * {@link org.opensaml.saml2.core.StatusCode}
     * @param secondLevelSAMLStatusCode The second-level statuscode to be used.
     * Has to match the SAML-standard; use one of the constants in
     * {@link org.opensaml.saml2.core.StatusCode}
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @param clientID The ID of the client; will be used for the destination
     * attribute in form of 'http://<clientID>@sec2.org/'; may be null or empty
     * if the destination should not be present
     * @param requestID The ID of the request that this response answers; may be
     * null or empty if the InResponseTo-Attribute should not be present
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     * @return The error SAML Response as String
     */
    private String generateErrorResponse(final String topLevelSAMLStatusCode,
            final String secondLevelSAMLStatusCode, final String message,
            final String clientID, final String requestID)
            throws MarshallingException, ErrorResponseException {
        final Response response = generateErrorResponseElement(
                topLevelSAMLStatusCode, secondLevelSAMLStatusCode,
                message, clientID, requestID);
        return XMLHelper.getXMLString(response);
    }

    /**
     * Generates a SAML Response that indicates an error.
     *
     * @param topLevelSAMLStatusCode The statuscode to be used. Has to match the
     * SAML-standard; use one of the constants in
     * {@link org.opensaml.saml2.core.StatusCode}
     * @param secondLevelSAMLStatusCode The second-level statuscode to be used.
     * Has to match the SAML-standard; use one of the constants in
     * {@link org.opensaml.saml2.core.StatusCode}
     * @param message A message to explain the status; optional, may be null or
     * empty if it should be omitted
     * @param clientID The ID of the client; will be used for the destination
     * attribute in form of 'http://<clientID>@sec2.org/'; may be null or empty
     * if the destination should not be present
     * @param requestID The ID of the request that this response answers; may be
     * null or empty if the InResponseTo-Attribute should not be present
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     * @return The error SAML Response
     */
    private Response generateErrorResponseElement(
            final String topLevelSAMLStatusCode,
            final String secondLevelSAMLStatusCode, final String message,
            final String clientID, final String requestID)
            throws MarshallingException, ErrorResponseException {
        Response response;
        try {
            response = engine.getPreparedElementGenerator().buildBasicResponse(
                    engine.getEntityIDBase64(), clientID, null,
                    topLevelSAMLStatusCode, message);
        } catch (SAMLEngineException e) {
            e.log();
            throw new ErrorResponseException(e);
        }

        if (requestID != null && !requestID.isEmpty()) {
            response.setInResponseTo(requestID);
        }

        if (secondLevelSAMLStatusCode != null) {
            response.getStatus().getStatusCode().setStatusCode(
                    SAMLEngine.getSAMLBuilder(StatusCode.class,
                    StatusCode.DEFAULT_ELEMENT_NAME).buildObject());
            response.getStatus().getStatusCode().getStatusCode().setValue(
                    secondLevelSAMLStatusCode);
        }

        try {
            engine.getSignatureEngine().signXMLObject(response);
        } catch (SignatureEngineException e) {
            e.log();
            response.setSignature(null);
        }
        return response;
    }

    /**
     * Singleton getter.
     *
     * @return The singleton instance
     * @throws SAMLEngineException if the SAMLBinding instance cannot be created
     */
    public static SAMLBinding getInstance() throws SAMLEngineException {
        if (SAMLBindingHolder.exception != null) {
            throw SAMLBindingHolder.exception;
        }
        return SAMLBindingHolder.instance;
    }

    /**
     * Unregisters crypto providers.
     */
    public static void unregisterProviders() {
        BackendHolder.getBackend().shutdown();
        Security.removeProvider(Sec2Provider.PROVIDER_NAME);
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class SAMLBindingHolder {

        /**
         * The singleton instance.
         */
        private static SAMLBinding instance;
        /**
         * SAMLEngineException that might have been thrown during creation of
         * SAMLBinding instance.
         */
        private static SAMLEngineException exception;

        static {
            try {
                instance = new SAMLBinding();
                //TODO: Find a cleverer trick to register a crypto provider
                Security.insertProviderAt(new Sec2Provider(
                        ConfigurationFactory.createDefault()), 1);
                Security.insertProviderAt(new BouncyCastleProvider(), 2);
            } catch (SAMLEngineException e) {
                e.log();
                exception = e;
            }
        }
    }
}
