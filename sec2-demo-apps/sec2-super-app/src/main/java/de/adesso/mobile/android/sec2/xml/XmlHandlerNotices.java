package de.adesso.mobile.android.sec2.xml;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Notice;

/**
 * A handler for parsing the XML representation of a notice object. The handler expects an
 * XML representation of the following form:
 * 
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
 * </notice>
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author schuessler
 *
 */
public class XmlHandlerNotices extends DefaultHandler {

    private static final String NOTICE_TAG = "notice";
    private static final String SUBJECT_TAG = "subject";
    private static final String PLAINTEXT_TAG = "plaintext";
    private static final String CREATION_TAG = "creation";
    private static final String LOCK_TAG = "lock";
    private static final String ENC_PARTS_TAG = "encryptionParts";
    private static final String ENC_TAG = "encrypt";
    private static final String ERR_NOT_IN_NOTICE = "Tag <{0}> nicht in <" + NOTICE_TAG + ">.";
    private static final String ERR_NOT_IN_ENC_PART = "Tag <{0}> nicht in <" + ENC_PARTS_TAG + ">.";
    private static final String ERR_MULTIPLE_TAG = "Tag <{0}> darf nicht mehrmals vorkommen!";
    private static final String ERR_TAG_NOT_EXPECTED = "Tag <{0} nicht erwartet!";

    private boolean inNotice = false;
    private boolean inEncPart = false;
    private boolean inTag = false;
    private boolean subjectOccured = false;
    private boolean plaintextOccured = false;
    private boolean creationOccured = false;
    private boolean lockOccured = false;
    private boolean encPartsOccured = false;
    private List<String> encParts = null;
    private int tagCount = 0;
    private String currentValue = null;
    private final Notice notice = new Notice();

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        tagCount++;

        if (NOTICE_TAG.equals(localName)) {
            if (tagCount != 1) {
                throw new SAXException("Tag <" + localName + "> in Zeile " + tagCount
                        + " nicht erwartet. Erwartet: <" + NOTICE_TAG + ">.");
            }
            inNotice = true;
        } else if (SUBJECT_TAG.equals(localName)) {
            if (!inNotice || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_NOTICE, localName));
            }
            if (subjectOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            subjectOccured = true;
            inTag = true;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            if (!inNotice || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_NOTICE, localName));
            }
            if (plaintextOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            plaintextOccured = true;
            inTag = true;
        } else if (CREATION_TAG.equals(localName)) {
            if (!inNotice || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_NOTICE, localName));
            }
            if (creationOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            creationOccured = true;
            inTag = true;
        } else if (LOCK_TAG.equals(localName)) {
            if (!inNotice || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_NOTICE, localName));
            }
            if (lockOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            lockOccured = true;
            inTag = true;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            if (!inNotice || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_NOTICE, localName));
            }
            if (encPartsOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            encPartsOccured = true;
            inEncPart = true;
            encParts = new LinkedList<String>();
        } else if (ENC_TAG.equals(localName)) {
            if (!inEncPart || inTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_ENC_PART, localName));
            }
            inTag = true;
        } else {
            throw new SAXException(MessageFormat.format(ERR_TAG_NOT_EXPECTED, localName));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (NOTICE_TAG.equals(localName)) {
            inNotice = false;
        } else if (SUBJECT_TAG.equals(localName)) {
            notice.setSubject(currentValue);
            inTag = false;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            notice.setNoticeText(currentValue);
            inTag = false;
        } else if (CREATION_TAG.equals(localName)) {
            notice.setCreationDate(createCreationDate(currentValue));
            inTag = false;
        } else if (LOCK_TAG.equals(localName)) {
            notice.setLock(convertStringToLock(currentValue));
            inTag = false;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            notice.setPartsToEncrypt(encParts);
            inEncPart = false;
        } else if (ENC_TAG.equals(localName)) {
            encParts.add(currentValue);
            inTag = false;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        currentValue = new String(ch, start, length);
    }

    public Notice getNotice() {
        return notice;
    }

    private GregorianCalendar createCreationDate(final String timeInMillis) throws SAXException {
        final GregorianCalendar creationDate = new GregorianCalendar();

        try {
            creationDate.setTimeInMillis(Long.parseLong(timeInMillis));
        } catch (final NumberFormatException nfe) {
            throw new SAXException(nfe);
        }

        return creationDate;
    }

    private Lock convertStringToLock(final String lock) throws SAXException {
        try {
            return Lock.valueOf(lock);
        } catch (final Exception e) {
            throw new SAXException(e);
        }
    }
}
