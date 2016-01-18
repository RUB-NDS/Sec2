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
package org.sec2.frontend.samlengine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.signature.ContentReference;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.SignatureConstants;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.frontend.exceptions.KeyserverSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A verifier that checks whether a request meets all requirements to allow
 * further processing.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 15, 2012
 */
public class KeyserverRequestVerifier {

    /**
     * Class logger.
     */
    private final Logger log = LoggerFactory.getLogger(
            KeyserverRequestVerifier.class);
    /**
     * The allowed difference between a timestamp in a request and the
     * keyserver's clock in seconds. Value is +/-; if it's 12:00 and the offset
     * is set to 300 then all request-timestamps between 11:55 and 12:05 are
     * valid.
     */
    private final int timestampOffset;

    /**
     * Constructor.
     *
     * @param allowedTimestampOffsetInSeconds The allowed difference between a
     * timestamp in a request and the keyserver's clock in seconds.
     */
    public KeyserverRequestVerifier(final int allowedTimestampOffsetInSeconds) {
        if (allowedTimestampOffsetInSeconds < 0) {
            throw new IllegalArgumentException(
                    "Parameter allowedTimestampOffsetInSeconds has to be "
                    + "positive");
        }
        timestampOffset = allowedTimestampOffsetInSeconds;
    }

    /**
     * Checks whether a request meets all requirements to allow further
     * processing.
     *
     * @param request the request to check
     * @param requestURL the URL the request was sent to
     * @throws KeyserverSecurityException if further processing of the request
     * is denied.
     */
    public void verify(final AttributeQuery request, final String requestURL)
            throws KeyserverSecurityException {
        verifyIssuer(request);
        verifyID(request);
        verifyVersion(request);
        verifyDestination(request, requestURL);
        verifyTimestamp(request);
        verifyAttributeStructure(request);
        verifyAllowedCryptoAlgos(request);
    }

    /**
     * Checks that the issuer of the request and the key reference matches.
     * Ensures that client A cannot sign a request of client B.
     *
     * @param request the request to check
     * @return The validated issuer of the request
     * @throws KeyserverSecurityException if the issuer cannot be validated
     */
    private String verifyIssuer(final AttributeQuery request)
            throws KeyserverSecurityException {
        // check that issuer and keyInfo match
        if (request.getIssuer() == null
                || request.getIssuer().getValue() == null
                || request.getIssuer().getValue().isEmpty()) {
            throw new KeyserverSecurityException("Request has no Issuer");
        }
        String issuerID = request.getIssuer().getValue();
        if (request.getSignature() == null
                || request.getSignature().getKeyInfo() == null) {
            throw new KeyserverSecurityException("Request has no KeyInfo");
        }
        List<KeyName> names = request.getSignature().getKeyInfo().getKeyNames();
        if (names.size() != 1) {
            throw new KeyserverSecurityException("Request has " + names.size()
                    + " KeyNames in its KeyInfo rather than a single one");
        }
        if (!names.get(0).getValue().equals(issuerID)) {
            throw new KeyserverSecurityException("KeyName and Issuer are not "
                    + "equal");
        }
        log.debug("Issuer: {}", issuerID);
        MDC.put(KeyserverFrontendConfig.MDC_KEY_USER, issuerID);
        return issuerID;
    }

    /**
     * Checks that the request has an ID.
     *
     * @param request the request to check
     * @return The request's ID
     * @throws KeyserverSecurityException if the request has no ID
     */
    private String verifyID(final AttributeQuery request)
            throws KeyserverSecurityException {
        String requestID = request.getID();
        if (requestID == null || requestID.isEmpty()) {
            throw new KeyserverSecurityException(
                    "Request has no ID");
        }
        log.debug("Request-ID: {}", requestID);
        MDC.put(KeyserverFrontendConfig.MDC_KEY_REQUEST, requestID);
        return requestID;
    }

    /**
     * Checks that SAML version 2.0 is used.
     *
     * @param request the request to check
     * @throws KeyserverSecurityException if a version different to 2.0 is used
     */
    private void verifyVersion(final AttributeQuery request)
            throws KeyserverSecurityException {
        SAMLVersion version = request.getVersion();
        if (version == null || version.getMajorVersion() != 2
                || version.getMinorVersion() != 0) {
            throw new KeyserverSecurityException(
                    "Request's SAML version is not 2.0");
        }
    }

