package ch.unibe.scg.zeeguuu.Core;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Fragment that keeps all data that needs to be restored when the phone is rotated
 */
public class DataFragment extends Fragment {
    // Stored data
    private ZeeguuConnectionManager connectionManager;

    // This method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment
        setRetainInstance(true);
    }

    public void onRestore(Activity activity) {
        connectionManager.onRestore(activity);
    }

    // Getters and Setters
    public ZeeguuConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ZeeguuConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
