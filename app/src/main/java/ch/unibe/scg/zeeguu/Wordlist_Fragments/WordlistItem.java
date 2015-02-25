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
    private String nativeWord;
    private String translation;

    private String nativeLanguage;
    private String learningLanguage;
    private String context;


    public WordlistItem(String nativeWord, String translation, String context) {
        this(nativeWord, translation, context, "", "");
    }

    public WordlistItem(String nativeWord, String translation, String context, String nativeLanguage, String learningLanguage) {
        this.nativeWord = nativeWord;
        this.translation = translation;
        this.context = context;
        this.nativeLanguage = nativeLanguage;
        this.learningLanguage = learningLanguage;
    }


    public String getTranslation() {
        return translation;
    }

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public String getLearningLanguage() {
        return learningLanguage;
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

    public void setLearningLanguage(String learningLanguage) {
        this.learningLanguage = learningLanguage;
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
            holder.learning_language = (TextView) convertView.findViewById(R.id.wordlist_learning_language);
            holder.context = (TextView) convertView.findViewById(R.id.wordlist_context);
            holder.flag_native = (ImageView) convertView.findViewById(R.id.flag_native);
            holder.flag_learning = (ImageView) convertView.findViewById(R.id.flag_learning);

            convertView.setTag(holder);
        }

        holder.native_language.setText(nativeWord);
        holder.learning_language.setText(translation);

        //if context, write it into the textview, if not, don't show the textview
        if(!context.equals("")) {
            holder.context.setVisibility(View.VISIBLE);
            holder.context.setText(context);
        }
        else
            holder.context.setVisibility(View.GONE);

        if(nativeLanguage != null)
            ZeeguuFragment.setFlag(holder.flag_native, nativeLanguage);
        if(learningLanguage != null)
            ZeeguuFragment.setFlag(holder.flag_learning, learningLanguage);

        return convertView;
    }


    static class ViewHolder {
        TextView native_language;
        TextView learning_language;
        TextView context;

        ImageView flag_native;
        ImageView flag_learning;
    }

}

