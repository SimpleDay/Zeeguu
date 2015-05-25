package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.SlidingTab.SlidingTabLayout;

import ch.unibe.scg.zeeguuu.R;

/**
 * Fragment that creates the sliding menu so that the user can switch pretty fast between the single fragments
 */
public class SlidingFragment extends Fragment {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ZeeguuFragmentPagerAdapter adapter;

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

        adapter = new ZeeguuFragmentPagerAdapter(getActivity(), getFragmentManager(), this);
        viewPager.setAdapter(adapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setDistributeEvenly(true);

        //to customize the indicator and divider
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return adapter.get(position).getIndicatorColor();
            }
        });
    }
}