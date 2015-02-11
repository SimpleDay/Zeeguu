package ch.unibe.scg.zeeguu.Search_Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
    private EditText native_language_text;
    private EditText to_language_text;
    private Activity activity;
    private ConnectionManager connectionManager;

    private TextToSpeech textToSpeechNativeLanguage;
    private TextToSpeech textToSpeechOtherLanguage;


    //flags
    private ImageView flag_translate_from;
    private ImageView flag_translate_to;
    private boolean switchLanguage;

    //buttons
    ImageView button_read_to_user_native_language;
    ImageView button_read_to_user_learning_language;


    public FragmentText() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //initialize class variables
        native_language_text = (EditText) view.findViewById(R.id.text_native);
        to_language_text = (EditText) view.findViewById(R.id.text_translated);
        activity = getActivity();
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) activity);

        textToSpeechNativeLanguage = new TextToSpeech(activity, this);
        textToSpeechOtherLanguage = new TextToSpeech(activity, this);

        //Set done button to translate
        native_language_text.setOnKeyListener(new TextViewListener());

        //initialize flags
        flag_translate_from = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flag_translate_to = (ImageView) view.findViewById(R.id.ic_flag_translate_to);
        switchLanguage = false;

        //listeners for the flags to switch the flags by pressing on them
        flag_translate_from.setOnClickListener(new switchLanguageListener());
        flag_translate_to.setOnClickListener(new switchLanguageListener());

        //if a text was entered and the screen rotated, the text will be added again here.
        if (savedInstanceState != null) {
            native_language_text.setText(savedInstanceState.getString(
                    getString(R.string.preference_native_language)));
            to_language_text.setText(savedInstanceState.getString(
                    getString(R.string.preference_learning_language)));
        }

        //set listeners
        ImageView button_mic = (ImageView) view.findViewById(R.id.btn_microphone_search);
        button_mic.setOnClickListener(new voiceRecognitionListener());

        ImageView button_cam = (ImageView) view.findViewById(R.id.btn_camera_search);
        button_cam.setOnClickListener(new cameraRecognitionListener());

        ImageButton button_transl = (ImageButton) view.findViewById(R.id.btn_translate);
        button_transl.setOnClickListener(new translationListener());

        ImageButton button_contribute = (ImageButton) view.findViewById(R.id.btn_contribute);
        button_contribute.setOnClickListener(new uploadWordToLibraryListener());

        button_read_to_user_native_language = (ImageView) view.findViewById(R.id.btn_tts_native_language);
        button_read_to_user_native_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechNativeLanguage);
            }
        });

        button_read_to_user_learning_language = (ImageView) view.findViewById(R.id.btn_bookmark);
        button_read_to_user_learning_language.setOnClickListener(new View.OnClickListener() {
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
            native_language_text.setText(text.get(0));
        }
    }

    @Override
    public void actualizeFragment() {
    }

    @Override
    public void refreshLanguages() {
        setLanguageFlags();
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
        if (flag_translate_from != null || flag_translate_to != null || connectionManager != null) {
            switchLanguage = false;
            setLanguageFlags();
        }

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
        savedInstanceState.putString(getString(R.string.preference_native_language), native_language_text.getText().toString());
        savedInstanceState.putString(getString(R.string.preference_learning_language), to_language_text.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    //private Methods

    private void setLanguageFlags() {
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
            toast(error);
            logging(error);

            //button_read_to_user_native_language.setEnabled(true);
        }
    }

    private void speak(TextToSpeech tts) {
        String text;
        if (tts == textToSpeechNativeLanguage)
            text = native_language_text.getText().toString();
        else
            text = to_language_text.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    //Listeners

    private class translationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String input = native_language_text.getText().toString();
            connectionManager.getTranslation(input, switchLanguage, to_language_text);
        }
    }

    private class switchLanguageListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switchLanguage = !switchLanguage;
            setLanguageFlags();

            String native_entered = native_language_text.getText().toString();
            native_language_text.setText(to_language_text.getText().toString());
            to_language_text.setText(native_entered);
        }
    }

    private class TextViewListener implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String input = native_language_text.getText().toString();
                connectionManager.getTranslation(input, switchLanguage, to_language_text);
                return true;
            }
            return false;
        }
    }

    private class voiceRecognitionListener implements View.OnClickListener {
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

    private class cameraRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //ToDo: implement OCR
        }
    }

    private class uploadWordToLibraryListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String input = native_language_text.getText().toString();
            String translation = to_language_text.getText().toString();
            connectionManager.contributeToServer(input, translation);
        }
    }

}