    /**
     * Checks that the request was not rerouted and really designed to be
     * processed by this keyserver instance. Filter may be to aggressive if an
     * HTTP-firewall or sth. similar is used which legally forwards a request to
     * another URL.
     *
     * @param requestURLString the URL the request was sent to
     * @param request the request to check
     * @throws KeyserverSecurityException if the request has no destination or
     * was designed to be processed by another keyserver instance.
     */
    private void verifyDestination(final AttributeQuery request,
            final String requestURLString) throws KeyserverSecurityException {
        String destination = request.getDestination();
        if (destination == null || destination.isEmpty()) {
            throw new KeyserverSecurityException(
                    "Request's destination is unknown");
        }
        if (requestURLString == null || requestURLString.isEmpty()) {
            throw new IllegalArgumentException(
                    "Request's intended destination must not be null");
        }
        try {
            // Using URIs prevents name resolution
            if (!normalizeURL(destination).equals(
                    normalizeURL(requestURLString))) {
                log.error("Request was sent to '{}', but received by "
                        + "'{}'. Are you using an HTTP-firewall or sth. similar"
                        + " that legally forwards requests to another URL?",
                        destination, requestURLString);
                throw new KeyserverSecurityException(
                        "Request's destination is not the URL it was sent to");
            }
        } catch (URIException e) {
            throw new KeyserverSecurityException(
                    "Request's destination URLs cannot be compared", e);
        } catch (MalformedURLException e) {
            throw new KeyserverSecurityException(
                    "Request's destination URLs cannot be compared", e);
        }
        log.debug("Request-Destination: {}", destination);
    }

