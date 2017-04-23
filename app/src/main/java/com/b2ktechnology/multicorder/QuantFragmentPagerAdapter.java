package com.b2ktechnology.multicorder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Iggy on 11/18/2016.
 */

public class QuantFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    int tab = 0;
    private String tabTitles[] = new String[] { "Correlation", "Functional", "Statistical" };
    private Context context;

    public QuantFragmentPagerAdapter(FragmentManager fm, SensorDisplay sensorDisplay, int t) {
        super(fm);
        tab = t;
    }

    @Override
    public Fragment getItem(int position) {
        return QuantFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
