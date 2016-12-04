package com.elliott85.mystepcounter.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.elliott85.mystepcounter.database.StepDBManager;

import java.util.List;

/**
 * Created by 박현우 on 2016-12-04.
 */
public class DataProvider extends ContentProvider {
    private static final String TAG = "DataProvider";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String AUTHORITY = "com.elliott85.mystepcounter.dataprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private String AUTH_KEY = "1q2w3e4r5t";

    StepDBManager mStepDBManager;

    public String getAuthkey() {
        return AUTH_KEY;
    }

    public void setAuthkey(String authkey) {
        AUTH_KEY = authkey;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mStepDBManager = new StepDBManager(getContext());
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert called");

        List<String> reqValue = uri.getPathSegments();

        if(reqValue.size() > 0) {
            String serviceType = reqValue.get(0);
            Log.d(TAG, "ServiceType = " + serviceType);

            if(serviceType.equals("AUTH_GET")){
                return Uri.parse(CONTENT_URI + "/" + serviceType + "/" + getAuthkey());
            }
        }
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "Update is called");

        List<String> reqValue = uri.getPathSegments();

        if(reqValue.size() > 0) {
            String serviceType = reqValue.get(0);
            Log.d(TAG, "ServiceType = " + serviceType);

            if(serviceType.equals("AUTH_UPDATE")){
                String new_authkey = values.getAsString("new_authkey");
                Log.i(TAG, "update new_authkey = " + new_authkey);

                setAuthkey(new_authkey);
            }
        }

        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
