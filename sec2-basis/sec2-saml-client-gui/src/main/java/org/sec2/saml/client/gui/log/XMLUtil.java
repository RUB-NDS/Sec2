/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.saml.client.gui.log;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author dev
 */
public class XMLUtil {

    public static String formatMessage(String msg) {
        String result;
        String trim = msg.trim();
        if (!trim.startsWith("<")) {
            trim = trim.substring(trim.indexOf(": ") + 2);
        }
        return prettyPrintXML(trim);
    }
    
    private static String prettyPrintXML(String unformattedXML) {
        try {
            return domToString(stringToDom(unformattedXML));
        } catch (SAXException ex) {
            Logger.getLogger(XMLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return unformattedXML;
        }
    }
    
    private static String domToString(Node n) {
        StringWriter output = new StringWriter();
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(n), new StreamResult(output));
        } catch (Exception e) {
            throw new IllegalStateException("Should never happen");
        }
        return output.toString();
    }
    
    
    private static Document stringToDom(String xmlString) throws SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        StringReader reader = new StringReader(xmlString);
        InputSource input = new InputSource(reader);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("This should never happen");
        }
        Document dom;
        try {
            dom = builder.parse(input);
        } catch (IOException e) {
            // will never happen
            dom = createDomDocument();
        }
        return dom;
    }

    
   private static Document createDomDocument() {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("This should never happen");
        } finally {
            return doc;
        }
    }
}
