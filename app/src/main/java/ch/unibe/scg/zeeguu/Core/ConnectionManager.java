package ch.unibe.scg.zeeguu.Core;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.google.Volley.Request;
import com.google.Volley.RequestQueue;
import com.google.Volley.Response;
import com.google.Volley.VolleyError;
import com.google.Volley.toolbox.JsonArrayRequest;
import com.google.Volley.toolbox.StringRequest;
import com.google.Volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.Header;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.Item;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.TranslatedWord;

public class ConnectionManager extends Application {

    //local variables
    private final String url = "https://www.zeeguu.unibe.ch/";

    private RequestQueue mRequestQueue;
    private ProgressDialog pDialog;
    private AlertDialog aDialog;
    private SharedPreferences settings;

    //TAGS
    private static final boolean debugOn = true;
    private static final String tag_wordlist_Req = "tag_wordlist_id";
    private static final String tag_SessionID_Req = "tag_session_id";
    private static final String tag_language_Req = "tag_language_id";
    private static final String TAG = "tag_logging";

    //user login information
    private String email;
    private String pw;
    private String session_id;
    private String native_language;
    private String learning_language;

    private static ConnectionManager instance;
    private static Activity activity;

    private static ArrayList<Item> wordList;
    private static EditText translationEditText;

    public ConnectionManager(Activity activity) {
        super();

        //initialise all variables
        this.activity = activity;
        this.wordList = new ArrayList<>();
        this.instance = this;

        //try to get the users information
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        loadAllUserInformation();

        //ToDo: Delete after debugging
        //email = "p.giehl@gmx.ch";
        //pw = "Micky";
        //session_id = "1467847111";

        //get the information that is missing from start point
        if (!userHasLoginInfo())
            getLoginInformation();
        else if (!userHasSessionId())
            getSessionIdFromServer();
        else
            getAllWordsFromServer();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static ConnectionManager getConnectionManager(Activity activity) {
        if (instance == null)
            return new ConnectionManager(activity);

        return instance;
    }

    public void updateUserInformation() {
        String tmpEmail = settings.getString(activity.getString(R.string.preference_email), "").toString();
        String tmpPw = settings.getString(activity.getString(R.string.preference_password), "").toString();

        if(!tmpEmail.equals(email) || !tmpPw.equals(pw)){
            email = tmpEmail;
            pw = tmpPw;
            getSessionId();
        }
    }

    public boolean userHasSessionId() {
        return !session_id.equals("");
    }

    public boolean userHasLoginInfo() {
        return !email.equals("") && !pw.equals("");
    }

    public static synchronized ConnectionManager getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelAllPendingRequests() {
        cancelPendingRequests(tag_wordlist_Req);
        cancelPendingRequests(tag_SessionID_Req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void getTranslation(String text, Boolean switchTransl, EditText translationView) {
        //more words can be translated in parallel, but no special characters
        if (!userHasLoginInfo() || text.equals("") || text == null || !isNetworkAvailable())
            return;

        //parse string to URL
        text = Uri.encode(text);

        String url_translation;
        if (!switchTransl)
            url_translation = url + "goslate_from_to/" + text + "/" +
                    native_language + "/" + learning_language + "?session=" + session_id;
        else
            url_translation = url + "goslate_from_to/" + text + "/" +
                    learning_language + "/" + native_language + "?session=" + session_id;

        translationEditText = translationView;
        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_translation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                translationEditText.setText(response.toString());
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_SessionID_Req);
    }


    //Getter und setter

    public String getSessionId() {
        return session_id;
    }

    public static ArrayList<Item> getWordList() {
        return wordList;
    }

    public String getNativeLanguage() {
        return native_language;
    }

    public void setNativeLanguage(String native_language_key) {
        setUserLanguageOnServer(true, native_language_key);
    }

    public String getLearningLanguage() {
        return learning_language;
    }

    public void setLearningLanguage(String learning_language_key) {
        setUserLanguageOnServer(false, learning_language_key);
    }


    //private methods

    private void loadAllUserInformation(){
        //only need to load all information once during start up
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        native_language = settings.getString("native_language", "").toString();
        learning_language = settings.getString("learning_language", "").toString();
    }

    private void getLoginInformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_sign_in, null))
                .setPositiveButton(R.string.button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editTextEmail = (EditText) aDialog.findViewById(R.id.dialog_email);
                        EditText editTextpw = (EditText) aDialog.findViewById(R.id.dialog_password);

                        email = editTextEmail.getText().toString();
                        pw = editTextpw.getText().toString();

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(activity.getString(R.string.preference_email), email);
                        editor.putString(activity.getString(R.string.preference_password), pw);
                        editor.commit();

                        getSessionIdFromServer();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        aDialog = builder.create();
        aDialog.show();
    }

