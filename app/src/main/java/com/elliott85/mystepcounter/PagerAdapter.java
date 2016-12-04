package com.elliott85.mystepcounter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by 박현우 on 2016-12-03.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    TodayStepCounter mTab1 = null;
    TotalStepResult mTab2 = null;

    private Context mContext;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context context) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;

        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                mTab1 = TodayStepCounter.getInstance(mContext);
                return mTab1;
            case 1:
                mTab2 = TotalStepResult.getInstance(mContext);
                return mTab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}