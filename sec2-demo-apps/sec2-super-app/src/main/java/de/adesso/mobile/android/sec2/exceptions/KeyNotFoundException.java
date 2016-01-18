package de.adesso.mobile.android.sec2.exceptions;

import android.content.Context;
import de.adesso.mobile.android.sec2.R;

/**
 * 
 * This exception is thrown, if there is no key present while loading data from cloud
 * 
 * @author hoppe
 *
 */
public class KeyNotFoundException extends Exception {

    private static final long serialVersionUID = -4252985306949092974L;

    /**
     * The constructor for this exception
     *
     */
    public KeyNotFoundException() {
        super("Key must not be NULL!");
    }

    /**
     * The constructor for this exception, expecting the context.
     *
     * @param context - The context being used to load the default error message
     */
    public KeyNotFoundException(Context context) {
        super(context.getString(R.string.service_key_null));
    }

    /**
     * The constructor for this exception, expecting an error message.
     *
     * @param message - The error message
     */
    public KeyNotFoundException(String message) {
        super(message);
    }

}
