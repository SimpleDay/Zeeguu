package ch.unibe.scg.zeeguu.Data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguu.MyWords_Fragments.Item;
import ch.unibe.scg.zeeguu.MyWords_Fragments.MyWordsHeader;
import ch.unibe.scg.zeeguu.MyWords_Fragments.MyWordsItem;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 16/04/15.
 */
public class User implements IO {
    //user login information
    private String email;
    private String pw;
    private String session_id;
    private String languageFrom;
    private String languageTo;

    //User words
    private ArrayList<MyWordsHeader> myWords;
    private String myWordsFileName = "zeeguuMyWordsTmp";

    private ZeeguuActivity activity;
    private ConnectionManager connectionManager;
    private SharedPreferences settings;


    public User(ZeeguuActivity activity, ConnectionManager connectionManager) {
        this.activity = activity;
        this.connectionManager = connectionManager;
        this.settings = PreferenceManager.getDefaultSharedPreferences(activity);

        this.myWords = new ArrayList<>();

        //try to get the users information
        loadAllUserInformationLocally();
    }


    //// User management functions ////

    public void loadAllUserInformationLocally() {
        //only need to load all information once during start up
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        languageFrom = settings.getString(activity.getString(R.string.preference_language_from), "").toString();
        languageTo = settings.getString(activity.getString(R.string.preference_language_to), "").toString();
    }

    public void saveUserInformationLocally() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(activity.getString(R.string.preference_email), email);
        editor.putString(activity.getString(R.string.preference_password), pw);
        editor.putString(activity.getString(R.string.preference_user_session_id), session_id);
        editor.apply();
    }

    public void saveUserLanguagesLocally() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(activity.getString(R.string.preference_language_from), languageFrom);
        editor.putString(activity.getString(R.string.preference_language_to), languageTo);
        editor.apply();
    }

    public void logoutUser() {
        //delete Session ID when connection Info is not right
        session_id = "";
        email = "";
        pw = "";

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(activity.getString(R.string.preference_email));
        editor.remove(activity.getString(R.string.preference_password));
        editor.remove(activity.getString(R.string.preference_user_session_id));
        editor.apply();

        myWords.clear();
        clearMyWordsOnPhone();

        connectionManager.notifyMyWordsChange();
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

    public boolean userHasLoginInfo(String email, String pw) {
        return !email.equals("") && !pw.equals("");
    }

    public boolean userHasSessionId() {
        return !session_id.equals("");
    }

    public void switchLanguages() {
        String tmpLanguageFrom = languageFrom;
        languageFrom = languageTo;
        languageTo = tmpLanguageFrom;
    }

    public void setDefaultLanguages() {
        languageFrom = "en";
        languageTo = "de";
    }

    //// validation functions ////

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    //// Getter and Setter ////

    public String getEmail() {
        return email;
    }

    public boolean setEmail(String email) {
        if(isEmailValid(email)) {
            this.email = email;
            return true;
        }
        return false;
    }

    public String getPw() {
        return pw;
    }

    public boolean setPw(String pw) {
        if(pw.length() > 0) {
            this.pw = pw;
            return true;
        }
        return false;
    }

    public String getSession_id() {
        return session_id;
    }

    public boolean setSession_id(String session_id) {
        if(session_id != null) {
            this.session_id = session_id;
            return true;
        }
        return false;
    }

    public String getLanguageFrom() {
        return languageFrom;
    }

    public boolean setLanguageFrom(String languageFrom) {
        if (languageFrom.length() == 2) {
            this.languageFrom = languageFrom;
            return true;
        }
        return false;
    }

    public String getLanguageTo() {
        return languageTo;
    }

    public boolean setLanguageTo(String languageTo) {
        if (languageTo.length() == 2) {
            this.languageTo = languageTo;
            return true;
        }
        return false;
    }

    public ArrayList<MyWordsHeader> getMyWords() {
        return myWords;
    }

    public boolean setMyWords(ArrayList<MyWordsHeader> myWords) {
        if (myWords != null) {
            this.myWords = myWords;
            saveMyWordsToPhone();
            return true;
        }
        return false;
    }


    //// interaction with MyWords  ////

    public boolean isMyWordsEmpty() {
        return myWords.isEmpty();
    }

    public Item deleteWord(long id) {
        for (MyWordsHeader myWordsHeader : myWords) {
            for (int itemPosition = 0; itemPosition < myWordsHeader.getChildrenSize(); itemPosition++) {
                if (id == myWordsHeader.getChild(itemPosition).getItemId()) {
                    return myWordsHeader.removeChild(itemPosition);
                }
            }
        }
        return null;
    }

    public MyWordsItem checkMyWordsForTranslation(String input, String inputLanguage, String outputLanguage) {
        for (MyWordsHeader myWordsHeader : myWords) {
            MyWordsItem result = myWordsHeader.checkMyWordsForTranslation(input, inputLanguage, outputLanguage);
            if (result != null)
                return result;
        }
        return null;
    }


    //// loading and writing my words from and to memory, IO interface  ////

    public void saveMyWordsToPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            file.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            write(bufferedWriter);
            bufferedWriter.close();

            Log.d("TAG", "Saved words to file at location: " + file.getPath());
        } catch (IOException e) {
            Toast.makeText(activity, R.string.error_mywords_not_saved, Toast.LENGTH_LONG).show();
            Log.d(activity.getString(R.string.logging_tag), e.getMessage());
        }
    }

    @Override
    public void write(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("" + myWords.size());
        bufferedWriter.newLine();
        for (MyWordsHeader r : myWords) {
            bufferedWriter.write(r.getName());
            bufferedWriter.newLine();
            r.write(bufferedWriter);
            bufferedWriter.flush();
        }
    }

    public void loadMyWordsFromPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            read(bufferedReader);
            bufferedReader.close();
            Log.d("TAG", "Load words from file at location: " + activity.getFilesDir().toString());
        } catch (Exception e) {
            Toast.makeText(activity, R.string.error_mywords_not_loaded, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void read(BufferedReader bufferedReader) throws IOException {
        myWords.clear();

        int size = Integer.parseInt(bufferedReader.readLine());
        for (int i = 0; i < size; i++) {
            //get the name of the header group and create it
            MyWordsHeader r = new MyWordsHeader(bufferedReader.readLine().trim());
            //read all entries from the group and add it to the list
            r.read(bufferedReader);
            myWords.add(r);
        }
    }

    public void clearMyWordsOnPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            write(bufferedWriter);
            bufferedWriter.close();
        } catch (Exception e) {
            Log.d("TAG", "MyWords on phone could not be deleted");
        }
    }

}
