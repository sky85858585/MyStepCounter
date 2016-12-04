package com.elliott85.mystepcounter.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.elliott85.mystepcounter.TodayStepCounter;

/**
 * Created by 박현우 on 2016-12-04.
 */
public class EventReceiver extends BroadcastReceiver {
    private static final String TAG = "EventReceiver";

    private Context mContext;

    public EventReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {


                case Intent.ACTION_SCREEN_ON:
                    Log.i(TAG, "Get screen on intent");

                    checkPowerOptimization(true);
                    break;

                case Intent.ACTION_SCREEN_OFF:
                    Log.i(TAG, "Get screen off intent");

                    checkPowerOptimization(false);
                    break;
            }
        }
    }

    private void checkPowerOptimization(boolean flag) {
        TodayStepCounter todayStepCounter2 = TodayStepCounter.getInstance(mContext);
        todayStepCounter2.powerOptimization(flag);
    }
}
