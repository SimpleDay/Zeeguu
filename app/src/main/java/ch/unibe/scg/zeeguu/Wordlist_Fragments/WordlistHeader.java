package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class WordlistHeader implements Item {
    private final String name;

    public WordlistHeader(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getViewType() {
        return WordlistAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_header, null);
            holder = new ViewHolder();

            holder.header_title = (TextView) convertView.findViewById(R.id.txtHeader);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.header_title.setText(getName());

        return convertView;
    }

    static class ViewHolder {
        TextView header_title;
    }

}
