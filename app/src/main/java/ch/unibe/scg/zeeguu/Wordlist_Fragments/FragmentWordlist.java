package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private ConnectionManager connectionManager;

    //flags
    private ImageView flag_translate_from;
    private ImageView flag_translate_to;

    //TODO: Show tags without context small

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
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) getActivity());
        list = connectionManager.getWordlist();
        adapter = new WordlistAdapter(getActivity(), list);

        //create listview for wordlist and customize it
        ListView wordlist = (ListView) view.findViewById(R.id.wordlist_listview);
        wordlist.setAdapter(adapter);

        //Set text when listview empty
        TextView emptyText = (TextView) view.findViewById(R.id.wordlist_empty);
        wordlist.setEmptyView(emptyText);

        //set language flags
        flag_translate_from = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flag_translate_to = (ImageView) view.findViewById(R.id.ic_flag_translate_to);

        updateFlags();
    }

    @Override
    public void actualizeFragment() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanguages() {
        updateFlags();
        //implement word filter
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
    }

    private void updateFlags() {
        setFlag(flag_translate_from, connectionManager.getNativeLanguage());
        setFlag(flag_translate_to, connectionManager.getLearningLanguage());
    }

}
