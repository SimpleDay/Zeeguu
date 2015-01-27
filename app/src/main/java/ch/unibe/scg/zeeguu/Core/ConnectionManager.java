package ch.unibe.scg.zeeguu.Core;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
        this.activity = activity;
        this.wordList = new ArrayList<>();

        //try to get the users information
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        updateUserInformation();
        //settings = activity.getSharedPreferences("ch.unibe.scg.zeeguu_preferences", 0);

        //ToDo: Delete after debugging
        //email = "p.giehl@gmx.ch";
        //pw = "Micky";
        //session_id = "1467847111";

        if (!userHasLoginInfo())
            getLoginInformation();
        else if (!userHasSessionId())
            getSessionIdFromServer();


        instance = this;
    }

    public void updateUserInformation() {
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        native_language = settings.getString("native_language", "").toString();
        learning_language = settings.getString("learning_language", "").toString();
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

    public void getSessionIdFromServer() {
        String url_session_ID = url + "session/" + email;

        if (!userHasLoginInfo())
            return;

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
                getUserLanguageFromServer(); //ask for the language when session ID arrived

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

    public void getAllWordsFromServer() {
        if (!userHasSessionId()) return;

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
                        for(int i = 0; i < contribs.length(); i++) {
                            JSONObject translation = contribs.getJSONObject(i);
                            String nativeWord = translation.getString("from");
                            String translatedWord = translation.getString("to");
                            String context = translation.getString("context");
                            wordList.add(new TranslatedWord(nativeWord, translatedWord, context));
                        }
                    }

                } catch (JSONException error) {
                    logging(TAG, error.toString());
                    if(debugOn)
                        Toast.makeText(activity,"Error: " + error.toString(), Toast.LENGTH_LONG).show();
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

    public void getUserLanguageFromServer(){
        if (!userHasSessionId())
            return;

        String url_learned_language = url + "learned_language?session=" + session_id;

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_learned_language, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                logging(TAG, response.toString());
                //Save language
                SharedPreferences.Editor editor = settings.edit();
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

    public void setUserLanguageOnServer(){
        if (!userHasSessionId())
            return;

        final String set_language = settings.getString("learning_language", "").toString();
        String url_learn_language = url + "learned_language/" + set_language + "?session=" + session_id;

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_learn_language, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                logging(TAG, response.toString());
                //Save language
                if(response.toString().equals("OK"))
                    learning_language = set_language;
                else
                    Toast.makeText(getApplicationContext(), R.string.change_learning_language_not_possible,
                            Toast.LENGTH_LONG).show();

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

    public String getSessionId() { return session_id; }

    public void getTranslation(String text, EditText translationView) {
        //more words can be translated in parallel, but no special characters
        if (!userHasLoginInfo())
            return;

        text = text.replaceAll("\\s+", "%20");
        String url_translation = url + "goslate_from_to/" + text + "/" +
                native_language + "/" + learning_language + "?session=" + session_id;

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

    public static ArrayList<Item> getWordList() {
        return wordList;
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

    private void logging(String tag, String message) {
        if(debugOn)
            Log.d(TAG, message);
    }

    private void createLoadingDialog() {
        if(pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }
    }

    private void dismissDialog() {
        if(pDialog != null)
            pDialog.dismiss();
    }



    /*
    public ConnectionManager() {
        RequestQueue mRequestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        mRequestQueue.start();

    }*/
}
