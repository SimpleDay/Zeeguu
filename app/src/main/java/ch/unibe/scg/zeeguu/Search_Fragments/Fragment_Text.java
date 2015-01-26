package ch.unibe.scg.zeeguu.Search_Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguu.R;

/**
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Text extends ZeeguuFragment {
    private EditText native_language_text;
    private EditText to_language_text;
    private ConnectionManager connectionManager;

    public Fragment_Text() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        native_language_text = (EditText) view.findViewById(R.id.text_native_translation_user_entered);
        to_language_text = (EditText) view.findViewById(R.id.text_translated);
        connectionManager = ConnectionManager.getConnectionManager(getActivity());

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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private class voiceRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            try {
                startActivityForResult(intent, RESULT_SPEECH);
            } catch (ActivityNotFoundException a) {
                Toast t = Toast.makeText(getActivity(),
                        getString(R.string.mic_search_not_supported),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }

    private class translationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String input = native_language_text.getText().toString();
            connectionManager.getTranslation(input, "en", "de", to_language_text);
        }
    }

    private class cameraRecognitionListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            //ToDo: implement OCR
        }
    }

}
