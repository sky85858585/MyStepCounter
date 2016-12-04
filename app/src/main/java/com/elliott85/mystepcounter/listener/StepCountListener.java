package com.elliott85.mystepcounter.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.elliott85.mystepcounter.TodayStepCounter;

/**
 * Created by 박현우 on 2016-12-04.
 */
public class StepCountListener implements SensorEventListener {
    private static final String TAG = "StepCountListener";

    private TodayStepCounter mTodayStepCounter;

    public StepCountListener(Context context) {
        mTodayStepCounter = TodayStepCounter.getInstance(context);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int result = (int) sensorEvent.values[0];

            Log.i(TAG, "updated new sensorEvent = " + result);
            mTodayStepCounter.updateStepCounter(result);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
