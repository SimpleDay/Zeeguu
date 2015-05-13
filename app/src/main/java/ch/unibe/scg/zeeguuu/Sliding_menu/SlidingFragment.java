package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.SlidingTab.SlidingTabLayout;

import ch.unibe.scg.zeeguuu.Core.ZeeguuFragment;
import ch.unibe.scg.zeeguuu.R;

public class SlidingFragment extends Fragment {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ZeeguuFragmentPagerAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ZeeguuFragmentPagerAdapter(getActivity(), getFragmentManager(), this);
    }

    public ZeeguuFragment getActiveFragment() {
        return adapter.get(viewPager.getCurrentItem()).getFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_menu, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

        //to customize the indicator and divider
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return adapter.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return adapter.get(position).getDividerColor();
            }

        });
    }

}