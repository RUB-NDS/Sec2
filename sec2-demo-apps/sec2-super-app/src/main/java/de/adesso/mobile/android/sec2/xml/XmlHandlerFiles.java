package de.adesso.mobile.android.sec2.xml;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Sec2File;

/**
 * A handler for parsing the XML representation of a file object. The handler expects an
 * XML representation of the following form:
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <file>
 *  <name>[subject]</name>
 *  <encryptionParts xmlns:sec2="http://sec2.org/2012/03/middleware/enc/v1" sec2:groups=[Groups]>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *   <encrypt>[Textpart to encrypt]</encrypt>
 *  </encryptionParts>)
 *  <plaintext>[Plaintext]</plaintext>
 *  <creation>[Creationdate]</creation>
 *  <lock>[Lock status]</lock>
 * </file>}
 * </pre>
 * Parts in parentheses are optional. The order of tags of one tree-level may be arbitrary.
 * 
 * @author hoppe
 *
 */
public class XmlHandlerFiles extends DefaultHandler {

    private static final String FILE_TAG = "file";
    private static final String NAME_TAG = "name";
    private static final String PLAINTEXT_TAG = "plaintext";
    private static final String PLAINTEXT_PARTS_TAG = "plaintextParts";
    private static final String CREATION_TAG = "creation";
    private static final String LOCK_TAG = "lock";
    private static final String ENC_PARTS_TAG = "encryptionParts";
    private static final String ENC_TAG = "encrypt";
    private static final String ERR_NOT_IN_FILE = "Tag <{0}> nicht in <" + FILE_TAG + ">.";
    private static final String ERR_NOT_IN_ENC_PART = "Tag <{0}> nicht in <" + ENC_PARTS_TAG + ">.";
    private static final String ERR_NOT_IN_PLAINTEXT_PART = "Tag <{0}> nicht in <"
            + PLAINTEXT_PARTS_TAG + ">.";
    private static final String ERR_MULTIPLE_TAG = "Tag <{0}> darf nicht mehrmals vorkommen!";
    private static final String ERR_TAG_NOT_EXPECTED = "Tag <{0} nicht erwartet!";

    private boolean mInFile = false;
    private boolean mInEncPart = false;
    private boolean mInTag = false;
    private boolean mInPlaintextPart = false;
    private boolean mNameOccured = false;
    private boolean mCreationOccured = false;
    private boolean mPlaintextPartsOccured = false;
    private boolean mLockOccured = false;
    private boolean mEncPartsOccured = false;
    private List<String> mPlaintextParts = null;
    private List<String> mEncParts = null;
    private int mTagCount = 0;
    private String mCurrentValue = null;
    private final Sec2File mSec2File = new Sec2File();

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        mTagCount++;

        if (FILE_TAG.equals(localName)) {
            if (mTagCount != 1) {
                throw new SAXException("Tag <" + localName + "> in Zeile " + mTagCount
                        + " nicht erwartet. Erwartet: <" + FILE_TAG + ">.");
            }
            mInFile = true;
        } else if (NAME_TAG.equals(localName)) {
            if (!mInFile || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_FILE, localName));
            }
            if (mNameOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mNameOccured = true;
            mInTag = true;
        } else if (CREATION_TAG.equals(localName)) {
            if (!mInFile || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_FILE, localName));
            }
            if (mCreationOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mCreationOccured = true;
            mInTag = true;
        } else if (LOCK_TAG.equals(localName)) {
            if (!mInFile || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_FILE, localName));
            }
            if (mLockOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mLockOccured = true;
            mInTag = true;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            if (!mInFile || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_FILE, localName));
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
            if (!mInPlaintextPart || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_PLAINTEXT_PART, localName));
            }
            mInTag = true;
        } else if (PLAINTEXT_PARTS_TAG.equals(localName)) {
            if (!mInFile || mInTag) {
                throw new SAXException(MessageFormat.format(ERR_NOT_IN_FILE, localName));
            }
            if (mPlaintextPartsOccured) {
                throw new SAXException(MessageFormat.format(ERR_MULTIPLE_TAG, localName));
            }
            mPlaintextPartsOccured = true;
            mInPlaintextPart = true;
            mPlaintextParts = new LinkedList<String>();
        } else {
            throw new SAXException(MessageFormat.format(ERR_TAG_NOT_EXPECTED, localName));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (FILE_TAG.equals(localName)) {
            mInFile = false;
        } else if (NAME_TAG.equals(localName)) {
            mSec2File.setName(mCurrentValue);
            mInTag = false;
        } else if (CREATION_TAG.equals(localName)) {
            mSec2File.setCreationDate(createCreationDate(mCurrentValue));
            mInTag = false;
        } else if (LOCK_TAG.equals(localName)) {
            mSec2File.setLock(convertStringToLock(mCurrentValue));
            mInTag = false;
        } else if (ENC_PARTS_TAG.equals(localName)) {
            mSec2File.setPartsToEncrypt(mEncParts.toArray(new String[mEncParts.size()]));
            mInEncPart = false;
        } else if (ENC_TAG.equals(localName)) {
            mEncParts.add(mCurrentValue);
            mInTag = false;
        } else if (PLAINTEXT_TAG.equals(localName)) {
            mPlaintextParts.add(mCurrentValue);
            mInTag = false;
        } else if (PLAINTEXT_PARTS_TAG.equals(localName)) {
            mSec2File.setPlainText(mPlaintextParts.toArray(new String[mPlaintextParts.size()]));
            mInPlaintextPart = false;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        mCurrentValue = new String(ch, start, length);
    }

    public Sec2File getSec2File() {
        return mSec2File;
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
