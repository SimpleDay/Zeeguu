package pascalgiehl_unibe.zeeguu.Sliding_menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pascalgiehl_unibe.zeeguu.R;
import pascalgiehl_unibe.zeeguu.Search_Fragments.Fragment_Text;
import pascalgiehl_unibe.zeeguu.Wordlist_Fragments.Fragment_Wordlist;

public class SlidingFragment extends Fragment {
    /**
     * This class represents a tab for the sliding menu
     */
    static class PagerItemTab {
        private final CharSequence title;
        private final int indicatorColor;
        private final int dividerColor;
        private final Fragment fragment;

        PagerItemTab(CharSequence title, int indicatorColor, int dividerColor, Fragment fragment) {
            this.title = title;
            this.indicatorColor = indicatorColor; //possible to give every indicator a separate color
            this.dividerColor = dividerColor;
            this.fragment = fragment;
        }

        /**
         * @return the fragment which the tab represents
         */
        Fragment getFragment() {
            return fragment;
        }

        /**
         * @return title of the tab
         */
        CharSequence getTitle() {
            return title;
        }

        /**
         * @return color of indicator
         */
        int getIndicatorColor() {
            return indicatorColor;
        }

        /**
         * @return color of divider
         */
        int getDividerColor() {
            return dividerColor;
        }
    }


    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private List<PagerItemTab> tabs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Add tabs to the sliding menu
         */
        tabs.add(new PagerItemTab(
                getString(R.string.search_menu), // Title
                getResources().getColor(R.color.sliding_menu_line), // Indicator color
                getResources().getColor(R.color.sliding_menu_divider), // Divider color
                new Fragment_Text() //fragment which the tab represents
        ));

        tabs.add(new PagerItemTab(
                getString(R.string.wordlist_menu), // Title
                getResources().getColor(R.color.sliding_menu_line), // Indicator color
                getResources().getColor(R.color.sliding_menu_divider), // Divider color
                new Fragment_Wordlist() //fragment which the tab represents
        ));


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_menu, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new ZeeguuFragmentPagerAdapter(getChildFragmentManager()));

        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

        //to customize the indicator and divider
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return tabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return tabs.get(position).getDividerColor();
            }

        });
    }


    class ZeeguuFragmentPagerAdapter extends FragmentPagerAdapter {

        ZeeguuFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * gets the fragment of the tab at position i
         * @param i is the position of tab
         * @return fragment for tab i
         */
        @Override
        public Fragment getItem(int i) {
            return tabs.get(i).getFragment();
        }

        /**
         * @return the number of all tabs
         */
        @Override
        public int getCount() {
            return tabs.size();
        }

        /**
         * gets the title of the page
         * @param position of tab
         * @return the title of the tab
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position).getTitle();
        }

    }

}