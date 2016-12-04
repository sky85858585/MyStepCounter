package com.elliott85.mystepcounter.listener;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.elliott85.mystepcounter.TodayStepCounter;

/**
 * Created by 박현우 on 2016-12-03.
 */
public class LocListener implements android.location.LocationListener {
    private static final String TAG = LocListener.class.getSimpleName();

    private Context mContext;

    public LocListener(Context context) {
        mContext = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            TodayStepCounter todayStepCounter = TodayStepCounter.getInstance(mContext);
            todayStepCounter.updateNewLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
