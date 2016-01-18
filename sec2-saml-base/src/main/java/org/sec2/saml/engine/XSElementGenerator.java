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

import javax.xml.namespace.QName;
import org.opensaml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.sec2.saml.Sec2SAMLBootstrap;

/**
 * Generates instances of basic XML types like xs:string, xs:integer, etc.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 29, 2012
 */
public final class XSElementGenerator {

    /*
     * make sure that everything gets bootstrapped
     */
    static {
        try {
            Sec2SAMLBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new Error("A serious misconfiguration of OpenSAML was found",
                    e);
        }
    }

    /**
     * The main builder factory for OpenSAML.
     */
    private XMLObjectBuilderFactory builderFactory;

    /**
     * The builder to build Elements from any namespace.
     */
    private XMLObjectBuilder<XSAny> xsAnyBuilder;

    /**
     * The builder used for Integer elements.
     */
    private XMLObjectBuilder<XSInteger> xsIntegerBuilder;

    /**
     * The builder used for String elements.
     */
    private XMLObjectBuilder<XSString> xsStringBuilder;

    /**
     * The builder used for Base64 encoded elements.
     */
    private XMLObjectBuilder<XSBase64Binary> xsBase64Builder;

    /**
     * Constructor.
     */
    public XSElementGenerator() {
        builderFactory   = Configuration.getBuilderFactory();
        xsAnyBuilder     = builderFactory.getBuilder(XSAny.TYPE_NAME);
        xsIntegerBuilder = builderFactory.getBuilder(XSInteger.TYPE_NAME);
        xsStringBuilder  = builderFactory.getBuilder(XSString.TYPE_NAME);
        xsBase64Builder  = builderFactory.getBuilder(XSBase64Binary.TYPE_NAME);
    }

    /**
     * Returns a new xs:any element with the given qualified name.
     *
     * @param elementName The new elements name
     * @return A new xs:any element with the name provided
     */
    public XSAny buildXSAny(final QName elementName) {
        checkParams(elementName);
        return xsAnyBuilder.buildObject(elementName);
    }

    /**
     * Returns a new xs:integer element.
     * When xs:integer elements are created, it is crucial to provide a
     * schema type to the builder. If you forget this, you'll get a class cast
     * exception while marshalling. When using this method, the schema type is
     * set correctly.
     *
     * @param elementName The new elements name
     * @return A new xs:integer element with the name provided
     */
    public XSInteger buildXSInteger(final QName elementName) {
        checkParams(elementName);
        return xsIntegerBuilder.buildObject(elementName, XSInteger.TYPE_NAME);
    }

    /**
     * Returns a new xs:string element.
     * When xs:string elements are created, it is crucial to provide a
     * schema type to the builder. If you forget this, you'll get a class cast
     * exception while marshalling. When using this method, the schema type is
     * set correctly.
     *
     * @param elementName The new elements name
     * @return A new xs:string element with the name provided
     */
    public XSString buildXSString(final QName elementName) {
        checkParams(elementName);
        return xsStringBuilder.buildObject(elementName, XSString.TYPE_NAME);
    }

    /**
     * Returns a new xs:base64binary element.
     * When xs:base64binary elements are created, it is crucial to provide a
     * schema type to the builder. If you forget this, you'll get a class cast
     * exception while marshalling. When using this method, the schema type is
     * set correctly.
     *
     * @param elementName The new elements name
     * @return A new xs:base64binary element with the name provided
     */
    public XSBase64Binary buildXSBase64Binary(final QName elementName) {
        checkParams(elementName);
        return xsBase64Builder.buildObject(
                elementName, XSBase64Binary.TYPE_NAME);
    }

    /**
     * Checks the validity of the new elements QName.
     * @param elementName The new elements name
     */
    private void checkParams(final QName elementName) {
        if (elementName == null) {
            throw new IllegalArgumentException("elementName must not be null");
        }
    }
}
