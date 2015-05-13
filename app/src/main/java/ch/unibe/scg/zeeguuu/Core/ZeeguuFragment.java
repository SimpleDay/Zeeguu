package ch.unibe.scg.zeeguuu.Core;

import android.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import ch.unibe.scg.zeeguuu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public abstract class ZeeguuFragment extends Fragment {
    protected int RESULT_SPEECH = 1;
    protected boolean debugOn = true;

    public ZeeguuFragment() {
        super();
    }

    //Functions that get called when a tab gets focused or swiped away
    public abstract void focusFragment();

    public abstract void defocusFragment();

    public abstract void refreshLanguages();

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

    protected void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    protected void logging(String message) {
        logging(getString(R.string.logging_tag), message);
    }

    protected void logging(String tag, String message) {
        if (debugOn)
            Log.d(tag, message);
    }


    public void onResume() {
        super.onResume();
    }
}
