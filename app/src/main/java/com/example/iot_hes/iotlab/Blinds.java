package com.example.iot_hes.iotlab;

import com.estimote.coresdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class Blinds {
    private String groupAddress;
    private String value = "0";

    public Blinds(String Group)
    {
        this.groupAddress = Group;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public JSONObject getJsonObj()
    {
        JSONObject json = new JSONObject();
        try{
            json.put("group_address", this.groupAddress);
            json.put("value", this.value);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return json;
    }

}
