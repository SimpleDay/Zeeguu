package pascalgiehl_unibe.zeeguu.Sliding_menu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import pascalgiehl_unibe.zeeguu.Search_Fragments.Fragment_Text;
import pascalgiehl_unibe.zeeguu.Sliding_menu_items.Fragment_Wordlist;

/**
 * Created by Pascal on 16/01/15.
 */
public class ZeeguuPagerAdapter extends FragmentPagerAdapter {
    String tabs[] = {"Search", "Wordlist"};

    public ZeeguuPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new Fragment_Text();
        } else {
            return new Fragment_Wordlist();
        }

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
