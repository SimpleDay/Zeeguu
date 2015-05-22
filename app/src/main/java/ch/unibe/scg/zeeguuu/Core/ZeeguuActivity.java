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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Method;

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
import ch.unibe.zeeguulibrary.MyWords.FragmentMyWords;

/**
 * Main activity that handles all fragments and the interaction between them
 * Works with interfaces to keep the fragments and classes as decoupled as possible
 */
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
    private DataFragment dataFragment;
    private FragmentSearch fragmentSearch;
    private FragmentMyWords fragmentMyWords;

    private FrameLayout slidingTabLayoutView;
    private FrameLayout preferenceView;

    //tags
    private String fragmentSlidingMenuTag = "fragmentSlidingMenu";
    private String fragmentPreferenceTag = "fragmentPreferenceTag";

    private ActionBar actionBar;
    private boolean isInSettings = false;

    private Menu menu;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreDataFragment();
        //setTheme(false);
        setContentView(R.layout.activity_zeeguu);

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        slidingTabLayoutView = (FrameLayout) findViewById(R.id.fragment_menu);
        preferenceView = (FrameLayout) findViewById(R.id.fragment_preferences);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        fragmentSlidingMenu = (SlidingFragment) fragmentManager.findFragmentByTag(fragmentSlidingMenuTag);
        if (fragmentSlidingMenu == null){
            fragmentSlidingMenu = new SlidingFragment();

            //add the fragments to the layout when added the first time
            transaction.replace(R.id.fragment_menu, fragmentSlidingMenu, fragmentSlidingMenuTag);
            transaction.replace(R.id.fragment_preferences, new FragmentPreference(), fragmentPreferenceTag);
            transaction.commit();
        }

        //starts the settings task when entered in settings before rotation
        if (savedInstanceState != null && savedInstanceState.getBoolean(fragmentPreferenceTag))
            switchMainFragmentTo(fragmentPreferenceTag);

        //Datafragment
        createDataFragment();

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
                switchMainFragmentTo(fragmentPreferenceTag);
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
            switchMainFragmentTo(fragmentSlidingMenuTag);
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
        zeeguuLogoutDialog.show(getFragmentManager(), "zeeguuLogoutDialog");
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
        //not used in this application
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
        fragmentSearch = dataFragment.getFragmentSearch();
        if (fragmentSearch == null){
            fragmentSearch = new FragmentSearch();
            dataFragment.setFragmentSearch(fragmentSearch);
        }
        return fragmentSearch;
    }

    @Override
    public FragmentMyWords getFragmentMyWords() {
        fragmentMyWords = dataFragment.getFragmentMyWords();
        if (fragmentMyWords == null) {
            fragmentMyWords = new FragmentMyWords();
            dataFragment.setFragmentMyWords(fragmentMyWords);
        }
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

    private void showLoginButtonIfNotLoggedIn() {
        MenuItem item = menu.findItem(R.id.action_log_in);
        if (item != null && connectionManager != null)
            item.setVisible(!connectionManager.getAccount().isUserInSession());

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

    private void createDataFragment() {
        // Data fragment so that the instance of the ConnectionManager is never destroyed
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            addFragment(dataFragment, "data");

            connectionManager = new ZeeguuConnectionManager(this);
            dataFragment.setConnectionManager(connectionManager);
        }
    }

    private void restoreDataFragment() {
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment != null) dataFragment.onRestore(this);
    }

    private void addFragment(Fragment fragment, String title) {
        fragmentManager.beginTransaction()
                .add(fragment, title)
                .commit();
    }

    private void switchMainFragmentTo(String fragmentTag) {
        isInSettings = fragmentTag.equals(fragmentPreferenceTag);
        actionBar.setTitle(isInSettings ? getString(R.string.preference_title)
                : getString(R.string.app_name_actionbar));
        actionBar.setDisplayHomeAsUpEnabled(isInSettings);
        actionBar.setDisplayUseLogoEnabled(!isInSettings);

        preferenceView.setVisibility(isInSettings ? View.VISIBLE : View.GONE);
        slidingTabLayoutView.setVisibility(isInSettings ? View.GONE : View.VISIBLE);

        if (isInSettings && menu != null)
            menu.clear();
        else
            invalidateOptionsMenu();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (connectionManager != null)
            connectionManager.getAccount().saveLanguages();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(fragmentPreferenceTag, isInSettings);
    }

    public static void setFlag(ImageView flag, String language) {
        switch (language) {
            case "en":
                flag.setImageResource(R.drawable.flag_uk);
                break;
            case "de":
                flag.setImageResource(R.drawable.flag_german);
                break;
            case "fr":
                flag.setImageResource(R.drawable.flag_france);
                break;
            case "it":
                flag.setImageResource(R.drawable.flag_italy);
                break;
        }
    }

}

