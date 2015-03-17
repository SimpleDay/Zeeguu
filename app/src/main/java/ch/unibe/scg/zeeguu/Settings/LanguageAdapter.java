package ch.unibe.scg.zeeguu.Settings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Zeeguu Application
 * Created by Pascal on 10/03/15.
 */
public class LanguageAdapter extends BaseAdapter{
    private Activity activity;
    private ArrayList<LanguageItem> list;
    private LayoutInflater inflater;

    public LanguageAdapter(Activity activity, ArrayList<LanguageItem> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public LanguageItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return getItem(position).getView(inflater, convertView);
    }

}
