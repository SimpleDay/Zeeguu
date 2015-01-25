package pascalgiehl_unibe.zeeguu.Search_Fragments;

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

import pascalgiehl_unibe.zeeguu.R;
import pascalgiehl_unibe.zeeguu.ZeeguuFragment;

/**
 * Created by Pascal on 12/01/15.
 */
public class Fragment_Text extends ZeeguuFragment {
    private EditText native_language_text;

    public Fragment_Text() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        native_language_text = (EditText) view.findViewById(R.id.text_native_translation_user_entered);

        ImageButton button_mic = (ImageButton) view.findViewById(R.id.microphone_search_button);
        button_mic.setOnClickListener(new View.OnClickListener() {
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
        });

        ImageButton button_cam = (ImageButton) view.findViewById(R.id.camera_search_button);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Integrate camera search
            }
        });

        ImageButton button_transl = (ImageButton) view.findViewById(R.id.translate_button);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Integrate translation fct
            }
        });


        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && null != data) {
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            native_language_text.setText(text.get(0));
        }
    }

    @Override
    public void actualizeFragment() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


}
