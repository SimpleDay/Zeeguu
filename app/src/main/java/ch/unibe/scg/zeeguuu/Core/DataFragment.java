package ch.unibe.scg.zeeguuu.Core;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import ch.unibe.scg.zeeguuu.Search_Fragments.FragmentSearch;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.MyWords.FragmentMyWords;

/**
 * Fragment that keeps all data that needs to be restored when the phone is rotated
 */
public class DataFragment extends Fragment {
    // Stored data
    private ZeeguuConnectionManager connectionManager;
    private FragmentMyWords fragmentMyWords;
    private FragmentSearch fragmentSearch;

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

    public FragmentMyWords getFragmentMyWords() {
        return fragmentMyWords;
    }

    public void setFragmentMyWords(FragmentMyWords fragmentMyWords) {
        this.fragmentMyWords = fragmentMyWords;
    }

    public FragmentSearch getFragmentSearch() {
        return fragmentSearch;
    }

    public void setFragmentSearch(FragmentSearch fragmentSearch) {
        this.fragmentSearch = fragmentSearch;
    }
}
