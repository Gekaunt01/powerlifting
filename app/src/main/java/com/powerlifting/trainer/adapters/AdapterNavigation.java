package com.powerlifting.trainer.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.powerlifting.trainer.fragments.FragmentCategories;
import com.powerlifting.trainer.fragments.FragmentPrograms;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.powerlifting.trainer.R;

public class AdapterNavigation extends CacheFragmentStatePagerAdapter {

    private  String[] sPagerTitles;

    public AdapterNavigation(Context c, FragmentManager fm) {
        super(fm);

        Context mContext = c;

        sPagerTitles = mContext.getResources().getStringArray(R.array.home_pager_titles);
    }

    @Override
    protected Fragment createItem(int position) {
        Fragment f;
        switch (position) {
            case 0: {
                f = new FragmentCategories();
                break;
            }
            case 1:
            default: {
                f = new FragmentPrograms();
                break;
            }
        }
        return f;
    }

    @Override
    public int getCount() {
        return sPagerTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return sPagerTitles[position];
    }
}