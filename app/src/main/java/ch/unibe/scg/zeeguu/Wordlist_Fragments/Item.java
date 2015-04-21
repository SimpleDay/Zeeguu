package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public interface Item {
    public View getView(LayoutInflater inflater, View convertView);

    public long getItemId();
}
