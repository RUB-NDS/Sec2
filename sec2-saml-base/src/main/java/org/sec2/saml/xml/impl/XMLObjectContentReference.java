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
package org.sec2.saml.xml.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.NamespaceManager;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.ContentReference;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.LazyList;
import org.opensaml.xml.util.LazySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//CHECKSTYLE.OFF: LineLengthCheck - Cannot make these lines shorter
/**
 * A content reference for XML objects that will be signed. Class is analogous
 * to {@link org.opensaml.common.impl.SAMLObjectContentReference}.
 *
 * <p>
 * The default digest algorithm used is the value configured in the global
 * security configuration's
 * {@link org.opensaml.xml.security.SecurityConfiguration#getSignatureReferenceDigestMethod()},
 * if available, otherwise it will be
 * {@link org.opensaml.xml.signature.SignatureConstants#ALGO_ID_DIGEST_SHA384}.
 * </p>
 *
 * <p>
 * The default set of transforms applied consists of
 * {@link org.opensaml.xml.signature.SignatureConstants#TRANSFORM_ENVELOPED_SIGNATURE}
 * and
 * {@link org.opensaml.xml.signature.SignatureConstants#TRANSFORM_C14N_EXCL_WITH_COMMENTS}.
 * </p>
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
//CHECKSTYLE.ON: LineLengthCheck
public class XMLObjectContentReference implements ContentReference {

    /**
     * Class logger.
     */
    private final Logger log = LoggerFactory.getLogger(
            XMLObjectContentReference.class);
    /**
     * XMLObject this reference refers to.
     */
    private SignableXMLObject signableObject;
    /**
     * Algorithm used to digest the content.
     */
    private String digestAlgorithm;
    /**
     * Transforms applied to the content.
     */
    private List<String> transforms;

    /**
     * Constructor.
     *
     * @param newSignableObject the XMLObject this reference refers to
     */
    public XMLObjectContentReference(
            final SignableXMLObject newSignableObject) {
        signableObject = newSignableObject;
        transforms = new LazyList<String>();

        // Set defaults
        if (Configuration.getGlobalSecurityConfiguration() != null) {
            digestAlgorithm = Configuration.getGlobalSecurityConfiguration().
                    getSignatureReferenceDigestMethod();
        }
        if (digestAlgorithm == null) {
            digestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA384;
        }

        transforms.add(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
    }

    /**
     * Gets the transforms applied to the content prior to digest generation.
     *
     * @return the transforms applied to the content prior to digest generation
     */
    public List<String> getTransforms() {
        return transforms;
    }

    /**
     * Gets the algorithm used to digest the content. s
     *
     * @return the algorithm used to digest the content
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * Sets the algorithm used to digest the content.
     *
     * @param newAlgorithm the algorithm used to digest the content
     */
    public void setDigestAlgorithm(final String newAlgorithm) {
        digestAlgorithm = newAlgorithm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createReference(final XMLSignature signature) {
        try {
            Transforms dsigTransforms = new Transforms(signature.getDocument());
            for (int i = 0; i < transforms.size(); i++) {
                String transform = transforms.get(i);
                dsigTransforms.addTransform(transform);

                if (transform.equals(
                        SignatureConstants.TRANSFORM_C14N_EXCL_WITH_COMMENTS)
                        || transform.equals(
                        SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS)) {

                    processExclusiveTransform(signature,
                            dsigTransforms.item(i));
                }
            }

            /*
             * Find the reference
             *
             * Why did the OpenSAML Devs define the method
             * "getSignatureReferenceID()" in SignableSAMLObject rather than in
             * SignableXMLObject. It not SAML specific!
             */
            String referenceID = null;
            try {
                referenceID = (String) signableObject.getClass().
                        getDeclaredMethod("getSignatureReferenceID").
                        invoke(signableObject);
            } catch (NoSuchMethodException e) {
                log.debug("Class {} has no method getSignatureReferenceID()",
                        signableObject.getClass().getName());
            } catch (IllegalAccessException e) {
                log.debug("Method getSignatureReferenceID() of Class {} is not "
                        + "public", signableObject.getClass().getName());
            } catch (InvocationTargetException e) {
                log.debug("Method getSignatureReferenceID() of Class {} threw "
                        + "an exception: {}",
                        signableObject.getClass().getName(), e.getCause());
            } catch (ClassCastException e) {
                log.debug("Method getSignatureReferenceID() of Class {} does "
                        + "not return a String",
                        signableObject.getClass().getName());
            }

            if (!DatatypeHelper.isEmpty(referenceID)) {
                signature.addDocument("#" + referenceID, dsigTransforms,
                        digestAlgorithm);
            } else {
                log.debug("SignableXMLObject had no reference ID, "
                        + "signing using whole document Reference URI");
                signature.addDocument("", dsigTransforms, digestAlgorithm);
            }

        } catch (TransformationException e) {
            log.error("Unsupported signature transformation", e);
        } catch (XMLSignatureException e) {
            log.error("Error adding content reference to signature", e);
        }
    }

    /**
     * Populate the inclusive namspace prefixes on the specified Apache
     * (exclusive) transform object.
     *
     * @param signature the Apache XMLSignature object
     * @param transform the Apache Transform object representing an exclusive
     * transform
     */
    private void processExclusiveTransform(final XMLSignature signature,
            final Transform transform) {
        /*
         *  Namespaces that aren't visibly used, such as those used in QName
         * attribute values, would be stripped out by exclusive
         * canonicalization. Need to make sure they aren't by explicitly telling
         * the transformer about them.
         */
        log.debug("Adding list of inclusive namespaces for signature exclusive "
                + "canonicalization transform");
        LazySet<String> inclusiveNamespacePrefixes = new LazySet<String>();
        populateNamespacePrefixes(inclusiveNamespacePrefixes, signableObject);

        if (inclusiveNamespacePrefixes.size() > 0) {
            InclusiveNamespaces inclusiveNamespaces =
                    new InclusiveNamespaces(signature.getDocument(),
                    inclusiveNamespacePrefixes);
            transform.getElement().appendChild(
                    inclusiveNamespaces.getElement());
        }
    }

    /**
     * Populates the given set with the non-visibly used namespace prefixes used
     * by the given XMLObject and all of its descendants, as determined by the
     * signature content object's namespace manager.
     *
     * @param namespacePrefixes the namespace prefix set to be populated
     * @param signatureContent the XMLObject whose namespace prefixes will be
     * used to populate the set
     */
    private void populateNamespacePrefixes(final Set<String> namespacePrefixes,
            final XMLObject signatureContent) {
        for (String prefix : signatureContent.getNamespaceManager().
                getNonVisibleNamespacePrefixes()) {
            if (prefix != null) {
                /*
                 * For the default namespace prefix, exclusive c14n uses the
                 * special token "#default". Apache xmlsec requires this to be
                 * represented in the set with the (completely undocumented)
                 * string "xmlns".
                 */
                if (NamespaceManager.DEFAULT_NS_TOKEN.equals(prefix)) {
                    namespacePrefixes.add("xmlns");
                } else {
                    namespacePrefixes.add(prefix);
                }
            }
        }
    }
}
