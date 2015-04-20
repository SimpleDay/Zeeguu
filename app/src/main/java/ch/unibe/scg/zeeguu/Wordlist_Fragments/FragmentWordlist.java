package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
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
    SwipeRefreshLayout swipeLayout;

    private ImageView btnListviewExpandCollapse;
    private ImageView btnListviewRefresh;
    private boolean listviewExpanded;
    private boolean listviewRefreshing;

    private ActionMode mode;
    private View lastSelectedView;


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

        //open actionbar menu for deleting the items when longclick
        wordlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (id != 0) {
                    mode = getActivity().startActionMode(new ActionBarCallBack(id));
                    lastSelectedView = view;
                    view.setSelected(true);
                    return true;
                }
                return false;
            }
        });

        //when clicked somewhere else, close the longclick dialog
        wordlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                if (lastSelectedView != null && mode != null)
                    mode.finish();
                return true;
            }
        });

        //Set text when listview empty
        TextView emptyText = (TextView) view.findViewById(R.id.wordlist_empty);
        wordlist.setEmptyView(emptyText);

        btnListviewExpandCollapse = (ImageView) view.findViewById(R.id.listview_expand_collapse);
        btnListviewExpandCollapse.setOnClickListener(new ExpandAndCollapseListener());

        btnListviewRefresh = (ImageView) view.findViewById(R.id.listview_refresh);
        btnListviewRefresh.setOnClickListener(new RefreshListener());

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.wordlist_listview_swipe_refresh_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWordlist();
            }
        });
    }


    //Public functions

    @Override
    public void focusFragment() {
        adapter.notifyDataSetChanged();
        expandWordlist();
    }

    @Override
    public void defocusFragment() {
        if (mode != null) {
            mode.finish();
        }
    }

    @Override
    public void refreshLanguages(boolean switchFlagsIfNeeded) {
        //maybe integrate a filter here which only shows words of the selected language pair
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


    //Listener

    public class WordlistListener {

        public void startRefreshingAction() {
            if (btnListviewRefresh.getAnimation() == null) {
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

        public void stopRefreshingAction() {
            //stop rotation
            btnListviewRefresh.clearAnimation();
            listviewRefreshing = false;
            swipeLayout.setRefreshing(false);
        }

        public void notifyDataSetChanged() {
            adapter.notifyDataSetChanged();
            expandWordlist();
        }
    }

    //private classes

    private class ExpandAndCollapseListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(!connectionManager.loggedIn())
                toast(getString(R.string.error_user_not_logged_in_yet));
            else if (listviewExpanded)
                collapseWordlist();
            else
                expandWordlist();
        }
    }

    private class RefreshListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            refreshWordlist();
        }
    }

    private void refreshWordlist() {
        if(!connectionManager.loggedIn())
            toast(getString(R.string.error_user_not_logged_in_yet));
        else if (!listviewRefreshing) {
            listviewRefreshing = true;

            //start refreshing
            connectionManager.refreshWordlist();
        } else {
            toast(getString(R.string.error_refreshing_already_running));
        }
    }

    private class ActionBarCallBack implements ActionMode.Callback {
        private long id;

        public ActionBarCallBack(long id) {
            this.id = id;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:
                    logging(TAG, "");
                    connectionManager.deleteContribution(id);
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_wordlist, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (lastSelectedView != null) {
                lastSelectedView.setSelected(false);
                lastSelectedView = null;
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }
}