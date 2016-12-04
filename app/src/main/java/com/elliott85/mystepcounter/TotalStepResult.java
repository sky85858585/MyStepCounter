package com.elliott85.mystepcounter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elliott85.mystepcounter.content.StepCountData;
import com.elliott85.mystepcounter.database.StepDBManager;

import java.util.ArrayList;

/**
 * Created by 박현우 on 2016-12-03.
 */
public class TotalStepResult extends Fragment {
    private static final String TAG = TotalStepResult.class.getSimpleName();

    private StepDBManager mStepDBManager;

    private static volatile TotalStepResult sTotalStepCounter;
    private static Object sLock = new Object();

    private TextView mTextView;
    private Context mContext;

    private TotalStepResult(Context context) {
        mContext = context;
        mStepDBManager = new StepDBManager(context);
    }

    public static TotalStepResult getInstance(Context context) {
        if (sTotalStepCounter == null) {
            synchronized (sLock) {
                if (sTotalStepCounter == null)
                    sTotalStepCounter = new TotalStepResult(context);
            }
        }
        return sTotalStepCounter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.total_step_result, container, false);

        mTextView = (TextView) view.findViewById(R.id.total_step_count);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume called");

        refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause called");
    }

    public void refreshData() {
        TodayStepCounter todayStepCounter = TodayStepCounter.getInstance(mContext);

        ArrayList<StepCountData> data = mStepDBManager.selectAll();
        if (data == null) {
            mTextView.setText("No data!!!");
        } else {
            Log.i(TAG, "data is not null, size = " + data.size());
            StringBuilder sb = new StringBuilder();

            for (StepCountData stepCountData : data) {
                if (todayStepCounter.isStarted() && stepCountData.getDate().equals(todayStepCounter.getCurrentDate())) {
                    sb.append(stepCountData.getDate()).append(" : ").append(todayStepCounter.getStepCount()).append(" steps, ")
                            .append((long) todayStepCounter.getStepDistance()).append(" m");
                    sb.append("\n");
                } else {
                    sb.append(stepCountData.getDate()).append(" : ").append(stepCountData.getCount()).append("steps, ")
                            .append((long) stepCountData.getDistance()).append(" m");
                    sb.append("\n");
                }
            }
            mTextView.setText(sb.toString());
        }
    }
}