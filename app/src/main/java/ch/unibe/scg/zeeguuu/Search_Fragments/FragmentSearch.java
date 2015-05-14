package ch.unibe.scg.zeeguuu.Search_Fragments;

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

import ch.unibe.scg.zeeguuu.Core.ZeeguuFragment;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Settings.LanguageListPreference;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Created by Pascal on 12/01/15.
 */
public class FragmentSearch extends ZeeguuFragment implements TextToSpeech.OnInitListener {
    private Activity activity;
    private ZeeguuFragmentTextCallbacks callback;
    private ZeeguuConnectionManager connectionManager;
    private ClipboardManager clipboard;

    //text fields
    private EditText editTextLanguageFrom;
    private EditText editTextLanguageTo;

    //TTS
    private TextToSpeech textToSpeechLanguageFrom;
    private TextToSpeech textToSpeechLanguageTo;
    private TextToSpeech activeTextToSpeech;

    //flags
    private ImageView flagTranslateFrom;
    private ImageView flagTranslateTo;

    private String switchedText;
    private SharedPreferences settings;

    //buttons
    private ImageView btnttsLanguageFrom;
    private ImageView btnttsLanguageTo;

    private ImageView btnCopy;
    private ImageView btnPaste;

    private ImageView btnBookmark;
    private boolean bookmarked;


    public interface ZeeguuFragmentTextCallbacks {
        ZeeguuConnectionManager getConnectionManager();
    }

