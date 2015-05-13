package ch.unibe.scg.zeeguuu.Sliding_menu;

import ch.unibe.scg.zeeguuu.Core.ZeeguuFragment;

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
    private final int dividerColor;
    private final ZeeguuFragment fragment;

    public PagerFragmentTab(CharSequence title, int indicatorColor, int dividerColor, ZeeguuFragment fragment) {
        this.title = title;
        this.indicatorColor = indicatorColor; //possible to give every indicator a separate color
        this.dividerColor = dividerColor;
        this.fragment = fragment;
    }

    /**
     * @return the fragment which the tab represents
     */
    public ZeeguuFragment getFragment() {
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

    /**
     * @return color of divider
     */
    public int getDividerColor() {
        return dividerColor;
    }
}

