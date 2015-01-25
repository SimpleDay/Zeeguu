package ch.unibe.scg.zeeguu.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

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
    }
}


