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
package org.sec2.saml.client.engine;

import java.util.List;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.signature.KeyName;
import org.sec2.saml.client.exceptions.ClientSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A verifier that checks whether a response meets all requirements
 * to allow further processing.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public class ClientResponseVerifier {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(
            ClientResponseVerifier.class);

    /**
     * The allowed difference between a timestamp in a response and the
     * client's clock in seconds. Value is +/-; if it's 12:00 and the offset
     * is set to 300 then all response-timestamps between 11:55 and 12:05 are
     * valid.
     */
    private final int timestampOffset;

    /**
     * Constructor.
     * @param allowedTimestampOffsetInSeconds The allowed difference between a
     *          timestamp in a response and the client's clock in seconds.
     */
    public ClientResponseVerifier(final int allowedTimestampOffsetInSeconds) {
        if (allowedTimestampOffsetInSeconds < 0) {
            throw new IllegalArgumentException(
                    "Parameter allowedTimestampOffsetInSeconds has to be "
                    + "positive");
        }
        timestampOffset = allowedTimestampOffsetInSeconds;
    }

    /**
     * Checks whether a response meets all requirements to allow further
     * processing.
     *
     * @param response the response to check
     * @param requestID the ID of request that the keyserver responded to
     * @throws ClientSecurityException if further processing of the response
     *          is denied.
     */
    public void verify(final Response response, final String requestID)
            throws ClientSecurityException {
        verifyAssertionStructure(response);
        verifyIssuer(response);
        verifyIDs(response, requestID);
        verifyVersion(response);
        verifyTimestamps(response);
        verifyAssertionConditions(response);
        verifyAttributeStructure(response);
    }

    /**
     * @param response the response to check
     * @throws ClientSecurityException if the response does not contain a
     *          single assertion with a single AttributeValue containing an
     *          EncryptedAttribute
     */
    private void verifyAssertionStructure(final Response response)
            throws ClientSecurityException {
        List<Assertion> assertions = response.getAssertions();
        if (assertions.size() != 1) {
            throw new ClientSecurityException("Response contains "
                    + assertions.size() + " assertions rather than a single "
                    + "one");
        }
    }

    /**
     * Checks that the issuer of the response and its assertion and the
     * key reference matches.
     * Ensures that keyserver A cannot sign a response of keyserver B.
     *
     * @param response the response to check
     * @return The validated issuer of the response
     * @throws ClientSecurityException if the issuer cannot be validated
     */
    private String verifyIssuer(final Response response)
            throws ClientSecurityException {
        // check that issuer and keyInfo match
        if (response.getIssuer() == null
                || response.getIssuer().getValue() == null
                || response.getIssuer().getValue().isEmpty()) {
            throw new ClientSecurityException("Response has no Issuer");
        }
        String issuerID = response.getIssuer().getValue();
        if (response.getSignature() == null
                || response.getSignature().getKeyInfo() == null) {
            throw new ClientSecurityException("Response has no KeyInfo");
        }
        List<KeyName> names =
                response.getSignature().getKeyInfo().getKeyNames();
        if (names.size() != 1) {
            throw new ClientSecurityException("Response has " + names.size()
                    + " KeyNames in its KeyInfo rather than a single one");
        }
        if (!names.get(0).getValue().equals(issuerID)) {
            throw new ClientSecurityException("KeyName and Issuer are not "
                    + "equal");
        }
        Issuer assertionIssuer = response.getAssertions().get(0).getIssuer();
        if (assertionIssuer == null || assertionIssuer.getValue() == null
                || assertionIssuer.getValue().isEmpty()) {
            throw new ClientSecurityException("Assertion has no Issuer");
        }
        if (!assertionIssuer.getValue().equals(issuerID)) {
            throw new ClientSecurityException(
                    "Response and Assertion have different issuers");
        }
        log.debug("Issuer: {}", issuerID);
        return issuerID;
    }

    /**
     * Checks that the response and its assertion have an ID and that it was
     * sent in response to the correct request.
     *
     * @param response the response to check
     * @param requestID the ID of request that the keyserver responded to
     * @return The response's ID
     * @throws ClientSecurityException if the response has no ID or the response
     *          was sent in response to some other request
     */
    private String verifyIDs(final Response response, final String requestID)
            throws ClientSecurityException {
        String responseID = response.getID();
        String assertionID = response.getAssertions().get(0).getID();
        if (responseID == null || responseID.isEmpty()) {
            throw new ClientSecurityException("Response has no ID");
        }
        if (assertionID == null || assertionID.isEmpty()) {
            throw new ClientSecurityException("Assertion has no ID");
        }
        if (response.getInResponseTo() != null
                && !response.getInResponseTo().equals(requestID)) {
            throw new ClientSecurityException(
                    "Response was sent in response to some other request");
        }
        log.debug("Response-ID: {}", responseID);
        return responseID;
    }

    /**
     * Checks that SAML version 2.0 is used.
     *
     * @param response the response to check
     * @throws ClientSecurityException if a version different to 2.0 is used
     */
    private void verifyVersion(final Response response)
            throws ClientSecurityException {
        SAMLVersion version = response.getVersion();
        if (version == null || version.getMajorVersion() != 2
                || version.getMinorVersion() != 0) {
            throw new ClientSecurityException(
                    "Response's SAML version is not 2.0");
        }
    }

    /**
     * Checks that the response is not outdated or not yet valid.
     *
     * @param response the response to check
     * @throws ClientSecurityException if the response has no timestamp
     *          or the timestamp is outdated or not yet valid.
     */
    private void verifyTimestamps(final Response response)
            throws ClientSecurityException {
        //TODO: check what happens if there is a clock change (DST <--> ST)
        DateTime notBefore = new DateTime().minusSeconds(timestampOffset);
        DateTime notAfter  = new DateTime().plusSeconds(timestampOffset);
        // check response
        DateTime timestamp = response.getIssueInstant();
        if (timestamp == null) {
            throw new ClientSecurityException(
                    "Response has no IssueInstant-timestamp");
        }
        if (timestamp.isBefore(notBefore)) {
            log.error("Response-Timestamp {} must not be before {}",
                    timestamp, notBefore);
            throw new ClientSecurityException("Response is outdated");
        }
        if (timestamp.isAfter(notAfter)) {
            log.error("Response-Timestamp {} must not be after {}",
                    timestamp, notAfter);
            throw new ClientSecurityException("Response is not yet valid");
        }
        // check assertion
        Assertion assertion = response.getAssertions().get(0);
        timestamp = assertion.getIssueInstant();
        if (timestamp == null) {
            throw new ClientSecurityException(
                    "Assertion has no IssueInstant-timestamp");
        }
        if (timestamp.isBefore(notBefore)) {
            log.error("Assertion-Timestamp {} must not be before {}",
                    timestamp, notBefore);
            throw new ClientSecurityException("Assertion is outdated");
        }
        if (timestamp.isAfter(notAfter)) {
            log.error("Assertion-Timestamp {} must not be after {}",
                    timestamp, notAfter);
            throw new ClientSecurityException("Assertion is not yet valid");
        }
    }

    /**
     * Checks the response-assertion's conditions.
     *
     * @param response the response to check
     * @throws ClientSecurityException if the response has no conditions or they
     *          are not met.
     */
    private void verifyAssertionConditions(final Response response)
            throws ClientSecurityException {
        Conditions conditions = response.getAssertions().get(0).getConditions();
        if (conditions == null) {
            throw new ClientSecurityException("Assertion has no conditions");
        }
        if (conditions.getNotBefore() == null
                || conditions.getNotOnOrAfter() == null) {
            throw new ClientSecurityException(
                    "Assertion-conditions miss a timestamp");
        }
        if (conditions.getNotBefore().isAfterNow()) {
            log.error("Assertion-Condition 'notBefore' ({}) is after now",
                    conditions.getNotBefore());
            throw new ClientSecurityException("Assertion is not yet valid");
        }
        if (conditions.getNotOnOrAfter().isBeforeNow()) {
            log.error("Assertion-Condition 'notOnOrAfter' ({}) is before now",
                   conditions.getNotOnOrAfter());
            throw new ClientSecurityException("Assertion is outdated");
        }
        if (conditions.getOneTimeUse() == null) {
            throw new ClientSecurityException(
                    "Assertion does not specify a OneTimeUsage-condition");
        }
    }

    /**
     * @param response the response to check
     * @throws ClientSecurityException if the response-assertion does not
     *          contain a single AttributeStatement/Attribute with a single
     *          AttributeValue containing an EncryptedAttribute
     */
    private void verifyAttributeStructure(final Response response)
            throws ClientSecurityException {
        List<AttributeStatement> attributeStatements =
                response.getAssertions().get(0).getAttributeStatements();
        if (attributeStatements.size() != 1) {
            throw new ClientSecurityException("Response contains "
                    + attributeStatements.size() + " AttributeStatements rather"
                    + " than a single one");
        }
        List<Attribute> attributes = attributeStatements.get(0).getAttributes();
        if (attributes.size() != 1) {
            throw new ClientSecurityException("Response contains "
                    + attributes.size() + " attributes rather than a single"
                    + " one");
        }
        List<XMLObject> attributeValues =
                attributes.get(0).getAttributeValues();
        if (attributeValues.size() != 1) {
            throw new ClientSecurityException("Attribute contains "
                    + attributeValues.size() + " AttributeValues rather than a "
                    + "single one");
        }
        if (!(attributeValues.get(0) instanceof XSAny)) {
            throw new ClientSecurityException("Attribute contains an "
                    + attributeValues.get(0).getElementQName() + " element "
                    + "instead of an AttributeValue (XSAny)");
        }
        XSAny attributeValue = (XSAny) attributeValues.get(0);
        if (attributeValue.getUnknownXMLObjects().size() != 1) {
            throw new ClientSecurityException("AttributeValue contains "
                    + attributeValue.getUnknownXMLObjects().size()
                    + " children rather than a single one");
        }
        if (!(attributeValue.getUnknownXMLObjects().get(0)
                instanceof EncryptedAttribute)) {
            throw new ClientSecurityException("AttributeValue contains an "
                    + attributeValue.getUnknownXMLObjects().get(0).
                    getElementQName() + " element "
                    + "instead of an EncryptedAttribute");
        }
    }

    /**
     * @return the allowed timestamp offset in seconds
     */
    public final int getAllowedTimestampDifferenceInSeconds() {
        return timestampOffset;
    }
}
