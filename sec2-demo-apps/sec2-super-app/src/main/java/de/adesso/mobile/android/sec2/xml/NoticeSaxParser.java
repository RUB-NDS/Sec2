/**
 * 
 */

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
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Notice;

/**
 * A handler for parsing the XML representation of a notice object. The handler expects an
 * XML representation of the following form:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <notice>
 *  <subject>[subject]</subject>
 *  (<encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>)
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
 * </notice>}
 * </pre>
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author hoppe
 *
 */
public final class NoticeSaxParser {

    private static final String NAMESPACE = "http://exist.sourceforge.net/NS/exist";
    private static final String ROOT = "result";

    private static final String NOTICE = "notice";
    private static final String SUBJECT = "subject";
    private static final String ENC_PARTS = "encryptionParts";
    private static final String ENC = "encrypt";

    private static final String PLAINTEXT = "plaintext";
    private static final String CREATION = "creation";
    private static final String LOCK = "lock";
    
    private static final String FILE = "file";

    /**
     * Constructor
     */
    private NoticeSaxParser() {

    }

    /**
     * Method to parse the given xml content into a list of Notice.java
     * @param inputStream an InputStream containing xml content 
     * @return List<Notice> representing the given xml content
     * @throws SAXException thrown when an Exception during the parsing process occurs
     * @throws IOException thrown when an Exception during the parsing process occurs
     */
    public static List<Notice> parseList(final InputStream inputStream) throws SAXException,
            IOException {

        final List<Notice> notices = new ArrayList<Notice>();

        final Notice notice = new Notice();
        final RootElement root = new RootElement(NAMESPACE, ROOT);
        final Element element = root.getChild(NOTICE);

        element.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                notices.add((Notice) notice.clone());
            }
        });

        element.getChild(SUBJECT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                notice.setSubject(body);
            }
        });

        element.getChild(PLAINTEXT).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                notice.setNoticeText(body);
            }
        });

        element.getChild(ENC_PARTS).setStartElementListener(new StartElementListener() {

            @Override
            public void start(final Attributes attributes) {
                notice.setPartsToEncrypt(new ArrayList<String>());
            }
        });

        element.getChild(ENC_PARTS).getChild(ENC)
                .setEndTextElementListener(new EndTextElementListener() {

                    @Override
                    public void end(final String body) {
                        notice.getPartsToEncrypt().add(body);
                    }
                });

        element.getChild(CREATION).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                final GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(Long.valueOf(body));
                notice.setCreationDate(calendar);
            }
        });

        element.getChild(LOCK).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                notice.setLock(Lock.valueOf(body));
            }
        });
        
        element.getChild(FILE).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(final String body) {
                notice.setFilename(body);
            }
        });

        Xml.parse(inputStream, Xml.Encoding.ISO_8859_1, root.getContentHandler());
        return notices;
    }
}
