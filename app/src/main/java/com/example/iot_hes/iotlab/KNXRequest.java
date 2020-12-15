package com.example.iot_hes.iotlab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.estimote.coresdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;
import com.google.api.core.ApiService;

import org.json.JSONObject;

public class KNXRequest {
    private final String TAG = "KNXRequest";
    private final String write_topic = "knx_commands";
    private final String read_sub = "knx_data_sub";
    private PubSub client;
    private final rooms Room = rooms.BEDROOM;
    private final String [] roomsNames ={"Living room", "Bedroom"};

    public KNXRequest(Context context, EventHandler cb)
    {
        client = new PubSub(context, context.getString(R.string.cloud_project_id));
        client.subscribe(read_sub, cb);
    }

    private void publish(String payload, rooms R, Toast t)
    {
        if(!checkRoom(R))
        {
            t.setText("You must move to Room : " + roomsNames[this.Room.ordinal()]);
            t.show();
        }
        else
        {
            try {
                client.publish(write_topic, payload);
            }
            catch (InterruptedException e) {
                Log.d(TAG, "Failed to publish");
                e.printStackTrace();
            }
        }
    }

    private boolean checkRoom(rooms R)
    {
        return this.Room == R;
    }

    public void open_blinds(String addr, rooms R, Toast t)
    {

        String payload = "blind open " + addr;
        publish(payload, R, t);
    }

    public void close_blinds(String addr, rooms R, Toast t)
    {
        String payload = "blind close " + addr;
        publish(payload, R, t);
    }

    public void set_blinds(String addr, String value, rooms R, Toast t)
    {
        String payload = "blind set " + addr + " " + value;
        publish(payload, R, t);
    }

    public void get_blinds(String addr, rooms R, Toast t)
    {
        String payload = "blind get " + addr;
        publish(payload, R, t);
    }

    public void set_radiator(String addr, String value, rooms R, Toast t)
    {
        String payload = "valve set " + addr + " " + value;
        publish(payload, R, t);
    }

    public void get_radiator(String addr, rooms R, Toast t)
    {
        String payload = "valve get " + addr;
        publish(payload, R, t);
    }
}
