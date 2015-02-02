package ch.unibe.scg.zeeguu.Search_Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentText extends ZeeguuFragment {
    private EditText native_language_text;
    private EditText to_language_text;
    private ConnectionManager connectionManager;

    //flags
    private ImageView flag_translate_from;
    private ImageView flag_translate_to;
    private boolean switchLanguage;


    public FragmentText() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //initialize class variables
        native_language_text = (EditText) view.findViewById(R.id.text_native_translation_user_entered);
        to_language_text = (EditText) view.findViewById(R.id.text_translated);
        connectionManager = ConnectionManager.getConnectionManager((ZeeguuActivity) getActivity());

        //Set done button to translate
        native_language_text.setOnKeyListener(new TextViewListener());

        //initialize flags
        flag_translate_from = (ImageView) view.findViewById(R.id.flag_translate_from);
        flag_translate_to = (ImageView) view.findViewById(R.id.flag_translate_to);
        switchLanguage = false;

        //listeners for the flags to switch the flags by pressing on them
        flag_translate_from.setOnClickListener(new cameraRecognitionListener());
        flag_translate_to.setOnClickListener(new cameraRecognitionListener());

        //set listeners
        ImageButton button_mic = (ImageButton) view.findViewById(R.id.microphone_search_button);
        button_mic.setOnClickListener(new voiceRecognitionListener());

        ImageButton button_cam = (ImageButton) view.findViewById(R.id.camera_search_button);
        button_cam.setOnClickListener(new cameraRecognitionListener());

        ImageButton button_transl = (ImageButton) view.findViewById(R.id.translate_button);
        button_transl.setOnClickListener(new translationListener());

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
    public void actualizeLanguages() {
        setLanguageFlags();
    }

    @Override
    public void onResume() {
        // The activity has become visible (it is now "resumed").
        super.onResume();
        if (flag_translate_from != null || flag_translate_to != null || connectionManager != null)
            setLanguageFlags();
    }


    //private Methods

    //TODO: Implement listener that when language change in settings delays, flags are still changed
    private void setLanguageFlags() {
        if (!switchLanguage) {
            setFlag(flag_translate_from, connectionManager.getNativeLanguage());
            setFlag(flag_translate_to, connectionManager.getLearningLanguage());
        } else {
            setFlag(flag_translate_to, connectionManager.getNativeLanguage());
            setFlag(flag_translate_from, connectionManager.getLearningLanguage());
        }
    }

    private void setFlag(ImageView flag, String language) {
        switch (language) {
            case "en":
                flag.setImageResource(R.drawable.flag_uk);
                break;
            case "de":
                flag.setImageResource(R.drawable.flag_german);
                break;
            case "fr":
                flag.setImageResource(R.drawable.flag_france);
                break;
            case "it":
                flag.setImageResource(R.drawable.flag_italy);
                break;
        }
    }

    //Listeners

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
                Toast t = Toast.makeText(getActivity(),
                        getString(R.string.error_mic_search_not_supported),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }

    private class translationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String input = native_language_text.getText().toString();
            connectionManager.getTranslation(input, switchLanguage, to_language_text);
        }
    }

    private class cameraRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //ToDo: implement OCR
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
}
