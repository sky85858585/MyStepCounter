package com.elliott85.mystepcounter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elliott85.mystepcounter.database.StepDBManager;
import com.elliott85.mystepcounter.listener.LocListener;
import com.elliott85.mystepcounter.listener.StepCountListener;

import java.util.Calendar;

/**
 * Created by 박현우 on 2016-12-03.
 */
public class TodayStepCounter extends Fragment implements View.OnClickListener  {
    private static final String TAG = TodayStepCounter.class.getSimpleName();

    private static final String START_STEP_COUNTER = "Start step counter";
    private static final String STOP_STEP_COUNTER = "Stop step counter";

    private static final double AVG_PERSON_STEPS_DISTANCE = 0.8; // For an avg peron 1km = 1250 steps

    // max batch latency is specified in microseconds
    private static final int NO_BATCHING = 0; // no batching
    private static final int BATCH_LATENCY_20s = 20000000;
    private static final int BATCH_LATENCY_5s = 5000000;

    private boolean isStarted = false;  // To check if now starting
    private int mYear, mMonth, mDay;    // Current date

    private long mStepInit = 0;
    private long mPrevStepCnt = 0;
    private long mStepCnt = 0;
    private double mStepDistance = 0;
    private String mLastUpdatedDate = null;

    private TextView mCurrentTime, mCurrentLocation, mStepCount, mDistance;
    private Button mStartButton;

    private LocationManager mLocationManager;
    private PackageManager mPackageManager;
    private SensorManager mSensorManager;

    // Database manager
    private StepDBManager mStepDBManager;
    private String mLatestDateInDB = null;

    // Register Listener
    private LocListener mLocListener;
    private StepCountListener mStepCountListener;

    // Singleton design to prevent lazy initialization
    private static volatile TodayStepCounter sTodayStepCounter;
    private static Object sLock = new Object();

    private Context mContext;

    private boolean mNeedToAutoStart = false;

    private TodayStepCounter(Context context) {
        Log.i(TAG, "TodayStepCounter constructed");

        mContext = context;
        mStepDBManager = new StepDBManager(context);

        if (getPreferences()) {
            Log.i(TAG, "Killed by user or system so need to auto restart!!!");
            mNeedToAutoStart = true;
        }
    }

    public static TodayStepCounter getInstance(Context context) {
        if (sTodayStepCounter == null) {
            synchronized (sLock) {
                if (sTodayStepCounter == null)
                    sTodayStepCounter = new TodayStepCounter(context);
            }
        }
        return sTodayStepCounter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate called");

        mPackageManager = mContext.getPackageManager();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.today_step_counter, container, false);

        mCurrentTime = (TextView) view.findViewById(R.id.today_current_time);
        mCurrentLocation = (TextView) view.findViewById(R.id.today_current_location);
        mStepCount = (TextView) view.findViewById(R.id.today_step_count);
        mDistance = (TextView) view.findViewById(R.id.today_distance);

        mStartButton = (Button) view.findViewById(R.id.today_step_start);
        mStartButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called, isStarted = " + isStarted);

        mCurrentTime.setText(getCurrentDate()); // Set the current time
        requestCurrentLocation();   // Get the current location

        if (isStarted) {
            mStartButton.setText(STOP_STEP_COUNTER);
        } else {
            mStartButton.setText(START_STEP_COUNTER);

            int count = mStepDBManager.getCount();
            if (count > 0) {
                Log.d(TAG, "Count of data in DB = " + count);

                int data = mStepDBManager.isExistData(getCurrentDate());
                if (data != -1) {
                    Log.i(TAG, "Today data is existed in database, so set to this activity");
                    mLatestDateInDB = getCurrentDate();

                    setStepResult(data);
                } else {
                    Log.d(TAG, "No data for today");
                }
            } else {
                Log.d(TAG, "Count of data in DB = 0");
            }
        }

        long distance = (long) mStepDistance;
        // Set the currentStep
        mStepCount.setText(String.valueOf(mStepCnt) + " steps");
        mDistance.setText(String.valueOf(distance) + " m");

