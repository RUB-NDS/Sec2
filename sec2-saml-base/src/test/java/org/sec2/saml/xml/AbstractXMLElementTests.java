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

import java.util.List;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.AbstractXMLTests;
import org.sec2.saml.XMLProcessingTestHelper;
import org.sec2.saml.engine.SAMLEngine;
import org.w3c.dom.Element;

/**
 * Abstract test framework for testing XML elements, their builders,
 * marshallers and unmarshallers.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 *
 * @param <T> The ValidatingXMLObject that will be tested
 */
public abstract class AbstractXMLElementTests<T extends ValidatingXMLObject>
                    extends AbstractXMLTests {
    /**
     * The builder used for testing.
     */
    private XMLObjectBuilder<T> builder;

    /**
     * Set up: Get builder.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        super.setUp();
        builder = (XMLObjectBuilder<T>) SAMLEngine.getXMLBuilder(
                ValidatingXMLObject.class, this.getElementQName());
    }

    /**
     * Tear down: Delete references to builder.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        super.tearDown();
        builder = null;
    }

    /**
     * @return An element that will be used for testing the unmarshaller
     */
    protected abstract T getElementForUnmarshalling();

    /**
     * Tests the element's builder.
     */
    public void testBuilder() {
        T xml = getBuilder().buildObject(this.getElementQName());
        assertNotNull(xml);
    }

    /**
     * Tests the element's marshaller.
     */
    public void testMarshaller() {
        T xml = getBuilder().buildObject(this.getElementQName());
        Element plaintextElement = null;
        try {
            plaintextElement = Configuration.getMarshallerFactory().
                    getMarshaller(xml).marshall(xml);
        } catch (MarshallingException e) {
            fail(e.getMessage());
        }
        assertNotNull(XMLHelper.nodeToString(plaintextElement));
    }

    /**
     * Tests the element's unmarshaller.
     */
    public void testUnmarshaller() {
        T xml = this.getElementForUnmarshalling();
        Element plaintextElement = null;
        try {
            plaintextElement = Configuration.getMarshallerFactory().
                    getMarshaller(xml).marshall(xml);
        } catch (MarshallingException e) {
            fail(e.getMessage());
        }

        xml = null;
        try {
            xml = (T) Configuration.getUnmarshallerFactory().
                    getUnmarshaller(this.getElementQName()).
                    unmarshall(plaintextElement);
        } catch (UnmarshallingException e) {
            fail(e.getMessage());
        }
        assertNotNull(xml);
    }

    /**
     * Tests that the element's unmarshaller throws exception when provided with
     * invalid elements.
     */
    public void testUnmarshallingInvalidElements() {
        List<String> invalidElements = getInvalidElements();
        if (invalidElements == null || invalidElements.isEmpty()) {
            return; //nothing to test
        }

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().
                    getUnmarshaller(this.getElementQName());
        for (String xml : invalidElements) {
            try {
                unmarshaller.unmarshall(
                        XMLProcessingTestHelper.parseXMLElement(xml));
                fail("Should have failed with a UnmarshallingException");
            } catch (UnmarshallingException e) {
                xml = null;
            }
            assertNull(xml);
        }
    }

    /**
     * @return A list of invalid elements that makes the element's unmarshaller
     * throw exceptions
     */
    protected List<String> getInvalidElements() {
        return null;
    }

    /**
     * @return the builder
     */
    protected XMLObjectBuilder<T> getBuilder() {
        return builder;
    }
}
