package com.app.synclient;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private static boolean is_pull = false;
    private static int DELAY = 1000; // miliseconds
    // Instantiate the RequestQueue.
    private static final RequestQueue queue = Volley.newRequestQueue(DashboardActivity.getAppContext());

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(DashboardActivity.getAppContext(), "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(BackgroundService.is_pull == true) {
                    BackgroundService.pull_data();
                    Log.d(BackgroundService.class.getSimpleName(), "PULL DATA");
                } else {
                    BackgroundService.pull_data();
                    Log.d(BackgroundService.class.getSimpleName(), "ELSE");
                }

                handler.postDelayed(runnable, BackgroundService.DELAY);

//                Toast.makeText(DashboardActivity.getAppContext(), "Service is still running", Toast.LENGTH_LONG).show();
//                handler.postDelayed(runnable, 10000);
//                String webSource = "https://dragino.000webhostapp.com/assets.zip";
//                GlobalLibrary.downloadZip(webSource, GlobalEnvironment.path.getAbsolutePath());
//                GlobalLibrary.unpackZip(GlobalEnvironment.path.getAbsolutePath()+ File.separator,"data.zip");
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    public static void push_data() {
        String genUrl = GlobalSetting.PUSH_REST+"?act=close_update&devid="+GlobalSetting.DEVICE_ID;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, genUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            if(jsonObj.getInt("is_update") == 1) {
                                BackgroundService.is_pull = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().getSimpleName(),error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        BackgroundService.queue.add(stringRequest);
    }

    public static void pull_data() {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GlobalSetting.PULL_REST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            if(jsonObj.getInt("is_update") == 1) {
                                BackgroundService.is_pull = true;
                                // proses update template disini

                                // set is_update from server
                                // BackgroundService.push_data();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().getSimpleName(),error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        BackgroundService.queue.add(stringRequest);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(DashboardActivity.getAppContext(), "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }
}
