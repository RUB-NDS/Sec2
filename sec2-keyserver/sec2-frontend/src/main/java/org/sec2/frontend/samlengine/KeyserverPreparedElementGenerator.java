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

import java.security.NoSuchAlgorithmException;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Pair;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.PreparedElementGenerator;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.Sec2ResponseMessage;

/**
 * Generates prepared SAML elements that contain basic information for the
 * keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 18, 2012
 */
public class KeyserverPreparedElementGenerator
                extends PreparedElementGenerator {

    /**
     * Constructor.
     * @param cipherEngine Engine that is used for encryption of attributes
     * @throws NoSuchAlgorithmException if the ID-generator cannot be created
     */
    public KeyserverPreparedElementGenerator(final CipherEngine cipherEngine)
            throws NoSuchAlgorithmException {
        super(cipherEngine);
    }

    /**
     * Creates a new Response with preset version, id, timestamp, etc.
     *
     * @param issuer The issuer of the Response (e.g. the keyserver's ID)
     * @param clientID The ID of the client; will be used for the destination
     *          attribute in form of 'http://<clientID>@sec2.org/';
     *          may be null or empty if the destination should not be present
     * @param requestID the ID of the request that triggered this response;
     *          used for the InResponseTo-Attribute; may be null to omit the
     *          attribute
     * @param samlStatusCode The statuscode to be used. Has to match the
     *          SAML-standard; use one of the constants in
     *          {@link org.opensaml.saml2.core.StatusCode}
     * @param message A message to explain the status; optional,
     *          may be null or empty if it should be omitted
     * @return the preset Response
     * @throws SAMLEngineException if the default element name of the
     *          Response cannot be determined
     */
    public Response buildBasicResponse(final String issuer,
            final String clientID, final String requestID,
            final String samlStatusCode, final String message)
            throws SAMLEngineException {
        Response response = SAMLEngine.getXMLObject(Response.class);
        response.setVersion(SAMLVersion.VERSION_20);
        response.setID(this.getIdGenerator().
                generatePrefixedIdentifier("response"));
        response.setIssueInstant(new DateTime());
        response.setInResponseTo(requestID);
        response.setIssuer(SAMLEngine.getXMLObject(Issuer.class));
        response.getIssuer().setValue(issuer);
        // Set destination
        if (clientID != null && !clientID.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("http://");
            builder.append(clientID);
            builder.append("@sec2.org/");
            response.setDestination(builder.toString());
        }
        response.setStatus(SAMLEngine.getXMLObject(Status.class));
        response.getStatus().setStatusCode(
                SAMLEngine.getXMLObject(StatusCode.class));
        response.getStatus().getStatusCode().setValue(samlStatusCode);
        if (message != null && !message.isEmpty()) {
            response.getStatus().setStatusMessage(
                    SAMLEngine.getXMLObject(StatusMessage.class));
            response.getStatus().getStatusMessage().setMessage(message);
        }
        return response;
    }

    /**
     * Creates a new Assertion with preset version, id, timestamp, etc.
     *
     * @param issuer The issuer of the Assertion (e.g. the keyserver's ID)
     * @return the preset Assertion
     * @throws SAMLEngineException if the default element name of the
     *          Assertion cannot be determined
     */
    public Assertion buildBasicAssertion(final String issuer)
            throws SAMLEngineException {
        Assertion assertion = SAMLEngine.getXMLObject(Assertion.class);
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setID(this.getIdGenerator().
                generatePrefixedIdentifier("assertion"));
        assertion.setIssueInstant(new DateTime());
        assertion.setIssuer(SAMLEngine.getXMLObject(Issuer.class));
        assertion.getIssuer().setValue(issuer);
        assertion.setConditions(SAMLEngine.getXMLObject(Conditions.class));
        assertion.getConditions().setNotBefore(new DateTime().minusSeconds(
                KeyserverFrontendConfig.ASSERTION_VALIDITY));
        assertion.getConditions().setNotOnOrAfter(new DateTime().plusSeconds(
                KeyserverFrontendConfig.ASSERTION_VALIDITY));
        assertion.getConditions().getConditions().add(
                SAMLEngine.getXMLObject(OneTimeUse.class));
        // Set subject
        assertion.setSubject(SAMLEngine.getXMLObject(Subject.class));
        assertion.getSubject().setNameID(SAMLEngine.getXMLObject(NameID.class));
        assertion.getSubject().getNameID().setFormat(NameID.ENTITY);
        assertion.getSubject().getNameID().setValue(issuer);
        return assertion;
    }

    /**
     * Generates a SAML-Attribute that contains an EncryptedAttribute
     * which in turn contains the content provided in encrypted form.
     *
     * @param content The content to encrypt
     * @param recipientKeyDigest the digest of the recipient's public key
     * @param dataKey The key to encrypt the data; optional, may be null for a
     *          random key
     * @return The Attribute and the key used for encryption
     * @throws CipherEngineException if the encryption goes wrong
     */
    public Pair<Attribute, Credential>
            buildAttributeWithEncryptedContent(
            final Sec2ResponseMessage content, final byte[] recipientKeyDigest,
            final Credential dataKey) throws CipherEngineException {
        Pair<Attribute, Credential> pair =
                this.buildAttributeWithEncryptedContent(content,
                recipientKeyDigest, dataKey, "response");
        XSAny attributeValue = (XSAny) pair.getFirst().
                getAttributeValues().get(0);
        EncryptedAttribute encContent = (EncryptedAttribute)
                attributeValue.getUnknownXMLObjects().get(0);
        encContent.getEncryptedData().setKeyInfo(null);
        return pair;
    }
}
