package ch.unibe.scg.zeeguuu.Games;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;

import ch.unibe.scg.zeeguuu.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Fragment to display the zeeguu games webview
 */
public class FragmentWebGames extends Fragment {
    private ZeeguuFragmentWebGamesCallback callback;

    public interface ZeeguuFragmentWebGamesCallback {
        ZeeguuConnectionManager getConnectionManager();
    }


    private WebView mWebView;

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The system calls this when it's time for the fragment to draw its user interface for the
     * first time. To draw a UI for your fragment, you must return a View from this method that
     * is the root of your fragment's layout. You can return null if the fragment does not
     * provide a UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_web_view, container, false);
        mWebView = (WebView) mainView.findViewById(R.id.main_webview);
        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());

        //login to the website
        String postData = "email=p.giehl@gmx.ch&password=Micky&login=1";
        mWebView.postUrl("https://www.zeeguu.unibe.ch/login", EncodingUtils.getBytes(postData, "BASE64"));

        //as soon as logged in, always open recognize tab - allow no other
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.equals("https://www.zeeguu.unibe.ch/login"))
                    view.loadUrl("https://www.zeeguu.unibe.ch/m_recognize");

                // return true; //Indicates WebView to NOT load the url;
                return false; //Allow WebView to load url
            }
        });

        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuFragmentWebGamesCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuFragmentWebGamesCallback");
        }
    }

    /**
     * The system calls this method as the first indication that the user is leaving the fragment
     * (though it does not always mean the fragment is being destroyed). This is usually where you
     * should commit any changes that should be persisted beyond the current user session
     * (because the user might not come back).
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    public boolean onBackPressed() {
        if (mWebView == null)
            return true;
        else if (mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        } else
            return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void getSelection() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT)
            mWebView.evaluateJavascript("window.getSelection().toString()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Toast.makeText(getActivity(), value.substring(1, value.length() - 1), Toast.LENGTH_SHORT).show();
                }
            });
        else
            Toast.makeText(getActivity(), "Not supported on your android version", Toast.LENGTH_SHORT).show();
    }
}
