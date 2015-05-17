package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.unibe.scg.zeeguuu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguuu.MyWords_Fragments.FragmentMyWords;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Search_Fragments.FragmentSearch;

/**
 * Zeeguu Application
 * Created by Pascal on 25/01/15.
 */
public class ZeeguuFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<PagerFragmentTab> tabs;

    public interface ZeeguuSlidingFragmentInterface {

        FragmentSearch getFragmentSearch();

        FragmentMyWords getFragmentMyWords();
    }

    ZeeguuFragmentPagerAdapter(Activity activity, FragmentManager fm, Fragment fragment) {
        super(fm);

        ZeeguuSlidingFragmentInterface callbacks = (ZeeguuSlidingFragmentInterface) activity;
        /**
         * Add tabs to the sliding menu
         */
        tabs = new ArrayList<>();
        tabs.add(new PagerFragmentTab(
                fragment.getString(R.string.search_menu), // Title
                fragment.getResources().getColor(R.color.sliding_menu_line), // Indicator color
                fragment.getResources().getColor(R.color.sliding_menu_divider), // Divider color
                callbacks.getFragmentSearch() //fragment which the tab represents
        ));

        tabs.add(new PagerFragmentTab(
                fragment.getString(R.string.mywords_menu), // Title
                fragment.getResources().getColor(R.color.sliding_menu_line), // Indicator color
                fragment.getResources().getColor(R.color.sliding_menu_divider), // Divider color
                callbacks.getFragmentMyWords() //fragment which the tab represents
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

}
