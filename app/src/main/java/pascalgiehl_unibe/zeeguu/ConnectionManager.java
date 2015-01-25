package pascalgiehl_unibe.zeeguu;

/**
 * Zeeguu Application
 * Created by Pascal on 19/01/15.
 */

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.Volley.Request;
import com.google.Volley.RequestQueue;
import com.google.Volley.Response;
import com.google.Volley.VolleyError;
import com.google.Volley.toolbox.JsonArrayRequest;
import com.google.Volley.toolbox.StringRequest;
import com.google.Volley.toolbox.Volley;
import pascalgiehl_unibe.zeeguu.Wordlist_Fragments.Header;
import pascalgiehl_unibe.zeeguu.Wordlist_Fragments.Item;
import pascalgiehl_unibe.zeeguu.Wordlist_Fragments.TranslatedWord;

public class ConnectionManager extends Application {

    //local variables
    private final String url = "https://www.zeeguu.unibe.ch/";

    private RequestQueue mRequestQueue;
    private ProgressDialog pDialog;
    private SharedPreferences settings;

    //TAGS
    private static final String tag_wordlist_Req = "tag_wordlist_id";
    private static final String tag_SessionID_Req = "tag_session_id";
    private static final String tag_language_Req = "tag_language_id";
    private static final String TAG = "tag_logging";

    //user login information
    private String email;
    private String pw;
    private String session_id;

    private static ConnectionManager instance;
    private static Activity activity;

    private static ArrayList<Item> tmpList;

    public ConnectionManager(Activity activity) {
        super();
        this.activity = activity;

        //try to get the login information
        settings = activity.getSharedPreferences("UserInfo", 0);
        email = settings.getString("Username", "").toString();
        pw = settings.getString("Password", "").toString();
        session_id = settings.getString("Session_ID", "").toString();

        //ToDo: Delete after debugging
        //email = "p.giehl@gmx.ch";
        //pw = "Micky";
        //session_id = "1467847111";

        instance = this;
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

    public void getSessionID() {
        String url_session_ID = url + "session/" + email;

        if (!userHasLoginInfo())
            return;

        if(pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_session_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                //Save session ID
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Session_ID", response.toString());
                editor.commit();
                pDialog.dismiss();

                getUserLanguage(); //ask for the language when session ID arrived

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
                pDialog.dismiss();
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

    public void getAllWords(ArrayList<Item> list) {

        if (!userHasSessionId())
            return;

        String url_session_ID = url + "contribs_by_day/with_context?session=" + session_id;
        tmpList = list;

        if(pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }

        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());

                try {
                    for (int j = 0; j < response.length(); j++) {
                        JSONObject dates = response.getJSONObject(j);
                        tmpList.add(new Header(dates.getString("date")));
                        JSONArray contribs = dates.getJSONArray("contribs");
                        for(int i = 0; i < contribs.length(); i++) {
                            JSONObject translation = contribs.getJSONObject(i);
                            String nativeWord = translation.getString("from");
                            String translatedWord = translation.getString("to");
                            String context = translation.getString("context");
                            tmpList.add(new TranslatedWord(nativeWord, translatedWord, context));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                pDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        });


        this.addToRequestQueue(request, tag_wordlist_Req);
    }

    public void getUserLanguage(){
        String url_learned_language = url + "learned_language?session=" + session_id;

        if (!userHasSessionId())
            return;

        if(pDialog == null) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_learned_language, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                //Save language
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("zeeguu_language", response.toString());
                editor.commit();
                pDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
                pDialog.dismiss();
            }
        });

        this.addToRequestQueue(strReq, tag_language_Req);
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