    //TODO: separate onCreate and onCreateView
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        //initialize class variables
        editTextLanguageFrom = (EditText) view.findViewById(R.id.edit_text_language_from);
        editTextLanguageTo = (EditText) view.findViewById(R.id.edit_text_language_to);
        clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);

        //TTS
        textToSpeechLanguageFrom = new TextToSpeech(activity, this);
        textToSpeechLanguageTo = new TextToSpeech(activity, this);
        activeTextToSpeech = null;

        //initialize flags
        flagTranslateFrom = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flagTranslateTo = (ImageView) view.findViewById(R.id.ic_flag_translate_to);

        //remembers if languages was switched
        settings = PreferenceManager.getDefaultSharedPreferences(activity);

        //listeners for the flags to switch the flags by pressing on them
        flagTranslateFrom.setOnClickListener(new LanguageSwitchListener());
        flagTranslateTo.setOnClickListener(new LanguageSwitchListener());

        flagTranslateFrom.setOnLongClickListener(new LanguageChangeListener(true));
        flagTranslateTo.setOnLongClickListener(new LanguageChangeListener(false));

        //if a text was entered and the screen rotated, the text will be added again here.
        if (savedInstanceState != null) {
            editTextLanguageFrom.setText(savedInstanceState.getString(
                    getString(R.string.preference_language_from)));
            editTextLanguageTo.setText(savedInstanceState.getString(
                    getString(R.string.preference_language_to)));
        }

        //set listeners
        clipboard.addPrimaryClipChangedListener(new ClipboardChangeListener());

        ImageView btn_mic = (ImageView) view.findViewById(R.id.btn_microphone_search);
        btn_mic.setOnClickListener(new VoiceRecognitionListener());

        ImageView btn_cam = (ImageView) view.findViewById(R.id.btn_camera_search);
        btn_cam.setOnClickListener(new CameraRecognitionListener());

        TextView btn_transl = (TextView) view.findViewById(R.id.btn_translate);
        btn_transl.setOnClickListener(new TranslationListener());

        btnBookmark = (ImageView) view.findViewById(R.id.btn_bookmark);
        btnBookmark.setOnClickListener(new BookmarkListener());
        bookmarked = false;

        //Set done button to translate
        editTextLanguageFrom.setOnKeyListener(new KeyboardTranslationListener());

        editTextLanguageFrom.addTextChangedListener(new EditTextListener(false));
        editTextLanguageTo.addTextChangedListener(new EditTextListener(true));

        //Clipboard button listeners
        btnPaste = (ImageView) view.findViewById(R.id.btn_paste);
        btnPaste.setOnClickListener(new PasteListener());

        btnCopy = (ImageView) view.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new CopyListener());


        //See if something has been added to clipboard and if activate paste button
        showActiveButton(btnPaste, hasClipboardEntry());
        showActiveButton(btnCopy, !editTextLanguageTo.getText().toString().isEmpty());


        //TTS
        btnttsLanguageFrom = (ImageView) view.findViewById(R.id.btn_tts_language_from);
        btnttsLanguageFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechLanguageFrom, editTextLanguageFrom);
            }
        });
        showActiveButton(btnttsLanguageFrom, !editTextLanguageFrom.getText().toString().equals(""));

        btnttsLanguageTo = (ImageView) view.findViewById(R.id.btn_tts_language_to);
        btnttsLanguageTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechLanguageTo, editTextLanguageTo);
            }
        });
        showActiveButton(btnttsLanguageTo, !editTextLanguageTo.getText().toString().equals(""));

        //Open tutorial when the app is first opened
        if (connectionManager.getAccount().isFirstLogin()) {
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
            editTextLanguageFrom.setText(text.get(0));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuFragmentTextCallbacks) activity;
            connectionManager = callback.getConnectionManager();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuFragmentTextCallbacks");
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

    public void refreshLanguages(boolean isLanguageFrom) {
        setLanguagesTextFields();
        resetTextFields(isLanguageFrom);
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
        savedInstanceState.putString(getString(R.string.preference_language_from), editTextLanguageFrom.getText().toString());
        savedInstanceState.putString(getString(R.string.preference_language_to), editTextLanguageTo.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void markEntriesAsBookmarked() {
        bookmarked = true;
        btnBookmark.setImageResource(R.drawable.btn_bookmark_filled);

    }

    public void setTranslatedText(String text) {
        editTextLanguageTo.setText(text);
        showActiveButton(btnCopy, !text.equals(""));
    }

    @Override
    public void onDestroy() {
        // Shut down both TTS to prevent memory leaks
        if (textToSpeechLanguageFrom != null) {
            textToSpeechLanguageFrom.stop();
            textToSpeechLanguageFrom.shutdown();
        }

        if (textToSpeechLanguageTo != null) {
            textToSpeechLanguageTo.stop();
            textToSpeechLanguageTo.shutdown();
        }
        super.onDestroy();
    }


    //// private Methods ////

    private void setLanguagesTextFields() {
        ZeeguuAccount account = connectionManager.getAccount();
        setFlag(flagTranslateFrom, account.getLanguageNative());
        setTTS(textToSpeechLanguageFrom, account.getLanguageNative());

        setFlag(flagTranslateTo, account.getLanguageLearning());
        setTTS(textToSpeechLanguageTo, account.getLanguageLearning());
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

    private void showActiveButton(ImageView imageView, Boolean enabled) {
        if (enabled) {
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
        String input = getEditTextTrimmed(editTextLanguageFrom);
        ZeeguuAccount account = connectionManager.getAccount();
        //search in MyWords if i already bookmarked that word
        MyWordsItem myWordsSearch = connectionManager.getAccount().checkMyWordsForTranslation(input, account.getLanguageNative(), account.getLanguageLearning());

        if (myWordsSearch == null)
            connectionManager.translate(input, account.getLanguageNative(), account.getLanguageLearning());
        else {
            if (myWordsSearch.getLanguageFrom().equals(account.getLanguageNative()))
                setTranslatedText(myWordsSearch.getLanguageToWord());
            else
                setTranslatedText(myWordsSearch.getLanguageFromWord());

            markEntriesAsBookmarked();
        }
        closeKeyboard();
    }


    private String getEditTextTrimmed(EditText editText) {
        return editText.getText().toString().replaceAll("[ ]+", " ").trim();
    }

    private void resetTextFields(boolean isLanguageFrom) {
        if (isLanguageFrom)
            editTextLanguageFrom.setText("");
        else
            editTextLanguageTo.setText("");

        switchedText = "";
        showActiveButton(btnCopy, false);
    }

    private void closeKeyboard() {
        if (activity != null) {
            InputMethodManager iMM = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            iMM.hideSoftInputFromWindow(editTextLanguageFrom.getWindowToken(), 0);
        }
    }


    ///// Listeners /////

    private class TranslationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            translate();
        }
    }

    private class KeyboardTranslationListener implements View.OnKeyListener {
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
            connectionManager.getAccount().switchLanguages();

            String tmpLanguageFromWord = getEditTextTrimmed(editTextLanguageFrom);
            String tmpLanguageToWord = getEditTextTrimmed(editTextLanguageTo);
            if (tmpLanguageToWord.equals(""))
                editTextLanguageFrom.setText(switchedText);
            else
                editTextLanguageFrom.setText(tmpLanguageToWord);

            switchedText = tmpLanguageFromWord;
            editTextLanguageTo.setText("");

            //initialize back the view
            showActiveButton(btnCopy, false);
            setLanguagesTextFields();
        }
    }

    private class LanguageChangeListener implements View.OnLongClickListener {
        private boolean isLanguageFrom;

        public LanguageChangeListener(boolean isLanguageFrom) {
            this.isLanguageFrom = isLanguageFrom;
        }

        @Override
        public boolean onLongClick(View v) {
            LanguageListPreference listPreference = new LanguageListPreference(activity, null);

            Resources res = activity.getResources();
            listPreference.setEntries(res.getStringArray(R.array.languages));
            listPreference.setEntryValues(res.getStringArray(R.array.language_keys));

            listPreference.showDialog(activity, isLanguageFrom);
            return true;
        }
    }

    private class VoiceRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, connectionManager.getAccount().getLanguageNative());

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

    private class BookmarkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ZeeguuAccount account = connectionManager.getAccount();
            if (!account.isUserLoggedIn())
                toast(getString(R.string.error_user_not_logged_in_yet));
            else if (editTextLanguageFrom.getText().length() != 0 && editTextLanguageTo.getText().length() != 0) {
                if (!bookmarked) {
                    String input = getEditTextTrimmed(editTextLanguageFrom);
                    String translation = getEditTextTrimmed(editTextLanguageTo);

                    connectionManager.bookmarkWithContext(input, account.getLanguageNative(), translation, account.getLanguageLearning(),
                            getString(R.string.bookmark_title), activity.getString(R.string.bookmark_url_code), "");
                } else {
                    toast(getString(R.string.error_bookmarked_already)); //TODO: press it again when filled deletes bookmark
                }
            } else {
                toast(getString(R.string.error_no_text_to_bookmark));
            }
        }
    }

    private class EditTextListener implements TextWatcher {
        private boolean isLanguageToListener;

        public EditTextListener(boolean isLanguageToListener) {
            this.isLanguageToListener = isLanguageToListener;
        }

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
            if (isLanguageToListener) {
                //when language to textfield changes, it can be bookmarked again.
                bookmarked = false;
                btnBookmark.setImageResource(R.drawable.btn_bookmark);

                boolean editTextIsEmpty = editTextLanguageTo.getText().toString().equals("");
                showActiveButton(btnCopy, !editTextIsEmpty);
                showActiveButton(btnttsLanguageTo, !editTextIsEmpty);
            } else {
                showActiveButton(btnttsLanguageFrom, !editTextLanguageFrom.getText().toString().equals(""));
            }
        }
    }


    ///// Listeners for copy and paste functions /////

    private class PasteListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            editTextLanguageFrom.getText().insert(editTextLanguageFrom.getSelectionStart(), item.getText());
        }
    }

    private class CopyListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ClipData clip = ClipData.newPlainText("paste", editTextLanguageTo.getText().toString());
            clipboard.setPrimaryClip(clip);
            //Feedback that text has been copied
            toast(getString(R.string.successful_text_copied));
        }
    }

    private class ClipboardChangeListener implements ClipboardManager.OnPrimaryClipChangedListener {
        @Override
        public void onPrimaryClipChanged() {
            //initialize paste button because something is added to clipboard
            showActiveButton(btnPaste, true);
        }
    }

}


