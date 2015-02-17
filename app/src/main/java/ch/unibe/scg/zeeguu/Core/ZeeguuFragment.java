package ch.unibe.scg.zeeguu.Core;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public abstract class ZeeguuFragment extends Fragment {
    protected int RESULT_SPEECH = 1;
    protected boolean debugOn = true;
    protected String TAG = "tag_logging";

    public ZeeguuFragment() {
        super();
    }

    public abstract void actualizeFragment();
    public abstract void refreshLanguages();

    protected void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    protected void logging(String message) {
        logging(TAG, message);
    }

    protected void logging(String tag, String message) {
        if (debugOn)
            Log.d(tag, message);
    }


    public void onResume() { super.onResume(); }
}
