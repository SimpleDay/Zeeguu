package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public class TranslatedWord implements Item {
    private String nativeWord;
    private String translation;

    private Locale nativeLanguage;
    private Locale translationLanguage;

    private String context;

    public TranslatedWord(String nativeWord, String translation) {
        this(nativeWord, translation, "", null, null);
    }

    public TranslatedWord(String nativeWord, String translation, String context) {
        this(nativeWord, translation, context, null, null);
    }

    public TranslatedWord(String nativeWord, String translation, String context, Locale nativeLanguage, Locale translationLanguage) {
        this.nativeWord = nativeWord;
        this.translation = translation;
        this.context = context;
        this.nativeLanguage = nativeLanguage;
        this.translationLanguage = translationLanguage;
    }


    public String getTranslation() {
        return translation;
    }

    public Locale getNativeLanguage() {
        return nativeLanguage;
    }

    public Locale getTranslationLanguage() {
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

    public void setNativeLanguage(Locale nativeLanguage) {
        this.nativeLanguage = nativeLanguage;
    }

    public void setTranslationLanguage(Locale translationLanguage) {
        this.translationLanguage = translationLanguage;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public int getViewType() {
        return WordlistAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_item, null);
            holder = new ViewHolder();

            holder.native_language = (TextView) convertView.findViewById(R.id.wordlist_native_language);
            holder.other_language = (TextView) convertView.findViewById(R.id.wordlist_other_language);
            holder.context = (TextView) convertView.findViewById(R.id.wordlist_context);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.native_language.setText(getNativeWord());
        holder.other_language.setText(getTranslation());
        holder.context.setText(getContext());

        return convertView;
    }


    static class ViewHolder {
        TextView native_language;
        TextView other_language;
        TextView context;
    }

}

