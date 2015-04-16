package ch.unibe.scg.zeeguu.Core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistHeader;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistItem;

/**
 * Zeeguu Application
 * Created by Pascal on 16/04/15.
 */
public class User {
    //user login information
    private String email;
    private String pw;
    private String session_id;
    private String native_language;
    private String learning_language;

    //User words
    private ArrayList<WordlistHeader> wordlist;
    private ArrayList<WordlistItem> wordlistItems; //used to make local search, not nice, is a small hack at the moment //TODO: remove


    private Activity activity;
    private ConnectionManager connectionManager;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private AlertDialog aDialog;


    public User(Activity activity, ConnectionManager connectionManager) {
        this.activity = activity;
        this.connectionManager = connectionManager;
        this.settings = PreferenceManager.getDefaultSharedPreferences(activity);
        this.editor = settings.edit();

        this.wordlist = new ArrayList<>();
        this.wordlistItems = new ArrayList<>();
    }

    public void loadAllUserInformationLocally() {
        //only need to load all information once during start up
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        native_language = settings.getString(activity.getString(R.string.preference_native_language), "").toString();
        learning_language = settings.getString(activity.getString(R.string.preference_learning_language), "").toString();
    }

    public void updateUserInformation() {
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
    }

    public void saveUserInformationLocally() {
        editor.putString(activity.getString(R.string.preference_email), email);
        editor.putString(activity.getString(R.string.preference_password), pw);
        editor.putString(activity.getString(R.string.preference_user_session_id), session_id);
        editor.apply();
    }

    public void saveUserLanguagesLocally() {
        editor.putString(activity.getString(R.string.preference_native_language), native_language);
        editor.putString(activity.getString(R.string.preference_learning_language), learning_language);
        editor.apply();
    }

    public void resetUserInformation() {
        //delete Session ID when connection Info is not right
        session_id = "";
        editor.remove(activity.getString(R.string.preference_user_session_id));
        editor.apply();

        wordlist.clear();
        wordlistItems.clear();
        Toast.makeText(activity, activity.getString(R.string.error_user_logged_out), Toast.LENGTH_LONG).show();
    }

    public boolean userHasLoginInfo() {
        return !email.equals("") && !pw.equals("");
    }

    public boolean userHasSessionId() {
        return !session_id.equals("");
    }

    //// information Dialogs ////

    public void getLoginInformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sign_in, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextPW = (EditText) aDialog.findViewById(R.id.dialog_password);

                        email = editTextEmail.getText().toString();
                        pw = editTextPW.getText().toString();

                        connectionManager.getSessionIdFromServer();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                createNewAccount();
            }
        });
        aDialog.show();
    }

    public void createNewAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_account, null);

        final Spinner spinnerNativeLanguage = (Spinner) dialogView.findViewById(R.id.dialog_new_account_spinner_native);
        final Spinner spinnerLearningLanguage = (Spinner) dialogView.findViewById(R.id.dialog_new_account_spinner_learning);

        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(activity,
                R.array.languages, android.R.layout.simple_spinner_item);

        spinnerNativeLanguage.setAdapter(languageAdapter);
        spinnerLearningLanguage.setAdapter(languageAdapter);

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextpw = (EditText) aDialog.findViewById(R.id.dialog_password);

                        String email = editTextEmail.getText().toString();
                        String pw = editTextpw.getText().toString();
                        //TODO: check input

                        //TODO: add languages to server creation
                        String nativeLanguage = spinnerNativeLanguage.getSelectedItem().toString();
                        String learningLanguage = spinnerLearningLanguage.getSelectedItem().toString();

                        connectionManager.createAccountOnServer(email, pw);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                getLoginInformation();
            }
        });
        aDialog.show();
    }

    //// Getter and Setter ////

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getNative_language() {
        return native_language;
    }

    public void setNative_language(String native_language) {
        this.native_language = native_language;
    }

    public String getLearning_language() {
        return learning_language;
    }

    public void setLearning_language(String learning_language) {
        this.learning_language = learning_language;
    }

    public ArrayList<WordlistHeader> getWordlist() {
        return wordlist;
    }

    public void setWordlist(ArrayList<WordlistHeader> wordlist) {
        this.wordlist = wordlist;
    }

    public ArrayList<WordlistItem> getWordlistItems() {
        return wordlistItems;
    }

    public void setWordlistItems(ArrayList<WordlistItem> wordlistItems) {
        this.wordlistItems = wordlistItems;
    }

}
