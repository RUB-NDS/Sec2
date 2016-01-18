package org.sec2.android.app.component;

import org.sec2.android.persistence.DbPersistenceManager;
import org.sec2.android.util.CryptoUtils;
import org.sec2.middleware.R;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * This class represents the edit field for setting the login password in the
 * preferences of the Sec2-middleware.
 *
 * @author schuessler
 */
public class LoginPasswordEditTextPreference extends EditTextPreference
{
    private static final Class<?> CLAZZ =
            LoginPasswordEditTextPreference.class;

    private String loginPassword = null;
    private IPasswordChangedListener listener = null;

    /**
     * Constructor inherited from superclass.
     *
     * @param context - @see EditTextPreference
     * @param attrs - @see EditTextPreference
     * @param defStyle - @see EditTextPreference
     */
    public LoginPasswordEditTextPreference(final Context context,
            final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * Constructor inherited from superclass.
     *
     * @param context - @see EditTextPreference
     * @param attrs - @see EditTextPreference
     */
    public LoginPasswordEditTextPreference(final Context context,
            final AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * Constructor inherited from superclass.
     *
     * @param context - @see EditTextPreference
     */
    public LoginPasswordEditTextPreference(final Context context)
    {
        super(context);
    }

    /**
     * Sets the password, which was used to login to the administration app.
     *
     * @param loginPassword - The password, which was used for login
     */
    public void setLoginPassword(final String loginPassword)
    {
        this.loginPassword = loginPassword;
    }

    /**
     * Sets the PasswordChangedListener.
     *
     * @param listener - The PasswordChangedListener
     */
    public void setPasswordChangedListener(
            final IPasswordChangedListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onAddEditTextToDialogView(final View dialogView,
            final EditText editText)
    {
        editText.setText("");
        super.onAddEditTextToDialogView(dialogView, editText);
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult)
    {
        EditText editText = null;
        String input = null;
        CryptoUtils cryptoUtils = null;

        if (positiveResult)
        {
            editText = getEditText();
            input = editText.getText().toString();
            if (input != null && !input.isEmpty())
            {
                //Encrypt DB with new password
                DbPersistenceManager.setDbPassword(getContext(), loginPassword,
                        input);
                //Try to save the hexadecimal encoded hash of the new password
                //in preferences
                cryptoUtils = new CryptoUtils();
                try
                {
                    editText.setText(cryptoUtils.encodeHex(
                            cryptoUtils.hashBytes(input.getBytes("UTF-8")),
                            true));
                    if (listener != null)
                    {
                        listener.onPasswordChanged(input);
                    }
                    loginPassword = input;
                }
                catch (final Exception e)
                {
                    LogHelper.logE(CLAZZ, e);
                    LogHelper.logE(CLAZZ, getContext().getString(
                            R.string.service_conf_pw_store_error));
                    //Set password to the old password
                    DbPersistenceManager.setDbPassword(getContext(), input,
                            loginPassword);
                }
                super.onDialogClosed(positiveResult);
            }
            else
            {
                editText.setError(getContext().getText(
                        R.string.service_conf_pw_empty_error));
                showDialog(null);
            }
        }
        else
        {
            super.onDialogClosed(positiveResult);
        }
    }
}
