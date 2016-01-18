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

import java.security.NoSuchAlgorithmException;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.saml.xml.Sec2Message;

/**
 * Generates prepared SAML elements that contain basic information.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 31, 2012
 */
public abstract class PreparedElementGenerator {

    /**
     * Engine that is used for encryption of attributes.
     */
    private CipherEngine cipherEngine;

    /**
     * Generator used to generate random IDs.
     */
    private PrefixedIdentifierGenerator idGenerator;

    /**
     * Generator used to generate basic XML types.
     */
    private XSElementGenerator xsGenerator;

    /**
     * Constructor.
     * @param pCipherEngine Engine that is used for encryption of attributes
     * @throws NoSuchAlgorithmException if the ID-generator cannot be created
     */
    public PreparedElementGenerator(final CipherEngine pCipherEngine)
            throws NoSuchAlgorithmException {
        this.cipherEngine = pCipherEngine;
        this.idGenerator  = PrefixedIdentifierGenerator.getInstance();
        this.xsGenerator  = new XSElementGenerator();
    }

    /**
     * @return the idGenerator
     */
    public final PrefixedIdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * @return the xsGenerator
     */
    protected final XSElementGenerator getXsGenerator() {
        return xsGenerator;
    }

    /**
     * Generates an Attribute with a certain name which contains
     * the content provided.
     *
     * @param content The content of the attribute
     * @param name The name of the attribute
     * @return The new attribute
     */
    protected final Attribute buildAttribute(final XMLObject content,
            final String name) {
        Attribute attribute = SAMLEngine.getSAMLBuilder(Attribute.class,
                Attribute.DEFAULT_ELEMENT_NAME).buildObject();
        attribute.setName(name);
        XSAny attributeValue = xsGenerator.buildXSAny(
                AttributeValue.DEFAULT_ELEMENT_NAME);
        attributeValue.getUnknownXMLObjects().add(content);
        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    /**
     * Generates an EncryptedAttribute with a certain name which contains
     * the content provided in encrypted form.
     * The provided ID gets an "encrypted"-Prefix.
     *
     * @param content The content to encrypt
     * @param name The ID of the EncryptedAttribute; gets an "encrypted"-Prefix.
     * @param recipientKeyDigest the digest of the recipient's public key
     * @param dataKey The key to encrypt the data; optional, may be null for a
     *          random key
     * @return The EncryptedAttribute and the key used for encryption
     * @throws CipherEngineException if the encryption goes wrong
     */
    protected final Pair<EncryptedAttribute, Credential>
            buildEncryptedAttribute(final Sec2Message content,
            final String name, final byte[] recipientKeyDigest,
            final Credential dataKey) throws CipherEngineException {
        // Prepare attribute with name, AttributeValue and content
        Attribute attribute = buildAttribute((XMLObject) content,
                "encrypted" + name);

        // XML encrypt attribute
        Pair<Encrypter, Credential> encPair =
                cipherEngine.getEncrypter(recipientKeyDigest, dataKey);
        EncryptedAttribute encryptedAttribute = null;
        try {
            encryptedAttribute = encPair.getFirst().encrypt(attribute);
        } catch (org.opensaml.xml.encryption.EncryptionException e) {
            throw new CipherEngineException(e);
        }

        return new Pair<EncryptedAttribute, Credential>(
                encryptedAttribute, encPair.getSecond());
    }

    /**
     * Generates a SAML-Attribute that contains an EncryptedAttribute
     * which in turn contains the content provided in encrypted form.
     *
     * @param content The content to encrypt
     * @param recipientKeyDigest the digest of the recipient's public key
     * @param dataKey The key to encrypt the data; optional, may be null for a
     *          random key
     * @param prefix A prefix for the Attribute's name
     * @return The Attribute and the key used for encryption
     * @throws CipherEngineException if the encryption goes wrong
     */
    protected final Pair<Attribute, Credential>
            buildAttributeWithEncryptedContent(final Sec2Message content,
            final byte[] recipientKeyDigest, final Credential dataKey,
            final String prefix) throws CipherEngineException {
        String id = this.idGenerator.generatePrefixedIdentifier(prefix);
        Pair<EncryptedAttribute, Credential> encAttributePair =
                buildEncryptedAttribute(content, id, recipientKeyDigest,
                dataKey);
        // Wrap the EncryptedAttribute with an unencrypted Attribute
        Attribute attribute = buildAttribute(encAttributePair.getFirst(), id);
        return new Pair<Attribute, Credential>(attribute,
                encAttributePair.getSecond());
    }
}
