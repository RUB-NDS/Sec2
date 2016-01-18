package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * A handler for parsing the incoming content from the Sec2-middleware when the
 * getRegisteredUser or getUser function was called. Validates, if the incoming
 * XML data have the following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *  &lt;sec2:user&gt;
 *   &lt;sec2:useridentifier&gt;[UserID]&lt;/sec2:useridentifier&gt;
 *   &lt;sec2:useremail&gt;[Email]&lt;/sec2:useremail&gt;
 *  &lt;/sec2:user&gt;
 *  &lt;sec2:x-sec2-nonce&gt;[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/response&gt;
 * </pre>
 * 
 * @author schuessler
 *
 */
public class XmlHandlerGetUser extends DefaultHandler implements IXmlHandler
{
    private static final String RESPONSE_TAG = "response";
    private static final String USER_TAG = "user";
    private static final String USERIDENTIFIER_TAG = "useridentifier";
    private static final String USEREMAIL_TAG = "useremail";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String URI = "http://sec2.org/2012/03/middleware";

    private boolean errorOccured = false;
    private final LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private int tagCount = 0;
    private String currentValue = null;
    private String nonce = null;
    private final User user = new User();

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
        switch(tagCount)
        {
            case 1:
                //1. Tag muß <response> sein.
                if(!RESPONSE_TAG.equals(localName))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 1 nicht erwartet. Erwartet: <" + RESPONSE_TAG + ">.");
                }
                break;
            case 2:
                //2. Tag muß <sec2:user> sein.
                if(!USER_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 2 nicht erwartet. Erwartet: <" + USER_TAG + ">.");
                }
                break;
            case 3:
                //3. Tag muß <sec2:useridentifier> sein.
                if(!USERIDENTIFIER_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 3 nicht erwartet. Erwartet: <" + USERIDENTIFIER_TAG + ">.");
                }
                break;
            case 4:
                //4. Tag muß <sec2:useremail> sein.
                if(!USEREMAIL_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 4 nicht erwartet. Erwartet: <" + USEREMAIL_TAG + ">.");
                }
                break;
            case 5:
                //5. Tag muß <x-sec2-nonce> sein
                if(!NONCE_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 6 nicht erwartet. Erwartet: <" + NONCE_TAG + ">.");
                }
                break;
            default: throw new SAXException("Zu viele Tags für Benutzerinformationsabfrage. Erwartet: 5");
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if(USERIDENTIFIER_TAG.equals(localName)) user.setUserId(currentValue);
        else if(USEREMAIL_TAG.equals(localName)) user.setUserEmail(currentValue);
        else if(NONCE_TAG.equals(localName)) nonce = currentValue;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException
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
     * Returns the objects of type User that was constructed based on the
     * parsed XML content.
     *
     * @return The constructed User object
     */
    public User getUser()
    {
        return user;
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
