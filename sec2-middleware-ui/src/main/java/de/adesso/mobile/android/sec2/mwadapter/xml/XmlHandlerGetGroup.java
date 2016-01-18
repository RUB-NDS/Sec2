package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * A handler for parsing the incoming content from the Sec2-middleware when the
 * getGroup function was called. Validates, if the incoming XML data have the
 * following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;sec2:response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *  &lt;sec2:group&gt;
 *   &lt;sec2:groupidentifier&gt;[GroupID]&lt;/sec2:groupidentifier&gt;
 *   &lt;sec2:groupname&gt;[GroupName]&lt;/sec2:groupname&gt;
 *   &lt;sec2:groupowner&gt;
 *    &lt;sec2:useridentifier&gt;[UserID]&lt;/sec2:useridentifier&gt;
 *    &lt;sec2:useremail&gt;[Email]&lt;/sec2:useremail&gt;
 *   &lt;/sec2:groupowner&gt;
 *  &lt;/sec2:group&gt;
 *  &lt;sec2:x-sec2-nonce>[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/sec2:response&gt;
 * </pre>
 * 
 * @author schuessler
 *
 */
public class XmlHandlerGetGroup extends DefaultHandler implements IXmlHandler
{

    private static final String RESPONSE_TAG = "response";
    private static final String GROUP_TAG = "group";
    private static final String USERIDENTIFIER_TAG = "useridentifier";
    private static final String GROUPIDENTIFIER_TAG = "groupidentifier";
    private static final String USEREMAIL_TAG = "useremail";
    private static final String GROUPNAME_TAG = "groupname";
    private static final String GROUPOWNER_TAG = "groupowner";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String URI = "http://sec2.org/2012/03/middleware";

    private boolean errorOccured = false;
    private final LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private int tagCount = 0;
    private String currentValue = null;
    private String nonce = null;
    private final User user = new User();
    private final Group group = new Group();

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
                //2. Tag muß <sec2:group> sein.
                if(!GROUP_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 2 nicht erwartet. Erwartet: <" + GROUP_TAG + ">.");
                }
                break;
            case 3:
                //3. Tag muß <sec2:groupidentifier> sein.
                if(!GROUPIDENTIFIER_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 3 nicht erwartet. Erwartet: <" + GROUPIDENTIFIER_TAG + ">.");
                }
                break;
            case 4:
                //4. Tag muß <sec2:groupname> sein.
                if(!GROUPNAME_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 4 nicht erwartet. Erwartet: <" + GROUPNAME_TAG + ">.");
                }
                break;
            case 5:
                //5. Tag muß <sec2:groupowner> sein.
                if(!GROUPOWNER_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 5 nicht erwartet. Erwartet: <" + GROUPOWNER_TAG + ">.");
                }
                break;
            case 6:
                //6. Tag muß <sec2:useridentifier> sein.
                if(!USERIDENTIFIER_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 6 nicht erwartet. Erwartet: <" + USERIDENTIFIER_TAG + ">.");
                }
                break;
            case 7:
                //7. Tag muß <sec2:useremail> sein.
                if(!USEREMAIL_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 7 nicht erwartet. Erwartet: <" + USEREMAIL_TAG + ">.");
                }
                break;
            case 8:
                //8. Tag muß <x-sec2-nonce> sein.
                if(!NONCE_TAG.equals(localName) || !URI.equals(uri))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 8 nicht erwartet. Erwartet: <" + NONCE_TAG + ">.");
                }
                break;
            default: throw new SAXException("Zu viele Tags für Benutzerinformationsabfrage. Erwartet: 8");
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if(GROUPIDENTIFIER_TAG.equals(localName)) group.setGroupId(currentValue);
        else if(GROUPNAME_TAG.equals(localName)) group.setGroupName(currentValue);
        else if(USERIDENTIFIER_TAG.equals(localName)) user.setUserId(currentValue);
        else if(USEREMAIL_TAG.equals(localName)) user.setUserEmail(currentValue);
        else if(GROUPOWNER_TAG.equals(localName)) group.setGroupOwner(user);
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
     * Returns the object of type Group that was constructed based on the
     * parsed XML content.
     *
     * @return The constructed Group object
     */
    public Group getGroup()
    {
        return group;
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
