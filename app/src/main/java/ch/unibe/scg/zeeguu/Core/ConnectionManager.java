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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.Volley.NoConnectionError;
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

import ch.unibe.scg.zeeguu.Data.DialogBuilder;
import ch.unibe.scg.zeeguu.Data.User;
import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Search_Fragments.FragmentText;
import ch.unibe.scg.zeeguu.MyWords_Fragments.FragmentMyWords;
import ch.unibe.scg.zeeguu.MyWords_Fragments.MyWordsHeader;
import ch.unibe.scg.zeeguu.MyWords_Fragments.MyWordsInfoHeader;
import ch.unibe.scg.zeeguu.MyWords_Fragments.MyWordsItem;

public class ConnectionManager {

    //local variables
    private final String API_URL = "https://www.zeeguu.unibe.ch/";

    private RequestQueue mRequestQueue;
    private ProgressDialog pDialog;

    //TAGS
    private static final boolean debugOn = true;
    private static final String tagZeeguuRequest = "tag_zeeguu_request";
    private static final String TAG = "tag_logging";

    private User user;
    private DialogBuilder dialogBuilder;
    private static ConnectionManager instance;
    private static ZeeguuActivity activity;


    private FragmentMyWords.MyWordsListener myWordsListener;


    public ConnectionManager(ZeeguuActivity activity) {
        super();

        //initialise all variables
        this.activity = activity;
        this.user = new User(activity, this);
        this.dialogBuilder = new DialogBuilder(activity, user, this);
        this.instance = this;

        //get the information that is missing from start point
        if (!user.userHasLoginInfo())
            dialogBuilder.getLoginInformation("");
        else if (!user.userHasSessionId())
            getSessionIdFromServer(user.getEmail(), user.getPw());
        else if (user.getLanguageTo().equals("") || user.getLanguageFrom().equals("")) {
            getUserLanguagesFromServer();
            getMyWordsFromServer();
        } else {
            getMyWordsFromServer();
        }
    }

