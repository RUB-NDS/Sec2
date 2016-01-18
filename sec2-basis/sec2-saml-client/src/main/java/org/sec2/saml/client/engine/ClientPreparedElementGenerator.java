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

import java.security.NoSuchAlgorithmException;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.PreparedElementGenerator;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.Sec2RequestMessage;

/**
 * Generates prepared SAML elements that contain basic information for the
 * client.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 31, 2012
 */
public final class ClientPreparedElementGenerator
        extends PreparedElementGenerator {

    /**
     * Constructor.
     * @param cipherEngine Engine that is used for encryption of attributes
     * @throws NoSuchAlgorithmException if the ID-generator cannot be created
     */
    public ClientPreparedElementGenerator(final CipherEngine cipherEngine)
            throws NoSuchAlgorithmException {
        super(cipherEngine);
    }

    /**
     * Creates a new AttributeQuery with preset version, id, timestamp,
     * issuer, etc.
     *
     * @param issuer The issuer of the AttributeQuery (e.g. the user's ID)
     * @return the preset AttributeQuery
     * @throws SAMLEngineException if the default element name of the
     *          AttributeQuery cannot be determined
     */
    public AttributeQuery buildBasicAttributeQuery(final String issuer)
            throws SAMLEngineException {
        AttributeQuery attQuery = SAMLEngine.getXMLObject(
                AttributeQuery.class);
        attQuery.setVersion(SAMLVersion.VERSION_20);
        attQuery.setID(getIdGenerator().
                generatePrefixedIdentifier("query"));
        attQuery.setIssueInstant(new DateTime());
        attQuery.setDestination(SAMLClientConfig.SEC2_KEYSERVER_URL);
        //Set Issuer
        Issuer issuerElement = SAMLEngine.getSAMLBuilder(Issuer.class,
                Issuer.DEFAULT_ELEMENT_NAME).buildObject();
        issuerElement.setValue(issuer);
        attQuery.setIssuer(issuerElement);
        // Set subject
        attQuery.setSubject(SAMLEngine.getXMLObject(Subject.class));
        attQuery.getSubject().setNameID(SAMLEngine.getXMLObject(NameID.class));
        attQuery.getSubject().getNameID().setFormat(NameID.ENTITY);
        attQuery.getSubject().getNameID().setValue(
                SAMLClientConfig.SEC2_KEYSERVER_URL);
        return attQuery;
    }

    /**
     * Generates a SAML-Attribute that contains an EncryptedAttribute
     * which in turn contains the content provided in encrypted form.
     *
     * @param content The content to encrypt
     * @param recipientKeyDigest the digest of the recipient's public key
     * @return The Attribute and the key used for encryption
     * @throws CipherEngineException if the encryption goes wrong
     */
    public Pair<Attribute, Credential>
            buildAttributeWithEncryptedContent(final Sec2RequestMessage content,
            final byte[] recipientKeyDigest) throws CipherEngineException {
        return this.buildAttributeWithEncryptedContent(content,
                recipientKeyDigest, null, "request");
    }
}
