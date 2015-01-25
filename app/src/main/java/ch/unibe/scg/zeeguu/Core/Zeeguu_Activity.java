package ch.unibe.scg.zeeguu.Core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Search_Fragments.Fragment_Text;
import ch.unibe.scg.zeeguu.Settings.SettingsActivity;
import ch.unibe.scg.zeeguu.Sliding_menu.SlidingFragment;

public class Zeeguu_Activity extends FragmentActivity {
    //TODO: horizontal mode (if phone is rotated by 90 degree, it starts some connections..)

    ConnectionManager connectionManager;
    SlidingFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zeeguu);

        //TODO: implement changable Design of app
        //SharedPreferences settings = getSharedPreferences("ch.unibe.scg.zeeguu_preferences", 0);
        //String theme = settings.getString("zeeguu_design", "").toString();

        //Customize Actionbar
        //getActionBar().setDisplayShowTitleEnabled(false); // hides action bar title

        //create slidemenu
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = new SlidingFragment();
        transaction.replace(R.id.fragment_menu, fragment);
        transaction.replace(R.id.viewpager, new Fragment_Text());
        transaction.commit();

        connectionManager = ConnectionManager.getConnectionManager(this);
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
            // action with ID action_refresh was selected
            case R.id.action_text:
                Toast.makeText(this, "Text selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            // action with ID action_settings was selected
            case R.id.action_voice:
                Toast.makeText(this, "Voice selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.action_camera:
                Toast.makeText(this, "Camera selected", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        connectionManager.cancelAllPendingRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment.getActiveFragment().onActivityResult(requestCode, resultCode, data);
    }

}
