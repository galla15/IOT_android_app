package com.example.iot_hes.iotlab;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
    private final rooms Room = rooms.BEDROOM;
    private final String [] roomsNames ={"Living room", "Bedroom"};

    public ZWaveRequest(Context context, EventHandler cb) {
        client = new PubSub(context, context.getString(R.string.cloud_project_id));
        client.subscribe(read_sub, cb);
    }

    private boolean publish(String payload, rooms R, Toast t)
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

        return checkRoom(R);
    }

    private void simplePublish(String payload)
    {
        try {
            client.publish(write_topic, payload);
        }
        catch (InterruptedException e) {
            Log.d(TAG, "Failed to publish");
            e.printStackTrace();
        }
    }

    private boolean checkRoom(rooms R)
    {
        return this.Room == R;
    }

    public boolean networkAddNode(rooms R, Toast t)
    {
        String payload = "network add";
        return publish(payload, R, t);
    }

    public boolean networkRemoveNode(rooms R, Toast t)
    {
        String payload = "network remove";
        return publish(payload, R, t);
    }

    public boolean networkReset(rooms R, Toast t)
    {
        String payload = "network reset";
        return publish(payload, R, t);
    }

    public boolean networkInfo(rooms R, Toast t)
    {
        String payload = "network info";
        return publish(payload, R ,t);
    }

    public void dimmerOn(rooms R, Toast t)
    {
        String payload = "dimmer on";
        publish(payload, R, t);
    }

    public void dimmerOff(rooms R, Toast t)
    {
        String payload = "dimmer off";
        publish(payload, R, t);
    }

    public void dimmerSet(String val, rooms R, Toast t)
    {
        String payload = "dimmer set " + val;
        publish(payload, R, t);
    }

    public void dimmerGet(rooms R, Toast t)
    {
        String payload = "dimmer get";
        publish(payload, R, t);
    }

    public void rulesOn()
    {
        String payload = "rules on";
        simplePublish(payload);
    }

    public void rulesOff()
    {
        String payload = "rules off";
        simplePublish(payload);
    }

}
