package ch.unibe.scg.zeeguu.Settings;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public class LanguageItem  {

    private String language;
    private String languageKeys;

    public LanguageItem(String languageName, String languageKeys) {
        this.language = languageName;
        this.languageKeys = languageKeys;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public View getView(LayoutInflater inflater, View convertView) {
        ViewHolder holder;

        if (convertView != null && convertView.getTag().getClass() == ViewHolder.class)
        {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.language_item, null);

            holder = new ViewHolder();

            holder.language = (TextView) convertView.findViewById(R.id.item_language);
            holder.flag = (ImageView) convertView.findViewById(R.id.item_flag);

            convertView.setTag(holder);
        }

        holder.language.setText(language);
        ZeeguuFragment.setFlag(holder.flag, languageKeys);;

        return convertView;
    }


    static class ViewHolder {
        TextView language;
        ImageView flag;
    }

}

