package ch.unibe.scg.zeeguu.Settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Settings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference session_id_preference = findPreference(this.getString(R.string.preference_user_session_id));
        String session_id = getSessionId();
        session_id_preference.setSummary(session_id);
    }

    private String getSessionId() {
        return ConnectionManager.getConnectionManager(this.getActivity()).getSessionId();
    }
}


