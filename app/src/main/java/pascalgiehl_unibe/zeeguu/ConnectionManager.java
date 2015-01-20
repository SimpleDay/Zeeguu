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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pascalgiehl_unibe.zeeguu.Volley.Request;
import pascalgiehl_unibe.zeeguu.Volley.RequestQueue;
import pascalgiehl_unibe.zeeguu.Volley.Response;
import pascalgiehl_unibe.zeeguu.Volley.VolleyError;
import pascalgiehl_unibe.zeeguu.Volley.VolleyLog;
import pascalgiehl_unibe.zeeguu.Volley.toolbox.JsonObjectRequest;
import pascalgiehl_unibe.zeeguu.Volley.toolbox.StringRequest;
import pascalgiehl_unibe.zeeguu.Volley.toolbox.Volley;

public class ConnectionManager extends Application {

    //local variables
    private final String url = "https://www.zeeguu.unibe.ch/";

    private RequestQueue mRequestQueue;
    private ProgressDialog pDialog;
    private SharedPreferences settings;

    //TAGS
    private static final String tag_wordlist_Req = "tag_wordlist_id";
    private static final String tag_SessionID_Req = "tag_session_id";
    private static final String TAG = "tag_logging";

    //user login information
    private String email;
    private String pw;
    private String session_id;

    private static ConnectionManager instance;
    private static Activity activity;

    public ConnectionManager(Activity activity) {
        super();
        this.activity = activity;

        //try to get the login information
        settings = activity.getSharedPreferences("UserInfo", 0);
        email = settings.getString("Username", "").toString();
        pw = settings.getString("Password", "").toString();
        session_id = settings.getString("Session_ID", "").toString();

        //ToDo: Delete after debugging
        email = "p.giehl@gmx.ch";
        pw = "Micky";
        session_id = "1467847111";

        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static ConnectionManager getConnectionManager(Activity activity) {
        if(instance == null)
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

        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url_session_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                //Save session ID
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Session_ID", response.toString());
                editor.commit();
                pDialog.hide();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
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

    public void getAllWords() {

        if (!userHasSessionId())
            return;

        String url_session_ID = url + "user_words?session=" + session_id;
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Loading...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url_session_ID, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                pDialog.hide();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
            }
        });

        this.addToRequestQueue(jsonObjReq, tag_wordlist_Req);
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
