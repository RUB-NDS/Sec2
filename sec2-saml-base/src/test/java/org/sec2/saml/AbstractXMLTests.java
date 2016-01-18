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

import javax.xml.namespace.QName;
import junit.framework.TestCase;
import org.opensaml.saml2.core.Assertion;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XSElementGenerator;

/**
 * Abstract test framework for testing XML.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public abstract class AbstractXMLTests extends TestCase {

    /**
     * The XS Generator used for testing.
     */
    private XSElementGenerator xsGenerator;

    /**
     * The xml declaration and the opening tag of the element.
     */
    private String prefix;

    /**
     * The closing tag of the element.
     */
    private String suffix;

    /**
     * The StringBuilder used for building the xml string.
     */
    private StringBuilder sb;

    /**
     * The initial capacity of the StringBuilder.
     */
    protected static final int INITIAL_BUFFER = 300;

    /**
     * The standard XML declaration that is used as prefix for every xml.
     */
    protected static final String XML_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /**
     * The namespace declaration for sec2saml.
     */
    protected static final String NAMESPACE = " xmlns=\""
            + "http://sec2.org/saml/v1/" + "\"";

    /**
     * Some dummy base64.
     */
    protected static final String BASE64DUMMY = "AAECAwQFBgcICQoLDA0ODw==";

    /**
     * A dummy group xml fragment.
     */
    protected static final String GROUPNAME = "<groupName>testgroup"
            + "</groupName>";

    /**
     * A dummy empty groupname xml fragment.
     */
    protected static final String INVALID_GROUPNAME = "<groupName />";

    /**
     * A dummy user xml fragment.
     */
    protected static final String USERID = "<userID>"
            + BASE64DUMMY + "</userID>";

    /**
     * @return The tested element's QName
     */
    protected abstract QName getElementQName();

    /**
     * @return The name of the xml element to test
     */
    protected final String getElementName() {
        return this.getElementQName().getLocalPart();
    }

    /**
     * Set up: Prepare prefix and suffix.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        xsGenerator = new XSElementGenerator();
        SAMLEngine.getSAMLBuilder(Assertion.class, //ensures bootstrapping
                Assertion.DEFAULT_ELEMENT_NAME);
        sb = new StringBuilder(INITIAL_BUFFER);
        sb.append("</");
        sb.append(this.getElementName());
        sb.append('>');
        this.suffix = sb.toString();
        sb.setLength(0);
        sb.append(XML_DECLARATION);
        sb.append('<');
        sb.append(this.getElementName());
        sb.append(' ');
        sb.append(NAMESPACE);
        sb.append('>');
        this.prefix = sb.toString();
    }

    /**
     * Tear down: Delete references.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        sb.setLength(0);
        this.prefix = null;
        this.suffix = null;
    }

    /**
     * @return the prefix
     */
    protected final String getPrefix() {
        return prefix;
    }

    /**
     * @return the suffix
     */
    protected final String getSuffix() {
        return suffix;
    }

    /**
     * @return the StringBuilder
     */
    protected final StringBuilder getStringBuilder() {
        return sb;
    }

    /**
     * Resets the StringBuilder and appends the prefix.
     */
    protected final void resetStringBuilder() {
        this.sb.setLength(0);
        this.sb.append(this.prefix);
    }

    /**
     * @return the xsGenerator
     */
    public XSElementGenerator getXsGenerator() {
        return xsGenerator;
    }
}
