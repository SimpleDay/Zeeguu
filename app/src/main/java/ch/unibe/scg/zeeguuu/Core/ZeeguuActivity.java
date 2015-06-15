package ch.unibe.scg.zeeguuu.Core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Method;

import ch.unibe.scg.zeeguuu.Exercises.ExerciseFragment;
import ch.unibe.scg.zeeguuu.Preference.LanguageListPreference;
import ch.unibe.scg.zeeguuu.Preference.ZeeguuPreferenceFragment;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Search_Fragments.SearchFragment;
import ch.unibe.scg.zeeguuu.Sliding_menu.SlidingTabFragment;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuCreateAccountDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuDialogCallbacks;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLoginDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLogoutDialog;
import ch.unibe.zeeguulibrary.MyWords.MyWordsFragment;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;
import ch.unibe.zeeguulibrary.WebView.BrowserFragment;
import ch.unibe.zeeguulibrary.WebView.ZeeguuTranslationActionMode;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewInterface;

/**
 * Main activity that handles all fragments and the interaction between them
 * Works with interfaces to keep the fragments and classes as decoupled as possible
 */
public class ZeeguuActivity extends AppCompatActivity implements
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        SearchFragment.ZeeguuFragmentTextCallbacks,
        MyWordsFragment.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuPreferenceFragment.ZeeguuPreferenceCallbacks,
        ExerciseFragment.ZeeguuFragmentExerciseCallback,
        LanguageListPreference.ZeeguuLanguageListCallbacks,
        SlidingTabFragment.SlidingFragmentCallback,
        ZeeguuDialogCallbacks,
        ZeeguuWebViewInterface.ZeeguuWebViewInterfaceCallbacks,
        ZeeguuWebViewFragment.ZeeguuWebViewCallbacks,
        BrowserFragment.BrowserCallbacks {

    public static int ITEMIDSEARCH = 100;
    public static final long ITEMIDBROWSER = 101;
    public static int ITEMIDMYWORDS = 102;
    public static int ITEMIDEXERCISES = 103;

    private FragmentManager fragmentManager = getFragmentManager();
    private ZeeguuConnectionManager connectionManager;

    //fragments
    private SlidingTabFragment slidingMenuFragment;
    private DataFragment dataFragment;
    private SearchFragment searchFragment;
    private BrowserFragment browserFragment;
    private MyWordsFragment myWordsFragment;
    private ExerciseFragment exerciseFragment;

    private FrameLayout slidingTabLayoutView;
    private FrameLayout preferenceView;

    private ZeeguuTranslationActionMode translationActionMode;

    //tags
    private String SlidingMenuTag = "slidingMenuFragment";
    private String PreferenceTag = "PreferenceTag";

    private ActionBar actionBar;
    private boolean isInSettings = false;

    private Menu menu;
    private ActionMode actionMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zeeguu);

        //setTheme(false);
        restoreDataFragment();

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        connectionManager = dataFragment.getConnectionManager();
        actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        slidingTabLayoutView = (FrameLayout) findViewById(R.id.fragment_slidingmenu);
        preferenceView = (FrameLayout) findViewById(R.id.fragment_preferences);

        searchFragment = (SearchFragment) fragmentManager.findFragmentByTag(getFragmentTag(ITEMIDSEARCH));
        if (searchFragment == null) searchFragment = new SearchFragment();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            browserFragment = (BrowserFragment) fragmentManager.findFragmentByTag(getFragmentTag(ITEMIDBROWSER));
            if (browserFragment == null) browserFragment = new BrowserFragment();
            translationActionMode = new ZeeguuTranslationActionMode(browserFragment);
        }

        myWordsFragment = (MyWordsFragment) fragmentManager.findFragmentByTag(getFragmentTag(ITEMIDMYWORDS));
        if (myWordsFragment == null) myWordsFragment = new MyWordsFragment();

        exerciseFragment = (ExerciseFragment) fragmentManager.findFragmentByTag(getFragmentTag(ITEMIDEXERCISES));
        if (exerciseFragment == null) exerciseFragment = new ExerciseFragment();

        slidingMenuFragment = (SlidingTabFragment) fragmentManager.findFragmentByTag(SlidingMenuTag);
        if (slidingMenuFragment == null) slidingMenuFragment = new SlidingTabFragment();

        //add the fragments to the layout when added the first time
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_slidingmenu, slidingMenuFragment, SlidingMenuTag);
        transaction.replace(R.id.fragment_preferences, new ZeeguuPreferenceFragment(), PreferenceTag);
        transaction.commit();

        //starts the settings task when entered in settings before rotation
        if (savedInstanceState != null && savedInstanceState.getBoolean(PreferenceTag))
            switchMainFragmentTo(PreferenceTag);

        //TODO: Language change affects whole app
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zeeguu, menu);
        this.menu = menu;

        updateLoginButton();
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
                switchMainFragmentTo(PreferenceTag);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Methods that support the browser fragment
    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        actionMode = mode;

        if (slidingMenuFragment.isBrowserActive()) {
            translationActionMode.onPrepareActionMode(mode, mode.getMenu());
            translationActionMode.onCreateActionMode(mode, mode.getMenu());
        }
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionMode = null;

        if (slidingMenuFragment.isBrowserActive())
            translationActionMode.onDestroyActionMode(mode);

        super.onSupportActionModeFinished(mode);
    }

    public void onActionItemClicked(MenuItem item) {
        if (actionMode != null && slidingMenuFragment.isBrowserActive())
            translationActionMode.onActionItemClicked(actionMode, item);
    }


    //// INTERFACE METHODS ////

    @Override
    public void onBackPressed() {
        if (isInSettings)
            switchMainFragmentTo(SlidingMenuTag);
        else if (slidingMenuFragment.isBrowserActive())
            browserFragment.goBack();
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
        if (browserFragment != null && slidingMenuFragment.isBrowserActive())
            browserFragment.setTranslation(translation);
        else
            searchFragment.setTranslatedText(translation);
    }

    @Override
    public void highlight(String word) {
        if (browserFragment != null &&
                connectionManager.getAccount().isHighlightOn())
            browserFragment.highlight(word);
    }

    @Override
    public void bookmarkWord(String bookmarkID) {
        try {
            searchFragment.setAsBookmarked(Long.parseLong(bookmarkID));
        } catch (Exception e) {
            displayMessage(getString(R.string.error_bookmark));
        }
    }

    //// ZeeguuAccount interface ////

    @Override
    public void notifyDataChanged(boolean myWordsChanged) {
        myWordsFragment.notifyDataSetChanged(myWordsChanged);
        exerciseFragment.reloginWebView();

        updateLoginButton(); //when account info changes, check if
    }

    //// user interaction interface ////

    @Override
    public SearchFragment getSearchFragment() {
        return searchFragment;
    }

    @Override
    public MyWordsFragment getMyWordsFragment() {
        return myWordsFragment;
    }

    @Override
    public ExerciseFragment getExerciseFragment() {
        return exerciseFragment;
    }

    @Override
    public BrowserFragment getBrowserFragment() {
        return browserFragment;
    }

    //// display messages interface ////

    @Override
    public void displayErrorMessage(String error, boolean isToast) {
        if (slidingMenuFragment.isBrowserActive() && !isToast)
            browserFragment.setTranslation(error);
        else
            displayMessage(error);
    }

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //// Preference interface ////

    @Override
    public void notifyLanguageChanged(boolean isLanguageFrom) {
        searchFragment.refreshLanguages(isLanguageFrom);
    }

    //// SlidingTabLayoutInterface ////
    @Override
    public void focusFragment(int number) {
        slidingMenuFragment.focusFragment(number);
    }

    //// Browser Methods ////

    @Override
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public ZeeguuWebViewFragment getWebViewFragment() {
        return browserFragment;
    }

    //// Private Methods ////

    private void updateLoginButton() {
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

    private void restoreDataFragment() {
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment != null)
            dataFragment.onRestore(this);
        else {
            dataFragment = new DataFragment();
            addFragment(dataFragment, "data");

            dataFragment.setConnectionManager(new ZeeguuConnectionManager(this));
        }

        // Data fragment so that the instance of the ConnectionManager is never destroyed

    }

    private void addFragment(Fragment fragment, String title) {
        fragmentManager.beginTransaction()
                .add(fragment, title)
                .commit();
    }

    private void switchMainFragmentTo(String fragmentTag) {
        isInSettings = fragmentTag.equals(PreferenceTag);
        actionBar.setTitle(isInSettings ? getString(R.string.preference_title)
                : getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(isInSettings);
        actionBar.setDisplayUseLogoEnabled(!isInSettings);

        if (isInSettings)
            slidingMenuFragment.onPause();

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
        outState.putBoolean(PreferenceTag, isInSettings);
    }

    public static void setFlag(ImageView flag, String language) {
        MyWordsItem.setFlag(flag, language);
    }

    private String getFragmentTag(long id) {
        return "android:switcher:" + SlidingTabFragment.getContainerID() + ":" + id;
    }
}