    /**
     * Canonicalizes URLs for comparison.
     *
     * @param pURL the URL as string
     * @return the URL as URI
     * @throws MalformedURLException if the URL is malformed
     * @throws URIException if the URL cannot be converted into a URI
     */
    private URI normalizeURL(final String pURL)
            throws MalformedURLException, URIException {
        URL url = new URL(pURL);
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        String file = url.getFile();
        if (file.isEmpty()) {
            file = "/";
        }
        return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                port, file, url.getRef());
    }

    /**
     * Checks that the request is not outdated or not yet valid.
     *
     * @param request the request to check
     * @throws KeyserverSecurityException if the request has no timestamp or the
     * timestamp is outdated or not yet valid.
     */
    private void verifyTimestamp(final AttributeQuery request)
            throws KeyserverSecurityException {
        //TODO: check what happens if there is a clock change (DST <--> ST)
        DateTime notBefore = new DateTime().minusSeconds(timestampOffset);
        DateTime notAfter = new DateTime().plusSeconds(timestampOffset);
        DateTime timestamp = request.getIssueInstant();
        if (timestamp == null) {
            throw new KeyserverSecurityException(
                    "Request has no IssueInstant-timestamp");
        }
        if (timestamp.isBefore(notBefore)) {
            log.error("Request-Timestamp {} must not be before {}",
                    timestamp, notBefore);
            throw new KeyserverSecurityException("Request is outdated");
        }
        if (timestamp.isAfter(notAfter)) {
            log.error("Request-Timestamp {} must not be after {}",
                    timestamp, notAfter);
            throw new KeyserverSecurityException("Request is not yet valid");
        }
    }

    /**
     * @param request the request to check
     * @throws KeyserverSecurityException if the request does not contain a
     * single attribute with a single AttributeValue containing an
     * EncryptedAttribute
     */
    private void verifyAttributeStructure(final AttributeQuery request)
            throws KeyserverSecurityException {
        List<Attribute> attributes = request.getAttributes();
        if (attributes.size() != 1) {
            throw new KeyserverSecurityException("Request contains "
                    + attributes.size() + " attributes rather than a single"
                    + " one");
        }
        List<XMLObject> attributeValues =
                attributes.get(0).getAttributeValues();
        if (attributeValues.size() != 1) {
            throw new KeyserverSecurityException("Attribute contains "
                    + attributeValues.size() + " AttributeValues rather than a "
                    + "single one");
        }
        if (!(attributeValues.get(0) instanceof XSAny)) {
            throw new KeyserverSecurityException("Attribute contains an "
                    + attributeValues.get(0).getElementQName() + " element "
                    + "instead of an AttributeValue (XSAny)");
        }
        XSAny attributeValue = (XSAny) attributeValues.get(0);
        if (attributeValue.getUnknownXMLObjects().size() != 1) {
            throw new KeyserverSecurityException("AttributeValue contains "
                    + attributeValue.getUnknownXMLObjects().size()
                    + " children rather than a single one");
        }
        if (!(attributeValue.getUnknownXMLObjects().get(0)
                instanceof EncryptedAttribute)) {
            throw new KeyserverSecurityException("AttributeValue contains an "
                    + attributeValue.getUnknownXMLObjects().get(0).
                    getElementQName() + " element "
                    + "instead of an EncryptedAttribute");
        }
    }

    /**
     * NOTE: Requires that verifyAttributeStructure() has been called before.
     *
     * @param request the request to check
     * @throws KeyserverSecurityException if the request uses a discouraged
     * crypto algorithm
     */
    private void verifyAllowedCryptoAlgos(final AttributeQuery request)
            throws KeyserverSecurityException {
        // XML Signature
        if (request.isSigned()) {
            String signAlgo = request.getSignature().getSignatureAlgorithm();
            if (!signAlgo.equals(
                    SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256)
                    && !signAlgo.equals(
                    SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384)
                    && !signAlgo.equals(
                    SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512)) {
                throw new KeyserverSecurityException("A signature that does "
                        + "not use RSA and a SHA2-family-hash was found: "
                        + signAlgo);
            }
            for (ContentReference ref
                    : request.getSignature().getContentReferences()) {
                if (!(ref instanceof SAMLObjectContentReference)) {
                    throw new KeyserverSecurityException("Signature in SAML "
                            + "request does not sign a SAML object");
                }
                SAMLObjectContentReference sRef =
                        (SAMLObjectContentReference) ref;
                String digestAlgo = sRef.getDigestAlgorithm();
                if (!digestAlgo.equals(SignatureConstants.ALGO_ID_DIGEST_SHA256)
                        && !digestAlgo.equals(
                        SignatureConstants.ALGO_ID_DIGEST_SHA384)
                        && !digestAlgo.equals(
                        SignatureConstants.ALGO_ID_DIGEST_SHA512)) {
                    throw new KeyserverSecurityException("A signature "
                            + "reference that does not use a SHA2-family-hash "
                            + "was found: " + digestAlgo);
                }
            }
        }

        // XML Encryption
        EncryptedAttribute encAttrib;
        try {
            XSAny attributeValue = (XSAny) request.getAttributes().get(0).
                    getAttributeValues().get(0);
            encAttrib = (EncryptedAttribute) attributeValue.
                    getUnknownXMLObjects().get(0);
        } catch (NullPointerException e) {
            log.error("You did not verify the request's attribute structure "
                    + "before verifying the allowed crypto algorithms", e);
            throw e;
        } catch (ClassCastException e) {
            log.error("You did not verify the request's attribute structure "
                    + "before verifying the allowed crypto algorithms", e);
            throw e;
        }
        String encAlgo = encAttrib.getEncryptedData().getEncryptionMethod().
                getAlgorithm();
        if (!encAlgo.equals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM)
                && !encAlgo.equals(
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192_GCM)
                && !encAlgo.equals(
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM)) {
            throw new KeyserverSecurityException("Non GCM-enabled algorithm "
                    + "used for encryption: " + encAlgo);
        }
        // Check that there is only one encrypted key
        if (encAttrib.getEncryptedKeys().size() != 1) {
            // well, XML-encryption has reasons to allow multiple keys, but this
            // only complicates things in the sec2 case. Here one could also
            // implement a method that iterates over all keys and picks the
            // correct one
            throw new KeyserverSecurityException("EncryptedAttribute contains "
                    + encAttrib.getEncryptedKeys().size() + " keys");
        }
        String keyTransportAlgo = encAttrib.getEncryptedKeys().get(0).
                getEncryptionMethod().getAlgorithm();
        if (!keyTransportAlgo.equals(
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP)) {
            throw new KeyserverSecurityException("Non RSA-OAEP-MGF1P algorithm "
                    + "used for encryption of key: " + keyTransportAlgo);
        }
    }

    /**
     * @return the allowed timestamp offset in seconds
     */
    public final int getAllowedTimestampDifferenceInSeconds() {
        return timestampOffset;
    }
}
