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
import junit.framework.TestCase;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;

/**
 * Tests for XSElementGenerator.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
public class XSElementGeneratorTests extends TestCase {

    /**
     * The generator which is tested.
     */
    private XSElementGenerator generator;

    /**
     * Name of generated elements.
     */
    private QName name;

    /**
     * Set up: Get XSElementGenerator.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        generator = new XSElementGenerator();
        name = new QName("namespace:test", "Test", "t");
    }

    /**
     * Tear down: Delete reference to XSElementGenerator.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        generator = null;
        name = null;
    }

    /**
     * Tests generating an xs:any element with a null QName.
     */
    public void testXSAnyNull() {
        XSAny element = null;
        try {
            element = generator.buildXSAny(null);
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        assertNull(element);
    }

    /**
     * Tests generating an xs:integer element with a null QName.
     */
    public void testXSIntegerNull() {
        XSInteger element = null;
        try {
            element = generator.buildXSInteger(null);
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        assertNull(element);
    }

    /**
     * Tests generating an xs:string element with a null QName.
     */
    public void testXSStringNull() {
        XSString element = null;
        try {
            element = generator.buildXSString(null);
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        assertNull(element);
    }

    /**
     * Tests generating an xs:base64Binary element with a null QName.
     */
    public void testXSBase64BinaryNull() {
        XSBase64Binary element = null;
        try {
            element = generator.buildXSBase64Binary(null);
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        assertNull(element);
    }

    /**
     * Tests generating an xs:any element.
     */
    public void testXSAny() {
        XSAny element = generator.buildXSAny(name);
        assertNotNull(element);
    }

    /**
     * Tests generating an xs:integer element.
     */
    public void testXSInteger() {
        XSInteger element = generator.buildXSInteger(name);
        assertNotNull(element);
    }

    /**
     * Tests generating an xs:string element.
     */
    public void testXSString() {
        XSString element = generator.buildXSString(name);
        assertNotNull(element);
    }

    /**
     * Tests generating an xs:base64Binary element.
     */
    public void testXSBase64Binary() {
        XSBase64Binary element = generator.buildXSBase64Binary(name);
        assertNotNull(element);
    }
}
