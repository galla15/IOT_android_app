package com.example.iot_hes.iotlab;

import org.json.JSONException;
import org.json.JSONObject;

public class Valve {
    private String groupAddress;
    private String value = "0";

    public Valve(String group)
    {
        this.groupAddress = group;
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
