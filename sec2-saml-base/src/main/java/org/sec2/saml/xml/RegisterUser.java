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
package org.sec2.saml.xml;

import javax.xml.namespace.QName;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * An interface for an XML element containing a user's certificates.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public interface RegisterUser extends ValidatingXMLObject, Sec2RequestMessage {
    /** Local name of the RegisterUser element. */
    String DEFAULT_ELEMENT_LOCAL_NAME = "registerUser";

    /** Default element name. */
    QName DEFAULT_ELEMENT_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * Get the signatureCertificate child element.
     *
     * @return the signatureCertificate child element
     */
    XSBase64Binary getSignatureCertificate();

    /**
     * @param newSignatureCertificate the signature certificate to set
     */
    void setSignatureCertificate(XSBase64Binary newSignatureCertificate);

    /**
     * Get the encryptionCertificate child element.
     *
     * @return the encryptionCertificate child element
     */
    XSBase64Binary getEncryptionCertificate();

    /**
     * @param newEncryptionCertificate the encryption certificate to set
     */
    void setEncryptionCertificate(XSBase64Binary newEncryptionCertificate);
}
