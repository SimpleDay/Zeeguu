package ch.unibe.scg.zeeguuu.Settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLogoutDialog;

/**
 * Zeeguu Application
 * Created by Pascal on 12/01/15.
 */
public class FragmentSettings extends PreferenceFragment {
    private PreferenceListener listener;
    private SharedPreferences settings;

    private ZeeguuSettingsCallbacks callback;

    //preferences
    PreferenceCategory preference_loginInfo;
    Preference preference_email;
    Preference preference_login_button;
    Preference preference_logout_button;


    public interface ZeeguuSettingsCallbacks {
        void showZeeguuLoginDialog(String title, String tmpEmail);

        void showLoginButtonIfNotLoggedIn();

        void returnToMainActivity();

        ZeeguuConnectionManager getConnectionManager();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //initialize variables

        //add change listener
        listener = new PreferenceListener();
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings.registerOnSharedPreferenceChangeListener(listener);

        //add Email and Session ID to info box when logged in or not show at all
        preference_loginInfo = (PreferenceCategory) findPreference(getString(R.string.category_user_information));
        preference_email = findPreference(getString(R.string.preference_email));
        preference_login_button = findPreference(getString(R.string.preference_login));
        preference_logout_button = findPreference(getString(R.string.preference_logout));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
    }

    private void updateView() {
        if (callback.getConnectionManager().getAccount().isUserLoggedIn()) {
            String email = callback.getConnectionManager().getAccount().getEmail();
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
                    callback.showZeeguuLoginDialog("", "");
                    callback.returnToMainActivity();
                    return true;
                }
            });
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuSettingsCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SettingsCallbacks");
        }
    }


    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_zeeguu_username")) {
                updateView();
                callback.showLoginButtonIfNotLoggedIn();
            } else if (key.equals("APP_DESIGN")) {
                //TODO: implement interface for theme change
            } else { /* Do nothing yet */ }
        }
    }
}


