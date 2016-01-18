package de.adesso.mobile.android.sec2.activity;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.mwadapter.gui.MwAdapterPreferenceActivity;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.view.LoginPasswordEditTextPreference;

public class Sec2PreferenceActivity extends MwAdapterPreferenceActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        PreferenceScreen preferenceScreen = null;
        PreferenceCategory preferenceCategory = null;
        LoginPasswordEditTextPreference loginField = null;

        super.onCreate(savedInstanceState);
        preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceCategory = new PreferenceCategory(this);
            preferenceCategory.setTitle(R.string.pref_cat_login);
            preferenceScreen.addPreference(preferenceCategory);
            loginField = new LoginPasswordEditTextPreference(this);
            loginField.setDialogTitle(R.string.pref_login_dialog);
            loginField.setKey(Constants.PREF_KEY_LOGIN);
            loginField.setTitle(R.string.pref_login_title);
            loginField.setSummary(R.string.pref_login_summary);
            loginField.getEditText().setHint(R.string.pref_login_hint);
            preferenceCategory.addPreference(loginField);
        }
    }
}
