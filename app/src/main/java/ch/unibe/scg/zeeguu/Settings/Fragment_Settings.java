package ch.unibe.scg.zeeguu.Settings;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //add change listener
        listener = new PreferenceListener();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(listener);

        //add Session ID to info box
        Preference session_id_preference = findPreference(this.getString(R.string.preference_user_session_id));
        String session_id = getSessionId();
        session_id_preference.setSummary(session_id);
    }

    private String getSessionId() {
        return ConnectionManager.getConnectionManager(this.getActivity()).getSessionId();
    }

    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("zeeguu_design"))
                getActivity().setResult(1); //used to refresh view
        }
    }
}


