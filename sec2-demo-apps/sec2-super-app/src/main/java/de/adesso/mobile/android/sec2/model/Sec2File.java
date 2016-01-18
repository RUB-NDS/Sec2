package de.adesso.mobile.android.sec2.model;

import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 * Class representing a task.
 * 
 * @author hoppe
 *
 */
public class Sec2File implements Serializable {

    /**
     * Place holder for parts of the notice text, which are to be encrypted. These parts are extracted from
     * the notice text and moved to a special array.
     */
    public static final String PLACE_HOLDER = "[?]";

    /**
     * The pattern for the String sequence of the place holder.
     */
    public static final String PLACE_HOLDER_PATTERN = "\\[\\?\\]";

    private static final long serialVersionUID = 1343143810669063547L;
    private String mName = null;
    private String[] mPlainText = null;
    private String[] mPartsToEncrypt = null;
    private GregorianCalendar mCreationDate = null;

    private Lock mLock = null;

    /**
     * @return the mName
     */
    public final String getName() {
        return mName;
    }

    /**
     * @param name the name to set
     */
    public final void setName(final String name) {
        this.mName = name;
    }

    /**
     * @return the Base64Encoded file
     */
    public final String[] getPlainText() {
        return mPlainText.clone();
    }

    /**
     * @param noticeText the noticeText to set
     */
    public final void setPlainText(final String[] base64EncodedText) {
        this.mPlainText = base64EncodedText.clone();
    }

    /**
     * @return the mPartsToEncrypt
     */
    public final String[] getPartsToEncrypt() {
        return mPartsToEncrypt.clone();
    }

    /**
     * @param partsToEncrypt the partsToEncrypt to set
     */
    public final void setPartsToEncrypt(final String[] partsToEncrypt) {
        this.mPartsToEncrypt = partsToEncrypt.clone();
        // TODO: @hoppe:  (28.02.2013)
        //        this.partsToEncrypt = new String[] { base64EncodedText };
    }

    /**
     * @return the mLock
     */
    public final Lock getLock() {
        return mLock;
    }

    /**
     * @param lock the lock to set
     */
    public final void setLock(final Lock lock) {
        this.mLock = lock;
    }

    /**
     * @return the mCreationDate
     */
    public final GregorianCalendar getCreationDate() {
        return mCreationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public final void setCreationDate(final GregorianCalendar creationDate) {
        this.mCreationDate = creationDate;
    }
}
