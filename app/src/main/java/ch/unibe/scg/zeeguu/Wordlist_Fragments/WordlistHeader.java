package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class WordlistHeader {
    private final String name;
    private ArrayList<Item> children;
    private boolean groupOpen;

    public WordlistHeader(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        groupOpen = false;
    }

    public String getName() {
        return name;
    }

    public void addChild(Item item) {
        children.add(item);
    }

    public Item getChild(int position) {
        return children.get(position);
    }

    public int getChildrenSize() {
        return children.size();
    }

    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_header, null);
            holder = new ViewHolder();

            holder.header_title = (TextView) convertView.findViewById(R.id.txtHeader);
            holder.group_status = (ImageView) convertView.findViewById(R.id.ic_action_group_status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.header_title.setText(getName());

        if(groupOpen)
            holder.group_status.setImageResource(R.drawable.ic_action_close_list_holo_light);
        else
            holder.group_status.setImageResource(R.drawable.ic_action_open_list_holo_light);

        return convertView;
    }

    public View getChildView(int childPosition, LayoutInflater inflater, View convertView) {
        return children.get(childPosition).getView(inflater, convertView);
    }

    public void setGroupOpen(boolean groupOpen) {
        this.groupOpen = groupOpen;
    }

    static class ViewHolder {
        TextView header_title;
        ImageView group_status;
    }
}
