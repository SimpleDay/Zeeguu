package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public class WordlistItem implements Item {
    private String nativeWord;
    private String translation;

    private String nativeLanguage;
    private String translationLanguage;
    private String context;


    public WordlistItem(String nativeWord, String translation, String context) {
        this(nativeWord, translation, context, "", "");
    }

    public WordlistItem(String nativeWord, String translation, String context, String nativeLanguage, String translationLanguage) {
        this.nativeWord = nativeWord;
        this.translation = translation;
        this.context = context;
        this.nativeLanguage = nativeLanguage;
        this.translationLanguage = translationLanguage;
    }


    public String getTranslation() {
        return translation;
    }

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public String getTranslationLanguage() {
        return translationLanguage;
    }

    public String getContext() {
        return context;
    }

    public String getNativeWord() {

        return nativeWord;
    }

    public void setNativeWord(String nativeWord) {
        this.nativeWord = nativeWord;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public void setNativeLanguage(String nativeLanguage) {
        this.nativeLanguage = nativeLanguage;
    }

    public void setTranslationLanguage(String translationLanguage) {
        this.translationLanguage = translationLanguage;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        ViewHolder holder;

        if (convertView != null && convertView.getTag().getClass() == ViewHolder.class)
        {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listview_item, null);

            holder = new ViewHolder();

            holder.native_language = (TextView) convertView.findViewById(R.id.wordlist_native_language);
            holder.other_language = (TextView) convertView.findViewById(R.id.wordlist_other_language);
            holder.context = (TextView) convertView.findViewById(R.id.wordlist_context);

            convertView.setTag(holder);
        }

        holder.native_language.setText(nativeWord);
        holder.other_language.setText(translation);

        //if context, write it into the textview, if not, don't show the textview
        if(!context.equals("")) {
            holder.context.setVisibility(View.VISIBLE);
            holder.context.setText(context);
        }
        else
            holder.context.setVisibility(View.GONE);

        return convertView;
    }


    static class ViewHolder {
        TextView native_language;
        TextView other_language;
        TextView context;
    }

}

