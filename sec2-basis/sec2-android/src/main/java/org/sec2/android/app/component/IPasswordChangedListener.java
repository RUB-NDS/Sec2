package org.sec2.android.app.component;

/**
 * This interface specifies a listener. It provides a method which is called,
 * if the password has been changed by the user.
 *
 * @author nike
 */
public interface IPasswordChangedListener
{
    /**
     * This method is called, if the password has be changed by the user.
     *
     * @param newPassword - The new password
     */
    public void onPasswordChanged(String newPassword);
}
