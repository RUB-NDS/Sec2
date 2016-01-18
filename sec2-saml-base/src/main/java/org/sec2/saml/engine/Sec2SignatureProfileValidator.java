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

import org.opensaml.common.SignableSAMLObject;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validator that enforces additional security-related requirements for
 * XML signatures.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 16, 2012
 */
public class Sec2SignatureProfileValidator
                extends SAMLSignatureProfileValidator {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(
            Sec2SignatureProfileValidator.class);

    /** {@inheritDoc} */
    @Override
    protected void validateSignatureImpl(final SignatureImpl sigImpl)
            throws ValidationException {
        super.validateSignatureImpl(sigImpl);
        validateReferenceIsRoot((SignableSAMLObject) sigImpl.getParent());
    }

    /**
     * Validates that the XML signature signs the root element of the
     * corresponding xml document. Signing a subtree is not allowed to hamper
     * signature wrapping attacks.
     *
     * @param signableObject the SignableSAMLObject whose signature is being
     *          validated
     * @throws ValidationException if the Signature signs a subtree rather than
     *          the root element
     */
    protected void validateReferenceIsRoot(
            final SignableSAMLObject signableObject)
            throws ValidationException {
        Element reference = signableObject.getDOM();
        Document doc = reference.getOwnerDocument();
        if (!reference.isSameNode(doc.getDocumentElement())) {
            log.error("Signature Reference URI does not point to the documents "
                    + "root element");
            throw new ValidationException("Signature Reference URI does not "
                    + "point to the documents root element");
        }
    }
}
