package ch.unibe.scg.zeeguu.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Zeeguu Application
 * Created by Pascal on 10/03/15.
 */
public class LanguageListPreference extends ListPreference{
    Activity activity;

    public LanguageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        LanguageAdapter languageListAdapter = new LanguageAdapter(activity, entries());

        if (languageListAdapter == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array "
                            +"and an entryValues array which are both the same length");
        }

        builder.setAdapter(languageListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private ArrayList<LanguageItem> entries() {
        CharSequence languages[] = getEntries();
        CharSequence languageKeys[] = getEntryValues();

        ArrayList<LanguageItem> list = new ArrayList<>();

        for(int i = 0; i < languages.length || i < languageKeys.length; i++)
            list.add(new LanguageItem((String) languages[i], (String) languageKeys[i]));

        return list;
    }

}
