package pascalgiehl_unibe.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public interface Item {
    public int getViewType();
    public View getView(LayoutInflater inflater, View convertView);
}
