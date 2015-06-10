package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.SlidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import ch.unibe.scg.zeeguuu.Core.ZeeguuActivity;
import ch.unibe.scg.zeeguuu.Games.ExerciseFragment;
import ch.unibe.scg.zeeguuu.R;
import ch.unibe.scg.zeeguuu.Search_Fragments.SearchFragment;
import ch.unibe.zeeguulibrary.MyWords.MyWordsFragment;
import ch.unibe.zeeguulibrary.WebView.BrowserFragment;

/**
 * Fragment that creates the sliding menu so that the user can switch pretty fast between the single fragments
 */
public class SlidingTabFragment extends Fragment {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private SlidingFragmentCallback callback;
    private List<PagerFragmentTab> tabs = new ArrayList<>();
    private ZeeguuFragmentPagerAdapter adapter;
    private static int containerID;

    public interface SlidingFragmentCallback {
        void focusFragment(int number);

        SearchFragment getSearchFragment();

        MyWordsFragment getMyWordsFragment();

        ExerciseFragment getExerciseFragment();

        BrowserFragment getBrowserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the ViewPager and slidingtablayout
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (SlidingFragmentCallback) getActivity();

        tabs.add(new PagerFragmentTab(
                ZeeguuActivity.ITEMIDSEARCH,
                getString(R.string.search_menu),
                getResources().getColor(R.color.sliding_menu_line),
                callback.getSearchFragment()));

        //add webbrowser if version higher than kitkat (if lower, method will return null)
        if (callback.getBrowserFragment() != null)
            tabs.add(new PagerFragmentTab(
                    ZeeguuActivity.ITEMIDBROWSER,
                    "Browser",
                    getResources().getColor(R.color.sliding_menu_line),
                    callback.getBrowserFragment()
            ));


        tabs.add(new PagerFragmentTab(
                ZeeguuActivity.ITEMIDMYWORDS,
                getString(R.string.mywords_menu),
                getResources().getColor(R.color.sliding_menu_line),
                callback.getMyWordsFragment()));

        tabs.add(new PagerFragmentTab(
                ZeeguuActivity.ITEMIDEXERCISES,
                getString(R.string.exercise_menu),
                getResources().getColor(R.color.sliding_menu_line),
                callback.getExerciseFragment()));


        adapter = new ZeeguuFragmentPagerAdapter(getFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setOnPageChangeListener(new ZeeguuPageChangeListener());
        slidingTabLayout.setDistributeEvenly(true);

        //to customize the indicator and divider
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return adapter.get(position).getIndicatorColor();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (SlidingFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SlidingFragmentCallback");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        for(PagerFragmentTab p : tabs)
            p.getFragment().onPause();
    }

    public boolean isBrowserActive() {
        return tabs.get(viewPager.getCurrentItem()).getItemId() == ZeeguuActivity.ITEMIDBROWSER;
    }

    public void focusFragment(int number) {
        tabs.get(number).getFragment().onResume();

        if(number + 1 < tabs.size())
            tabs.get(number + 1).getFragment().onPause();
        if(number - 1 >= 0)
            tabs.get(number - 1).getFragment().onPause();
    }

    public static int getContainerID() {
        return containerID;
    }

    private class ZeeguuPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (callback != null)
                callback.focusFragment(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }


    /**
     * Adapter that handles all fragments which are in the sliding menu
     */
    private class ZeeguuFragmentPagerAdapter extends FragmentPagerAdapter {

        ZeeguuFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * gets the fragment of the tab at position i
         */
        @Override
        public Fragment getItem(int i) {
            return tabs.get(i).getFragment();
        }

        @Override
        public long getItemId(int position) {
            return tabs.get(position).getItemId();
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

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            containerID = container.getId();
            return super.instantiateItem(container, position);
        }
    }

}