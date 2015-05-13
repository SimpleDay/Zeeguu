package ch.unibe.scg.zeeguuu.Core;

import android.app.ActionBar;
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

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.MyWords_Fragments.FragmentMyWords;
import ch.unibe.scg.zeeguuu.Search_Fragments.FragmentText;
import ch.unibe.scg.zeeguuu.Settings.SettingsActivity;
import ch.unibe.scg.zeeguuu.Sliding_menu.SlidingFragment;
import ch.unibe.scg.zeeguuu.Sliding_menu.ZeeguuFragmentPagerAdapter;
import ch.unibe.zeeguulibrary.ZeeguuAccount;
import ch.unibe.zeeguulibrary.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.ZeeguuCreateAccountDialog;
import ch.unibe.zeeguulibrary.ZeeguuLoginDialog;

public class ZeeguuActivity extends AppCompatActivity implements
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuLoginDialog.ZeeguuLoginDialogCallbacks,
        FragmentText.ZeeguuFragmentTextCallbacks,
        FragmentMyWords.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        ZeeguuCreateAccountDialog.ZeeguuCreateAccountDialogCallbacks,
        ZeeguuFragmentPagerAdapter.ZeeguuSlidingFragmentInterface {

    private FragmentManager fragmentManager = getFragmentManager();
    private ZeeguuConnectionManager connectionManager;

    //fragments
    private static SlidingFragment slidingFragment;
    private DataFragment dataFragment;
    private FragmentText fragmentText;
    private FragmentMyWords fragmentMyWords;
    private ZeeguuLoginDialog zeeguuLoginDialog;

    private Menu menu;
    private final int SETTINGSCHANGED = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(false);
        setContentView(R.layout.activity_zeeguu);

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Data fragment so that the instance of the ConnectionManager is never destroyed
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fragmentManager.beginTransaction()
                    .add(dataFragment, "data")
                    .commit();
            dataFragment.setConnectionManager(new ZeeguuConnectionManager(this));
        }

        // Login Dialog
        zeeguuLoginDialog = (ZeeguuLoginDialog) fragmentManager.findFragmentByTag("zeeguuLoginDialog");
        if (zeeguuLoginDialog == null) zeeguuLoginDialog = new ZeeguuLoginDialog();

        //create slidemenu
        slidingFragment = (SlidingFragment) fragmentManager.findFragmentByTag("slidingMenu");
        if (slidingFragment == null) {
            // FragmentText
            fragmentText = (FragmentText) fragmentManager.findFragmentByTag("fragmentText");
            if (fragmentText == null) fragmentText = new FragmentText();

            // FragmentMyWords
            fragmentMyWords = (FragmentMyWords) fragmentManager.findFragmentByTag("fragmentMyWords");
            if (fragmentMyWords == null) fragmentMyWords = new FragmentMyWords();

            slidingFragment = new SlidingFragment();
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu, slidingFragment, "slidingMenu");
        transaction.commit();

        ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

    public void showLoginButtonIfNotLoggedIn() {
        if(connectionManager != null) {
            MenuItem item = menu.findItem(R.id.action_log_in);
            item.setVisible(!connectionManager.getAccount().isUserInSession());
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_in:
                showZeeguuLoginDialog(getString(R.string.login_title), "");
                return true;

            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingIntent, SETTINGSCHANGED);
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
        switch (requestCode) {
            case SETTINGSCHANGED:
                if (resultCode == 1)
                    setTheme(true);
                if (resultCode == 2)
                    showZeeguuLoginDialog(getString(R.string.login_title), "");
                break;
            default:
                slidingFragment.getActiveFragment().onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    //// ZeeguuLoginDialog interface ////

    public ZeeguuConnectionManager getConnectionManager() {
        return dataFragment.getConnectionManager();
    }

    //// ConnectionManager interface methods ////

    @Override
    public void showZeeguuLoginDialog(String title, String tmpEmail) {
        if (title.equals(""))
            title = getString(R.string.login_title);
        zeeguuLoginDialog.setTitle(title);
        zeeguuLoginDialog.setEmail(tmpEmail);
        zeeguuLoginDialog.show(fragmentManager, "zeeguuLoginDialog");
    }

    @Override
    public void showZeeguuCreateAccountDialog(String recallUsername, String recallEmail) {

    }

    @Override
    public void setTranslation(String translation, boolean isErrorMessage) {

    }

    @Override
    public void highlight(String word) {

    }

    //// ZeeguuAccount interface ////

    @Override
    public void notifyDataChanged() {
        fragmentMyWords.notifyDataSetChanged();
        showLoginButtonIfNotLoggedIn();
    }

    //// user interaction interface ////

    @Override
    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void log(String text) {
        Log.d("ZeeguuActivity", text);
    }

    @Override
    public FragmentText getFragmentText() { return fragmentText; }

    @Override
    public FragmentMyWords getFragmentMyWords() { return fragmentMyWords; }

}
