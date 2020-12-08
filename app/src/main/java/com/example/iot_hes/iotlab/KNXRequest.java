package com.example.iot_hes.iotlab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.estimote.coresdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;

import org.json.JSONObject;

public class KNXRequest {
    private final String TAG = "KNXRequest";

    private Context context;
    private String baseURL;
    private RequestQueue queue;

    public KNXRequest(Context context, String url, RequestQueue queue)
    {
        this.context = context;
        this.baseURL = url;
        this.queue = queue;
    }

    public void setBlind(Blinds b)
    {
        sendHttpRequest("blind/set", b.getJsonObj());
    }

    public void setValve(Valve v) { sendHttpRequest("valve/set", v.getJsonObj()); }

    private void sendHttpRequest(String path, JSONObject data)
    {
        String url = this.baseURL + path;

        Log.d(TAG, url);
        Log.d(TAG, data.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, response.toString());
            }
        },
                null);
        queue.add(jsonObjectRequest);
    }
}
