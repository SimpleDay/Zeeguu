package ch.unibe.scg.zeeguu.Wordlist_Fragments;

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
public class WordlistItem implements Item {

    private long id;
    private String nativeWord;
    private String fromLanguage;
    private String translationedWord;
    private String toLanguage;
    private String context;


    public WordlistItem(long id, String nativeWord, String translationedWord, String context, String fromLanguage, String toLanguage) {
        this.id = id;
        this.nativeWord = nativeWord;
        this.translationedWord = translationedWord;
        this.context = context;
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;
    }


    public String getTranslationedWord() {
        return translationedWord;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public String getToLanguage() {
        return toLanguage;
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

    public void setTranslationedWord(String translationedWord) {
        this.translationedWord = translationedWord;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public void setToLanguage(String toLanguage) {
        this.toLanguage = toLanguage;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        ViewHolder holder;

        if (convertView != null && convertView.getTag().getClass() == ViewHolder.class) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listview_item, null);

            holder = new ViewHolder();

            holder.native_language = (TextView) convertView.findViewById(R.id.wordlist_native_language);
            holder.learning_language = (TextView) convertView.findViewById(R.id.wordlist_learning_language);
            holder.context = (TextView) convertView.findViewById(R.id.wordlist_context);
            holder.flag_native = (ImageView) convertView.findViewById(R.id.flag_native);
            holder.flag_learning = (ImageView) convertView.findViewById(R.id.flag_learning);

            convertView.setTag(holder);
        }

        holder.native_language.setText(nativeWord);
        holder.learning_language.setText(translationedWord);

        //if context, write it into the textview, if not, don't show the textview
        if (!context.equals("")) {
            holder.context.setVisibility(View.VISIBLE);
            holder.context.setText(context);
        } else
            holder.context.setVisibility(View.GONE);

        if (fromLanguage != null)
            ZeeguuFragment.setFlag(holder.flag_native, fromLanguage);
        if (toLanguage != null)
            ZeeguuFragment.setFlag(holder.flag_learning, toLanguage);

        return convertView;
    }

    @Override
    public long getItemId() {
        return id;
    }

    public String isTranslation(String input, String fromLanguage, String toLanguage) {
        if (fromLanguage.equals(this.fromLanguage) && toLanguage.equals(this.toLanguage)) {
            if (input.equals(nativeWord))
                return translationedWord;
        } else if (fromLanguage.equals(this.toLanguage) && toLanguage.equals(this.fromLanguage)) {
            if (input.equals(translationedWord))
                return nativeWord;
        }
        return null;
    }

    //// View holder for the list elements so that they can be reused ////

    static class ViewHolder {
        TextView native_language;
        TextView learning_language;
        TextView context;

        ImageView flag_native;
        ImageView flag_learning;
    }

}

