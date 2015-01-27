package ch.unibe.scg.zeeguu.Core;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public abstract class ZeeguuFragment extends Fragment {
    protected int RESULT_SPEECH = 1;

    public ZeeguuFragment() {
        super();
    }

    public abstract void actualizeFragment();

    //TODO: make this work
    /**
     * Force opens the soft keyboard
     */
    //TODO: DO this right - now its just toggling the window..
    public void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    /**
     * Force closes the soft keyboard
     */
    //TODO: DO this right - now its just toggling the window...
    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void onResume() { super.onResume(); }
}
