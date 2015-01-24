package pascalgiehl_unibe.zeeguu.Wordlist_Fragments;

import java.util.Locale;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public class TranslatedWord {
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

}
