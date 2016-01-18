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

import java.io.Reader;
import java.io.StringReader;
import junit.framework.TestCase;
import org.opensaml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.w3c.dom.Element;

/**
 * A helper that provides some often used functions.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 03, 2012
 */
public abstract class XMLProcessingTestHelper extends TestCase {
    /**
     * Parses an XMLObject from a String source. May die with a
     * ClassCastException at runtime if the wrong type is provided. However,
     * this saves a lot of code in the tests.
     *
     * @param <T> The subclass of XMLObject to parse
     * @param xml the string source
     * @param type The subclass of XMLObject to parse
     * @return the XMLObject
     */
    public static <T extends XMLObject> T parseXMLElement(
            final String xml, final Class<T> type) {
        Element root = parseXMLElement(xml);
        UnmarshallerFactory unmarshallerFactory =
                Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller =
                unmarshallerFactory.getUnmarshaller(root);
        T returnValue = null;
        try {
            returnValue = (T) unmarshaller.unmarshall(root);
        } catch (UnmarshallingException e) {
            fail(e.getMessage());
        }
        return returnValue;
    }

    /**
     * Parses a W3C-DOM element from a String source.
     *
     * @param xml the string source
     * @return the W3C element
     */
    public static Element parseXMLElement(final String xml) {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
        Reader in = new StringReader(xml);
        Element root = null;
        try {
            root = ppMgr.parse(in).getDocumentElement();
        } catch (XMLParserException e) {
            fail(e.getMessage());
        }
        return root;
    }
}
