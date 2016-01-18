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
 * getUsersInGroup or getAllUsers function was called. Validates, if the
 * incoming XML data have the following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *  &lt;sec2:users&gt;
 *   &lt;sec2:user&gt;
 *    &lt;sec2:useridentifier&gt;[UserID]&lt;/sec2:useridentifier&gt;
 *    &lt;sec2:useremail&gt;[Email]&lt;/sec2:username&gt;
 *   &lt;/sec2:user&gt;
 *                          .
 *                          .
 *                          .
 *  &lt;/sec2:users&gt;
 *  &lt;sec2:x-sec2-nonce&gt;[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/response&gt;
 * </pre>
 * 
 * @author schuessler
 *
 */
public class XmlHandlerGetUsers extends DefaultHandler implements IXmlHandler
{
    private static final String RESPONSE_TAG = "response";
    private static final String USERS_TAG = "users";
    private static final String USER_TAG = "user";
    private static final String USER_IDENTIFIER_TAG = "useridentifier";
    private static final String USER_EMAIL = "useremail";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String URI = "http://sec2.org/2012/03/middleware";

    private boolean errorOccured = false;
    private boolean inUsersPart = false;
    private boolean inUserPart = false;
    private boolean userIdTagHasOccured = false;
    private boolean userEmailTagHasOccured = false;
    private boolean nonceTagHasOccured = false;
    private LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private LinkedList<User> users = new LinkedList<User>();
    private int tagCount = 0;
    private String currentValue = null;
    private String nonce = null;
    private String userId = null;
    private String userEmail = null;

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
                //1. Tag muß <response> sein
                if(!RESPONSE_TAG.equals(localName))
                {
                    throw new SAXException("Tag <" + localName + "> in Zeile 1 nicht erwartet. Erwartet: <" + RESPONSE_TAG + ">.");
                }
                break;
            case 2:
                //2. Tag muß <sec2:users> sein.
                if(USERS_TAG.equals(localName) && URI.equals(uri)) inUsersPart = true;
                else throw new SAXException("Tag <" + localName + "> in Zeile 2 nicht erwartet. Erwartet: <" + USERS_TAG + ">.");
                break;
            default:
            {
                /* Ist man im <sec2:users>-Teil, aber noch nicht im <sec2:user>-Teil, muß als nächstes Starttag <sec2:user> kommen
                 * und man ist im <sec:user>-Teil
                 */
                if(inUsersPart && !inUserPart)
                {
                    if(USER_TAG.equals(localName) && URI.equals(uri)) inUserPart = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + USER_TAG + ">.");
                    }
                }
                // Ist man im <sec2:users>- und im <sec2:user>-Teil, muß als nächstes Starttag <sec2:useridentifier> kommen
                else if(inUsersPart && inUserPart && !userIdTagHasOccured && !userEmailTagHasOccured)
                {
                    if(USER_IDENTIFIER_TAG.equals(localName) && URI.equals(uri)) userIdTagHasOccured = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + USER_IDENTIFIER_TAG + ">.");
                    }
                }
                /* Ist man im <sec2:users>- und im <sec2:user>-Teil und kam das <sec2:useridentifier>-Tag, muß als
                 * nächstes Starttag <sec2:username> kommen
                 */
                else if(inUsersPart && inUserPart && userIdTagHasOccured && !userEmailTagHasOccured)
                {
                    if(USER_EMAIL.equals(localName) && URI.equals(uri)) userEmailTagHasOccured = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + USER_EMAIL + ">.");
                    }
                }
                // Nach dem <sec2:users>-Tag muß das <sec2:nonce>-Tag kommen
                else if(!inUsersPart && !nonceTagHasOccured)
                {
                    if(NONCE_TAG.equals(localName) && URI.equals(uri)) nonceTagHasOccured = true;
                    else throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                            + NONCE_TAG + ">.");
                }
                // Ansonsten werfe eine Exception
                else throw new SAXException("Zu viele Tags für die Gruppenabfrage.");
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(USERS_TAG.equals(localName)) inUsersPart = false;
        else if(USER_TAG.equals(localName))
        {
            inUserPart = false;
            userIdTagHasOccured = false;
            userEmailTagHasOccured = false;
            users.add(new User(userId, null, userEmail));
        }
        else if(USER_IDENTIFIER_TAG.equals(localName)) userId = currentValue;
        else if(USER_EMAIL.equals(localName)) userEmail = currentValue;
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
     * Returns an array of objects of type User which were constructed based on
     * the parsed XML content.
     *
     * @return An array of constructed User objects
     */
    public User[] getUsers()
    {
        return users.toArray(new User[users.size()]);
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
