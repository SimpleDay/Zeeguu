package ch.unibe.scg.zeeguu.Core;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Settings.SettingsActivity;
import ch.unibe.scg.zeeguu.Sliding_menu.SlidingFragment;

public class ZeeguuActivity extends FragmentActivity {
    private ConnectionManager connectionManager;
    private static SlidingFragment fragment;

    private final int SETTINGSCHANGED = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(false);
        setContentView(R.layout.activity_zeeguu);

        //set default settings when app started, but don't overwrite active settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        connectionManager = ConnectionManager.getConnectionManager(this);

        //Customize Actionbar
        //getActionBar().setDisplayShowTitleEnabled(false); // hides action bar title

        //create slidemenu
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(fragment == null)
            fragment = new SlidingFragment();
        transaction.replace(R.id.fragment_menu, fragment);
        transaction.commit();

        //TODO: Language change affects whole app
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zeeguu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingIntent, SETTINGSCHANGED);
                break;
            default:
                break;
        }

        return true;
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
        if(actualizeView)
            recreate();
    }

    public void refreshLanguages() {
        for(ZeeguuFragment f : fragment.getAllFragments())
            f.refreshLanguages();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SETTINGSCHANGED:
                if(resultCode == 1)
                    setTheme(true);
                break;
            default:
                fragment.getActiveFragment().onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    //protected android methods

    @Override
    protected void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Another activity is taking focus (this activity is about to be "paused").
        super.onPause();
    }

    @Override
    protected void onStop() {
        // The activity is no longer visible (it is now "stopped")
        super.onStop();
        connectionManager.cancelAllPendingRequests();
    }

    @Override
    protected void onDestroy() {
        // The activity is about to be destroyed.
        super.onDestroy();
    }
}
