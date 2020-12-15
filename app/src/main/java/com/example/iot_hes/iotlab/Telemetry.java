package com.example.iot_hes.iotlab;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Telemetry {

    private double temperature = 0;
    private double temperature_mean = 0;
    private double humidity = 0;
    private double humidity_mean = 0;
    private double luminance = 0;
    private double luminance_mean = 0;
    private boolean motion = false;


    private int dataCount = 1;

    public void setData(JSONObject data)
    {
        if(!data.isNull("battery"))
        {
            try {
                temperature = data.getDouble("temperature");
                temperature_mean += temperature;

                humidity = data.getDouble("humidity");
                humidity_mean += humidity;

                luminance = data.getDouble("luminance");
                luminance_mean += luminance;

                motion = data.getBoolean("motion");

                dataCount++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (ArithmeticException e)
            {
                humidity_mean = 0;
                temperature_mean = 0;
                dataCount = 1;
                e.printStackTrace();
            }
        }
    }

    private String doubleFormat(double val)
    {
        return String.format(Locale.ENGLISH, "%.1f", val);
    }

    public String getTemperature()
    {
        return doubleFormat(temperature);
    }

    public String getTemperatureMean()
    {
        return doubleFormat(temperature_mean / dataCount);
    }

    public String getHumidity()
    {
        return doubleFormat(humidity);
    }

    public String getHumidityMean()
    {
        return doubleFormat(humidity_mean / dataCount);
    }

    public String getLuminance()
    {
        return doubleFormat(luminance);
    }

    public String getLuminanceMean()
    {
        return doubleFormat(luminance_mean / dataCount);
    }

    public String getMotion()
    {
        if(motion) return "True";
        else return "False";
    }
}
