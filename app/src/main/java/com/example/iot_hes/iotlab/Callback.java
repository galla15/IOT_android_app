package com.example.iot_hes.iotlab;

import android.util.Log;

interface EventHandler{
    void handleEvent(String msg);
}

public class Callback implements EventHandler   {

    public String msg;

    @Override
    public void handleEvent(String msg) {
        this.msg = msg;
        Log.d("Callback", msg);
    }
}
