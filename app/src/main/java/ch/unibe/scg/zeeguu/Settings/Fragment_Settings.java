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
public class Fragment_Settings extends PreferenceFragment {
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
        connectionManager = ConnectionManager.getConnectionManager(activity);

        //add change listener
        listener = new PreferenceListener();
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        settings.registerOnSharedPreferenceChangeListener(listener);

        //add Session ID to info box
        Preference session_id_preference = findPreference(this.getString(R.string.preference_user_session_id));
        String session_id = connectionManager.getSessionId();
        session_id_preference.setSummary(session_id);
    }

    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(activity.getString(R.string.preference_app_design)))
                activity.setResult(1); //used to refresh view

            else if (key.equals(activity.getString(R.string.preference_learning_language)))
                connectionManager.setLearningLanguage(sharedPreferences.getString(
                        activity.getString(R.string.preference_learning_language), "en"));

            else if (key.equals(activity.getString(R.string.preference_native_language)))
                connectionManager.setNativeLanguage(sharedPreferences.getString(
                        activity.getString(R.string.preference_native_language), "en"));

            else
                connectionManager.updateUserInformation();
        }
    }
}


