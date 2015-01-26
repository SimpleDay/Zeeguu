package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Wordlist extends ZeeguuFragment {

    public Fragment_Wordlist() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wordlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //getWordlist from server
        ConnectionManager connectionManager = ConnectionManager.getConnectionManager(this.getActivity());
        connectionManager.getAllWordsFromServer();
        ArrayList<Item> list = connectionManager.getWordList();

        //create listview for wordlist and customize it
        WordlistAdapter adapter = new WordlistAdapter(this.getActivity(), list);

        ListView resultList = (ListView) view.findViewById(R.id.wordlist_listview);
        resultList.setAdapter(adapter);
    }



    @Override
    public void actualizeFragment() {
        closeKeyboard();
    }

}
