
package de.adesso.mobile.android.sec2.xml;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Priority;
import de.adesso.mobile.android.sec2.model.Task;

/**
 * A handler for parsing the XML representation of a task object. The handler expects an
 * XML representation of the following form:
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <task>
 *  <subject>[subject]</subject>
 *  <due>[Date of task]</due>
 *  <reminder>[Reminder]</reminder>
 *  <isDone>[Is task done]</isDone>
 *  <priority>[Priority of a task]</priority>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>
 *  <plaintext>[Plaintext]</plaintext>
 *  <lock>[Lock status]</lock>
 * </task>
 * 
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author hoppe
 *
 */
public class XmlHandlerTasks extends DefaultHandler {

    private static final String TASK_TAG = "task";
    private static final String SUBJECT_TAG = "subject";
    private static final String DUE_TAG = "due";
    private static final String IS_DONE_TAG = "isDone";
    private static final String REMINDER_TAG = "reminder";
    private static final String PLAINTEXT_TAG = "plaintext";
    private static final String PRIORITY_TAG = "priority";
    private static final String LOCK_TAG = "lock";
    private static final String ENC_PARTS_TAG = "encryptionParts";
    private static final String ENC_TAG = "encrypt";
    private static final String CREATION_TAG = "creation";

    private static final String ERR_NOT_IN_TASK = "Tag <{0}> nicht in <" + TASK_TAG + ">.";
    private static final String ERR_NOT_IN_ENC_PART = "Tag <{0}> nicht in <" + ENC_PARTS_TAG + ">.";
    private static final String ERR_MULTIPLE_TAG = "Tag <{0}> darf nicht mehrmals vorkommen!";
    private static final String ERR_TAG_NOT_EXPECTED = "Tag <{0} nicht erwartet!";

    private boolean mInTask = false;
    private boolean mInEncPart = false;
    private boolean mInTag = false;
    private boolean mSubjectOccured = false;
    private boolean mDueOccured = false;
    private boolean mPriorityOccured = false;
    private boolean mIsDoneOccured = false;
    private boolean mReminderOccured = false;
    private boolean mPlaintextOccured = false;
    private boolean mLockOccured = false;
    private boolean mEncPartsOccured = false;
    private boolean mCreationOccured = false;
    private List<String> mEncParts = null;
    private int mTagCount = 0;
    private String mCurrentValue = null;
    private final Task mTask = new Task();

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        mTagCount++;

        if (TASK_TAG.equals(localName)) {
            if (mTagCount != 1) {
                throw new SAXException("Tag <" + localName + "> in Zeile " + mTagCount
                        + " nicht erwartet. Erwartet: <" + TASK_TAG + ">.");
            }
            mInTask = true;
        } else if (SUBJECT_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mSubjectOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mSubjectOccured = true;
            mInTag = true;
        } else if (DUE_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mDueOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mDueOccured = true;
            mInTag = true;
        } else if (REMINDER_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mReminderOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mReminderOccured = true;
            mInTag = true;
        } else if (IS_DONE_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mIsDoneOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mIsDoneOccured = true;
            mInTag = true;
        } else if (PRIORITY_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mPriorityOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mPriorityOccured = true;
            mInTag = true;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
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
        } else if (PLAINTEXT_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mPlaintextOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mPlaintextOccured = true;
            mInTag = true;
        } else if (LOCK_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mLockOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mLockOccured = true;
            mInTag = true;
        } else if (CREATION_TAG.equals(localName)) {
            if (!mInTask || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_TASK, localName));
            }
            if (mCreationOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mCreationOccured = true;
            mInTag = true;
        } else {
            throw new SAXException(MessageFormat.format(ERR_TAG_NOT_EXPECTED, localName));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (TASK_TAG.equals(localName)) {
            mInTask = false;
        } else if (SUBJECT_TAG.equals(localName)) {
            mTask.setSubject(mCurrentValue);
            mInTag = false;
        } else if (DUE_TAG.equals(localName)) {
            mTask.setDueDate(createDate(mCurrentValue));
            mInTag = false;
        } else if (REMINDER_TAG.equals(localName)) {
            mTask.setReminderDate(createDate(mCurrentValue));
            mInTag = false;
        } else if (IS_DONE_TAG.equals(localName)) {
            mTask.setIsDone(Boolean.parseBoolean(mCurrentValue));
            mInTag = false;
        } else if (PRIORITY_TAG.equals(localName)) {
            mTask.setPriority(convertStringToPriority(mCurrentValue));
            mInTag = false;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            mTask.setPartsToEncrypt(mEncParts);
            mInEncPart = false;
        } else if (ENC_TAG.equals(localName)) {
            mEncParts.add(mCurrentValue);
            mInTag = false;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            mTask.setTaskText(mCurrentValue);
            mInTag = false;
        } else if (LOCK_TAG.equals(localName)) {
            mTask.setLock(convertStringToLock(mCurrentValue));
            mInTag = false;
        } else if (CREATION_TAG.equals(localName)) {
            mTask.setCreationDate(createDate(mCurrentValue));
            mInTag = false;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        mCurrentValue = new String(ch, start, length);
    }

    public Task getTask() {
        return mTask;
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

    private Priority convertStringToPriority(final String priority) throws SAXException {
        try {
            return Priority.valueOf(priority);
        } catch (final Exception e) {
            throw new SAXException(e);
        }
    }
}
