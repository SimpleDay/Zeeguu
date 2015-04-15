package ch.unibe.scg.zeeguu.Core;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

import android.app.AlertDialog;
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
import com.google.Volley.toolbox.JsonObjectRequest;
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
import ch.unibe.scg.zeeguu.Wordlist_Fragments.FragmentWordlist;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistHeader;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistInfoHeader;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.WordlistItem;

public class ConnectionManager  {

    //local variables
    private final String API_URL = "https://www.zeeguu.unibe.ch/";

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

    private static ArrayList<WordlistHeader> wordlist;
    private static ArrayList<WordlistItem> wordlistItems; //used to make local search, not nice, is a small hack at the moment //TODO: remove

    private FragmentWordlist.WordlistListener wordlistListener;

    public ConnectionManager(ZeeguuActivity activity) {
        super();

        //initialise all variables
        this.activity = activity;
        this.wordlist = new ArrayList<>();
        this.wordlistItems = new ArrayList<>();
        this.instance = this;

        //try to get the users information
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        loadAllUserInformation();

        //get the information that is missing from start point
        if (!userHasLoginInfo())
            getLoginInformation();
        else if (!userHasSessionId())
            getSessionIdFromServer();
        else {
            getBothUserLanguageFromServer();
            getAllWordsFromServer();
        }
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

        String url_translation = API_URL + "translate_from_to/" + input + "/" +
                inputLanguage + "/" + outputLanguage + "?session=" + session_id;
        logging(TAG, url_translation);

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url_translation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.setTranslatedText(response.toString());
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

    public void contributeToServer(String input, String inputLangauge, String translation, String translationLanguage, final FragmentText fragmentText) {
        if (!userHasLoginInfo() || input.equals("") || translation.equals("") || !isNetworkAvailable())
            return;

        //parse string to URL
        input = Uri.encode(input);
        translation = Uri.encode(translation);

        String urlContribution = API_URL + "contribute_with_context/" + inputLangauge + "/" + input + "/" +
                translationLanguage + "/" + translation + "?session=" + session_id;
        logging(TAG, urlContribution);

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.activateContribution();
                logging(TAG, "successful contributed: " + response);
                toast(activity.getString(R.string.successful_contribution));
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
                params.put("title", activity.getString(R.string.contribution_title));
                params.put("url", activity.getString(R.string.contribution_url_code));
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

    public static ArrayList<WordlistHeader> getWordlist() {
        return wordlist;
    }

    public static ArrayList<WordlistItem> getWordlistItems() {
        return wordlistItems;
    }

    public String getNativeLanguage() {
        return native_language;
    }

    public void setNativeLanguage(String native_language_key, boolean switchFlagsIfNeeded) {
        native_language = native_language_key;
        activity.refreshLanguages(switchFlagsIfNeeded);
        setUserLanguageOnServer(activity.getString(R.string.preference_native_language), native_language_key);
    }

    public String getLearningLanguage() {
        return learning_language;
    }

    public void setLearningLanguage(String learning_language_key, boolean switchFlagsIfNeeded) {
        learning_language = learning_language_key;
        activity.refreshLanguages(switchFlagsIfNeeded);
        setUserLanguageOnServer(activity.getString(R.string.preference_learning_language), learning_language_key);
    }

    public void refreshWordlist() {
        getAllWordsFromServer();
    }

    public void setWordlistListener(FragmentWordlist.WordlistListener listener) {
        wordlistListener = listener;
    }

    public void deleteContribution(long ContributionId) {
        deleteContributionFromServer(ContributionId);
        getAllWordsFromServer(); //Get new wordlist TODO: delete word local
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

        String url_session_ID = API_URL + "session/" + email;

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
                getBothUserLanguageFromServer();
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

        //show that wordlist is refreshing
        if (wordlistListener != null)
            wordlistListener.startRefreshingAction();

        String url_session_ID = API_URL + "contribs_by_day/with_context?session=" + session_id;
        logging(TAG, url_session_ID);

        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                logging(TAG, response.toString());
                wordlist.clear();
                wordlistItems.clear();

                //ToDo: optimization that not everytime the whole list is sent
                try {
                    for (int j = 0; j < response.length(); j++) {
                        JSONObject dates = response.getJSONObject(j);
                        WordlistHeader header = new WordlistHeader(dates.getString("date"));
                        wordlist.add(header);
                        JSONArray contribs = dates.getJSONArray("contribs");
                        String title = "";

                        for (int i = 0; i < contribs.length(); i++) {
                            JSONObject translation = contribs.getJSONObject(i);
                            //add title when a new one is
                            if (!title.equals(translation.getString("title"))) {
                                title = translation.getString("title");
                                header.addChild(new WordlistInfoHeader(title));
                            }
                            //add word as entry to list
                            int id = translation.getInt("id");
                            String nativeWord = translation.getString("from");
                            String fromLanguage = translation.getString("from_language");
                            String translatedWord = translation.getString("to");
                            String toLanguage = translation.getString("to_language");
                            String context = translation.getString("context");

                            header.addChild(new WordlistItem(id, nativeWord, translatedWord, context, fromLanguage, toLanguage));
                            wordlistItems.add(new WordlistItem(id, nativeWord, translatedWord, context, fromLanguage, toLanguage));
                        }
                    }

                    toast(activity.getString(R.string.successful_wordlist_updated));
                    if (wordlistListener != null)
                        wordlistListener.notifyDataSetChanged();

                } catch (JSONException error) {
                    logging(TAG, error.toString());
                }

                dismissDialog();
                if (wordlistListener != null)
                    wordlistListener.stopRefreshingAction();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_server_not_online));
                logging(TAG, error.toString());
                dismissDialog();

                if (wordlistListener != null)
                    wordlistListener.stopRefreshingAction();
            }
        });


        this.addToRequestQueue(request, tag_wordlist_Req);
    }

    private void deleteContributionFromServer(long ContributionId) {
        if (!userHasLoginInfo() || !isNetworkAvailable())
            return;

        String url_delete_contribution = API_URL + "delete_contribution/" + ContributionId + "?session=" + session_id;
        logging(TAG, url_delete_contribution);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_delete_contribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                String answer = response.toString();
                if(answer.equals("OK")) {
                    toast(activity.getString(R.string.successful_contribution_deleted));
                    logging(TAG, activity.getString(R.string.successful_contribution_deleted));
                } else {
                    toast(activity.getString(R.string.error_contribution_delete));
                    logging(TAG, activity.getString(R.string.error_contribution_delete));
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

        this.addToRequestQueue(strReq, tag_SessionID_Req);
    }

    private void getUserLanguageFromServer(final String urlTag) {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + urlTag + "?session=" + session_id;

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

                activity.refreshLanguages(true);
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

    private void getBothUserLanguageFromServer() {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + "/learned_and_native_language?session=" + session_id;

        createLoadingDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_language, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            native_language = response.getString("native");
                            learning_language = response.getString("learned");
                            logging(TAG, "native: " + response.getString("native") + ", learned: " + response.getString("learned"));
                            //Save language
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(activity.getString(R.string.preference_native_language), native_language);
                            editor.putString(activity.getString(R.string.preference_learning_language), learning_language);
                            editor.commit();

                            activity.refreshLanguages(true);

                        } catch (JSONException error) {
                            logging(TAG, error.toString());
                        }
                        dismissDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logging(TAG, error.toString());
                        dismissDialog();
                    }

                });

        this.addToRequestQueue(request, tag_language_Req);
    }

    private void setUserLanguageOnServer(final String urlTag, final String language_key) {
        if (!userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + urlTag + "/" + language_key + "?session=" + session_id;

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

    private void dismissDialog() {
        if (pDialog != null)
            pDialog.dismiss();
    }

    private void toast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
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
