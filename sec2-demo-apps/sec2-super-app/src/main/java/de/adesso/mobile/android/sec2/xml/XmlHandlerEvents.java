
package de.adesso.mobile.android.sec2.xml;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.model.Lock;

/**
 * A handler for parsing the XML representation of an event object. The handler expects an
 * XML representation of the following form:
 * 
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
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author schuessler
 *
 */
public class XmlHandlerEvents extends DefaultHandler {

    private static final String EVENT_TAG = "event";
    private static final String SUBJECT_TAG = "subject";
    private static final String BEGIN_TAG = "begin";
    private static final String END_TAG = "end";
    private static final String WHOLE_DAY_TAG = "wholeDay";
    private static final String LOCATION_TAG = "location";
    private static final String PARTICIPANTS_TAG = "participants";
    private static final String REPEAT_RATE_TAG = "repeatRate";
    private static final String REMINDER_TAG = "reminder";
    private static final String PLAINTEXT_TAG = "plaintext";
    private static final String CREATION_TAG = "creation";
    private static final String LOCK_TAG = "lock";
    private static final String ENC_PARTS_TAG = "encryptionParts";
    private static final String ENC_TAG = "encrypt";
    private static final String ERR_NOT_IN_EVENT = "Tag <{0}> nicht in <" + EVENT_TAG + ">.";
    private static final String ERR_NOT_IN_ENC_PART = "Tag <{0}> nicht in <" + ENC_PARTS_TAG + ">.";
    private static final String ERR_MULTIPLE_TAG = "Tag <{0}> darf nicht mehrmals vorkommen!";
    private static final String ERR_TAG_NOT_EXPECTED = "Tag <{0} nicht erwartet!";

    private boolean mInEvent = false;
    private boolean mInEncPart = false;
    private boolean mInTag = false;
    private boolean mSubjectOccured = false;
    private boolean mBeginOccured = false;
    private boolean mEndOccured = false;
    private boolean mWholeDayOccured = false;
    private boolean mLocationOccured = false;
    private boolean mParticipantsOccured = false;
    private boolean mRepeatRateOccured = false;
    private boolean mReminderOccured = false;
    private boolean mPlaintextOccured = false;
    private boolean mCreationOccured = false;
    private boolean mLockOccured = false;
    private boolean mEncPartsOccured = false;
    private List<String> mEncParts = null;
    private int mTagCount = 0;
    private String mCurrentValue = null;
    private final Event mEvent = new Event();

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        mTagCount++;

        if (EVENT_TAG.equals(localName)) {
            if (mTagCount != 1) {
                throw new SAXException("Tag <" + localName + "> in Zeile " + mTagCount
                        + " nicht erwartet. Erwartet: <" + EVENT_TAG + ">.");
            }
            mInEvent = true;
        } else if (SUBJECT_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mSubjectOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mSubjectOccured = true;
            mInTag = true;
        } else if (BEGIN_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mBeginOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mBeginOccured = true;
            mInTag = true;
        } else if (END_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mEndOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mEndOccured = true;
            mInTag = true;
        } else if (WHOLE_DAY_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mWholeDayOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mWholeDayOccured = true;
            mInTag = true;
        } else if (LOCATION_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mLocationOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mLocationOccured = true;
            mInTag = true;
        } else if (PARTICIPANTS_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mParticipantsOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mParticipantsOccured = true;
            mInTag = true;
        } else if (REPEAT_RATE_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mRepeatRateOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mRepeatRateOccured = true;
            mInTag = true;
        } else if (REMINDER_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mReminderOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mReminderOccured = true;
            mInTag = true;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mPlaintextOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mPlaintextOccured = true;
            mInTag = true;
        } else if (CREATION_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mCreationOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mCreationOccured = true;
            mInTag = true;
        } else if (LOCK_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mLockOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mLockOccured = true;
            mInTag = true;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            if (!mInEvent || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_EVENT, localName));
            }
            if (mEncPartsOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mEncPartsOccured = true;
            mInEncPart = true;
            mEncParts = new LinkedList<String>();
        } else if (ENC_TAG.equals(localName)) {
            if (!mInEncPart || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_ENC_PART, localName));
            }
            mInTag = true;
        } else {
            throw new SAXException(MessageFormat.format(ERR_TAG_NOT_EXPECTED, localName));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (EVENT_TAG.equals(localName)) {
            mInEvent = false;
        } else if (SUBJECT_TAG.equals(localName)) {
            mEvent.setSubject(mCurrentValue);
            mInTag = false;
        } else if (BEGIN_TAG.equals(localName)) {
            mEvent.setBegin(createDate(mCurrentValue));
            mInTag = false;
        } else if (END_TAG.equals(localName)) {
            mEvent.setEnd(createDate(mCurrentValue));
            mInTag = false;
        } else if (WHOLE_DAY_TAG.equals(localName)) {
            mEvent.setWholeDay(Boolean.parseBoolean(mCurrentValue));
            mInTag = false;
        } else if (LOCATION_TAG.equals(localName)) {
            mEvent.setLocation(mCurrentValue);
            mInTag = false;
        } else if (PARTICIPANTS_TAG.equals(localName)) {
            mEvent.setParticipants(mCurrentValue);
            mInTag = false;
        } else if (REPEAT_RATE_TAG.equals(localName)) {
            mEvent.setEventRepeatRate(mCurrentValue);
            mInTag = false;
        } else if (REMINDER_TAG.equals(localName)) {
            mEvent.setReminder(mCurrentValue);
            mInTag = false;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            mEvent.setEventText(mCurrentValue);
            mInTag = false;
        } else if (CREATION_TAG.equals(localName)) {
            mEvent.setCreationDate(createDate(mCurrentValue));
            mInTag = false;
        } else if (LOCK_TAG.equals(localName)) {
            mEvent.setLock(convertStringToLock(mCurrentValue));
            mInTag = false;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            mEvent.setPartsToEncrypt(mEncParts);
            mInEncPart = false;
        } else if (ENC_TAG.equals(localName)) {
            mEncParts.add(mCurrentValue);
            mInTag = false;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        mCurrentValue = new String(ch, start, length);
    }

    public Event getEvent() {
        return mEvent;
    }

    private GregorianCalendar createDate(final String timeInMillis) throws SAXException {
        final GregorianCalendar date = new GregorianCalendar();

        try {
            date.setTimeInMillis(Long.parseLong(timeInMillis));
        } catch (final NumberFormatException nfe) {
            throw new SAXException(nfe);
        }

        return date;
    }

    private Lock convertStringToLock(final String lock) throws SAXException {
        try {
            return Lock.valueOf(lock);
        } catch (final Exception e) {
            throw new SAXException(e);
        }
    }
}
