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

    private RequestQueue queue;
    private String baseURL;

    private List<Dimmer> listDimmer = new ArrayList<Dimmer>();

    public ZWaveRequest(Context context, String baseURL, RequestQueue queue) {
        this.baseURL = baseURL;
        this.queue = queue;

        getAllInformations();
    }

    private void getAllInformations()
    {
        this.getAllDimmer();
        this.getAllNodes();
        this.getAllSensor();
    }

    public boolean getAllNodes() {
        boolean state = false;


        return state;

    }

    public boolean getAllSensor() {
        boolean state = false;


        return state;
    }

    public void getAllDimmer() {
        Log.d(TAG, "getAllDimmer called");
        // Create URL
        String url = baseURL + "network/dimmers";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "reponse : " + response);
                            JSONArray data = new JSONArray(response);
                            for(int i = 0; i<data.length();i++)
                            {
                                String id = data.getJSONArray(i).get(0).toString();
                                listDimmer.add(new Dimmer(id, "0"));
                                Log.d(TAG, "listDimmer : " + listDimmer.toString());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "error JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void setOnAllLight(String level) {
        String url = baseURL + "dimmer/set_level";

        JSONObject jsonObject = new JSONObject();

        for(int i = 0; i<listDimmer.size();i++)
        {
            try {
                jsonObject.put("node_id", listDimmer.get(i).getId());
                jsonObject.put("value", listDimmer.get(i).setLevel(level));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "id : " + listDimmer.get(i).getId() + " jsonObject : " + jsonObject.toString());


            JsonObjectRequest jsonRequest = new JsonObjectRequest( Request.Method.POST, url, jsonObject,
                    new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            Log.e(TAG, response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO
                        }
                    });
            // Add the request to the RequestQueue.
            queue.add(jsonRequest);
        }


    }


    public boolean setOffAllLight() {
        boolean state = false;


        return state;
    }
}

class Dimmer {
    private String id;
    private String level;

    public Dimmer(String id, String level)
    {
        this.id=id;
        this.level = level;
    }

    public String setLevel(String level)
    {
        int l = Integer.parseInt(level);
        if(l>=0 && l<=255) {
            this.level = level;
        }
        return this.level;
    }

    public String getId()
    {
        return this.id;
    }
}
