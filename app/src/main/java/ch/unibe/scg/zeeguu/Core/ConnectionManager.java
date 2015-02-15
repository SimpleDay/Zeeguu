package ch.unibe.scg.zeeguu.Core;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

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
import ch.unibe.scg.zeeguu.Search_Fragments.FragmentText;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.Item;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistHeader;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistItem;

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
    private static final String tag_translation_Req = "tag_translation_id";
    private static final String tag_contribute_Req = "tag_contrib_id";
    private static final String TAG = "tag_logging";

    //user login information
    private String email;
    private String pw;
    private String session_id;
    private String native_language;
    private String learning_language;

    private static ConnectionManager instance;
    private static ZeeguuActivity activity;

    private static ArrayList<Item> wordList;


    public ConnectionManager(ZeeguuActivity activity) {
        super();

        //initialise all variables
        this.activity = activity;
        this.wordList = new ArrayList<>();
        this.instance = this;

        //try to get the users information
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        loadAllUserInformation();

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

    public static ConnectionManager getConnectionManager(ZeeguuActivity activity) {
        if (instance == null && activity != null)
            return new ConnectionManager(activity);

        return instance;
    }

    public void updateUserInformation() {
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();

        getSessionIdFromServer();
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

    public void getTranslation(String input, String inputLanguage, String outputLanguage, final FragmentText fragmentText) {
        if (!userHasLoginInfo() || input.equals("") || input == null || !isNetworkAvailable())
            return;

        //parse string to URL
        input = Uri.encode(input);

        String url_translation = url + "goslate_from_to/" + input + "/" +
                    inputLanguage + "/" + outputLanguage + "?session=" + session_id;
        logging(TAG, url_translation);

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_translation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.setTranslatedText(response.toString());
                fragmentText.activateButtons();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_translation_Req);
    }

    public void contributeToServer(String input, String translation, final FragmentText fragmentText) {
        if (!userHasLoginInfo() || input.equals("") || translation.equals("") || !isNetworkAvailable())
            return;

        //parse string to URL
        input = Uri.encode(input);
        translation = Uri.encode(translation);

        String urlContribution = url + "contribute_with_context/" + learning_language + "/" + translation + "/" +
                native_language + "/" + input + "?session=" + session_id;
        logging(TAG, urlContribution);

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.activateContribution();
                logging(TAG, "successful contributed: " + response);
                toast("Contribution successful");
                getAllWordsFromServer(); //TODO: Not always get the whole list, just add word locally
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                logging(TAG, error.toString());
                dismissDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", "Android Application");
                params.put("url", "");
                params.put("context", "");

                return params;
            }
        };

        this.addToRequestQueue(strReq, tag_contribute_Req);
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
        native_language = native_language_key;
        activity.refreshLanguages();
        setUserLanguageOnServer(activity.getString(R.string.preference_native_language), native_language_key);
    }

    public String getLearningLanguage() {
        return learning_language;
    }

    public void setLearningLanguage(String learning_language_key) {
        learning_language = learning_language_key;
        activity.refreshLanguages();
        setUserLanguageOnServer(activity.getString(R.string.preference_learning_language), learning_language_key);
    }


    //private methods

    private void loadAllUserInformation() {
        //only need to load all information once during start up
        email = settings.getString(activity.getString(R.string.preference_email), "").toString();
        pw = settings.getString(activity.getString(R.string.preference_password), "").toString();
        session_id = settings.getString(activity.getString(R.string.preference_user_session_id), "").toString();
        native_language = settings.getString(activity.getString(R.string.preference_native_language), "").toString();
        learning_language = settings.getString(activity.getString(R.string.preference_learning_language), "").toString();
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
                toast(activity.getString(R.string.successful_login));
                logging(TAG, session_id);

                //Save session ID
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(activity.getString(R.string.preference_user_session_id), session_id);
                editor.commit();
                dismissDialog();

                getAllWordsFromServer();
                getUserLanguageFromServer("native_language"); //ask for the language when session ID arrived
                getUserLanguageFromServer("learned_language");
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_user_login_wrong));
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
        logging(TAG, url_session_ID);

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
                        wordList.add(new WordlistHeader(dates.getString("date")));
                        JSONArray contribs = dates.getJSONArray("contribs");
                        for (int i = 0; i < contribs.length(); i++) {
                            JSONObject translation = contribs.getJSONObject(i);
                            String nativeWord = translation.getString("from");
                            String translatedWord = translation.getString("to");

                            String context = translation.getString("context");
                            wordList.add(new WordlistItem(nativeWord, translatedWord, context));
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
                toast(activity.getString(R.string.error_server_not_online));
                logging(TAG, error.toString());
                dismissDialog();
            }
        });


        this.addToRequestQueue(request, tag_wordlist_Req);
    }

    private void getUserLanguageFromServer(final String urlTag) {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = url + urlTag + "?session=" + session_id;

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_language, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                String language = response.toString();
                logging(TAG, urlTag + ": " + language);

                //Save language
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(urlTag, language);
                editor.commit();

                //save it to the variable
                if (urlTag.equals("learned_language"))
                    learning_language = language;
                else
                    native_language = language;

                activity.refreshLanguages();
                dismissDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_server_not_online));
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_language_Req);
    }

    private void setUserLanguageOnServer(final String urlTag, final String language_key) {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = url + urlTag + "/" + language_key + "?session=" + session_id;

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, url_language,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        logging(TAG, response.toString());

                        //Save language
                        if (response.toString().equals("OK")) {
                            if (urlTag.equals(activity.getString(R.string.preference_learning_language)))
                                toast(activity.getString(R.string.successful_language_learning_changed) + " " + language_key);

                            else
                                toast(activity.getString(R.string.successful_language_native_changed) + " " + language_key);
                        } else
                            toast(activity.getString(R.string.error_change_learning_language_on_server_not_possible));

                        dismissDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_server_not_online));
                logging(TAG, error.toString());
                dismissDialog();
            }
        });

        this.addToRequestQueue(strReq, tag_language_Req);
    }

    private void logging(String tag, String message) {
        if (debugOn)
            Log.d(tag, message);
    }

    private void createLoadingDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }
    }

    private void toast(String text) {
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

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            toast(activity.getString(R.string.error_no_internet_connection));
            return false;
        }

        return true;
    }
}