    private void getSessionIdFromServer() {
        if (!userHasLoginInfo() || !isNetworkAvailable())
            return;

        String url_session_ID = url + "session/" + email;

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_session_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                session_id = response.toString();
                logging(TAG, session_id);

                //Save session ID
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(activity.getString(R.string.preference_user_session_id), session_id);
                editor.commit();
                dismissDialog();

                getAllWordsFromServer();
                getUserLanguageFromServer(false);
                getUserLanguageFromServer(true); //ask for the language when session ID arrived

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Message when used wrong name, pw
                logging(TAG, error.toString());
                dismissDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", pw);

                return params;
            }
        };

        this.addToRequestQueue(strReq, tag_SessionID_Req);
    }

    private void getAllWordsFromServer() {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_session_ID = url + "contribs_by_day/with_context?session=" + session_id;

        createLoadingDialog();
        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                logging(TAG, response.toString());
                wordList.clear();
                //ToDo: optimization that not everytime the whole list is sent
                try {
                    for (int j = 0; j < response.length(); j++) {
                        JSONObject dates = response.getJSONObject(j);
                        wordList.add(new Header(dates.getString("date")));
                        JSONArray contribs = dates.getJSONArray("contribs");
                        for (int i = 0; i < contribs.length(); i++) {
                            JSONObject translation = contribs.getJSONObject(i);
                            String nativeWord = translation.getString("from");
                            String translatedWord = translation.getString("to");
                            String context = translation.getString("context");
                            wordList.add(new TranslatedWord(nativeWord, translatedWord, context));
                        }
                    }

                } catch (JSONException error) {
                    logging(TAG, error.toString());
                }
                dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logging(TAG, error.toString());
                dismissDialog();
            }
        });


        this.addToRequestQueue(request, tag_wordlist_Req);
    }

    private void getUserLanguageFromServer(final boolean isNativeLanguage) {
        if (!userHasSessionId() || !isNetworkAvailable())

            return;

        //when native == true, get native language, else get learning language
        String url_language;
        if (isNativeLanguage)
            url_language = url + "native_language?session=" + session_id;
        else
            url_language = url + "learned_language?session=" + session_id;

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_language, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                logging(TAG, response.toString());
                //Save language
                SharedPreferences.Editor editor = settings.edit();
                if (isNativeLanguage)
                    editor.putString("native_language", response.toString());
                else
                    editor.putString("learning_language", response.toString());
                editor.commit();
                dismissDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_language_Req);
    }

    private void setUserLanguageOnServer(final Boolean isNativeLanguage, final String language_key) {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = url + "native_language/" + language_key + "?session=" + session_id;

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, url_language,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        logging(TAG, response.toString());
                        //Save language
                        if (response.toString().equals("OK")) {
                            if (isNativeLanguage)
                                learning_language = language_key;
                            else
                                native_language = language_key;
                        }
                        else
                            toast(activity.getString(R.string.change_learning_language_not_possible));

                        dismissDialog();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Catch error if no internet connection!!!
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_language_Req);
    }

    private void logging(String tag, String message) {
        if (debugOn)
            Log.d(TAG, message);
    }

    private void createLoadingDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }
    }

    private void toast(String text){
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
    }

    private void dismissDialog() {
        if (pDialog != null)
            pDialog.dismiss();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            toast(activity.getString(R.string.no_iternet_connection));
            return false;
        }

        return true;
    }
}