        if (mNeedToAutoStart && !isStarted) {
            Log.i(TAG, "Auto restart!!!!");
            isStarted = true;
            mStartButton.setText(STOP_STEP_COUNTER);
            startStepCounter();
            setPreferences(true);

            mNeedToAutoStart = false;
        }
    }

    /*
     * Owner : sky85858585
     * Description : Request gps and network provider to get the location
     */
    protected void requestCurrentLocation() {
        Log.i(TAG, "requestCurrentLocation called");

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mLocationManager == null) {
            Log.i(TAG, "LocationManager is not null so request GLP/NLP");

            // Request current location
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            mLocListener = new LocListener(mContext);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocListener);
        } else {
            Log.e(TAG, "mLocationManager == null");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.today_step_start) {
            Log.i(TAG, "onClick called, start button, current status = " + isStarted);

            if (isStarted) {
                Log.i(TAG, "Stop!!!!");

                isStarted = false;
                setPreferences(false);
                mStartButton.setText(START_STEP_COUNTER);
                stopStepCounter();
            } else {
                Log.i(TAG, "Start!!!!");

                isStarted = true;
                mStartButton.setText(STOP_STEP_COUNTER);
                startStepCounter();
                setPreferences(true);
            }
        }
    }

    /*
     * Owner : sky85858585
     * Description : When start button is clicked, register the step counter listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startStepCounter() {
        if (isSupportStepSensor()) {
            Log.i(TAG, "startStepCounter so register listener");

            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            mStepCountListener = new StepCountListener(mContext);
            boolean batchMode = mSensorManager.registerListener(mStepCountListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL, NO_BATCHING);
        } else {
            Log.e(TAG, "Cannot support step counter");
        }
    }

    /*
     * Owner : sky85858585
     * Description : When stop button is clicked, unregister the step counter listener
     *               and save the data to databases
     */
    private void stopStepCounter() {
        if (mStepCountListener != null) {
            mSensorManager.unregisterListener(mStepCountListener);

            mStepInit = 0;
            saveDataToDatabase(getCurrentDate(), mStepCnt, mStepDistance);
        }
    }

    private void saveDataToDatabase(String date, long count, double distance) {
        Log.d(TAG, "saveDataToDatabase called, date = " + date + " / count = " + count);

        ContentValues cv = new ContentValues();
        cv.put(StepDBManager.KEY_DATE, date);
        cv.put(StepDBManager.KEY_STEP_COUNT, count);
        cv.put(StepDBManager.KEY_DISTANCE, distance);

        if (mLatestDateInDB == null || !mLatestDateInDB.equals(date)) {
            if(mStepDBManager.insertData(cv) > 0) {
                Log.v(TAG, "Success to insert data!!!");
                mLatestDateInDB = date;
            } else {
                Log.e(TAG, "Failed to insert data");
            }
        } else {
            if(mStepDBManager.updateData(cv, date, count) > 0) {
                Log.v(TAG, "Success to update data!!!");
                mLatestDateInDB = date;
            } else {
                Log.e(TAG, "Failed to update data");
            }
        }
    }

    /*
     * Owner : sky85858585
     * Description : Check if it can support step count by over kitkat os
     */
    private boolean isSupportStepSensor() {
        // Require at least Android KitKat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT
                && mPackageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && mPackageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    /*
     * Owner : sky85858585
     * Description : When location listener is updated, then called this function
     */
    public void updateNewLocation(Location location) {
        if (location != null) {
            new GeocodeAsyncTask(mCurrentLocation).execute(location);
            //mCurrentLocation.setText(location.getLatitude() + "," + location.getLongitude());

            if (mLocListener != null) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mLocationManager.removeUpdates(mLocListener);
            }
        }
    }

    /*
     * Owner : sky85858585
     * Description : When sensor listener is updated for step counter, then called this function
     */
    public void updateStepCounter(int result) {
        Log.i(TAG, "updateStepCounter, " + result);

        String updatedTime = getCurrentDate();

        Log.i(TAG, "LastUpdatedTime = " + mLastUpdatedDate + " / updatedTime = " + updatedTime);

        if (mLastUpdatedDate == null || mLastUpdatedDate.equals(updatedTime)) {
            if (mStepInit == 0) {
                // initial value
                mStepInit = result;
                Log.i(TAG, "first step = " + mStepInit);
            } else {
                long increasedCnt = result - mPrevStepCnt;

                mStepCnt += increasedCnt;
                mStepDistance = (float) mStepCnt * AVG_PERSON_STEPS_DISTANCE;
                Log.i(TAG, "Increased cnt = " + increasedCnt + "/ stepDistance = " + mStepDistance);
            }
            mPrevStepCnt = result;

            Log.i(TAG, "step count = " + mStepCnt + " / prevStepCnt = " + mPrevStepCnt);

            long distanceResult = (long) mStepDistance;
            mStepCount.setText(String.valueOf(mStepCnt) + " steps");
            mDistance.setText(String.valueOf(distanceResult) + " m");
        } else if (!mLastUpdatedDate.equals(updatedTime)) {
            mCurrentTime.setText(updatedTime);
            saveDataToDatabase(mLastUpdatedDate, mStepCnt, mStepDistance); // Need to save the current status to database !!!!

            setStepResult(0);
        }

        mLastUpdatedDate = updatedTime;
    }

    /*
     * Owner : sky85858585
     * Description : Tot get the current date and set the data to mDay, mMonth and mYear;
     */
    protected String getCurrentDate() {
        Calendar calander = Calendar.getInstance();
        mDay = calander.get(Calendar.DAY_OF_MONTH);
        mMonth = calander.get(Calendar.MONTH) + 1;
        mYear = calander.get(Calendar.YEAR);

        return mYear + "/" + mMonth + "/" + mDay;
    }

    private void setStepResult(int data) {
        mStepCnt = data;
        mStepDistance = (float) mStepCnt * AVG_PERSON_STEPS_DISTANCE;
    }

    /*
     * Owner : sky85858585
     * Description : Need to optimize the power consumption
     *               When the LCD is off, try to change the mode to FIFO batching
      *              When the LCD is on, try to change the mode to onUpdate type
     */
    public void powerOptimization(boolean screen) {
        Log.i(TAG, "powerOptimization called, screen = " + screen);

        if (!isStarted) {
            Log.d(TAG, "Doesn't need to optimize the step counter batching");
        }
    }

    private boolean getPreferences(){
        SharedPreferences pref = mContext.getSharedPreferences("status", Context.MODE_PRIVATE);
        boolean result = pref.getBoolean("isStarted", false);

        return result;
    }

    private void setPreferences(boolean flag) {
        SharedPreferences pref = mContext.getSharedPreferences("status", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isStarted", flag);
        editor.commit();
    }

    protected boolean isStarted() {
        return isStarted;
    }

    protected long getStepCount() {
        return mStepCnt;
    }

    protected double getStepDistance() {
        return mStepDistance;
    }
}
