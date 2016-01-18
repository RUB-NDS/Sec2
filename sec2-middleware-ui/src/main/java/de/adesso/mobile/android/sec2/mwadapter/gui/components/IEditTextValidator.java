package de.adesso.mobile.android.sec2.mwadapter.gui.components;

/**
 * This interface specifies a validator for EditText-fields.
 * 
 * @author nike
 */
public interface IEditTextValidator
{
    /**
     * Validates the text
     * @param text - The text to be validated
     * @return True, if text is valid, otherwise false.
     */
    public boolean validate(String text);

    /**
     * Returns the error message.
     * @return The error message.
     */
    public String getErrorMsg();
}
