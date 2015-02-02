package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentWordlist extends ZeeguuFragment {
    private ArrayList<Item> list;
    private WordlistAdapter adapter;

    public FragmentWordlist() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wordlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //getWordlist from server and add it to adapter
        ConnectionManager connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) getActivity());
        list = connectionManager.getWordList();
        adapter = new WordlistAdapter(getActivity(), list);

        //create listview for wordlist and customize it
        ListView wordlist = (ListView) view.findViewById(R.id.wordlist_listview);
        wordlist.setAdapter(adapter);

        //Set text when listview empty
        TextView emptyText = (TextView) view.findViewById(R.id.wordlist_empty);
        wordlist.setEmptyView(emptyText);
    }

    @Override
    public void actualizeFragment() {
        closeKeyboard();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanguages() {
        //implement word filter
    }

}
