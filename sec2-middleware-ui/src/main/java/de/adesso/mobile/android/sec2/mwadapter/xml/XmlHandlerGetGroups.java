package de.adesso.mobile.android.sec2.mwadapter.xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * A handler for parsing the incoming content from the Sec2-middleware when the getGroups function was called.
 * Validates, if the incoming XML data have the following form:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;response xmlns:sec2=http://sec2.org/2012/03/middleware"&gt;
 *  &lt;sec2:groups&gt;
 *   &lt;sec2:group&gt;
 *    &lt;sec2:groupidentifier&gt;[GroupID]&lt;/sec2:groupidentifier&gt;
 *    &lt;sec2:groupname&gt;[Groupname]&lt;/sec2:groupname&gt;
 *   &lt;/sec2:group&gt;
 *                          .
 *                          .
 *                          .
 *  &lt;/sec2:groups&gt;
 *  &lt;sec2:x-sec2-nonce&gt;[99999999]&lt;/sec2:x-sec2-nonce&gt;
 * &lt;/response&gt;
 * </pre>
 * 
 * @author schuessler
 *
 */
public class XmlHandlerGetGroups extends DefaultHandler implements IXmlHandler {

    private static final String RESPONSE_TAG = "response";
    private static final String GROUPS_TAG = "groups";
    private static final String GROUP_TAG = "group";
    private static final String GROUP_IDENTIFIER_TAG = "groupidentifier";
    private static final String GROUP_NAME_TAG = "groupname";
    private static final String NONCE_TAG = "x-sec2-nonce";
    private static final String URI = "http://sec2.org/2012/03/middleware";

    private boolean errorOccured = false;
    private boolean inGroupsPart = false;
    private boolean inGroupPart = false;
    private boolean groupIdTagHasOccured = false;
    private boolean groupNameTagHasOccured = false;
    private boolean nonceTagHasOccured = false;
    private LinkedList<XMLParseException> errors = new LinkedList<XMLParseException>();
    private LinkedList<Group> groups = new LinkedList<Group>();
    private int tagCount = 0;
    private String currentValue = null;
    private String nonce = null;
    private String groupId = null;
    private String groupName = null;

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
                //2. Tag muß <sec2:groups> sein.
                if(GROUPS_TAG.equals(localName) && URI.equals(uri)) inGroupsPart = true;
                else throw new SAXException("Tag <" + localName + "> in Zeile 2 nicht erwartet. Erwartet: <" + GROUPS_TAG + ">.");
                break;
            default:
            {
                /* Ist man im <sec2:groups>-Teil, aber noch nicht im <sec2:group>-Teil, muß als nächstes Starttag <sec2:group> kommen
                 * und man ist im <sec:group>-Teil
                 */
                if(inGroupsPart && !inGroupPart)
                {
                    if(GROUP_TAG.equals(localName) && URI.equals(uri)) inGroupPart = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + GROUP_TAG + ">.");
                    }
                }
                // Ist man im <sec2:groups>- und im <sec2:group>-Teil, muß als nächstes Starttag <sec2:groupidentifier> kommen
                else if(inGroupsPart && inGroupPart && !groupIdTagHasOccured && !groupNameTagHasOccured)
                {
                    if(GROUP_IDENTIFIER_TAG.equals(localName) && URI.equals(uri)) groupIdTagHasOccured = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + GROUP_IDENTIFIER_TAG + ">.");
                    }
                }
                /* Ist man im <sec2:groups>- und im <sec2:group>-Teil und kam das <sec2:groupidentifier>-Tag, muß als
                 * nächstes Starttag <sec2:groupname> kommen
                 */
                else if(inGroupsPart && inGroupPart && groupIdTagHasOccured && !groupNameTagHasOccured)
                {
                    if(GROUP_NAME_TAG.equals(localName) && URI.equals(uri)) groupNameTagHasOccured = true;
                    else
                    {
                        throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount + " nicht erwartet. Erwartet: <"
                                + GROUP_NAME_TAG + ">.");
                    }
                }
                // Nach dem <sec2:groups>-Tag muß das <sec2:nonce>-Tag kommen
                else if(!inGroupsPart && !nonceTagHasOccured)
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
        if(GROUPS_TAG.equals(localName)) inGroupsPart = false;
        else if(GROUP_TAG.equals(localName))
        {
            inGroupPart = false;
            groupIdTagHasOccured = false;
            groupNameTagHasOccured = false;
            groups.add(new Group(groupId, groupName));
        }
        else if(GROUP_IDENTIFIER_TAG.equals(localName)) groupId = currentValue;
        else if(GROUP_NAME_TAG.equals(localName)) groupName = currentValue;
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
     * Returns an array of objects of type Group which were constructed based
     * on the parsed XML content.
     *
     * @return An array of constructed Group objects
     */
    public Group[] getGroups()
    {
        return groups.toArray(new Group[groups.size()]);
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
