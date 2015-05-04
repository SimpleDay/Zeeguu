package ch.unibe.scg.zeeguu.Sliding_menu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.unibe.scg.zeeguu.R;
import ch.unibe.scg.zeeguu.Search_Fragments.FragmentText;
import ch.unibe.scg.zeeguu.Wordlist_Fragments.FragmentWordlist;
import ch.unibe.scg.zeeguu.Core.ZeeguuFragment;

/**
 * Zeeguu Application
 * Created by Pascal on 25/01/15.
 */
public class ZeeguuFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<PagerFragmentTab> tabs;

    ZeeguuFragmentPagerAdapter(FragmentManager fm, Fragment fragment) {
        super(fm);
        tabs = new ArrayList<>();

        /**
         * Add tabs to the sliding menu
         */
        tabs.add(new PagerFragmentTab(
                fragment.getString(R.string.search_menu), // Title
                fragment.getResources().getColor(R.color.sliding_menu_line), // Indicator color
                fragment.getResources().getColor(R.color.sliding_menu_divider), // Divider color
                new FragmentText() //fragment which the tab represents
        ));

        tabs.add(new PagerFragmentTab(
                fragment.getString(R.string.wordlist_menu), // Title
                fragment.getResources().getColor(R.color.sliding_menu_line), // Indicator color
                fragment.getResources().getColor(R.color.sliding_menu_divider), // Divider color
                new FragmentWordlist() //fragment which the tab represents
        ));
    }

    /**
     * gets the fragment of the tab at position i
     *
     * @param position of tab
     * @return fragment for tab i
     */
    @Override
    public ZeeguuFragment getItem(int position) {
        return tabs.get(position).getFragment();
    }

    /**
     * @return the number of all tabs
     */
    @Override
    public int getCount() {
        return tabs.size();
    }

    /**
     * @return PagerFragmentTab
     */
    public PagerFragmentTab get(int i) {
        return tabs.get(i);
    }

    /**
     * gets the title of the page
     *
     * @param position of tab
     * @return the title of the tab
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    public ArrayList<ZeeguuFragment> getAllFragments() {
        ArrayList<ZeeguuFragment> fragments = new ArrayList<>();
        for (PagerFragmentTab i : tabs)
            fragments.add(i.getFragment());

        return fragments;
    }

}
