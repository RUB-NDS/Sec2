package de.adesso.mobile.android.sec2.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class representing an event.
 * 
 * @author schuessler
 * 
 */
public class Event implements Serializable, Cloneable {

	/**
	 * Place holder for parts of the event text, which are to be encrypted.
	 * These parts are extracted from the event text and moved to a special
	 * array.
	 */
	public static final String PLACE_HOLDER = "[?]";

	/**
	 * The pattern for the String sequence of the place holder.
	 */
	public static final String PLACE_HOLDER_PATTERN = "\\[\\?\\]";

	private static final long serialVersionUID = 6343143810669063548L;
	private String mSubject = null;
	private String mEventText = null;
	public List<String> mPartsToEncrypt = null;
	private String mLocation = null;
	private String mParticipants = null;
	private String mEventRepeatRate = null;
	private String mReminder = null;
	private Calendar mBegin = null;
	private Calendar mEnd = null;
	private GregorianCalendar mCreationDate = null;
	private Lock mLock = null;
	private boolean mWholeDay = false;

	private String mFilename = null;

	/**
	 * @return the subject
	 */
	public final String getSubject() {
		return mSubject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public final void setSubject(final String subject) {
		this.mSubject = subject;
	}

	/**
	 * @return the mEventText
	 */
	public final String getEventText() {
		return mEventText;
	}

	/**
	 * @param noticeText
	 *            the noticeText to set
	 */
	public final void setEventText(final String noticeText) {
		this.mEventText = noticeText;
	}

	/**
	 * @return the mPartsToEncrypt
	 */
	public final List<String> getPartsToEncrypt() {
		return mPartsToEncrypt;
	}

	/**
	 * @param partsToEncrypt
	 *            the partsToEncrypt to set
	 */
	public final void setPartsToEncrypt(final List<String> partsToEncrypt) {
		this.mPartsToEncrypt = partsToEncrypt;
	}

	/**
	 * @return the mCreationDate
	 */
	public final GregorianCalendar getCreationDate() {
		return mCreationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public final void setCreationDate(final GregorianCalendar creationDate) {
		this.mCreationDate = creationDate;
	}

	/**
	 * @return the mLock
	 */
	public final Lock getLock() {
		return mLock;
	}

	/**
	 * @param lock
	 *            the lock to set
	 */
	public final void setLock(final Lock lock) {
		this.mLock = lock;
	}

	/**
	 * @return the mLocation
	 */
	public final String getLocation() {
		return mLocation;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public final void setLocation(final String location) {
		this.mLocation = location;
	}

	/**
	 * @return the mParticipants
	 */
	public final String getParticipants() {
		return mParticipants;
	}

	public final String[] getAttendeesIdList() {
		return mParticipants.split(",");
	}

	/**
	 * @param participants
	 *            the participants to set
	 */
	public final void setParticipants(final String participants) {
		this.mParticipants = participants;
	}

	/**
	 * @return the mBegin
	 */
	public final Calendar getBegin() {
		return mBegin;
	}

	/**
	 * @param begin
	 *            the begin to set
	 */
	public final void setBegin(final Calendar begin) {
		this.mBegin = begin;
	}

	/**
	 * @return the mEnd
	 */
	public final Calendar getEnd() {
		return mEnd;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public final void setEnd(final Calendar end) {
		this.mEnd = end;
	}

	/**
	 * @return the mWholeDay
	 */
	public final boolean isWholeDay() {
		return mWholeDay;
	}

	/**
	 * @param wholeDay
	 *            the wholeDay to set
	 */
	public final void setWholeDay(final boolean wholeDay) {
		this.mWholeDay = wholeDay;
	}

	/**
	 * @return the mEventRepeatRate
	 */
	public final String getEventRepeatRate() {
		return mEventRepeatRate;
	}

	/**
	 * @return the mReminder
	 */
	public final String getReminder() {
		return mReminder;
	}

	/**
	 * @param eventRepeatRate
	 *            the eventRepeatRate to set
	 */
	public final void setEventRepeatRate(final String eventRepeatRate) {
		this.mEventRepeatRate = eventRepeatRate;
	}

	/**
	 * @param reminder
	 *            the reminder to set
	 */
	public final void setReminder(final String reminder) {
		this.mReminder = reminder;
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
