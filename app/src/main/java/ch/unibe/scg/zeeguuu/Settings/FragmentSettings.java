package ch.unibe.scg.zeeguuu.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.ZeeguuAccount;

/**
 * Zeeguu Application
 * Created by Pascal on 12/01/15.
 */
public class FragmentSettings extends PreferenceFragment {
    private PreferenceListener listener;
    private SharedPreferences settings;
    private ZeeguuAccount account;
    private SettingsActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //initialize variables
        activity = (SettingsActivity) getActivity();

        //add change listener
        listener = new PreferenceListener();
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        settings.registerOnSharedPreferenceChangeListener(listener);

        //add Email and Session ID to info box when logged in or not show at all
        PreferenceCategory preference_loginInfo = (PreferenceCategory) findPreference(getString(R.string.category_user_information));
        Preference preference_email = findPreference(getString(R.string.preference_email));
        Preference preference_login_button = findPreference(getString(R.string.preference_login));
        Preference preference_logout_button = findPreference(getString(R.string.preference_logout));

        if (account.isUserLoggedIn()) {
            String email = account.getEmail();
            preference_email.setSummary(email);
            preference_email.setEnabled(false);

            preference_loginInfo.removePreference(preference_login_button);

            //Logout button
            preference_logout_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ZeeguuLogoutDialog zeeguuLogoutDialog = new ZeeguuLogoutDialog();
                    zeeguuLogoutDialog.show(getFragmentManager(), "logout?");
                    return true;
                }
            });
        } else {
            preference_loginInfo.removePreference(preference_email);
            preference_loginInfo.removePreference(preference_logout_button);

            //Login button
            preference_login_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.setResult(2);
                    activity.finish();
                    return true;
                }
            });
        }
    }

    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(activity.getString(R.string.preference_app_design)))
                activity.setResult(1); //used to refresh view
            else { /* Do nothing yet */ }
        }
    }
}


