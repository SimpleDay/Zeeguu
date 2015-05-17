package ch.unibe.scg.zeeguuu.Core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.lang.reflect.Method;

import ch.unibe.scg.zeeguuu.MyWords_Fragments.FragmentMyWords;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Search_Fragments.FragmentSearch;
import ch.unibe.scg.zeeguuu.Settings.FragmentPreference;
import ch.unibe.scg.zeeguuu.Settings.LanguageListPreference;
import ch.unibe.scg.zeeguuu.Sliding_menu.SlidingFragment;
import ch.unibe.scg.zeeguuu.Sliding_menu.ZeeguuFragmentPagerAdapter;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuCreateAccountDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuDialogCallbacks;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLoginDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLogoutDialog;

public class ZeeguuActivity extends AppCompatActivity implements
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        FragmentSearch.ZeeguuFragmentTextCallbacks,
        FragmentMyWords.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuFragmentPagerAdapter.ZeeguuSlidingFragmentInterface,
        FragmentPreference.ZeeguuSettingsCallbacks,
        LanguageListPreference.ZeeguuLanguageListCallbacks,
        ZeeguuDialogCallbacks {

    private FragmentManager fragmentManager = getFragmentManager();
    private ZeeguuConnectionManager connectionManager;

    //fragments
    private SlidingFragment fragmentSlidingMenu;
    private boolean transactionActive = false;
    private DataFragment dataFragment;
    private FragmentSearch fragmentSearch;
    private FragmentMyWords fragmentMyWords;

    private ActionBar actionBar;
    private boolean isInSettings = false;

    private Menu menu;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(false);
        setContentView(R.layout.activity_zeeguu);

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //create toolbar and set it
        Toolbar toolbar = (Toolbar) findViewById(R.id.zeeguu_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);

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

        //TODO: Language change affects whole app
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
                showZeeguuLoginDialog("", "");
                return true;

            case R.id.action_settings:
                switchActiveFragmentTo(new FragmentPreference(), "fragmentSettings");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //fragmentSlidingMenu.getActiveFragment().onActivityResult(requestCode, resultCode, data);
    }


    //// INTERFACE METHODS ////

    @Override
    public void onBackPressed() {
        if (isInSettings)
            switchActiveFragmentTo(fragmentSlidingMenu, "slidingMenu");
        else
            super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //// ZeeguuLoginDialog interface ////

    @Override
    public ZeeguuConnectionManager getConnectionManager() {
        return dataFragment.getConnectionManager();
    }

    //// ConnectionManager interface methods ////

    @Override
    public void showZeeguuLoginDialog(String message, String email) {
        ZeeguuLoginDialog zeeguuLoginDialog = new ZeeguuLoginDialog();
        zeeguuLoginDialog.setEmail(email);
        zeeguuLoginDialog.setMessage(message);
        zeeguuLoginDialog.show(fragmentManager, "zeeguuLoginDialog");
    }


    @Override
    public void showZeeguuLogoutDialog() {
        ZeeguuLogoutDialog zeeguuLogoutDialog = new ZeeguuLogoutDialog();
        zeeguuLogoutDialog.show(getFragmentManager(), "logout?");
    }

    @Override
    public void showZeeguuCreateAccountDialog(String message, String username, String email) {
        ZeeguuCreateAccountDialog zeeguuCreateAccountDialog = new ZeeguuCreateAccountDialog();
        zeeguuCreateAccountDialog.setMessage(message);
        zeeguuCreateAccountDialog.setUsername(username);
        zeeguuCreateAccountDialog.setEmail(email);
        zeeguuCreateAccountDialog.show(fragmentManager, "zeeguuCreateAccountDialog");
    }

    @Override
    public void setTranslation(String translation) {
        fragmentSearch.setTranslatedText(translation);
    }

    @Override
    public void highlight(String word) {

    }

    @Override
    public void bookmarkWord(String bookmarkID) {
        try {
            fragmentSearch.setAsBookmarked(Long.parseLong(bookmarkID));
        } catch (Exception e) {
            displayMessage(getString(R.string.error_bookmark));
        }
    }

    //// ZeeguuAccount interface ////

    @Override
    public void notifyDataChanged(boolean myWordsChanged) {
        fragmentMyWords.notifyDataSetChanged(myWordsChanged);
        showLoginButtonIfNotLoggedIn(); //when account info changes, check if
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

    //// display messages interface ////

    @Override
    public void displayErrorMessage(String error, boolean isToast) {
        displayMessage(error);
    }

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //// Preference interface ////

    @Override
    public void notifyLanguageChanged(boolean isLanguageFrom) {
        fragmentSearch.refreshLanguages(isLanguageFrom);
    }

    //// Private Methods ////

    private void switchActiveFragmentTo(Fragment fragment, String title) {
        if (!transactionActive) {
            transactionActive = true;
            isInSettings = title.equals("fragmentSettings");
            actionBar.setDisplayHomeAsUpEnabled(isInSettings);
            actionBar.setDisplayUseLogoEnabled(!isInSettings);
            actionBar.setTitle(isInSettings ? getString(R.string.preference_title)
                    : getString(R.string.app_name_actionbar));

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_menu, fragment, title);
            transaction.commit();
            transactionActive = false;
        }
    }

    private void showLoginButtonIfNotLoggedIn() {
        if (connectionManager != null) {
            MenuItem item = menu.findItem(R.id.action_log_in);
            if (item != null)
                item.setVisible(!connectionManager.getAccount().isUserInSession());
        }
    }

    private void setTheme(boolean actualizeView) {
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
}
