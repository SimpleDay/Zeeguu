package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class WordlistInfoHeader implements Item {
    private final String name;

    //TODO: Implement this header, which shows by what device it was searched, into the wordlist -> also new listview_info_header.xml and changes in color.xml and WordlistAdapter
    public WordlistInfoHeader(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getViewType() {
        return WordlistAdapter.RowType.HEADER_INFO_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_info_header, null);
            holder = new ViewHolder();

            holder.header_title = (TextView) convertView.findViewById(R.id.txtInfoHeader);

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
