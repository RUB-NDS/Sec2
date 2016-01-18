
package de.adesso.mobile.android.sec2.util;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.adesso.mobile.android.sec2.model.Event;

/**
 * Creates an XML document representing the event. The document looks as follows:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <event>
 *  <subject>[subject]</subject>
 *  <begin>[Date of event begin]</begin>
 *  <end>[Date of event end]</end>
 *  <wholeDay>[Is whole day]</wholeDay>
 *  <location>[Location]</location>
 *  <participants>[Participants]</participants>
 *  <repeatRate>[Event repeat rate]</repeatRate>
 *  <reminder>[Reminder]</reminder>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
 * </event>}
 * </pre>
 * @author schuessler
 *
 */
public final class EventDomDocumentCreator extends AbstractDomDocumentCreator {

    private static final long serialVersionUID = -8122239917281281394L;
    // The namespace
    private static final String NS_URI = "http://sec2.org/2012/03/middleware";
    // The prefix incl. ":"
    private static final String PREFIX = "sec2:";
    // The tagname "notice"
    private static final String EVENT = "event";
    // The tagname "subject"
    private static final String SUBJECT = "subject";
    // The tagname "begin"
    private static final String BEGIN = "begin";
    // The tagname "end"
    private static final String END = "end";
    // The tagname "wholeDay"
    private static final String WHOLE_DAY = "wholeDay";
    // The tagname "location"
    private static final String LOCATION = "location";
    // The tagname "participants"
    private static final String PARTICIPANTS = "participants";
    // The tagname "repeatRate"
    private static final String REPEAT_RATE = "repeatRate";
    // The tagname "reminder"
    private static final String REMINDER = "reminder";
    // The tagname "plaintext"
    private static final String PLAINTEXT = "plaintext";
    // The tagname "creation"
    private static final String CREATION_DATE = "creation";
    // The tagname "encryptionParts"
    private static final String ENC_PARTS = "encryptionParts";
    // The tagname "encrypt"
    private static final String ENC = "encrypt";
    // The tagname "lock"
    private static final String LOCK = "lock";
    // The attribute "groups"
    private static final String ATTR_GROUPS = "groups";
    // The attribute "filename"
    private static final String FILENAME = "file";

    private Event event = null;

    /**
     * The standard constructor
     */
    public EventDomDocumentCreator() {
    }

    public EventDomDocumentCreator(final Event event) {
        this.event = event;
    }

    public EventDomDocumentCreator(final String name, final Event event) {
        this.event = event;
        this.event.setFilename(name);
        setDocumentName(name);
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event - The event to set
     */
    public void setEvent(final Event event) {
        this.event = event;
    }

    @Override
    public String createDomDocument(final CheckedGroupHandler groupHandler)
            throws ParserConfigurationException, TransformerException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        Element root = null;
        Element encParts = null;
        Element element = null;
        StringBuilder sb = null;
        final int numberOfCheckedGroups = groupHandler.getNumberOfCheckedGroups();
        int numberOfCheckedGroupsFound = 0;
        int i = 0;
        final List<String> partsToEncrypt = event.getPartsToEncrypt();
        String eventText = event.getEventText();

        document.setXmlStandalone(true);
        root = document.createElement(EVENT);
        document.appendChild(root);
        element = document.createElement(SUBJECT);
        element.setTextContent(event.getSubject());
        root.appendChild(element);
        element = document.createElement(BEGIN);
        element.setTextContent(Long.toString(event.getBegin().getTimeInMillis()));
        root.appendChild(element);
        element = document.createElement(END);
        element.setTextContent(Long.toString(event.getEnd().getTimeInMillis()));
        root.appendChild(element);
        element = document.createElement(WHOLE_DAY);
        element.setTextContent(event.isWholeDay() ? Boolean.TRUE.toString() : Boolean.FALSE
                .toString());
        root.appendChild(element);
        element = document.createElement(LOCATION);
        element.setTextContent(event.getLocation());
        root.appendChild(element);
        element = document.createElement(PARTICIPANTS);
        element.setTextContent(event.getParticipants());
        root.appendChild(element);
        element = document.createElement(REPEAT_RATE);
        element.setTextContent(event.getEventRepeatRate());
        root.appendChild(element);
        element = document.createElement(REMINDER);
        element.setTextContent(event.getReminder());
        root.appendChild(element);

        if (partsToEncrypt.size() > 0) {
            if (numberOfCheckedGroups > 0) {
                encParts = document.createElement(ENC_PARTS);
                sb = new StringBuilder();
                while (numberOfCheckedGroupsFound <= numberOfCheckedGroups
                        && i < groupHandler.getNumberOfGroups()) {
                    if (groupHandler.isChecked(i)) {
                        sb.append(groupHandler.getId(i));
                        numberOfCheckedGroupsFound++;
                        if (numberOfCheckedGroupsFound < numberOfCheckedGroups) {
                            sb.append(";");
                        }
                    }
                    i++;
                }
                encParts.setAttributeNS(NS_URI, PREFIX + ATTR_GROUPS, sb.toString());
                root.appendChild(encParts);
                i = 0;
                for (; i < partsToEncrypt.size(); i++) {
                    element = document.createElement(ENC);
                    element.setTextContent(partsToEncrypt.get(i));
                    encParts.appendChild(element);
                }
            } else {
                for (; i < partsToEncrypt.size(); i++) {
                    eventText = eventText.replaceFirst(Event.PLACE_HOLDER_PATTERN,
                            partsToEncrypt.get(i));
                }
            }
        }
        element = document.createElement(PLAINTEXT);
        element.setTextContent(eventText);
        root.appendChild(element);
        element = document.createElement(CREATION_DATE);
        element.setTextContent(Long.toString(event.getCreationDate().getTimeInMillis()));
        root.appendChild(element);
        element = document.createElement(LOCK);
        element.setTextContent(event.getLock().toString());
        root.appendChild(element);

        element = document.createElement(FILENAME);
        element.setTextContent(event.getFilename());
        root.appendChild(element);
        
        return convertDomToString(new DOMSource(document));
    }
}
