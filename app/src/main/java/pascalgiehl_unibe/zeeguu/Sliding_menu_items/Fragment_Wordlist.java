package pascalgiehl_unibe.zeeguu.Sliding_menu_items;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pascalgiehl_unibe.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Wordlist extends Fragment {

    public Fragment_Wordlist() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wordlist, container, false);
    }

}
