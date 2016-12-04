package com.elliott85.mystepcounter;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by 박현우 on 2016-12-04.
 */
public class GeocodeAsyncTask extends AsyncTask<Location, Void, String> {
    private static final String TAG = "GeocodeAsyncTask";

    private static final String GEOCODE_RESULT = "result";
    private static final String GEOCODE_RESULT_ITEM = "items";
    private static final String GEOCODE_RESULT_ITEM_ADDRESS = "address";

    private TextView mTextView;

    public GeocodeAsyncTask (TextView textView) {
        mTextView = textView;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Log.i(TAG, "doInBackground called");

        String response = getAddressFromLocation(locations[0]);

        if (response != null) {
            Log.i(TAG, "Response = " + response);

            return parseJsonData(response);
        } else {
            Log.e(TAG, "Failed to get the address with location");
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (s != null) {
            mTextView.setText(s);
        }
    }

    private String getAddressFromLocation (Location location) {
        String clientId = "orxULhrSf4xih7KLHbto";
        String clientSecret = "EGvBz8GJqd";

        try {
            String apiURL = "https://openapi.naver.com/v1/map/reversegeocode?encoding=utf-8&coord=latlng&output=json&query="
                            + location.getLongitude() + "," + location.getLatitude();
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private String parseJsonData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject != null) {
                JSONObject result = jsonObject.getJSONObject(GEOCODE_RESULT);

                if (result != null) {
                    JSONArray items = result.getJSONArray(GEOCODE_RESULT_ITEM);

                    if (items != null) {
                        JSONObject object = items.getJSONObject(0);

                        if (object != null) {
                            return object.getString(GEOCODE_RESULT_ITEM_ADDRESS);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse the json data");
        }

        return null;
    }
}
