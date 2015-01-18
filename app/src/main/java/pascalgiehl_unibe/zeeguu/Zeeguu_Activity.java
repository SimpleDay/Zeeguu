package pascalgiehl_unibe.zeeguu;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pascalgiehl_unibe.zeeguu.Search_Fragments.Fragment_Text;
import pascalgiehl_unibe.zeeguu.Sliding_menu.SlidingFragment;

public class Zeeguu_Activity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zeeguu);

        //Customize Actionbar
        //getActionBar().setDisplayShowTitleEnabled(false); // hides action bar title

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SlidingFragment fragment = new SlidingFragment();
        transaction.replace(R.id.fragment_menu, fragment);
        transaction.replace(R.id.viewpager, new Fragment_Text());
        transaction.commit();
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
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }


}
