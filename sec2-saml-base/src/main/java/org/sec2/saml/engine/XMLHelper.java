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

import java.io.Reader;
import java.io.StringReader;
import org.opensaml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.w3c.dom.Element;

/**
 * Utility class that provides basic XML related methods.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 30, 2012
 */
public final class XMLHelper {
    /**
     * No instances allowed, utility class.
     */
    private XMLHelper() { }

    /**
     * Converts an XMLObject into an XML string.
     *
     * @param xmlObject The XMLObject that is to be converted into XML
     * @return The XML string of the XMLObject provided
     * @throws MarshallingException if the XMLObject cannot be marshalled
     */
    public static String getXMLString(final XMLObject xmlObject)
            throws MarshallingException {
        if (xmlObject == null) {
            throw new IllegalArgumentException("xmlObject must not be null");
        }
        Element plaintextElement = Configuration.getMarshallerFactory().
                getMarshaller(xmlObject).marshall(xmlObject);
        return org.opensaml.xml.util.XMLHelper.nodeToString(plaintextElement);
    }

    /**
     * Parses a W3C-DOM element from a String source.
     *
     * @param xml the string source; must not be null
     * @return the W3C element
     * @throws SAMLEngineException if the XML cannot be parsed
     */
    public static Element parseXMLElement(final String xml)
            throws SAMLEngineException {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
        Element root = null;
        try {
            Reader in = new StringReader(xml);
            root = ppMgr.parse(in).getDocumentElement();
        } catch (XMLParserException e) {
            throw new SAMLEngineException(
                    "XML parsing failed: not wellformed", e);
        } catch (NullPointerException e) {
            throw new SAMLEngineException(
                    "XML parsing failed: Processing aborted", e);
        }
        return root;
    }
}
