package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Fragment;

/**
 * Zeeguu Application
 * Created by Pascal on 25/01/15.
 */
public class PagerFragmentTab {

    /**
     * This class represents a tab for the sliding menu
     */
    private final CharSequence title;
    private final int indicatorColor;
    private final Fragment fragment;
    private long itemId;

    public PagerFragmentTab(long itemId,CharSequence title, int indicatorColor, Fragment fragment) {
        this.itemId = itemId;
        this.title = title;
        this.indicatorColor = indicatorColor; //possible to give every indicator a separate color
        this.fragment = fragment;
    }

    /**
     * @return the fragment which the tab represents
     */
    public Fragment getFragment() {
        return fragment;
    }

    /**
     * @return title of the tab
     */
    public CharSequence getTitle() {
        return title;
    }

    /**
     * @return color of indicator
     */
    public int getIndicatorColor() {
        return indicatorColor;
    }

    public long getItemId() {
        return itemId;
    }
}

