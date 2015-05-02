package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public interface Item {
    // to create the ListView so that every item gets it's own view
    public View getView(LayoutInflater inflater, View convertView);

    // to identify the item and get it's identification
    public long getItemId();

    // to see if an item is a translation from a word we are searching
    WordlistItem isTranslation(String input, String inputLanguage, String outputLanguage);
}
