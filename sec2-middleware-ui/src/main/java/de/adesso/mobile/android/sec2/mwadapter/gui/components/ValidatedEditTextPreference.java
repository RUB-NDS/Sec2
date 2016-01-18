package de.adesso.mobile.android.sec2.mwadapter.gui.components;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * This class extends the EditTextPreference-class from Android. It extends
 * that class by the possibility, to specify a validator-object, that
 * implements IEditTextValidator-interface. When the preference dialog is now
 * closed, the validate()-method of the validator-object is called.
 *
 * @author nike
 */
public class ValidatedEditTextPreference extends EditTextPreference
{
    private IEditTextValidator validator = null;

    /**
     * This constructor is simply inherited from the super-class. It just calls
     * the super-constructor super(context, attrs, defStyle).
     *
     * @param context - The context
     * @param attrs - The attribute set
     * @param defStyle - The style
     */
    public ValidatedEditTextPreference(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * This constructor is simply inherited from the super-class. It just calls
     * the super-constructor super(context, attrs).
     *
     * @param context - The context
     * @param attrs - The attribute set
     */
    public ValidatedEditTextPreference(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * This constructor is simply inherited from the super-class. It just calls
     * the super-constructor super(context).
     *
     * @param context - The context
     */
    public ValidatedEditTextPreference(final Context context)
    {
        super(context);
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult)
    {
        EditText editText = null;

        if(positiveResult && validator != null)
        {
            editText = getEditText();
            if(!validator.validate(editText.getText().toString()))
            {
                editText.setError(validator.getErrorMsg());
                showDialog(null);
            }
            else super.onDialogClosed(positiveResult);
        }
        else super.onDialogClosed(positiveResult);
    }

    /**
     * This method sets the validator.
     * 
     * @param validator - The validator to be set
     */
    public void setValidator(final IEditTextValidator validator)
    {
        this.validator = validator;
    }
}
