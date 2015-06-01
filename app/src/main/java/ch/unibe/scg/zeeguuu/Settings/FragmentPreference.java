package ch.unibe.scg.zeeguuu.Settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Fragment that handles all preferences and settings that a user can change
 * Used a fragment to keep it as small and fast as possible
 */
public class FragmentPreference extends PreferenceFragment {
    private ZeeguuPreferenceCallbacks callback;
    private PreferenceChangeListener listener; //Keep it here, otherwise Garbage Collection deletes it

    //preferences
    private PreferenceCategory preference_loginInfo;
    private Preference preference_email;
    private Preference preference_logInOut_button;


    public interface ZeeguuPreferenceCallbacks {
        ZeeguuConnectionManager getConnectionManager();
        void showZeeguuLogoutDialog();
        void showZeeguuLoginDialog(String message, String email);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //add Email and Session ID to info box when logged in or not show at all
        preference_loginInfo = (PreferenceCategory) findPreference(
                getActivity().getString(R.string.preference_category_user_information_tag));
        preference_email = findPreference(getActivity().getString(R.string.preference_email_tag));
        preference_logInOut_button = findPreference(getActivity().getString(R.string.preference_logInOut_tag));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (ZeeguuPreferenceCallbacks) getActivity();

        //initialize default sharedPrefs and add change listener
        listener = new PreferenceChangeListener(getActivity());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    private void updateView() {
        if(preference_loginInfo != null && preference_email != null
                && preference_logInOut_button != null) {
            if (callback.getConnectionManager().getAccount().isUserInSession()) {
                String email = callback.getConnectionManager().getAccount().getEmail();
                preference_loginInfo.addPreference(preference_email);
                preference_email.setSummary(email);
                preference_email.setEnabled(false);

                preference_logInOut_button.setTitle(getActivity().getString(R.string.preference_logout));
                preference_logInOut_button.setSummary(getActivity().getString(R.string.preference_logout_message));
            } else {
                preference_loginInfo.removePreference(preference_email);
                preference_logInOut_button.setTitle(getActivity().getString(R.string.preference_login));
                preference_logInOut_button.setSummary(getActivity().getString(R.string.preference_login_message));
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuPreferenceCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SettingsCallbacks");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Open Zeeguu login dialog
        if (preference.getKey().equals(getActivity().getString(R.string.preference_logInOut_tag))) {
            if (callback.getConnectionManager().getAccount().isUserInSession())
                callback.showZeeguuLogoutDialog();
            else
                callback.showZeeguuLoginDialog("", "");
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Activity activity;

        public PreferenceChangeListener(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(activity.getString(R.string.preference_user_session_id_tag)))
                updateView();
            else if (key.equals("APP_DESIGN"));
                //TODO: implement interface for theme change
            else{ /* Do nothing yet */ }
        }
    }

}


