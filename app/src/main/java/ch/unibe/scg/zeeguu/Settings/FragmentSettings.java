package ch.unibe.scg.zeeguu.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 12/01/15.
 */
public class FragmentSettings extends PreferenceFragment {
    private PreferenceListener listener;
    private SharedPreferences settings;
    private ConnectionManager connectionManager;
    private SettingsActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //initialize variables
        activity = (SettingsActivity) getActivity();
        connectionManager = ConnectionManager.getConnectionManager(null); //null because we don't want to create a new one

        //add change listener
        listener = new PreferenceListener();
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        settings.registerOnSharedPreferenceChangeListener(listener);

        //add Email and Session ID to info box when logged in or not show at all
        PreferenceCategory preference_loginInfo = (PreferenceCategory) findPreference(getString(R.string.category_user_information));
        Preference preference_email = findPreference(getString(R.string.preference_email));
        Preference preference_login_button = findPreference(getString(R.string.preference_login));
        Preference preference_logout_button = findPreference(getString(R.string.preference_logout));

        if (connectionManager.loggedIn()) {
            String email = connectionManager.getEmail();
            preference_email.setSummary(email);
            preference_email.setEnabled(false);

            preference_loginInfo.removePreference(preference_login_button);

            //Logout button
            preference_logout_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    YesNoDialog dialog = new YesNoDialog(activity, connectionManager);
                    dialog.onClick();
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
                    activity.finish();
                    connectionManager.showLoginScreen();
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


