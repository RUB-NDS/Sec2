package de.adesso.mobile.android.sec2.util;

import java.io.Serializable;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Abstract class for creating an XML document, which is sent through the XML encryption module of the Sec2-middleware to the cloud service.
 * 
 * @author schuessler
 *
 */
public abstract class AbstractDomDocumentCreator implements Serializable {

    private static final long serialVersionUID = -4113306843624636034L;

    private String documentName = null;

    /**
     * Returns the name of the document
     * @return the documentName
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Sets the name of the document
     * @param documentName the documentName to set
     */
    public void setDocumentName(final String documentName) {
        this.documentName = documentName;
    }

    @Override
    public boolean equals(Object obj) {
        String name = null;

        if (obj == null) {
            return false;
        }
        name = ((AbstractDomDocumentCreator) obj).getDocumentName();
        if (documentName == null) {
            if (name == null) {
                return true;
            } else {
                return false;
            }
        }

        return documentName.equals(name);
    }

    /**
     * Creates the DOM document
     * 
     * @param groupHandler - The group handler with the selected groups, for which some DOM parts are to be encrypted.
     * 
     * @return The String representation of the DOM document.
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public abstract String createDomDocument(CheckedGroupHandler groupHandler) throws ParserConfigurationException, TransformerException;

    /**
     * Convertes a DOM document into its String representation
     * 
     * @param source - The DOMSource
     * 
     * @return The String representation of the DOM document
     */
    protected String convertDomToString(final DOMSource source) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final StringWriter writer = new StringWriter();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, new StreamResult(writer));

        return writer.toString();
    }
}
