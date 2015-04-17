package ch.unibe.scg.zeeguu.Settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
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
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //initialize variables
        activity = getActivity();
        connectionManager = ConnectionManager.getConnectionManager(null); //null because we don't want to create a new one

        //add change listener
        listener = new PreferenceListener();
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        settings.registerOnSharedPreferenceChangeListener(listener);

        //add Email to info box
        Preference preference_email = findPreference(this.getString(R.string.preference_email));
        String email = connectionManager.getEmail();
        preference_email.setSummary(email);

        //add Session ID to info box
        Preference preference_session_id = findPreference(this.getString(R.string.preference_user_session_id));
        String session_id = connectionManager.getSessionId();
        preference_session_id.setSummary(session_id);
    }

    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(activity.getString(R.string.preference_app_design)))
                activity.setResult(1); //used to refresh view

            else if (key.equals(activity.getString(R.string.preference_learning_language)))
                connectionManager.setLearningLanguage(sharedPreferences.getString(
                        activity.getString(R.string.preference_learning_language), "en"), true);

            else if (key.equals(activity.getString(R.string.preference_native_language)))
                connectionManager.setNativeLanguage(sharedPreferences.getString(
                        activity.getString(R.string.preference_native_language), "en"), true);

            else { /* Do nothing yet */ }
        }
    }
}


