
package de.adesso.mobile.android.sec2.util;

import android.content.Context;
import de.adesso.mobile.android.sec2.exceptions.AlgorithmNotFoundException;
import de.adesso.mobile.android.sec2.exceptions.KeyNotFoundException;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;

/**
 * Utility class to handle any calls to the MwAdapterPreferenceManager including exception handling
 * 
 * @author hoppe
 *
 */
public final class MwAdapterHelper {

    private final MwAdapterPreferenceManager mManager;

    /**
     * The Constructor for this utility class
     * 
     * @param context Context for creating an instance of MwAdapterPreferenceManager
     */
    public MwAdapterHelper(final Context context) {
        mManager = new MwAdapterPreferenceManager(context);
    }

    /**
     * Returns the App key hex encoded as a String whereas the key may be NULL or empty.
     * @return The App key
     * @throws KeyNotFoundException custom exception which is thrown when no key is present
     */
    public final String getAppAuthKey() throws KeyNotFoundException {
        final String key = mManager.getAppAuthKey();
        if (key == null) {
            throw new KeyNotFoundException();
        } else {
            return key;
        }
    }

    /**
     * Returns the algorithm the app key has to be used for as a string. The algorithm may be NULL or empty.
     * @return The algorithm
     * @throws AlgorithmNotFoundException custom exception which is thrown when no key is present
     */
    public final String getAppAuthKeyAlgorithm() throws AlgorithmNotFoundException {
        final String algorithm = mManager.getAppAuthKeyAlgorithm();
        if (algorithm == null) {
            throw new AlgorithmNotFoundException();
        } else {
            return algorithm;
        }
    }

    /**
     * Returns the path to the directory, where the resource is stored within the cloud service. 
     * If no path was stored in the preferences, an empty string is returned
     * 
     * @return The directory path within the cloud service
     */
    public final String getCloudPath() {
        String path = mManager.getCloudPath();
        if (!path.isEmpty() && !path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    /**
     * Returns the hostname of the cloud service, which is stored in the preferences. 
     * If no hostname was stored, an empty string is returned
     * 
     * @return The hostname of the cloud service
     */
    public final String getCloudHostName() {
        return mManager.getCloudHostName();
    }

    /**
     * Returns the port stored in the preferences for the communication with the cloud. The method may throw
     * a NumberFormatException if the stored port can't be parsed to an integer.
     * 
     * @return The port
     */
    public final int getCloudPort() {
        return mManager.getCloudPort();
    }

    /**
     * Returns the port stored in the preferences for the communication with the middleware. The method may throw
     * a NumberFormatException if the stored port can't be parsed to an integer.
     * 
     * @return The port
     */
    public final int getMiddlewarePort() {
        return mManager.getMiddlewarePort();
    }

}
