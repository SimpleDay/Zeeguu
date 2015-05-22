package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.SlidingTab.SlidingTabLayout;

import ch.unibe.scg.zeeguuu.R;

public class SlidingFragment extends Fragment {
    private Activity activity;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ZeeguuFragmentPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        adapter = new ZeeguuFragmentPagerAdapter(activity, getFragmentManager(), this);
        viewPager.setAdapter(adapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCallback(activity);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }
}