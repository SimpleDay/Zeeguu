package ch.unibe.scg.zeeguuu.Search_Fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.unibe.scg.zeeguuu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentCamera extends Fragment {

    public FragmentCamera() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice, container, false);
    }

}
