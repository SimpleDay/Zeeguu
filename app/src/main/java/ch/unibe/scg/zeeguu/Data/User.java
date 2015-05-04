package ch.unibe.scg.zeeguu.Data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.Item;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistHeader;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistItem;

/**
 * Zeeguu Application
 * Created by Pascal on 16/04/15.
 */
public class User implements IO {
    //user login information
    private String email;
    private String pw;
    private String session_id;
    private String native_language;
    private String learning_language;

    //User words
    private ArrayList<WordlistHeader> wordlist;

    private ZeeguuActivity activity;
    private ConnectionManager connectionManager;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private AlertDialog aDialog;


    public User(ZeeguuActivity activity, ConnectionManager connectionManager) {
        this.activity = activity;
        this.connectionManager = connectionManager;
        this.settings = PreferenceManager.getDefaultSharedPreferences(activity);
        this.editor = settings.edit();

        this.wordlist = new ArrayList<>();
    }


    //// User management functions ////

    public void loadAllUserInformationLocally() {
        //only need to load all information once during start up
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        native_language = settings.getString(activity.getString(R.string.preference_native_language), "").toString();
        learning_language = settings.getString(activity.getString(R.string.preference_learning_language), "").toString();
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

    public void logoutUser() {
        //delete Session ID when connection Info is not right
        session_id = "";
        email = "";
        pw = "";
        editor.remove(activity.getString(R.string.preference_email));
        editor.remove(activity.getString(R.string.preference_password));
        editor.remove(activity.getString(R.string.preference_user_session_id));
        editor.apply();

        wordlist.clear();
        connectionManager.notifyWordlistChange();
        activity.showLoginButtonIfNotLoggedIn();
        Toast.makeText(activity, activity.getString(R.string.error_user_logged_out), Toast.LENGTH_LONG).show();
    }

    public boolean isFirstLogin() {
        if (settings.getBoolean("my_first_time", true)) {
            settings.edit().putBoolean("my_first_time", false).apply();
            return true;
        }
        return false;
    }

    public boolean userHasLoginInfo() {
        return !email.equals("") && !pw.equals("");
    }

    public boolean userHasSessionId() {
        return !session_id.equals("");
    }


    //// information Dialogs ////

    public void getLoginInformation(String tmpEmail) {
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

                        if (!userHasLoginInfo()) {
                            Toast.makeText(activity, activity.getString(R.string.error_userinfo_invalid), Toast.LENGTH_LONG).show();
                            getLoginInformation(email);
                        } else if (!isEmailValid(email)) {
                            Toast.makeText(activity, R.string.error_email_not_valid, Toast.LENGTH_LONG).show();
                            getLoginInformation(email);
                        } else {
                            connectionManager.getSessionIdFromServer();
                        }

                    }
                })
                .setNegativeButton(R.string.button_cancel, null);

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                createNewAccount(editTextEmail.getText().toString(), "");
            }
        });
        aDialog.show();

        //if email address info available, put it in
        if (!tmpEmail.isEmpty()) {
            EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
            editTextEmail.setText(tmpEmail);
        }
    }

    public void createNewAccount(String tmpEmail, String tmpUsername) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_account, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_create_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextUsername = (EditText) aDialog.findViewById(R.id.dialog_username);
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextpw = (EditText) aDialog.findViewById(R.id.dialog_password);

                        String username = editTextUsername.getText().toString();
                        String email = editTextEmail.getText().toString();
                        String pw = editTextpw.getText().toString();

                        if (!userHasLoginInfo()) {
                            Toast.makeText(activity, activity.getString(R.string.error_userinfo_invalid), Toast.LENGTH_LONG).show();
                            createNewAccount(email, username);
                        } else if (!isEmailValid(email)) {
                            Toast.makeText(activity, R.string.error_email_not_valid, Toast.LENGTH_LONG).show();
                            createNewAccount(email, username);
                        } else {
                            connectionManager.createAccountOnServer(username, email, pw);
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, null);

        aDialog = builder.create();

        TextView noAccountMessage = (TextView) dialogView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                //open login screen with last entered email address
                EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                getLoginInformation(editTextEmail.getText().toString());
            }
        });
        aDialog.show();

        //if email or username already entered, reload them
        if (!tmpEmail.isEmpty()) {
            EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
            editTextEmail.setText(tmpEmail);
        }
        if (!tmpUsername.isEmpty()) {
            EditText editTextUsername = (EditText) aDialog.findViewById(R.id.dialog_username);
            editTextUsername.setText(tmpUsername);
        }
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
        saveMyWordsLocally();
    }


    //// interaction with MyWords  ////

    public boolean isMyWordsEmpty() {
        return wordlist.isEmpty();
    }

    public Item deleteWord(long id) {
        for (WordlistHeader wordlistHeader : wordlist) {
            for (int itemPosition = 0; itemPosition < wordlistHeader.getChildrenSize(); itemPosition++) {
                if (id == wordlistHeader.getChild(itemPosition).getItemId()) {
                    return wordlistHeader.removeChild(itemPosition);
                }
            }
        }
        return null;
    }

    public WordlistItem checkMyWordsForTranslation(String input, String inputLanguage, String outputLanguage) {
        for (WordlistHeader wordlistHeader : wordlist) {
            WordlistItem result = wordlistHeader.checkWordlistForTranslation(input, inputLanguage, outputLanguage);
            if (result != null)
                return result;
        }
        return null;
    }


    //// loading and writing my words from and to memory, IO interface  ////

    public void saveMyWordsLocally() {
        try {
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(activity.openFileOutput("MyZeeguuWords.bin", Context.MODE_PRIVATE)));
            write(dataOut);
            dataOut.close();
        } catch (IOException e) {
            Toast.makeText(activity, R.string.error_mywords_not_saved, Toast.LENGTH_LONG).show();
        }
    }

    public void loadMyWordsLocally() {
        try {
            DataInputStream dataIn = new DataInputStream(new BufferedInputStream(activity.openFileInput("MyZeeguuWords.bin")));
            read(dataIn);
            dataIn.close();
        } catch (IOException e) {
            Toast.makeText(activity, R.string.error_mywords_not_loaded, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(wordlist.size());
        for (WordlistHeader r : wordlist) {
            out.writeInt(r.getName().length());
            out.writeChars(r.getName());
            r.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readInt();
        wordlist = new ArrayList<WordlistHeader>(size);
        for (int i = 0; i < size; i++) {
            //get the name of the header group and create it
            int headerNameSize = in.readInt();
            byte[] nameData = new byte[headerNameSize];
            in.readFully(nameData);
            WordlistHeader r = new WordlistHeader(new String(nameData, "UTF-8"));

            //read all entries from the group and add it to the list
            r.read(in);
            wordlist.add(r);
        }
    }

    //// validation functions ////

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
