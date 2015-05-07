package ch.unibe.scg.zeeguu.MyWords_Fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentMyWords extends ZeeguuFragment {
    private ConnectionManager connectionManager;

    //Listview variables
    private MyWordsExpandableAdapter adapter;
    private ExpandableListView myWordsListView;
    SwipeRefreshLayout swipeLayout;

    private boolean listviewExpanded;
    private boolean listviewRefreshing;

    private ActionMode mode;
    private MenuItem menuItem;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mywords, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //getMyWords from server
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) getActivity());
        connectionManager.setMyWordsListener(new MyWordsListener());
        listviewRefreshing = false;

        //create listview for myWordsListView and customize it
        ArrayList<MyWordsHeader> list = connectionManager.getMyWords();
        adapter = new MyWordsExpandableAdapter(getActivity(), list);
        myWordsListView = (ExpandableListView) view.findViewById(R.id.mywords_listview);
        myWordsListView.setAdapter(adapter);

        //open actionbar menu for deleting the items when longclick
        myWordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (id != 0) {
                    mode = getActivity().startActionMode(new ActionBarCallBack(id, view));
                    return true;
                }
                return false;
            }
        });

        //when clicked somewhere else, close the longclick dialog
        myWordsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                if (mode != null)
                    mode.finish();
                return true;
            }
        });

        //Set text when listview empty
        TextView emptyText = (TextView) view.findViewById(R.id.mywords_empty);
        myWordsListView.setEmptyView(emptyText);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.mywords_listview_swipe_refresh_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMyWords();
            }
        });

        //activate the menu for fragments
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mywords, menu);
        super.onCreateOptionsMenu(menu, inflater);

        menuItem = menu.findItem(R.id.listview_expand_collapse);
        updateOptionMenuItemsIcons();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.listview_refresh:
                refreshMyWords();
                return true;

            case R.id.listview_expand_collapse:
                if (!connectionManager.loggedIn())
                    toast(getString(R.string.error_user_not_logged_in_yet));
                else if (listviewExpanded)
                    collapseMyWordsList();
                else
                    expandMyWordsList();
                return true;
        }
        return false;
    }

    //// Public functions ////

    @Override
    public void focusFragment() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void defocusFragment() {
        if (mode != null) {
            mode.finish();
        }
    }

    @Override
    public void refreshLanguages() {
        //maybe integrate a filter here which only shows words of the selected language pair
    }

    //// Listener ////

    public class MyWordsListener {

        public void notifyDataSetChanged() {
            adapter.notifyDataSetChanged();
            listviewRefreshing = false;
            listviewRefreshing = false;
            swipeLayout.setRefreshing(false);
            expandMyWordsList();
        }
    }

    //// private classes ////

    private void expandMyWordsList() {
        listviewExpanded = true;
        for (int i = 0; i < adapter.getGroupCount(); i++)
            myWordsListView.expandGroup(i);

        updateOptionMenuItemsIcons();
    }

    private void collapseMyWordsList() {
        listviewExpanded = false;
        for (int i = 0; i < adapter.getGroupCount(); i++)
            myWordsListView.collapseGroup(i);

        updateOptionMenuItemsIcons();
    }

    private void updateOptionMenuItemsIcons() {
        if (menuItem != null) {
            if (listviewExpanded)
                menuItem.setTitle(R.string.mywords_collapse)
                        .setIcon(R.drawable.ic_action_collapse_holo_light);
            else
                menuItem.setTitle(R.string.mywords_expand)
                        .setIcon(R.drawable.ic_action_expand_holo_light);
        }
    }

    private void refreshMyWords() {
        if (!connectionManager.loggedIn()) {
            toast(getString(R.string.error_user_not_logged_in_yet));
        } else if (!listviewRefreshing) {
            listviewRefreshing = true;
            connectionManager.refreshMyWords();
        } else {
            toast(getString(R.string.error_refreshing_already_running));
        }
    }

    private class ActionBarCallBack implements ActionMode.Callback {
        private long id;
        private View lastSelectedView;


        public ActionBarCallBack(long id, View view) {
            this.id = id;
            lastSelectedView = view;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:
                    logging("deleted bookmark with id: " + id);
                    connectionManager.removeBookmark(id);
                    mode.finish();
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            lastSelectedView.setSelected(true);
            mode.getMenuInflater().inflate(R.menu.menu_mywords_actionmode, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (lastSelectedView != null) {
                lastSelectedView.setSelected(false);
                lastSelectedView = null;
            }
            mode.finish();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }
}