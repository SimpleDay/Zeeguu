package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
    private ArrayList<WordlistHeader> list;
    private ConnectionManager connectionManager;

    //Listview
    private WordlistExpandableAdapter adapter;
    private ExpandableListView wordlist;
    private ImageView btnListviewExpandCollapse;
    private ImageView btnListviewRefresh;
    private boolean listviewExpanded;
    private boolean listviewRefreshing;

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
        //getWordlist from server
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) getActivity());
        connectionManager.setWordlistListener(new WordlistListener());
        list = connectionManager.getWordlist();
        listviewRefreshing = false;

        //create listview for wordlist and customize it
        adapter = new WordlistExpandableAdapter(getActivity(), list);
        wordlist = (ExpandableListView) view.findViewById(R.id.wordlist_listview);
        wordlist.setAdapter(adapter);

        //Set text when listview empty
        TextView emptyText = (TextView) view.findViewById(R.id.wordlist_empty);
        wordlist.setEmptyView(emptyText);

        btnListviewExpandCollapse = (ImageView) view.findViewById(R.id.listview_expand_collapse);
        btnListviewExpandCollapse.setOnClickListener(new ExpandAndCollapseListener());

        btnListviewRefresh = (ImageView) view.findViewById(R.id.listview_refresh);
        btnListviewRefresh.setOnClickListener(new RefreshListener());
    }


    //Public functions

    @Override
    public void actualizeFragment() {
        adapter.notifyDataSetChanged();
        expandWordlist();
    }

    @Override
    public void refreshLanguages() {
        //maybe integrate a filter here which only shows words of the selected language pair
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
    }

    //private functions

    private void expandWordlist() {
        for (int i = 0; i < adapter.getGroupCount(); i++)
            wordlist.expandGroup(i);

        listviewExpanded = true;
        btnListviewExpandCollapse.setImageResource(R.drawable.ic_action_collapse_holo_light);
    }

    private void collapseWordlist() {
        for (int i = 0; i < adapter.getGroupCount(); i++)
            wordlist.collapseGroup(i);

        listviewExpanded = false;
        btnListviewExpandCollapse.setImageResource(R.drawable.ic_action_expand_holo_light);
    }

    private void startRefreshAnimation() {

        if(btnListviewRefresh.getAnimation() == null) {
            //start animation
            final int startRotationDegree = 0;
            final int endRotationDegree = 360;
            final long miliSecsForOneRotation = 1000;

            RotateAnimation rotateAnimation = new RotateAnimation(startRotationDegree, endRotationDegree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setDuration(miliSecsForOneRotation);

            btnListviewRefresh.startAnimation(rotateAnimation);
        }
    }


    //Listener

    public class WordlistListener {
        public void stopRefreshingAction() {
            //stop rotation
            btnListviewRefresh.clearAnimation();
            listviewRefreshing = false;
        }

        public void startRefreshingAction() {
            startRefreshAnimation();
        }
    }

    //private classes

    private class ExpandAndCollapseListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(listviewExpanded)
                collapseWordlist();
            else
                expandWordlist();
        }
    }

    private class RefreshListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(!listviewRefreshing) {
                listviewRefreshing = true;

                //start refreshing
                connectionManager.refreshWordlist();
            } else {
                toast(getString(R.string.error_refreshing_already_running));
            }
        }
    }
}