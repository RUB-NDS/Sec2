/**
 * 
 */
package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Base64;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;

/**
 * A handler for parsing the incoming content from the Sec2-middleware when the
 * request to the middleware has been redirected to an other server like the
 * cloud service. Validates, if the incoming XML data have the following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;sec2:response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *  &lt;sec2:original&gt;[Original content base64 encoded]&lt;/sec2:original&gt;
 *  &lt;sec2:x-sec2-nonce&gt;[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/sec2:response&gt;
 * </pre>
 * 
 * @author schuessler
 *
 */
public class XmlHandlerRedirectResponse extends DefaultHandler implements IXmlHandler
{
    private boolean errorOccured = false;
    private boolean originalOccured = false;
    private boolean nonceOccured = false;
    private boolean inResponse = false;
    private boolean inTag = false;
    private String nonce = null;
    private String currentValue = null;
    private byte[] originalContent = null;
    private int tagCount = 0;
    private final LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private static final String RESPONSE_TAG = "response";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String ORIGINAL_TAG = "original";
    private static final String URI = "http://sec2.org/2012/03/middleware";
    private static final String ERR_NOT_IN_RESPONSE = "Tag <{0}> nicht in <" + RESPONSE_TAG + ">.";
    private static final String ERR_MULTIPLE_TAG = "Tag <{0}> darf nicht mehrmals vorkommen!";
    private static final String ERR_TAG_NOT_EXPECTED = "Tag <{0} nicht erwartet!";
    private static final String ERR_URI_NOT_CORRECT = "Uri \"{0}\" ist falsch. Uri muß \"" + URI + "\" sein!";

    @Override
    public void error(final SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("XML-Fehler: " + e.getMessage()));
    }

    @Override
    public void fatalError(final SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("Fataler XML-Fehler: " + e.getMessage()));
    }

    @Override
    public void warning(final SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("XML-Warnung: " + e.getMessage()));
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        tagCount++;

        //URI muß stimmen
        if(!URI.equals(uri)) throw new SAXException(MessageFormat.format(ERR_URI_NOT_CORRECT, uri));
        //1. Tag muß <notice> sein
        if(RESPONSE_TAG.equals(localName))
        {
            if(tagCount != 1)
                throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <" + RESPONSE_TAG + ">.");
            inResponse = true;
        }
        else if(ORIGINAL_TAG.equals(localName))
        {
            if(!inResponse || inTag)
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_RESPONSE, localName));
            if(originalOccured)
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            originalOccured = true;
            inTag = true;
        }
        else if(NONCE_TAG.equals(localName))
        {
            if(!inResponse || inTag)
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_RESPONSE, localName));
            if(nonceOccured)
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            nonceOccured = true;
            inTag = true;
        }
        else throw new SAXException(MessageFormat.format(ERR_TAG_NOT_EXPECTED, localName));
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException
    {
        currentValue = new String(ch, start, length);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if(RESPONSE_TAG.equals(localName)) inResponse = false;
        else if(ORIGINAL_TAG.equals(localName))
        {
            originalContent = Base64.decode(currentValue, Base64.DEFAULT);
            inTag = false;
        }
        else if(NONCE_TAG.equals(localName))
        {
            nonce = currentValue;
        }
    }

    /* (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.mwadapter.xml.IXmlHandler#isErrorOccured()
     */
    @Override
    public boolean isErrorOccured()
    {
        return errorOccured;
    }

    /* (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.mwadapter.xml.IXmlHandler#getErrors()
     */
    @Override
    public List<XMLParseException> getErrors()
    {
        return errors;
    }

    /**
     * Returns the parsed nonce.
     *
     * @return The nonce
     */
    public String getNonce()
    {
        return nonce;
    }

    /**
     * Returns the decoded original content as byte-array.
     * 
     * @return The decoded original content
     */
    public byte[] getOriginalContent()
    {
        return originalContent;
    }
}
