package ch.unibe.scg.zeeguu.Search_Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentText extends ZeeguuFragment implements TextToSpeech.OnInitListener {
    private EditText edit_text_native;
    private EditText edit_text_translated;
    private Activity activity;
    private ConnectionManager connectionManager;
    private ClipboardManager clipboard;

    private TextToSpeech textToSpeechNativeLanguage;
    private TextToSpeech textToSpeechOtherLanguage;

    //flags
    private ImageView flag_translate_from;
    private ImageView flag_translate_to;
    private boolean switchLanguage;

    //buttons
    ImageView btn_tts_native_language;
    ImageView btn_tts_learning_language;

    private ImageView btn_copy;
    private ImageView btn_paste;


    public FragmentText() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //initialize class variables
        edit_text_native = (EditText) view.findViewById(R.id.text_native);
        edit_text_translated = (EditText) view.findViewById(R.id.text_translated);
        activity = getActivity();
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) activity);
        clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);

        //TTS
        textToSpeechNativeLanguage = new TextToSpeech(activity, this);
        textToSpeechOtherLanguage = new TextToSpeech(activity, this);

        //Set done button to translate
        edit_text_native.setOnKeyListener(new TranslationListenerKeyboard());

        //initialize flags
        flag_translate_from = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flag_translate_to = (ImageView) view.findViewById(R.id.ic_flag_translate_to);
        switchLanguage = false;

        //listeners for the flags to switch the flags by pressing on them
        flag_translate_from.setOnClickListener(new LanguageSwitchListener());
        flag_translate_to.setOnClickListener(new LanguageSwitchListener());

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

        ImageButton btn_transl = (ImageButton) view.findViewById(R.id.btn_translate);
        btn_transl.setOnClickListener(new TranslationListener());

        ImageButton btn_contribute = (ImageButton) view.findViewById(R.id.btn_contribute);
        btn_contribute.setOnClickListener(new ContributionListener());

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
                speak(textToSpeechNativeLanguage);
            }
        });

        btn_tts_learning_language = (ImageView) view.findViewById(R.id.btn_bookmark);
        btn_tts_learning_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechOtherLanguage);
            }
        });

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
    public void actualizeFragment() {
    }

    @Override
    public void refreshLanguages() {
        switchLanguage = false;
        setLanguagesTextFields();
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();

        if (flag_translate_from != null || flag_translate_to != null || connectionManager != null)
            setLanguagesTextFields();
    }

    public void onDestroy() {
        // Don't forget to shutdown!
        if (textToSpeechNativeLanguage != null) {
            textToSpeechNativeLanguage.stop();
            textToSpeechNativeLanguage.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

        } else {
            String error = getString(R.string.error_TTS_not_inititalized);
            toast(error);
            logging(error);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //TODO: NOT YET WORKING
        savedInstanceState.putString(getString(R.string.preference_native_language), edit_text_native.getText().toString());
        savedInstanceState.putString(getString(R.string.preference_learning_language), edit_text_translated.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void activateButtons() {
        initButton(btn_copy, !edit_text_translated.getText().toString().isEmpty());
        //TODO: Add read to button functionability
    }

    public void setTranslatedText(String text) {
        edit_text_translated.setText(text);
    }


    //private Methods

    private void setLanguagesTextFields() {
        if (!switchLanguage) {
            setFlag(flag_translate_from, textToSpeechNativeLanguage, connectionManager.getNativeLanguage());
            setFlag(flag_translate_to, textToSpeechOtherLanguage, connectionManager.getLearningLanguage());
        } else {
            setFlag(flag_translate_to, textToSpeechOtherLanguage, connectionManager.getNativeLanguage());
            setFlag(flag_translate_from, textToSpeechNativeLanguage, connectionManager.getLearningLanguage());
        }
    }

    private void setFlag(ImageView flag, TextToSpeech tts, String language) {
        int result = 0;
        switch (language) {
            case "en":
                flag.setImageResource(R.drawable.flag_uk);
                result = tts.setLanguage(Locale.UK);
                break;
            case "de":
                flag.setImageResource(R.drawable.flag_german);
                result = tts.setLanguage(Locale.GERMANY);
                break;
            case "fr":
                flag.setImageResource(R.drawable.flag_france);
                result = tts.setLanguage(Locale.FRANCE);
                break;
            case "it":
                flag.setImageResource(R.drawable.flag_italy);
                result = tts.setLanguage(Locale.ITALY);
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

    private void speak(TextToSpeech tts) {
        String text;
        if (tts == textToSpeechNativeLanguage)
            text = edit_text_native.getText().toString();
        else
            text = edit_text_translated.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void initButton(ImageView imageView, Boolean condition) {
        if(condition) {
            imageView.setEnabled(true);
            imageView.setAlpha(1f);
        } else {
            imageView.setEnabled(false);
            imageView.setAlpha(.3f);
        }
    }

    private boolean hasClipboardEntry() {
        if(clipboard == null) return false;
        return clipboard.hasPrimaryClip() && !clipboard.getPrimaryClip().getItemAt(0).getText().equals("");
    }

    private void translate() {
        String input = edit_text_native.getText().toString();
        connectionManager.getTranslation(input, switchLanguage, this);
    }


    //Listeners

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
                //TODO: close the keyboard here
                return true;
            }
            return false;
        }
    }

    private class LanguageSwitchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switchLanguage = !switchLanguage;
            setLanguagesTextFields();

            String native_entered = edit_text_native.getText().toString();
            edit_text_native.setText(edit_text_translated.getText().toString());
            edit_text_translated.setText(native_entered);
        }
    }

    private class VoiceRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            if (switchLanguage)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, connectionManager.getLearningLanguage());
            else
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, connectionManager.getNativeLanguage());

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
            String input = edit_text_native.getText().toString();
            String translation = edit_text_translated.getText().toString();
            connectionManager.contributeToServer(input, translation);
        }
    }

    private class PasteListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            edit_text_native.setText(item.getText());
        }
    }

    private class CopyListener implements View.OnClickListener {
        @Override
        public void onClick(View view){
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


