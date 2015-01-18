package pascalgiehl_unibe.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //create listview for wordlist and customize it
        ListView resultList = (ListView) view.findViewById(R.id.wordlist_listview);

        ArrayList<String> list = new ArrayList<String>();
        list.add("hello"); list.add("hello2"); list.add("hello3");
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.listview_item, list);

        View header = (View) this.getActivity().getLayoutInflater().inflate(R.layout.listview_header, null);
        resultList.addHeaderView(header);
        resultList.setAdapter(listAdapter);
    }

}
