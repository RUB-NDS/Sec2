
package de.adesso.mobile.android.sec2.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;
import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.model.Lock;

/**
 * A handler for parsing the XML representation of an event object. The handler expects an
 * XML representation of the following form:
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
 *  (<encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>)
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
 * </event>
 * </pre>
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author hoppe
 *
 */
public class EventParser {

    private static final String TAG = "EventParser";

    private static final String NAMESPACE = "http://exist.sourceforge.net/NS/exist";
    private static final String ROOT = "result";

    private static final String EVENT = "event";
    private static final String SUBJECT = "subject";
    private static final String ENC_PARTS = "encryptionParts";
    private static final String ENC = "encrypt";
    private static final String BEGIN = "begin";
    private static final String END = "end";
    private static final String WHOLE_DAY = "wholeDay";
    private static final String LOCATION = "location";
    private static final String PARTICIPANTS = "participants";
    private static final String REPEAT_RATE = "repeatRate";
    private static final String REMINDER = "reminder";

    private static final String PLAINTEXT = "plaintext";
    private static final String CREATION = "creation";
    private static final String LOCK = "lock";

    private static final String FILE = "file";
    
    public static List<Event> parseList(final InputStream inputStream) throws IOException,
            SAXException {
        final List<Event> events = new ArrayList<Event>();
        final Event event = new Event();

        final RootElement root = new RootElement(NAMESPACE, ROOT);
        final Element element = root.getChild(EVENT);

        element.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                events.add((Event) event.clone());
            }
        });

        element.getChild(SUBJECT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setSubject(body);
            }
        });

        element.getChild(PLAINTEXT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setEventText(body);
            }
        });

        element.getChild(ENC_PARTS).setStartElementListener(new StartElementListener() {

            @Override
            public void start(final Attributes attributes) {
                event.setPartsToEncrypt(new ArrayList<String>());
            }
        });

        element.getChild(ENC_PARTS).getChild(ENC)
                .setEndTextElementListener(new EndTextElementListener() {

                    @Override
                    public void end(final String body) {
                        event.mPartsToEncrypt.add(body);
                    }
                });

        element.getChild(CREATION).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setCreationDate(createCalendar(body));
            }
        });

        element.getChild(LOCK).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setLock(Lock.valueOf(body));
            }
        });

        element.getChild(BEGIN).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setBegin(createCalendar(body));
            }
        });
        element.getChild(END).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setEnd(createCalendar(body));
            }
        });
        element.getChild(WHOLE_DAY).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setWholeDay(Boolean.parseBoolean(body));
            }
        });
        element.getChild(LOCATION).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setLocation(body);
            }
        });
        element.getChild(PARTICIPANTS).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setParticipants(body);
            }
        });
        element.getChild(REPEAT_RATE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setEventRepeatRate(body);
            }
        });
        element.getChild(REMINDER).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setReminder(body);
            }
        });
        element.getChild(FILE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                event.setFilename(body);
            }
        });

        Xml.parse(inputStream, Xml.Encoding.ISO_8859_1, root.getContentHandler());

        return events;
    }

    private static GregorianCalendar createCalendar(final String timeInMillis) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(Long.valueOf(timeInMillis));
        return calendar;
    }

}
