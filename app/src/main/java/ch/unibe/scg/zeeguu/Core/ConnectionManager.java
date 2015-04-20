package ch.unibe.scg.zeeguu.Core;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
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

public class ConnectionManager {

    //local variables
    private final String API_URL = "https://www.zeeguu.unibe.ch/";

    private RequestQueue mRequestQueue;
    private ProgressDialog pDialog;

    //TAGS
    private static final boolean debugOn = true;
    private static final String tag_wordlist_Req = "tag_wordlist_id";
    private static final String tag_SessionID_Req = "tag_session_id";
    private static final String tag_language_Req = "tag_language_id";
    private static final String tag_translation_Req = "tag_translation_id";
    private static final String tag_contribute_Req = "tag_contrib_id";
    private static final String TAG = "tag_logging";

    private User user;
    private static ConnectionManager instance;
    private static ZeeguuActivity activity;


    private FragmentWordlist.WordlistListener wordlistListener;


    public ConnectionManager(ZeeguuActivity activity) {
        super();

        //initialise all variables
        this.activity = activity;
        this.user = new User(activity, this);

        this.instance = this;

        //try to get the users information
        user.loadAllUserInformationLocally();

        //get the information that is missing from start point
        if (!user.userHasLoginInfo())
            user.getLoginInformation();
        else if (!user.userHasSessionId())
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

    public boolean loggedIn() {
        return user.userHasSessionId();
    }

    public void showLoginScreen() {
        user.getLoginInformation();
    }

    public void logout() {
        user.logoutUser();
    }

    public void refreshWordlist() {
        getAllWordsFromServer();
    }

    public void notifyWordlistChange() {
        if (wordlistListener != null)
            wordlistListener.notifyDataSetChanged();
    }

    public void deleteContribution(long ContributionId) {
        deleteContributionFromServer(ContributionId);
        getAllWordsFromServer(); //Get new wordlist TODO: delete word local
    }

    public void contributeToServer(String input, String inputLangauge, String translation, String translationLanguage, final FragmentText fragmentText) {
        if (!user.userHasLoginInfo() || input.equals("") || translation.equals("") || !isNetworkAvailable())
            return;

        //parse string to URL
        input = Uri.encode(input);
        translation = Uri.encode(translation);

        String urlContribution = API_URL + "contribute_with_context/" + inputLangauge + "/" + input + "/" +
                translationLanguage + "/" + translation + "?session=" + user.getSession_id();
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

    public void createAccountOnServer(final String username, final String email, final String pw) {
        String url_create_account = API_URL + "add_user/" + email;
        logging(TAG, url_create_account);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_create_account, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                user.setEmail(email);
                user.setPw(pw);

                getSessionIDOutOfResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_account_not_created));
                logging(TAG, error.toString());
                dismissDialog();
                user.createNewAccount();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", pw);
                return params;
            }
        };

        this.addToRequestQueue(strReq, tag_SessionID_Req);

    }

    public void getSessionIdFromServer() {
        if (!user.userHasLoginInfo()) {
            user.logoutUser();
            toast(activity.getString(R.string.error_user_login_wrong));
            return;
        } else if (!isNetworkAvailable())
            return;

        String url_session_ID = API_URL + "session/" + user.getEmail();
        logging(TAG, url_session_ID);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_session_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                getSessionIDOutOfResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_user_login_wrong));
                logging(TAG, error.toString());
                dismissDialog();
                //reset user until relogged again successfully
                user.logoutUser();
                user.getLoginInformation();
                activity.updateMenuTitles();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", user.getPw());
                return params;
            }
        };

        this.addToRequestQueue(strReq, tag_SessionID_Req);
    }

    public void getTranslation(String input, String inputLanguage, String outputLanguage, final FragmentText fragmentText) {
        if (input.equals("") || input == null || !isNetworkAvailable())
            return;

        //parse string to URL
        input = Uri.encode(input);

        String url_translation = API_URL + "translate_from_to/" + input + "/" +
                inputLanguage + "/" + outputLanguage + "?session=" + user.getSession_id();
        logging(TAG, url_translation);

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
            }
        });

        this.addToRequestQueue(strReq, tag_translation_Req);
    }


    //Getter und setter

    public String getSessionId() {
        return user.getSession_id();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public ArrayList<WordlistHeader> getWordlist() {
        return user.getWordlist();
    }

    public ArrayList<WordlistItem> getWordlistItems() {
        return user.getWordlistItems();
    }

    public String getNativeLanguage() {
        return user.getNative_language();
    }

    public void setNativeLanguage(String native_language_key, boolean switchFlagsIfNeeded) {
        user.setNative_language(native_language_key);
        activity.refreshLanguages(switchFlagsIfNeeded);
        setUserLanguageOnServer(activity.getString(R.string.preference_native_language), native_language_key);
    }

    public String getLearningLanguage() {
        return user.getLearning_language();
    }

    public void setLearningLanguage(String learning_language_key, boolean switchFlagsIfNeeded) {
        user.setLearning_language(learning_language_key);
        activity.refreshLanguages(switchFlagsIfNeeded);
        setUserLanguageOnServer(activity.getString(R.string.preference_learning_language), learning_language_key);
    }

    public void setWordlistListener(FragmentWordlist.WordlistListener listener) {
        wordlistListener = listener;
    }


    //// private methods ////

    private void getSessionIDOutOfResponse(String response) {
        user.setSession_id(response.toString());
        user.saveUserInformationLocally();

        toast(activity.getString(R.string.successful_login));
        logging(TAG, user.getSession_id());
        dismissDialog();

        activity.updateMenuTitles();
        getAllWordsFromServer();
        getBothUserLanguageFromServer();
    }

    private void getAllWordsFromServer() {
        if (!user.userHasSessionId() || !isNetworkAvailable())
            return;

        //show that wordlist is refreshing
        if (wordlistListener != null)
            wordlistListener.startRefreshingAction();

        String url_session_ID = API_URL + "contribs_by_day/with_context?session=" + user.getSession_id();
        logging(TAG, url_session_ID);

        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                logging(TAG, response.toString());
                ArrayList<WordlistHeader> wordlist = user.getWordlist();
                ArrayList<WordlistItem> wordlistItems = user.getWordlistItems();
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

                    user.setWordlist(wordlist);
                    user.setWordlistItems(wordlistItems);

                    toast(activity.getString(R.string.successful_wordlist_updated));
                } catch (JSONException error) {
                    logging(TAG, error.toString());
                }

                dismissDialog();
                if (wordlistListener != null) {
                    wordlistListener.notifyDataSetChanged();
                    wordlistListener.stopRefreshingAction();
                }
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
        if (!user.userHasLoginInfo() || !isNetworkAvailable())
            return;

        String url_delete_contribution = API_URL + "delete_contribution/" + ContributionId + "?session=" + user.getSession_id();
        logging(TAG, url_delete_contribution);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_delete_contribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                String answer = response.toString();
                if (answer.equals("OK")) {
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

    private void getBothUserLanguageFromServer() {
        if (!user.userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + "/learned_and_native_language?session=" + user.getSession_id();

        createLoadingDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_language, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            user.setNative_language(response.getString("native"));
                            user.setLearning_language(response.getString("learned"));
                            user.saveUserLanguagesLocally();
                            logging(TAG, "native: " + response.getString("native") + ", learned: " + response.getString("learned"));


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
        if (!user.userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + urlTag + "/" + language_key + "?session=" + user.getSession_id();

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
