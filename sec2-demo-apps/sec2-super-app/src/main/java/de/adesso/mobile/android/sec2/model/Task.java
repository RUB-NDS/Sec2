
package de.adesso.mobile.android.sec2.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class representing a task.
 * 
 * @author schuessler
 *
 */
public class Task implements Serializable, Cloneable {

    /**
     * Place holder for parts of the notice text, which are to be encrypted. These parts are extracted from
     * the notice text and moved to a special array.
     */
    public static final String PLACE_HOLDER = "[?]";

    /**
     * The pattern for the String sequence of the place holder.
     */
    public static final String PLACE_HOLDER_PATTERN = "\\[\\?\\]";

    private static final long serialVersionUID = 5343143810669063547L;
    private String mSubject = null;
    private String mTaskText = null;
    private List<String> mPartsToEncrypt = null;
    private Calendar mDueDate = null;
    private Calendar mReminderDate = null;
    private Priority mPriority;
    private boolean mIsDone = false;
    private GregorianCalendar mCreationDate = null;

    private Lock mLock = null;

    private String mFilename = null;
    
    /**
     * @return the subject
     */
    public final String getSubject() {
        return mSubject;
    }

    /**
     * @param subject the subject to set
     */
    public final void setSubject(final String subject) {
        this.mSubject = subject;
    }

    /**
     * @return the mTaskText
     */
    public final String getTaskText() {
        return mTaskText;
    }

    /**
     * @param noticeText the noticeText to set
     */
    public final void setTaskText(final String taskText) {
        this.mTaskText = taskText;
    }

    /**
     * @return the mPartsToEncrypt
     */
    public final List<String> getPartsToEncrypt() {
        return mPartsToEncrypt;
    }

    /**
     * @param partsToEncrypt the partsToEncrypt to set
     */
    public final void setPartsToEncrypt(final List<String> partsToEncrypt) {
        this.mPartsToEncrypt = partsToEncrypt;
    }

    /**
     * @return the mDueDate
     */
    public final Calendar getDueDate() {
        return mDueDate;
    }

    /**
     * @param dueDate the dueDate to set
     */
    public final void setDueDate(final Calendar dueDate) {
        this.mDueDate = dueDate;
    }

    /**
     * @return the mReminderDate
     */
    public final Calendar getReminderDate() {
        return mReminderDate;
    }

    /**
     * @param reminderDate the reminderDate to set
     */
    public final void setReminderDate(final Calendar reminderDate) {
        this.mReminderDate = reminderDate;
    }

    /**
     * @return the mPriority
     */
    public final Priority getPriority() {
        return mPriority;
    }

    /**
     * @param priority the priority to set
     */
    public final void setPriority(final Priority priority) {
        this.mPriority = priority;
    }

    /**
     * @return the mIsDone
     */
    public final boolean getIsDone() {
        return mIsDone;
    }

    /**
     * @param isDone the isDone to set
     */
    public final void setIsDone(final boolean isDone) {
        this.mIsDone = isDone;
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

    
    /**
     * @return
     */
    public final String getFilename() {
        return mFilename;
    }


    /**
     * @param fileName
     */
    public final void setFilename(final String fileName) {
        this.mFilename = fileName;
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}
