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
package org.sec2.saml;

import java.util.Properties;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.signature.SignatureConstants;

/**
 * Configuration of the SAML engine. Sets algorithms, hash functions, etc.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public class SAMLBaseConfig extends AbstractPropertyReadingConfig {

    /**
     * No instances allowed, utility class only.
     */
    protected SAMLBaseConfig() { }

    /**
     * Used to access the config file.
     */
    private static final Properties PROPERTIES = getPropertiesFromXML(
            "saml-base-config.xml");

    /**
     * The encoding used for streams.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The hash algorithm used to hash the public key.
     */
    public static final String DIGEST_ALGORITHM = "SHA-256";

    /**
     * Namespace used for the sec2 SAML messages in SAML-attributes.
     */
    public static final String SEC2_SAML_NS = "http://sec2.org/saml/v1/";

    /**
     * Namespace prefix used for the sec2 SAML messages in SAML-attributes.
     */
    public static final String SEC2_SAML_PREFIX =
            PROPERTIES.getProperty("saml.base.xmlNamespace");

    /**
     * The certificate type to be used, default "X.509".
     * Do not simply change this, you also need to change all the certificate
     * handling code if you want to change the type of certificates used.
     */
    public static final String CERTIFICATE_TYPE = "X.509";

    /**
     * The type of the keystore used, default "HardwareToken".
     */
    public static final String KEYSTORE_TYPE = "HardwareToken";

    /**
     * The ID of the ValidatorSuite used. Has to match the ID in the
     * config-file. See "resources/sec2saml-config.xml".
     */
    public static final String VALIDATOR_SUITE_ID = "sec2saml-schema-validator";

    /* ==================================================
     * ************** XML Signature Config **************
     * ==================================================
     */

    /**
     * Algorithm used for signing, default: "RSA".
     * Has to be a JCE-name for a public key scheme supported by XML-Signature.
     */
    public static final String XML_SIGNATURE_ALGORITHM = "RSA";

    /**
     * Namespace used for creating an XML signature, default:
     * "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".
     * Has to match the algorithm used for signing.
     */
    public static final String XML_SIGNATURE_ALGORITHM_NS =
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;

    /**
     * Namespace used for XML canonicalization, default:
     * "http://www.w3.org/2001/10/xml-exc-c14n#".
     */
    public static final String XML_SIGNATURE_C14N_ALGORITHM_NS
            = SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    /**
     * Namespace used for generating digests of referenced ressources, default:
     * "http://www.w3.org/2001/04/xmldsig-more#sha256".
     */
    public static final String XML_SIGNATURE_DIGEST_METHOD_NS
            = SignatureConstants.ALGO_ID_DIGEST_SHA256;

    /* ==================================================
     * ************* XML Encryption Config **************
     * ==================================================
     */

    /**
     * Algorithm used for encryption, default: "AES".
     * Has to be a JCE-name for a symmetric cipher scheme supported by
     * XML-Encryption.
     */
    public static final String XML_ENCRYPTION_ALGORITHM = "AES";

    /**
     * Keysize of the symmetric cipher, default: 128.
     */
    public static final int XML_ENCRYPTION_KEYSIZE = 128;

    /**
     * Namespace used for symmetric encryption algorithm, default:
     * "http://www.w3.org/2001/04/xmlenc#aes128-cbc".
     */
    public static final String XML_ENCRYPTION_ALGORITHM_NS
            = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM;

    /**
     * Namespace used for encapsulation of the symmetric key, default:
     * "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p".
     */
    public static final String XML_ENCRYPTION_KEYTRANSPORT_NS
            = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;
}
