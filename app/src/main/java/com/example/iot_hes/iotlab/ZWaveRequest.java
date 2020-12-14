package com.example.iot_hes.iotlab;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZWaveRequest {

    private final String TAG = "ZwaveRequest";

    private final String write_topic = "zwave_commands";
    private final String read_sub = "zwave_data_sub";
    private PubSub client;

    public ZWaveRequest(Context context, EventHandler cb) {
        client = new PubSub(context, context.getString(R.string.cloud_project_id));
        client.subscribe(read_sub, cb);
    }

    public void networkAddNode()
    {
        String payload = "network add";
        try {
            client.publish(write_topic, payload);
        } catch (InterruptedException e) {
            Log.d(TAG, "Failed to publish");
            e.printStackTrace();
        }
    }

    public void networkRemoveNode()
    {
        String payload = "network remove";
        try {
            client.publish(write_topic, payload);
        } catch (InterruptedException e) {
            Log.d(TAG, "Failed to publish");
            e.printStackTrace();
        }
    }

    public void networkReset()
    {
        String payload = "network reset";
        try {
            client.publish(write_topic, payload);
        } catch (InterruptedException e) {
            Log.d(TAG, "Failed to publish");
            e.printStackTrace();
        }
    }
}
