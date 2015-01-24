package pascalgiehl_unibe.zeeguu.Wordlist_Fragments;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pascalgiehl_unibe.zeeguu.R;


public class WordlistAdapter extends BaseAdapter {
    // private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TranslatedWord> list;


    public WordlistAdapter(Context c, ArrayList<TranslatedWord> list) {
        mInflater = LayoutInflater.from(c);
        // mContext = c;
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.listview_item, null);
            holder = new ViewHolder();

            holder.native_language = (TextView) convertView.findViewById(R.id.wordlist_native_language);
            holder.other_language = (TextView) convertView.findViewById(R.id.wordlist_other_language);
            holder.context = (TextView) convertView.findViewById(R.id.wordlist_context);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.native_language.setText(list.get(position).getNativeWord());
        holder.other_language.setText(list.get(position).getTranslation());
        holder.context.setText(list.get(position).getContext());

        return convertView;
    }

    static class ViewHolder {
        TextView native_language;
        TextView other_language;
        TextView context;
    }
}