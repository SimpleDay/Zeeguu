package ch.unibe.scg.zeeguu.Search_Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Settings.LanguageListPreference;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistItem;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentText extends ZeeguuFragment implements TextToSpeech.OnInitListener {
    private Activity activity;
    private EditText edit_text_native;
    private EditText edit_text_translated;
    private ConnectionManager connectionManager;
    private ClipboardManager clipboard;

    //TTS
    private TextToSpeech textToSpeechNativeLanguage;
    private TextToSpeech textToSpeechOtherLanguage;
    private TextToSpeech activeTextToSpeech;

    //flags
    private ImageView flag_translate_from;
    private ImageView flag_translate_to;

    private boolean switchLanguage;
    private String switchedText;
    private SharedPreferences settings;

    //buttons
    private ImageView btn_tts_native_language;
    private ImageView btn_tts_learning_language;

    private ImageView btn_copy;
    private ImageView btn_paste;

    private ImageView btn_contribute;
    private boolean contributed;


    public FragmentText() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //initialize class variables
        activity = getActivity();
        edit_text_native = (EditText) view.findViewById(R.id.text_native);
        edit_text_translated = (EditText) view.findViewById(R.id.text_translated);
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) activity);
        clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);

        //TTS
        textToSpeechNativeLanguage = new TextToSpeech(activity, this);
        textToSpeechOtherLanguage = new TextToSpeech(activity, this);
        activeTextToSpeech = null;

        //Set done button to translate
        edit_text_native.setOnKeyListener(new TranslationListenerKeyboard());

        //initialize flags
        flag_translate_from = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flag_translate_to = (ImageView) view.findViewById(R.id.ic_flag_translate_to);

        //remembers if languages was switched
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        switchLanguage = settings.getBoolean("switchLanguage", false);

        //listeners for the flags to switch the flags by pressing on them
        flag_translate_from.setOnClickListener(new LanguageSwitchListener());
        flag_translate_to.setOnClickListener(new LanguageSwitchListener());

        flag_translate_from.setOnLongClickListener(new LanguageChangeListener(true));
        flag_translate_to.setOnLongClickListener(new LanguageChangeListener(false));

        //if a text was entered and the screen rotated, the text will be added again here.
        if (savedInstanceState != null) {
            edit_text_native.setText(savedInstanceState.getString(
                    getString(R.string.preference_native_language)));
            edit_text_translated.setText(savedInstanceState.getString(
                    getString(R.string.preference_learning_language)));
        }

        //set listeners
        clipboard.addPrimaryClipChangedListener(new ClipboardChangeListener());

        ImageView btn_mic = (ImageView) view.findViewById(R.id.btn_microphone_search);
        btn_mic.setOnClickListener(new VoiceRecognitionListener());

        ImageView btn_cam = (ImageView) view.findViewById(R.id.btn_camera_search);
        btn_cam.setOnClickListener(new CameraRecognitionListener());

        TextView btn_transl = (TextView) view.findViewById(R.id.btn_translate);
        btn_transl.setOnClickListener(new TranslationListener());

        btn_contribute = (ImageView) view.findViewById(R.id.btn_contribute);
        btn_contribute.setOnClickListener(new ContributionListener());
        contributed = false;

        edit_text_native.addTextChangedListener(new EditTextNativeLanguageListener());
        edit_text_translated.addTextChangedListener(new EditTextLearningLanguageListener());

        //Clipboard button listeners
        btn_paste = (ImageView) view.findViewById(R.id.btn_paste);
        btn_paste.setOnClickListener(new PasteListener());

        btn_copy = (ImageView) view.findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(new CopyListener());


        //See if something has been added to clipboard and if activate paste button
        initButton(btn_paste, hasClipboardEntry());
        initButton(btn_copy, !edit_text_translated.getText().toString().isEmpty());


        //TTS
        btn_tts_native_language = (ImageView) view.findViewById(R.id.btn_tts_native_language);
        btn_tts_native_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechNativeLanguage, edit_text_native);
            }
        });
        initButton(btn_tts_native_language, !edit_text_native.getText().toString().equals(""));

        btn_tts_learning_language = (ImageView) view.findViewById(R.id.btn_tts_learning_language);
        btn_tts_learning_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechOtherLanguage, edit_text_translated);
            }
        });
        initButton(btn_tts_learning_language, !edit_text_translated.getText().toString().equals(""));

        //Open tutorial when first opened
        if (connectionManager.firstLogin()) {
            RelativeLayout tutorial = (RelativeLayout) view.findViewById(R.id.fragment_text_tutorial);
            tutorial.setVisibility(View.GONE); //TODO only for usability testing, otherwise visible
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: distinguish between image and sound recognition
        if (resultCode == Activity.RESULT_OK && null != data) {
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            edit_text_native.setText(text.get(0));
        }
    }

    @Override
    public void focusFragment() {
        //Method that gets called when this fragement is focused
    }

    @Override
    public void defocusFragment() {
        //Method that gets called when this fragment is not in focus anymore. Then we will close the keyboard
        closeKeyboard();
    }

    @Override
    public void refreshLanguages(boolean switchFlagsIfNeeded) {
        if (switchLanguage && switchFlagsIfNeeded) {
            flipTextFields();
            switchLanguage = false;
        }

        setLanguagesTextFields();
        resetTextFields();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            setLanguagesTextFields();
        } else {
            String error = getString(R.string.error_TTS_not_inititalized);
            toast(error);
            logging(error);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(getString(R.string.preference_native_language), edit_text_native.getText().toString());
        savedInstanceState.putString(getString(R.string.preference_learning_language), edit_text_translated.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void activateContribution() {
        contributed = true;
        btn_contribute.setImageResource(R.drawable.btn_bookmark_filled);

    }

    public void setTranslatedText(String text) {
        edit_text_translated.setText(text);
        initButton(btn_copy, !text.equals(""));
    }

    @Override
    public void onStop() {
        // The activity has become visible (it is now "resumed").
        super.onStop();

        //Save if langauge is switched
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("switchLanguage", switchLanguage);
        editor.apply();
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Shut down both TTS to prevent memory leaks
        if (textToSpeechNativeLanguage != null) {
            textToSpeechNativeLanguage.stop();
            textToSpeechNativeLanguage.shutdown();
        }

        if (textToSpeechOtherLanguage != null) {
            textToSpeechOtherLanguage.stop();
            textToSpeechOtherLanguage.shutdown();
        }
        super.onDestroy();
    }


    //private Methods

    private void setLanguagesTextFields() {
        setFlag(flag_translate_from, getInputLanguage());
        setTTS(textToSpeechNativeLanguage, getInputLanguage());

        setFlag(flag_translate_to, getOutputLanguage());
        setTTS(textToSpeechOtherLanguage, getOutputLanguage());
    }

    private void setTTS(TextToSpeech tts, String language) {
        int result = 0;
        switch (language) {
            case "en":
                result = tts.setLanguage(Locale.UK);
                break;
            case "de":
                result = tts.setLanguage(Locale.GERMAN);
                break;
            case "fr":
                result = tts.setLanguage(Locale.FRENCH);
                break;
            case "it":
                result = tts.setLanguage(Locale.ITALIAN);
                break;
        }

        //if textToSpeech library is not installed, make a toast
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            String error = getString(R.string.error_language_speaking_not_supported);
            //toast(error); //TODO: why always get the error?
            logging(error);
        }
    }

    private void speak(TextToSpeech tts, EditText edit_text) {

        String text = edit_text.getText().toString();
        if (text != null && !text.equals("")) {
            if (activeTextToSpeech != null) {
                activeTextToSpeech.stop();
                activeTextToSpeech = null;
            } else {
                activeTextToSpeech = tts;
                activeTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else
            toast(getString(R.string.error_no_text_to_read));
    }

    private void initButton(ImageView imageView, Boolean condition) {
        if (condition) {
            imageView.setEnabled(true);
            imageView.setAlpha(1f);
        } else {
            imageView.setEnabled(false);
            imageView.setAlpha(.3f);
        }
    }

    private boolean hasClipboardEntry() {
        if (clipboard == null) return false;
        return clipboard.hasPrimaryClip() && !clipboard.getPrimaryClip().getItemAt(0).getText().equals("");
    }

    private void translate() {
        String input = getEditTextTrimmed(edit_text_native);
        String wordlistSearch = checkWordlist(input);

        if (wordlistSearch == null)
            connectionManager.getTranslation(input, getInputLanguage(), getOutputLanguage(), this);
        else {
            setTranslatedText(wordlistSearch);
            activateContribution();
        }
        closeKeyboard();
    }

    private String checkWordlist(String input) {
        ArrayList<WordlistItem> wordlist = connectionManager.getWordlistItems();
        //TODO: make it nice as soon as the database is integrated
        for (WordlistItem i : wordlist) {
            String result = i.isTranslation(input, getInputLanguage(), getOutputLanguage());
            if (result != null)
                return result;
        }
        return null;
    }

    private String getEditTextTrimmed(EditText editText) {
        return editText.getText().toString().replaceAll("[ ]+", " ").trim();
    }


    private void contribute() {
        if (!connectionManager.loggedIn())
            toast(getString(R.string.error_user_not_logged_in_yet));
        else if (edit_text_native.getText().length() != 0 && edit_text_translated.getText().length() != 0) {
            if (!contributed) {
                String input = getEditTextTrimmed(edit_text_native);
                String translation = getEditTextTrimmed(edit_text_translated);

                connectionManager.contributeToServer(input, getInputLanguage(), translation, getOutputLanguage(), this);
            } else {
                toast(getString(R.string.error_contributed_already)); //TODO: press it again when filled deletes contribution
            }
        } else {
            toast(getString(R.string.error_no_text_to_contribute));
        }
    }

    private String getInputLanguage() {
        return switchLanguage ? connectionManager.getLearningLanguage() : connectionManager.getNativeLanguage();
    }

    private String getOutputLanguage() {
        return switchLanguage ? connectionManager.getNativeLanguage() : connectionManager.getLearningLanguage();
    }

    private void flipTextFields() {
        switchLanguage = !switchLanguage;
        setLanguagesTextFields();

        String tmpText = getEditTextTrimmed(edit_text_native);
        String textLearning = getEditTextTrimmed(edit_text_translated);
        if (textLearning.equals(""))
            edit_text_native.setText(switchedText);
        else
            edit_text_native.setText(textLearning);

        switchedText = tmpText;
        edit_text_translated.setText("");
        initButton(btn_copy, false);
    }

    private void resetTextFields() {
        edit_text_native.setText("");
        edit_text_translated.setText("");
        initButton(btn_copy, false);
    }

    private void closeKeyboard() {
        if (activity != null) {
            InputMethodManager iMM = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            iMM.hideSoftInputFromWindow(edit_text_native.getWindowToken(), 0);
        }
    }


    ///// Listeners /////

    public class FragmentTextListener {

        public void updateLanguage(String languageCode, boolean nativeLanguage) {
            if (nativeLanguage)
                connectionManager.setNativeLanguage(languageCode, false);
            else
                connectionManager.setLearningLanguage(languageCode, false);
        }
    }

    private class TranslationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            translate();
        }
    }

    private class TranslationListenerKeyboard implements View.OnKeyListener {
        //Keyboard listener so that the enter button also will translate the text
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                translate();
                return true;
            }
            return false;
        }
    }

    private class LanguageSwitchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            flipTextFields();
        }
    }

    private class LanguageChangeListener implements View.OnLongClickListener {
        private boolean nativeLanguage;

        public LanguageChangeListener(boolean nativeLanguage) {
            this.nativeLanguage = nativeLanguage;
        }

        @Override
        public boolean onLongClick(View v) {
            LanguageListPreference listPreference = new LanguageListPreference(activity, null);

            Resources res = activity.getResources();
            listPreference.setEntries(res.getStringArray(R.array.languages));
            listPreference.setEntryValues(res.getStringArray(R.array.language_keys));

            listPreference.showDialog(activity, switchLanguage ? !nativeLanguage : nativeLanguage, new FragmentTextListener());
            return true;
        }
    }

    private class VoiceRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getInputLanguage());

            if (activeTextToSpeech != null) {
                activeTextToSpeech.stop();
                activeTextToSpeech = null;
            }

            try {
                startActivityForResult(intent, RESULT_SPEECH);
            } catch (ActivityNotFoundException a) {
                toast(getString(R.string.error_mic_search_not_supported));
            }
        }
    }

    private class CameraRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //ToDo: implement OCR
        }
    }

    private class ContributionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            contribute();
        }
    }

    private class EditTextNativeLanguageListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            initButton(btn_tts_native_language, !edit_text_native.getText().toString().equals(""));
        }
    }

    private class EditTextLearningLanguageListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //also do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            //when learning textfield changes, it can be contributed again.
            contributed = false;
            btn_contribute.setImageResource(R.drawable.btn_bookmark);

            boolean editTextIsEmpty = edit_text_translated.getText().toString().equals("");
            initButton(btn_copy, !editTextIsEmpty);
            initButton(btn_tts_learning_language, !editTextIsEmpty);
        }
    }


    ///// Listeners for copy and paste functions /////

    private class PasteListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            edit_text_native.getText().insert(edit_text_native.getSelectionStart(), item.getText());
        }
    }

    private class CopyListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ClipData clip = ClipData.newPlainText("paste", edit_text_translated.getText().toString());
            clipboard.setPrimaryClip(clip);
            //Feedback that text has been copied
            toast(getString(R.string.successful_text_copied));
        }
    }

    private class ClipboardChangeListener implements ClipboardManager.OnPrimaryClipChangedListener {
        @Override
        public void onPrimaryClipChanged() {
            //initialize paste button because something is added to clipboard
            initButton(btn_paste, true);
        }
    }

}


