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

import junit.framework.TestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.w3c.dom.Element;

/**
 * Tests for basic XML related methods.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 02, 2012
 */
public class XMLHelperTests extends TestCase {

    /**
     * Tests the conversion of an XMLObject to a String.
     */
    public void testXMLtoStringWithNull() {
        String s = null;
        try {
             s = XMLHelper.getXMLString(null);
             fail("Should have raised an IllegalArgumentException");
        } catch (MarshallingException e) {
            fail(e.toString());
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        assertNull(s);
    }

    /**
     * Tests the conversion of an XMLObject to a String.
     */
    public void testXMLtoString() {
        String s = null;
        try {
             s = XMLHelper.getXMLString(
                     SAMLEngine.getXMLObject(Assertion.class));
        } catch (MarshallingException e) {
            fail(e.toString());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
        assertNotNull(s);
        assertTrue(s.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<saml2"));
    }

    /**
     * Tests the conversion of a String into an XMLObject.
     */
    public void testStringToXML() {
        Element element = null;
        try {
             element = XMLHelper.parseXMLElement("<xml></xml>");
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
        assertNotNull(element);
    }

    /**
     * Tests the conversion of a String into an XMLObject with a null parameter.
     */
    public void testStringToXMLWithNull() {
        try {
             XMLHelper.parseXMLElement(null);
             fail("Should have raised a NullPointerException");
        } catch (SAMLEngineException e) {
            e.log();
            if (!(e.getCause() instanceof NullPointerException)) {
                fail(e.toString());
            }
            assertNull(e.getCause().getLocalizedMessage());
        }
    }

    /**
     * Tests the conversion of a String into an XMLObject with not
     * wellformed xml.
     */
    public void testStringToXMLNotWellformed() {
        try {
             XMLHelper.parseXMLElement("<xml></xlm>");
             fail("Should have raised a XMLParserException");
        } catch (SAMLEngineException e) {
            e.log();
            if (!(e.getCause() instanceof XMLParserException)) {
                fail(e.toString());
            }
            assertNotNull(e);
        }
    }
}
