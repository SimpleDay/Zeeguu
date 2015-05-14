package ch.unibe.scg.zeeguuu.Core;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.lang.reflect.Method;

import ch.unibe.scg.zeeguuu.MyWords_Fragments.FragmentMyWords;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Search_Fragments.FragmentSearch;
import ch.unibe.scg.zeeguuu.Settings.FragmentSettings;
import ch.unibe.scg.zeeguuu.Settings.LanguageListPreference;
import ch.unibe.scg.zeeguuu.Sliding_menu.SlidingFragment;
import ch.unibe.scg.zeeguuu.Sliding_menu.ZeeguuFragmentPagerAdapter;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuDialogCallbacks;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLoginDialog;

public class ZeeguuActivity extends AppCompatActivity implements
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        FragmentSearch.ZeeguuFragmentTextCallbacks,
        FragmentMyWords.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuFragmentPagerAdapter.ZeeguuSlidingFragmentInterface,
        FragmentSettings.ZeeguuSettingsCallbacks,
        LanguageListPreference.ZeeguuLanguageListCallbacks,
        ZeeguuDialogCallbacks {

    private FragmentManager fragmentManager = getFragmentManager();
    private ZeeguuConnectionManager connectionManager;

    //fragments
    private static SlidingFragment fragmentSlidingMenu;
    private ZeeguuLoginDialog zeeguuLoginDialog;
    private DataFragment dataFragment;
    private FragmentSearch fragmentSearch;
    private FragmentMyWords fragmentMyWords;
    private FragmentSettings fragmentSettings;

    private boolean isInSettings = false;

    private Menu menu;
    private final int SETTINGSCHANGED = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(false);
        setContentView(R.layout.activity_zeeguu);

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Login Dialog
        zeeguuLoginDialog = (ZeeguuLoginDialog) fragmentManager.findFragmentByTag("zeeguuLoginDialog");
        if (zeeguuLoginDialog == null) zeeguuLoginDialog = new ZeeguuLoginDialog();

        // Data fragment so that the instance of the ConnectionManager is never destroyed
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fragmentManager.beginTransaction()
                    .add(dataFragment, "data")
                    .commit();
            connectionManager = new ZeeguuConnectionManager(this);
            dataFragment.setConnectionManager(connectionManager);
        }

        //create slidemenu
        fragmentSlidingMenu = (SlidingFragment) fragmentManager.findFragmentByTag("slidingMenu");
        if (fragmentSlidingMenu == null) {
            // FragmentText
            fragmentSearch = (FragmentSearch) fragmentManager.findFragmentByTag("fragmentText");
            if (fragmentSearch == null) fragmentSearch = new FragmentSearch();

            // FragmentMyWords
            fragmentMyWords = (FragmentMyWords) fragmentManager.findFragmentByTag("fragmentMyWords");
            if (fragmentMyWords == null) fragmentMyWords = new FragmentMyWords();

            fragmentSlidingMenu = new SlidingFragment();
        }
        switchActiveFragmentTo(fragmentSlidingMenu, "slidingMenu");

        // Settings fragment
        fragmentSettings = (FragmentSettings) fragmentManager.findFragmentByTag("fragmentSettings");
        if (fragmentSettings == null) fragmentSettings = new FragmentSettings();

        ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //TODO: Language change affects whole app
    }

    public void switchActiveFragmentTo(Fragment fragment, String title) {
        isInSettings = title.equals("fragmentSettings");
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu, fragment, title);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zeeguu, menu);
        this.menu = menu;

        showLoginButtonIfNotLoggedIn();
        return true;
    }

    // used to display the icons in the options menu //
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL && menu != null) {
            try {
                Method m = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (NoSuchMethodException e) {
                Log.e("logging", "onMenuOpened", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return super.onMenuOpened(featureId, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_in:
                showZeeguuLoginDialog(getString(R.string.login_title), "");
                return true;

            case R.id.action_settings:
                switchActiveFragmentTo(fragmentSettings, "fragmentSettings");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setTheme(boolean actualizeView) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = settings.getString(getString(R.string.preference_app_design), "").toString();
        switch (theme) {
            case "AppThemeLight":
                setTheme(R.style.AppThemeLight);
                break;
            case "AppThemeLightActionbarDark":
                setTheme(R.style.AppThemeLightActionbarDark);
                break;
            case "AppThemeDark":
                setTheme(R.style.AppThemeDark);
                break;
        }
        if (actualizeView)
            recreate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //fragmentSlidingMenu.getActiveFragment().onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {if (isInSettings)
            switchActiveFragmentTo(fragmentSlidingMenu, "slidingMenu");
        else
            super.onBackPressed();
    }

    //// ZeeguuLoginDialog interface ////

    public ZeeguuConnectionManager getConnectionManager() {
        return dataFragment.getConnectionManager();
    }

    //// ConnectionManager interface methods ////

    @Override
    public void showZeeguuLoginDialog(String title, String email) {
        if (title.equals(""))
            title = getString(R.string.login_title);
        zeeguuLoginDialog.setTitle(title);
        zeeguuLoginDialog.setEmail(email);
        zeeguuLoginDialog.show(fragmentManager, "zeeguuLoginDialog");
    }

    @Override
    public void showZeeguuCreateAccountDialog(String recallUsername, String recallEmail) {

    }

    @Override
    public void setTranslation(String translation) {
        fragmentSearch.setTranslatedText(translation);
    }

    @Override
    public void highlight(String word) {

    }

    //// ZeeguuAccount interface ////

    @Override
    public void notifyDataChanged() {
        fragmentMyWords.notifyDataSetChanged();
    }

    //// user interaction interface ////

    @Override
    public FragmentSearch getFragmentSearch() {
        return fragmentSearch;
    }

    @Override
    public FragmentMyWords getFragmentMyWords() {
        return fragmentMyWords;
    }

    @Override
    public void returnToMainActivity() {
        onBackPressed();
    }


    //// display messages interface ////

    @Override
    public void displayErrorMessage(String error, boolean isToast) {
        displayMessage(error);
    }

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT);
    }

    //// Settings interface ////

    @Override
    public void showLoginButtonIfNotLoggedIn() {
        if (connectionManager != null) {
            MenuItem item = menu.findItem(R.id.action_log_in);
            item.setVisible(!connectionManager.getAccount().isUserInSession());
        }
    }

    @Override
    public void notifyLanguageChanged(boolean isLanguageFrom) {
        fragmentSearch.refreshLanguages(isLanguageFrom);
    }

}
