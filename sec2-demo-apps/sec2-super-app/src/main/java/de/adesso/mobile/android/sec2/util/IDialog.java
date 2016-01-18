
package de.adesso.mobile.android.sec2.util;

/**
 * Interface for creating updating and dismissiing a dialog 
 * 
 * @author hoppe
 *
 */
public interface IDialog {

    /**
     * @param message message to be shown when starting the Dialog
     */
    public void start(final String message);

    /**
     * @param message message used to update the current dialog
     */
    public void update(final String message);

    /**
     *  
     */
    public void stop();

}
