package org.sec2.mwserver.core.validators.impl;

import java.util.regex.Pattern;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * Implementation of the IHeaderValidator interface. Throws a
 * HandleRequestException if the value of the header field doesn't match the
 * format UNIXTIME-UNIXTIME (regex-pattern \d+-\d+). If the value before "-" is
 * not equal the value after "-", then an exceptio is thrown, too.
 *
 * @author schuessler
 */
public class UnixTimeHeaderValidator extends DefaultHeaderValidator
{
    /* (non-Javadoc)
     * @see org.sec2.desktop.servers.util.IHeaderValidator#validate(
     * java.lang.String)
     */
    @Override
    public void validate(final String key, final String value)
            throws HandleRequestException
            {
        super.validate(key, value);
        if (!(Pattern.matches("\\d+-\\d+", value.trim())
                && checkEquality(value)))
        {
            throw new HandleRequestException(this.getClass().getName()
                    + ": Wert \"" + value + "\" für Header-Field \"" + key
                    + "\" nicht gültig!");
        }
            }

    private boolean checkEquality(final String value)
    {
        String[] timestamps = value.split("-");

        return timestamps[0].equals(timestamps[1]);
    }
}
