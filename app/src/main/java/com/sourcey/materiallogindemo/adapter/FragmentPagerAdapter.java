package com.sourcey.materiallogindemo.adapter;

/**
 * Created by @santafebound on 2016-09-29.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sourcey.materiallogindemo.fragment.ContactFragment;
import com.sourcey.materiallogindemo.fragment.FeedFragment;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public FragmentPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new FeedFragment();
            case 1:
                return new ContactFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}