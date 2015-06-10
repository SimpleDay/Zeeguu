package ch.unibe.scg.zeeguuu.Search_Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import ch.unibe.scg.zeeguuu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Preference.LanguageListPreference;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;

/**
 * Fragment that handles all search request for new words
 */
public class SearchFragment extends Fragment implements TextToSpeech.OnInitListener {
    protected int RESULT_SPEECH = 1;

    private ZeeguuFragmentTextCallbacks callback;
    private ZeeguuConnectionManager connectionManager;
    private ClipboardManager clipboard;

    //layouts
    RelativeLayout tutorial;

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

    //buttons
    private ImageView btnttsLanguageFrom;
    private ImageView btnttsLanguageTo;

    private ImageView btnCopy;
    private ImageView btnPaste;
    private ImageView btnClearTextFrom;
    private ImageView btnClearTextTo;

    private ImageView btnBookmark;
    private boolean bookmarkActionActive;
    private long bookmarkID;


    public interface ZeeguuFragmentTextCallbacks {
        ZeeguuConnectionManager getConnectionManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        setHasOptionsMenu(true); //call onCreateOptionsMenu for this fragment to individualize it

        //initialize the layout variables
        tutorial = (RelativeLayout) view.findViewById(R.id.fragment_text_tutorial);

        //initialize class variables
        editTextLanguageFrom = (EditText) view.findViewById(R.id.edit_text_language_from);
        editTextLanguageTo = (EditText) view.findViewById(R.id.edit_text_language_to);

        //initialize flags
        flagTranslateFrom = (ImageView) view.findViewById(R.id.ic_flag_translate_from);
        flagTranslateTo = (ImageView) view.findViewById(R.id.ic_flag_translate_to);

        //listeners for the flags to switch the flags by pressing on them
        flagTranslateFrom.setOnClickListener(new LanguageChangeListener(true));
        flagTranslateTo.setOnClickListener(new LanguageChangeListener(false));

        //set listeners
        ImageView btn_mic = (ImageView) view.findViewById(R.id.btn_microphone_search);
        btn_mic.setOnClickListener(new VoiceRecognitionListener());

        TextView btn_transl = (TextView) view.findViewById(R.id.btn_translate);
        btn_transl.setOnClickListener(new TranslationListener());

        btnBookmark = (ImageView) view.findViewById(R.id.btn_bookmark);
        btnBookmark.setOnClickListener(new BookmarkListener());
        bookmarkActionActive = false;

        //Set done button to translate
        editTextLanguageFrom.setOnKeyListener(new KeyboardTranslationListener());

        editTextLanguageFrom.addTextChangedListener(new EditTextListener());
        editTextLanguageTo.addTextChangedListener(new EditTextListener());
        editTextLanguageTo.setOnClickListener(new LanguageSwitchListener());

        //Clipboard button listeners
        btnPaste = (ImageView) view.findViewById(R.id.btn_paste);
        btnPaste.setOnClickListener(new PasteListener());

        btnCopy = (ImageView) view.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new CopyListener());

        btnClearTextFrom = (ImageView) view.findViewById(R.id.ic_edit_text_clear_from);
        btnClearTextFrom.setOnClickListener(new ClearTextListener(editTextLanguageFrom));
        btnClearTextTo = (ImageView) view.findViewById(R.id.ic_edit_text_clear_to);
        btnClearTextTo.setOnClickListener(new ClearTextListener(editTextLanguageTo));

