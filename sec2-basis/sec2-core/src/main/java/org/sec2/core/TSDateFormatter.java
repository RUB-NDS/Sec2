/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */

package org.sec2.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class simply wraps <code>SimpleDateFormat</code> and synchronizes
 * necessary routines in order to provide a thread safe date formatter.
 * Add left out routines if needed.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * @see java.text.SimpleDateFormat
 * 04.01.2011
 */
public final class TSDateFormatter {

    /**
     * <code>SimpleDateFormat</code> is thread <b>UNSAFE</b> by default.
     */
    private SimpleDateFormat dateFormat;

    /**
     * Default Constructor.
     *
     * @param format    Desired date format
     *                  For syntax have a look at <code>SimpleDateFormat</code>
     */
    public TSDateFormatter(final String format) {
        this.setDateFormat(new SimpleDateFormat(format, Locale.getDefault()));
    }

    /**
     * Formats a handled date according to the given format.
     *
     * @param date      <code>Date</code> object
     * @return          Formatted date/time
     */
    public synchronized String format(final Date date) {
        return this.getDateFormat().format(date);
    }

    /**
     * Parses a handled <code>String</code> to a valid Date object.
     *
     * @param date              Date to be parsed
     * @return                  Valid Date object on parsing success
     * @throws ParseException   If the handled <code>String</code> could not be
     *                          parsed
     */
    public synchronized Date parse(final String date) throws ParseException {
        return this.getDateFormat().parse(date);
    }

    /**
     * Getter for date format.
     *
     * @return Date format object
     */
    private SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Setter for date format.
     *
     * @param sdf Date format that should be set
     */
    private void setDateFormat(final SimpleDateFormat sdf) {
        this.dateFormat = sdf;
    }
}
