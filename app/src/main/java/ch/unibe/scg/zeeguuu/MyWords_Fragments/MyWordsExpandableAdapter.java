package ch.unibe.scg.zeeguuu.MyWords_Fragments;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;

import ch.unibe.zeeguulibrary.MyWords.Item;
import ch.unibe.zeeguulibrary.MyWords.MyWordsHeader;

/**
 * Zeeguu Application
 * Created by Pascal on 23/02/15.
 */


public class MyWordsExpandableAdapter extends BaseExpandableListAdapter {

    private final ArrayList<MyWordsHeader> headers;
    public LayoutInflater inflater;
    public Activity activity;

    public MyWordsExpandableAdapter(Activity act, ArrayList<MyWordsHeader> headers) {
        this.activity = act;
        this.headers = headers;
        this.inflater = act.getLayoutInflater();
    }

    @Override
    public Item getChild(int groupPosition, int childPosition) {
        return headers.get(groupPosition).getChild(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return getChild((int) groupId, (int) childId).getItemId();
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return headers.get(groupPosition).getChildView(childPosition, inflater, convertView);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return headers.get(groupPosition).getChildrenSize();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        headers.get(groupPosition).setGroupOpen(false);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        headers.get(groupPosition).setGroupOpen(true);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        return headers.get(groupPosition).getView(inflater, convertView);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}