        //TTS Buttons
        btnttsLanguageFrom = (ImageView) view.findViewById(R.id.btn_tts_language_from);
        btnttsLanguageFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechLanguageFrom, editTextLanguageFrom, connectionManager.getAccount().getLanguageLearning());
            }
        });

        btnttsLanguageTo = (ImageView) view.findViewById(R.id.btn_tts_language_to);
        btnttsLanguageTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(textToSpeechLanguageTo, editTextLanguageTo, connectionManager.getAccount().getLanguageNative());
            }
        });

        updateButtons();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (ZeeguuFragmentTextCallbacks) getActivity();
        connectionManager = callback.getConnectionManager();

        clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardChangeListener());

        //TTS
        textToSpeechLanguageFrom = new TextToSpeech(getActivity(), this);
        textToSpeechLanguageTo = new TextToSpeech(getActivity(), this);
        activeTextToSpeech = null;

        //if a text was entered/bookmarked and the screen rotated, the text will be added again here.
        if (savedInstanceState != null) {
            editTextLanguageFrom.setText(savedInstanceState.getString(
                    getString(R.string.preference_language_from_tag)));
            editTextLanguageTo.setText(savedInstanceState.getString(
                    getString(R.string.preference_language_to_tag)));
            setAsBookmarked(savedInstanceState.getLong(
                    getString(R.string.preference_bookmark_id_tag)));
        }

        if (connectionManager.getAccount().isFirstLogin()) {
            tutorial.setVisibility(View.GONE); //TODO only for usability testing, otherwise visible
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: distinguish between image and sound recognition
        if (resultCode == Activity.RESULT_OK && null != data) {
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editTextLanguageFrom.setText(text.get(0));
            translate(); // translate the text immediately
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuFragmentTextCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuFragmentTextCallbacks");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard();
    }

    public void refreshLanguages(boolean isLanguageFrom) {
        updateTextFields();
        resetTextFields(isLanguageFrom);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            updateTextFields();
        } else {
            String error = getString(R.string.error_TTS_not_inititalized);
            toast(error);
            logging(error);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(getString(R.string.preference_language_from_tag), editTextLanguageFrom.getText().toString());
        savedInstanceState.putString(getString(R.string.preference_language_to_tag), editTextLanguageTo.getText().toString());
        savedInstanceState.putLong(getString(R.string.preference_bookmark_id_tag), bookmarkID);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void setAsBookmarked(long bookmarkID) {
        this.bookmarkID = bookmarkID;
        this.btnBookmark.setImageResource(bookmarkID != 0 ?
                R.drawable.btn_bookmark_filled : R.drawable.btn_bookmark);
        this.bookmarkActionActive = false;
    }

    public void setTranslatedText(String text) {
        editTextLanguageTo.setText(text);
        updateButtons();
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

    private void updateTextFields() {
        ZeeguuAccount account = connectionManager.getAccount();
        ZeeguuActivity.setFlag(flagTranslateFrom, account.getLanguageLearning());
        ZeeguuActivity.setFlag(flagTranslateTo, account.getLanguageNative());
    }

    private void speak(TextToSpeech tts, EditText edit_text, String language) {

        if (isNotEmpty(edit_text)) {
            if (activeTextToSpeech != null) {
                activeTextToSpeech.stop();
                activeTextToSpeech = null;
            } else {
                activeTextToSpeech = tts;
                setTTS(activeTextToSpeech, language);
                activeTextToSpeech.speak(edit_text.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                activeTextToSpeech = null;
            }
        } else
            toast(getString(R.string.error_no_text_to_read));
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

    private void updateButtons() {
        //copy button
        setButtonVisibility(btnCopy, isNotEmpty(editTextLanguageTo));
        //paste button
        setButtonVisibility(btnPaste, hasClipboardEntry());
        //both tts
        setButtonVisibility(btnttsLanguageFrom, isNotEmpty(editTextLanguageFrom));
        setButtonVisibility(btnttsLanguageTo, isNotEmpty(editTextLanguageTo));
        //both remove text
        setButtonVisibility(btnClearTextFrom, isNotEmpty(editTextLanguageFrom));
        setButtonVisibility(btnClearTextTo, isNotEmpty(editTextLanguageTo));

    }

    private void setButtonVisibility(ImageView imageView, Boolean enabled) {
        if (enabled) {
            imageView.setEnabled(true);
            imageView.setAlpha(1f);
        } else {
            imageView.setEnabled(false);
            imageView.setAlpha(.3f);
        }
    }

    private boolean isNotEmpty(EditText editText) {
        return editText.getText().toString().length() > 0;
    }

    private boolean hasClipboardEntry() {
        return clipboard != null && clipboard.hasPrimaryClip(); // && !clipboard.getPrimaryClip().getItemAt(0).getText().equals("");
    }

    private void translate() {
        if (!isNotEmpty(editTextLanguageFrom)) {
            setTranslatedText("");
            return;
        }

        String input = getEditTextTrimmed(editTextLanguageFrom);
        ZeeguuAccount account = connectionManager.getAccount();

        //search in MyWords if i already bookmarked that word
        MyWordsItem myWordsSearch = account.checkMyWordsForTranslation(input, account.getLanguageLearning(), account.getLanguageNative());

        if (myWordsSearch != null) {
            if (myWordsSearch.getLanguageFrom().equals(account.getLanguageLearning()))
                setTranslatedText(myWordsSearch.getLanguageToWord());
            else
                setTranslatedText(myWordsSearch.getLanguageFromWord());
            setAsBookmarked(myWordsSearch.getItemId());
        } else
            connectionManager.translate(input, account.getLanguageLearning(), account.getLanguageNative());

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
        updateButtons();
    }

    private void closeKeyboard() {
        if (isAdded()) {
            InputMethodManager iMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (editTextLanguageFrom != null)
                iMM.hideSoftInputFromWindow(editTextLanguageFrom.getWindowToken(), 0);
        }
    }

    private void openKeyboard() {
        if (isAdded()) {
            InputMethodManager iMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (editTextLanguageFrom != null)
                iMM.showSoftInput(editTextLanguageFrom, InputMethodManager.SHOW_IMPLICIT);
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
            updateButtons();
            updateTextFields();
            openKeyboard();
        }
    }


    private class LanguageChangeListener implements View.OnClickListener {
        private final boolean isLanguageFrom;

        public LanguageChangeListener(boolean isLanguageFrom) {
            this.isLanguageFrom = isLanguageFrom;
        }

        @Override
        public void onClick(View v) {
            LanguageListPreference listPreference = new LanguageListPreference(getActivity(), null);

            Resources res = getActivity().getResources();
            listPreference.setEntries(res.getStringArray(R.array.languages));
            listPreference.setEntryValues(res.getStringArray(R.array.language_keys));

            listPreference.showDialog(getActivity(), isLanguageFrom);
        }
    }

    private class VoiceRecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, connectionManager.getAccount().getLanguageLearning());

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

    private class BookmarkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!bookmarkActionActive) {
                if (bookmarkID == 0) {
                    bookmarkActionActive = true;
                    String input = getEditTextTrimmed(editTextLanguageFrom);
                    String translation = getEditTextTrimmed(editTextLanguageTo);

                    ZeeguuAccount account = connectionManager.getAccount();
                    connectionManager.bookmarkWithContext(input, account.getLanguageLearning(), translation, account.getLanguageNative(),
                            getString(R.string.bookmark_title), getString(R.string.bookmark_url_code), "");
                } else {
                    bookmarkActionActive = true;
                    connectionManager.removeBookmarkFromServer(bookmarkID);
                }
            } else
                toast(getString(R.string.error_bookmark_already));
        }
    }


    private class EditTextListener implements TextWatcher {
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
            //when language to textfield changes, it can be bookmarked again.
            bookmarkID = 0;
            bookmarkActionActive = false;
            btnBookmark.setImageResource(R.drawable.btn_bookmark);

            updateButtons();
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
            setButtonVisibility(btnPaste, true);
        }
    }

    private class ClearTextListener implements View.OnClickListener {
        private EditText editText;

        public ClearTextListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(View v) {
            editText.setText("");
            updateButtons();
        }
    }

    //// toast and loging functions ////
    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void logging(String message) {
        logging(getString(R.string.logging_tag), message);
    }

    private void logging(String tag, String message) {
        Log.d(tag, message);
    }
}

