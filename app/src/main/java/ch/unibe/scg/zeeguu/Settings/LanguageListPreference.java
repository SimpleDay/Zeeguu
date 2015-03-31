package ch.unibe.scg.zeeguu.Settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * New Preference List extended from ListPreference that allows to add flags in front of the language names.
 * @author Pascal
 */
public class LanguageListPreference extends ListPreference {

    private IconListPreferenceAdapter iconListPreferenceAdapter = null;
    private LayoutInflater mInflater;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String mKey;
    private int selectedEntry = -1;

    public LanguageListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LanguageListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        mInflater = LayoutInflater.from(context);
        mKey = getKey();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

    }

    @Override
    public CharSequence getEntry() {
        if (selectedEntry != -1)
            return entries[selectedEntry];
        return super.getEntry().toString();
    }

    @Override
    public String getValue() {
        if (selectedEntry != -1)
            return entryValues[selectedEntry].toString();
        return super.getValue();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        entries = getEntries();
        entryValues = getEntryValues();

        if (entries.length != entryValues.length) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array which are both the same length");
        }


        iconListPreferenceAdapter = new IconListPreferenceAdapter();


        String selectedValue = prefs.getString(mKey, "");
        for (int i = 0; i < entryValues.length; i++) {
            if (selectedValue.compareTo((String) entryValues[i]) == 0) {
                selectedEntry = i;
                break;
            }
        }
        builder.setAdapter(iconListPreferenceAdapter, null);
    }


    /**
     * Adapter that supports the LanguageListPreference by adding images before the language names
     */
    private class IconListPreferenceAdapter extends BaseAdapter {

        class ViewHolder {
            private CheckedTextView language = null;
            private ImageView flag = null;
        }

        public int getCount() {
            return entries.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final int p = position;

            if (convertView != null && convertView.getTag().getClass() == ViewHolder.class) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.language_item, null);

                holder = new ViewHolder();

                holder.language = (CheckedTextView) convertView.findViewById(R.id.item_language);
                holder.flag = (ImageView) convertView.findViewById(R.id.item_flag);

                convertView.setTag(holder);
            }

            holder.language.setText(entries[position]);
            holder.language.setChecked(selectedEntry == position);
            holder.language.setId(position);
            holder.language.setClickable(false);
            holder.language.setChecked(selectedEntry == position);

            ZeeguuFragment.setFlag(holder.flag, (String) entryValues[position]);

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.requestFocus();

                    Dialog mDialog = getDialog();
                    mDialog.dismiss();

                    LanguageListPreference.this.callChangeListener(entryValues[p]);
                    editor.putString(mKey, entryValues[p].toString());
                    selectedEntry = p;
                    editor.commit();

                }
            });

            return convertView;
        }

    }

}