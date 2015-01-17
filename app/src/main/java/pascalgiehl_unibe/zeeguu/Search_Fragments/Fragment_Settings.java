package pascalgiehl_unibe.zeeguu.Search_Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pascalgiehl_unibe.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Settings extends Fragment {

    public Fragment_Settings() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice, container, false);
    }

}
