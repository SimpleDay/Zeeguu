package ch.unibe.scg.zeeguuu.Sliding_menu;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.SlidingTab.SlidingTabLayout;

import ch.unibe.scg.zeeguuu.R;

public class SlidingFragment extends Fragment {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ZeeguuFragmentPagerAdapter adapter;

    private View view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (adapter == null)
            adapter = new ZeeguuFragmentPagerAdapter(getActivity(), getFragmentManager(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            return view = inflater.inflate(R.layout.fragment_sliding_menu, container, false);
        else
            return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (viewPager != null && slidingTabLayout != null)
            return;

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