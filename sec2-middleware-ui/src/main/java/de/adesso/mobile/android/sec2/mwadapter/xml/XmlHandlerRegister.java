package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;

/**
 * A handler for parsing the incoming content from the Sec2-middleware when the
 * register function was called. Validates, if the incoming XML data have the
 * following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *	&lt;sec2:app-auth-key&gt;[Key in hex format]&lt;/sec2:app-auth-key&gt;
 *  &lt;sec2:app-auth-key-alg&gt;[Algorithm]&lt;/sec2:app-auth-key&gt;
 *  &lt;sec2:x-sec2-nonce&gt;[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/response&gt;
 * </pre>
 * 
 * @author nike
 *
 */
public class XmlHandlerRegister extends DefaultHandler implements IXmlHandler
{
    private static final String RESPONSE_TAG = "response";
    private static final String APP_AUTH_KEY_TAG = "app-auth-key";
    private static final String APP_AUTH_KEY_ALG_TAG = "app-auth-key-alg";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String URI = "http://sec2.org/2012/03/middleware";

    private boolean errorOccured = false;
    private LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private int tagCount = 0;
    private String currentValue = null;
    private String key = null;
    private String algorithm = null;
    private String nonce = null;

    @Override
    public void error(SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("XML-Fehler: " + e.getMessage()));
    }

    @Override
    public void fatalError(SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("Fataler XML-Fehler: " + e.getMessage()));
    }

    @Override
    public void warning(SAXParseException e)
    {
        errorOccured = true;
        errors.add(new XMLParseException("XML-Warnung: " + e.getMessage()));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        tagCount++;
        switch(tagCount)
        {
            case 1:
                if(!RESPONSE_TAG.equals(localName))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 1 nicht erwartet. Erwartet: <" + RESPONSE_TAG + ">.");
                }
                break;
            case 2:
                if(!APP_AUTH_KEY_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 2 nicht erwartet. Erwartet: <" + APP_AUTH_KEY_TAG + ">.");
                }
                break;
            case 3:
                if(!APP_AUTH_KEY_ALG_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 3 nicht erwartet. Erwartet: <" + APP_AUTH_KEY_ALG_TAG + ">.");
                }
                break;
            case 4:
                if(!NONCE_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 4 nicht erwartet. Erwartet: <" + NONCE_TAG + ">.");
                }
                break;
            default: throw new SAXException("Zu viele Tags für Registrierung. Erwartet: 4");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(APP_AUTH_KEY_TAG.equals(localName))
        {
            if(currentValue.matches("[a-fA-F_0-9]+")) key = currentValue;
            else throw new SAXException("Schlüssel fehlerhaft hexadezimal kodiert.");
        }
        else if(APP_AUTH_KEY_ALG_TAG.equals(localName)) algorithm = currentValue;
        else if(NONCE_TAG.equals(localName)) nonce = currentValue;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        currentValue = new String(ch, start, length);
    }

    @Override
    public boolean isErrorOccured()
    {
        return errorOccured;
    }

    @Override
    public List<XMLParseException> getErrors()
    {
        return errors;
    }

    /**
     * Returns the value of tag sec2:app-auth-key.
     * 
     * @return The value of tag sec2:app-auth-key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the value of tag sec2:app-auth-key-alg.
     * 
     * @return The value of tag sec2:app-auth-key-alg
     */
    public String getAlgorithm()
    {
        return algorithm;
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
}