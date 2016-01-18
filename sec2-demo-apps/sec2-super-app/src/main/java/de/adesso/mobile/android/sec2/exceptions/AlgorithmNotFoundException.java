package de.adesso.mobile.android.sec2.exceptions;

import android.content.Context;
import de.adesso.mobile.android.sec2.R;

/**
 * 
 * This exception is thrown, if there is no algorithm  present while loading data from cloud
 * 
 * @author hoppe
 *
 */
public class AlgorithmNotFoundException extends Exception {

    private static final long serialVersionUID = -749128777712303846L;

    /**
     * The constructor for this exception
     *
     */
    public AlgorithmNotFoundException() {
        super("Algorithm must not be NULL!");
    }

    /**
     * The constructor for this exception, expecting the context.
     *
     * @param context - The context being used to load the default error message
     */
    public AlgorithmNotFoundException(Context context) {
        super(context.getString(R.string.service_algorithm_null));
    }

    /**
     * The constructor for this exception, expecting an error message.
     *
     * @param message - The error message
     */
    public AlgorithmNotFoundException(String message) {
        super(message);
    }

}
