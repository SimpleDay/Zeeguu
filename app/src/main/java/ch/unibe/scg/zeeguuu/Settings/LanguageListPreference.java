package ch.unibe.scg.zeeguuu.Settings;

import android.app.Activity;
import android.app.AlertDialog;
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

import ch.unibe.scg.zeeguuu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * New Preference List extended from ListPreference that allows to add flags in front of the language names.
 *
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

    //variables for showDialog
    private Dialog mDialog;
    private boolean isLanguageFrom;
    private ZeeguuLanguageListCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuLanguageListCallbacks {
        ZeeguuConnectionManager getConnectionManager();
        void notifyLanguageChanged(boolean isLanguageFrom);
    }


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

    public void showDialog(Activity activity, boolean isLanguageFrom) {
        entries = getEntries();
        entryValues = getEntryValues();
        mKey = isLanguageFrom ? activity.getString(R.string.preference_language_from) : activity.getString(R.string.preference_language_to);
        selectedEntry = updateSelectedEntry();

        if (iconListPreferenceAdapter == null)
            iconListPreferenceAdapter = new IconListPreferenceAdapter();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setAdapter(iconListPreferenceAdapter, null);
        builder.setTitle(isLanguageFrom ? activity.getString(R.string.language_from_dialog) :
                activity.getString(R.string.language_to_dialog));

        this.isLanguageFrom = isLanguageFrom;
        this.mDialog = builder.show();

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuLanguageListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuLanguageListCallbacks");
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        entries = getEntries();
        entryValues = getEntryValues();
        selectedEntry = updateSelectedEntry();

        if (entries.length != entryValues.length) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        if (iconListPreferenceAdapter == null)
            iconListPreferenceAdapter = new IconListPreferenceAdapter();
        builder.setAdapter(iconListPreferenceAdapter, null);

    }

    private int updateSelectedEntry() {
        String selectedValue = prefs.getString(mKey, "");
        for (int i = 0; i < entryValues.length; i++) {
            if (selectedValue.compareTo((String) entryValues[i]) == 0) {
                return i;
            }
        }
        return -1;
    }

    protected void closeDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (getDialog() != null)
            getDialog().dismiss();

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

                    if (mDialog == null) {
                        LanguageListPreference.this.callChangeListener(entryValues[p]);
                    } else {
                        if (isLanguageFrom)
                            callback.getConnectionManager().getAccount().setLanguageNative(entryValues[p].toString());
                        else
                            callback.getConnectionManager().getAccount().setLanguageLearning(entryValues[p].toString());
                        callback.notifyLanguageChanged(isLanguageFrom);
                    }

                    editor.putString(mKey, entryValues[p].toString());
                    selectedEntry = p;
                    editor.commit();

                    closeDialog();
                }
            });

            return convertView;
        }

    }

}