    public static ConnectionManager getConnectionManager(ZeeguuActivity activity) {
        if (instance == null && activity != null)
            return new ConnectionManager(activity);

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

    public void cancelAllPendingRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tagZeeguuRequest);
        }
    }

    public boolean loggedIn() {
        return user.userHasSessionId();
    }

    public void showLoginScreen() {
        dialogBuilder.getLoginInformation("");
    }

    public void logout() {
        user.logoutUser();
    }

    public void refreshMyWords() {
        getMyWordsFromServer();
    }

    public void notifyMyWordsChange() {
        if (myWordsListener != null)
            myWordsListener.notifyDataSetChanged();
    }

    public void removeBookmark(long bookmarkID) {
        if (myWordsListener != null && user.deleteWord(bookmarkID) != null) {
            myWordsListener.notifyDataSetChanged();
            removeBookmarkFromServer(bookmarkID);
        } else {
            toast(activity.getString(R.string.error_bookmark_delete));
        }
    }

    public void switchLanguages() {
        user.switchLanguages();
    }

    //// Checking if a word we are searching is already in the MyWords of the user ////

    public MyWordsItem checkMyWordsForTranslation(String input, String inputLanguage, String outputLanguage) {
        return user.checkMyWordsForTranslation(input, inputLanguage, outputLanguage);
    }


    //Getter und setter

    public String getEmail() {
        return user.getEmail();
    }

    public ArrayList<MyWordsHeader> getMyWords() {
        return user.getMyWords();
    }

    public String getLanguageFrom() {
        return user.getLanguageFrom();
    }

    public boolean setLanguageFrom(String languageFromKey, boolean switchFlagsIfNeeded) {
        boolean languageChanged = user.setLanguageFrom(languageFromKey);
        activity.refreshLanguages(switchFlagsIfNeeded);
        return languageChanged;
    }

    public String getLanguageTo() {
        return user.getLanguageTo();
    }

    public boolean setLanguageTo(String languageToKey, boolean switchFlagsIfNeeded) {
        boolean languageChanged = user.setLanguageTo(languageToKey);
        activity.refreshLanguages(switchFlagsIfNeeded);
        return languageChanged;
    }

    public void setMyWordsListener(FragmentMyWords.MyWordsListener listener) {
        myWordsListener = listener;
    }


    //// public requests ////

    public void createAccountOnServer(final String username, final String email, final String pw) {
        String url_create_account = API_URL + "add_user/" + email;
        logging(url_create_account);

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
                checkErrorCode(error);
                dialogBuilder.createNewAccount(email, username);
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

        this.addToRequestQueue(strReq, tagZeeguuRequest);

    }

    public void getSessionIdFromServer(final String email, final String pw) {
        if (!user.userHasLoginInfo(email, pw)) {
            user.logoutUser();
            toast(activity.getString(R.string.error_user_login_wrong));
            return;
        } else if (!isNetworkAvailable()) {
            return;
        }

        String url_session_ID = API_URL + "session/" + email;
        logging(url_session_ID);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_session_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                user.setEmail(email);
                user.setPw(pw);

                getSessionIDOutOfResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast(activity.getString(R.string.error_user_login_wrong));
                logging(error.getMessage());
                dismissDialog();

                //reset user until relogged again successfully
                user.logoutUser();
                dialogBuilder.getLoginInformation(email);
                activity.showLoginButtonIfNotLoggedIn();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", pw);
                return params;
            }
        };

        this.addToRequestQueue(strReq, tagZeeguuRequest);
    }

    public void getTranslation(@NonNull final String input, String inputLanguage, String outputLanguage, final FragmentText fragmentText) {
        if (inputLanguage.equals(outputLanguage)) {
            toast(activity.getString(R.string.error_same_language));
            return;
        } else if (input.equals("") || !isNetworkAvailable()) {
            return;
        }

        String url_translation = API_URL + "translate/" + inputLanguage + "/" + outputLanguage + "?session=" + user.getSession_id();
        logging(url_translation + ", POST word Variable: \"" + input + "\"");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_translation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.setTranslatedText(response.toString());
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                checkErrorCode(error);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // parse string for Zeeguu API //

                Map<String, String> params = new HashMap<>();
                params.put("word", Uri.encode(input));
                params.put("url", Uri.encode(activity.getString(R.string.bookmark_url_code)));
                params.put("context", Uri.encode(""));
                return params;
            }

        };

        this.addToRequestQueue(strReq, tagZeeguuRequest);
    }

    public void bookmarkWordOnServer(String inputWord, String inputLangauge, String translation, String translationLanguage, final FragmentText fragmentText) {
        if (!user.userHasLoginInfo() || inputWord.equals("") || translation.equals("") || !isNetworkAvailable())
            return;

        //parse string to URL
        inputWord = Uri.encode(inputWord);
        translation = Uri.encode(translation);

        String urlBookmark = API_URL + "contribute_with_context/" + inputLangauge + "/" + inputWord + "/" +
                translationLanguage + "/" + translation + "?session=" + user.getSession_id();
        logging(urlBookmark);

        createLoadingDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                urlBookmark, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                fragmentText.markEntriesAsBookmarked();
                logging(activity.getString(R.string.successful_bookmarked) + ", " + response);
                toast(activity.getString(R.string.successful_bookmarked));
                getMyWordsFromServer(); //TODO: Not always get the whole list, just add word locally
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                checkErrorCode(error);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", activity.getString(R.string.bookmark_title));
                params.put("url", activity.getString(R.string.bookmark_url_code));
                params.put("context", "");

                return params;
            }
        };

        this.addToRequestQueue(strReq, tagZeeguuRequest);
    }


    public boolean firstLogin() {
        return user.isFirstLogin();
    }


    //// private requests and methods ////

    private void getSessionIDOutOfResponse(String response) {
        user.setSession_id(response.toString());
        user.saveUserInformationLocally();

        toast(activity.getString(R.string.successful_login));
        logging(user.getSession_id());
        dismissDialog();

        activity.showLoginButtonIfNotLoggedIn();
        getMyWordsFromServer();
        getUserLanguagesFromServer();
    }

    private void getMyWordsFromServer() {
        if (!user.userHasSessionId() || !isNetworkAvailable()) {
            if (myWordsListener != null)
                myWordsListener.notifyDataSetChanged();
            return;
        }

        String url_session_ID = API_URL + "contribs_by_day/with_context?session=" + user.getSession_id();
        logging(url_session_ID);

        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                logging(response.toString());
                ArrayList<MyWordsHeader> myWords = user.getMyWords();
                myWords.clear();

                //ToDo: optimization that not everytime the whole list is sent
                try {
                    for (int j = 0; j < response.length(); j++) {
                        JSONObject dates = response.getJSONObject(j);
                        MyWordsHeader header = new MyWordsHeader(dates.getString("date"));
                        myWords.add(header);
                        JSONArray bookmarks = dates.getJSONArray("contribs");
                        String title = "";

                        for (int i = 0; i < bookmarks.length(); i++) {
                            JSONObject translation = bookmarks.getJSONObject(i);
                            //add title when a new one is
                            if (!title.equals(translation.getString("title"))) {
                                title = translation.getString("title");
                                header.addChild(new MyWordsInfoHeader(title));
                            }
                            //add word as entry to list
                            int id = translation.getInt("id");
                            String languageFromWord = translation.getString("from");
                            String languageFrom = translation.getString("from_language");
                            String languageToWord = translation.getString("to");
                            String languageTo = translation.getString("to_language");
                            String context = translation.getString("context");

                            header.addChild(new MyWordsItem(id, languageFromWord, languageToWord, context, languageFrom, languageTo));
                        }
                    }

                    user.setMyWords(myWords);

                    toast(activity.getString(R.string.successful_mywords_updated));
                } catch (JSONException error) {
                    logging(error.toString());
                }

                dismissDialog();
                if (myWordsListener != null) {
                    myWordsListener.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkErrorCode(error);
            }
        });


        this.addToRequestQueue(request, tagZeeguuRequest);
    }

    private void removeBookmarkFromServer(long bookmarkID) {
        if (!user.userHasLoginInfo() || !isNetworkAvailable())
            return;

        String urlRemoveBookmark = API_URL + "delete_contribution/" + bookmarkID + "?session=" + user.getSession_id();
        logging(urlRemoveBookmark);

        createLoadingDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                urlRemoveBookmark, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                String answer = response.toString();
                if (answer.equals("OK")) {
                    toast(activity.getString(R.string.successful_bookmark_deleted));
                    logging(activity.getString(R.string.successful_bookmark_deleted));
                } else {
                    toast(activity.getString(R.string.error_bookmark_delete));
                    logging(activity.getString(R.string.error_bookmark_delete));
                }
                dismissDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                checkErrorCode(error);
            }

        });

        this.addToRequestQueue(strReq, tagZeeguuRequest);
    }

    private void getUserLanguagesFromServer() {
        if (!user.userHasSessionId() || !isNetworkAvailable())
            return;

        String url_language = API_URL + "/learned_and_native_language?session=" + user.getSession_id();

        createLoadingDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_language, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!user.setLanguageFrom(response.getString("native")) ||
                                    !user.setLanguageTo(response.getString("learned"))) {
                                user.setDefaultLanguages();
                            }
                            user.saveUserLanguagesLocally();
                            logging("Language from: " + user.getLanguageFrom() + ", language to: " + user.getLanguageTo());

                            activity.refreshLanguages(true);

                        } catch (JSONException error) {
                            logging("JSON could not been parsed in the getBothLanguageFromServer method");
                        }
                        dismissDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        checkErrorCode(error);

                    }

                });

        this.addToRequestQueue(request, tagZeeguuRequest);
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
                        logging(response.toString());

                        //Save language
                        if (response.toString().equals("OK")) {
                            if (urlTag.equals(activity.getString(R.string.preference_language_to)))
                                toast(activity.getString(R.string.successful_language_to_changed) + " " + language_key);

                            else
                                toast(activity.getString(R.string.successful_language_from_changed) + " " + language_key);
                        } else
                            toast(activity.getString(R.string.error_change_language_from_on_server_not_possible));

                        dismissDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                checkErrorCode(error);
            }
        });

        this.addToRequestQueue(strReq, tagZeeguuRequest);
    }

    private void checkErrorCode(VolleyError error) {
        if (error instanceof NoConnectionError)
            toast(activity.getString(R.string.error_connection_error));
        else
            toast(activity.getString(R.string.error_unknown_error));

        logging(error.toString());
        dismissDialog();

    }

    private void logging(String message) {
        logging(TAG, message);
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
            //reload MyWords when empty
            if (user.isMyWordsEmpty())
                user.loadMyWordsLocally();
            return false;
        }

        return true;
    }
}
