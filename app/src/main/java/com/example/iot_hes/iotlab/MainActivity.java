// -*- mode: java -*-
package com.example.iot_hes.iotlab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

// import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.config.Flags;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;
import com.estimote.coresdk.service.BeaconManager;

import org.json.JSONException;
import org.json.JSONObject;

enum rooms{LIVING_ROOM, BEDROOM};

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "IoTLab";
    private static final String VERSION = "0.1.9";

    TextView PositionText;
    EditText Percentage;
    Button   IncrButton;
    Button   DecrButton;

    //ZWave
    Button LightButton_ON;
    Button LightButton_OFF;
    Button LightButton_Set;
    Button LightButton_Get;
    TextView LightValue_Text;
    Button Network_Add;
    Button Network_Remove;
    Button Network_Reset;
    TextView Temperature_Text;
    TextView Humidity_Text;
    TextView Luminance_Text;
    TextView Motion_Text;
    ImageView DimmerStatus_Image;
    ImageView SensorStatus_Image;
    Switch SensorSwitch_Mean;

    //KNX
    Button StoreButton_OPEN;
    Button StoreButton_Close;
    Button StoreButton_Set;
    Button StoreButton_Get;
    EditText Store_Addr;
    TextView Store_Value;

    Button RadiatorButton_Get;
    Button RadiatorButton_Set;
    EditText Radiator_Addr;
    TextView Radiator_Value;


    // In the "OnCreate" function below:
    // - TextView, EditText and Button elements are linked to their graphical parts (Done for you ;) )
    // - "OnClick" functions for Increment and Decrement Buttons are implemented (Done for you ;) )
    //
    // IoT Lab BeaconsApp minimal implementation:
    // - detect the closest Beacon and figure out the current Room
    //
    // - Set the PositionText with the Room name
    // - Implement the "OnClick" functions for LightButton, StoreButton and RadiatorButton

    private BeaconManager beaconManager;
    private BeaconRegion region;

    // private static Map<Integer, String> rooms;
    static int currentRoom;

    private RequestQueue queue;

    // ZWave manager
    private ZWaveRequest zwaveRequest;
    boolean hasClick = false;

    // KNX Manager
    private KNXRequest knxRequest;

    private ProgressDialog progressDialog;
    private Toast toast;

    private final int beacon_bedRoom = 19793;
    private final int beacon_livingRoom = 33567;

    private final String [] roomsNames ={"Living room", "Bedroom"};
    private rooms actualRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Version: "+VERSION);

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        }

        queue = Volley.newRequestQueue(this);

        PositionText   =  findViewById(R.id.PositionText);
        Percentage     =  findViewById(R.id.Percentage);
        IncrButton     =  findViewById(R.id.IncrButton);
        DecrButton     =  findViewById(R.id.DecrButton);

        LightButton_ON = findViewById(R.id.LightButtonOn);
        LightButton_OFF = findViewById(R.id.LightButtonOff);
        LightButton_Get = findViewById(R.id.LightButtonGet);
        LightButton_Set = findViewById(R.id.LightButtonSet);
        Network_Add = findViewById(R.id.ZwaveButtonAdd);
        Network_Remove = findViewById(R.id.ZwaveButtonRemove);
        Network_Reset = findViewById(R.id.ZwaveButtonReset);
        Temperature_Text = findViewById(R.id.temperatureData);
        Humidity_Text = findViewById(R.id.humidityData);
        Luminance_Text = findViewById(R.id.luminanceData);
        Motion_Text = findViewById(R.id.motionData);
        DimmerStatus_Image = findViewById(R.id.LightStatusImageView);
        SensorStatus_Image = findViewById(R.id.SensorStatusImage);
        SensorSwitch_Mean = findViewById(R.id.sensorMeanSwitch);
        LightValue_Text = findViewById(R.id.LightValueText);

        StoreButton_OPEN = findViewById(R.id.StoreButtonOpen);
        StoreButton_Close = findViewById(R.id.StoreButtonClose);
        Store_Addr = findViewById(R.id.StoreAddrText);
        StoreButton_Set = findViewById(R.id.StoreButtonSet);
        StoreButton_Get = findViewById(R.id.StoreButtonGet);
        Store_Value = findViewById(R.id.StoreGetTextView);

        RadiatorButton_Set =  findViewById(R.id.RadiatorButtonSet);
        RadiatorButton_Get = findViewById(R.id.RadiatorButtonGet);
        Radiator_Addr = findViewById(R.id.RadiatorAddrText);
        Radiator_Value = findViewById(R.id.RadiatorGetTextView);

        toast = Toast.makeText(getApplicationContext(), "Yo", Toast.LENGTH_SHORT);
        actualRoom = rooms.BEDROOM;

        final Telemetry telemetry = new Telemetry();

        Flags.DISABLE_BATCH_SCANNING.set(true);
        Flags.DISABLE_HARDWARE_FILTERING.set(true);

        EstimoteSDK.initialize(getApplicationContext(), //"", ""
                               // These are not needed for beacon ranging
                                "smarthepia-8d8",                       // App ID
                                "771bf09918ceab03d88d4937bdede558"   // App Token
                               );
        EstimoteSDK.enableDebugLogging(true);

        // we want to find all of our beacons on range, so no major/minor is
        // specified. However the student's labo has assigned a given major
        region = new BeaconRegion(TAG, UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                                  47039,    // major -- for the students it should be the assigned one 17644
                                  null      // minor
                                  );
        beaconManager = new BeaconManager(this);
        // beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
                @Override
                public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                    Log.d(TAG, "Beacons: found " + String.format("%d", list.size()) + " beacons in region "
                          + region.getProximityUUID().toString());

                    if (!list.isEmpty()) {
                        Beacon nearestBeacon = list.get(0);
                        currentRoom = nearestBeacon.getMinor();
                        switch (currentRoom)
                        {
                            case beacon_livingRoom:
                                actualRoom = rooms.LIVING_ROOM;
                                break;

                            case beacon_bedRoom:
                                actualRoom = rooms.BEDROOM;
                                break;

                        }
                        String msg = "Room : " +  roomsNames[actualRoom.ordinal()] + "\n(major ID " +
                            Integer.toString(nearestBeacon.getMajor()) + ")";
                        Log.d(TAG, msg);
                        PositionText.setText(msg);
                    }
                }
            });

        beaconManager.setForegroundScanPeriod(2000, 1000);

        // Only accept input values between 0 and 100
        Percentage.setFilters(new InputFilter[]{new InputFilterMinMax("0", "100")});

        IncrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int number = Integer.parseInt(Percentage.getText().toString());
                if (number<100) {
                    number++;
                    Log.d(TAG, "Inc: "+String.format("%d",number));
                    Percentage.setText(String.format("%d",number));
                }
            }
        });

        DecrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int number = Integer.parseInt(Percentage.getText().toString());
                if (number>0) {
                    number--;
                    Log.d(TAG, "Dec: "+String.format("%d",number));
                    Percentage.setText(String.format("%d",number));
                }
            }
        });

        /***
         * KNX Manager
         */
        knxRequest = new KNXRequest(getApplicationContext(), new EventHandler()
        {
            @Override
            public void handleEvent(String msg) {
                Log.d(TAG, msg);
                int value = 0;
                try {
                    JSONObject obj = new JSONObject(msg);
                    if(!obj.isNull("blind"))
                    {
                        value = obj.getInt("blind");
                        Store_Value.setText(String.format(Locale.ENGLISH,"%d", value));
                        Store_Value.postInvalidate();
                    }
                    else if(!obj.isNull("valve"))
                    {
                        value = obj.getInt("valve");
                        Radiator_Value.setText(String.format(Locale.ENGLISH,"%d", value));
                        Radiator_Value.postInvalidate();
                    }
                    else
                    {
                        Log.d(TAG, "Object not recognized : " + obj.toString());
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "Failed to initialize json object : " + e.getMessage());
                    e.printStackTrace();
                }

            }
        });
        StoreButton_OPEN.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                knxRequest.open_blinds(Store_Addr.getText().toString(), actualRoom, toast);
            }
        });

        StoreButton_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knxRequest.close_blinds(Store_Addr.getText().toString(), actualRoom, toast);
            }
        });

        StoreButton_Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knxRequest.set_blinds(Store_Addr.getText().toString(), Percentage.getText().toString(), actualRoom, toast);
            }
        });

        StoreButton_Get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knxRequest.get_blinds(Store_Addr.getText().toString(), actualRoom, toast);
            }
        });

        /***
         * Radiator Manager
         */
        RadiatorButton_Set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int value = Integer.parseInt(Percentage.getText().toString());
                value = (int)(((float)value / 100) * 255);
                knxRequest.set_radiator(Radiator_Addr.getText().toString(), String.valueOf(value), actualRoom, toast);
            }
        });

        RadiatorButton_Get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knxRequest.get_radiator(Radiator_Addr.getText().toString(), actualRoom, toast);
            }
        });


        /***
         * Z-Wave
         */
        zwaveRequest = new ZWaveRequest(getApplicationContext(), new EventHandler() {
            @Override
            public void handleEvent(String msg) {
                Log.d(TAG, msg);
                boolean showToast = true;
                try{
                    String res;
                    JSONObject obj = new JSONObject(msg);
                    if(!obj.isNull("cmd"))
                    {
                        progressDialog.stop();
                        switch (obj.getString("cmd"))
                        {
                            case "Add":
                                res = obj.getString("Result");
                                toast.setText("Node added! " + res);
                                if(res.contains("Dimmer added : Ok"))
                                {
                                    DimmerStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_online));
                                }
                                if(res.contains("Sensor added : Ok"))
                                {
                                    SensorStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_online));
                                }
                                break;
                            case "Remove":
                                res = obj.getString("Result");
                                toast.setText("Node removed! " + res);
                                if(res.contains("Dimmer removed : Ok"))
                                {
                                    DimmerStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_offline));
                                }
                                if(res.contains("Sensor removed : Ok"))
                                {
                                    SensorStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_offline));
                                }
                                break;
                            case "Reset":
                                toast.setText("Network Reset : " + obj.getString("Result"));
                                break;

                            case "Info":
                                if(!obj.isNull("Result"))
                                {
                                    JSONObject data = obj.getJSONObject("Result");
                                    String devices = data.toString();
                                    String toast_msg = "Network devices : ";
                                    if(devices.contains("Unknown"))
                                    {
                                        DimmerStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_online));
                                        toast_msg += "Dimmer";
                                    }
                                    else
                                    {
                                        DimmerStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_offline));
                                        toast_msg += "None";
                                    }

                                    if(devices.contains("MultiSensor 6"))
                                    {
                                        SensorStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_online));
                                        toast_msg += ", Sensor";
                                    }
                                    else
                                    {
                                        SensorStatus_Image.setImageDrawable(getDrawable(R.drawable.ic_status_offline));
                                        toast_msg += ", None";
                                    }

                                    toast.setText(toast_msg);
                                }
                                else
                                {
                                    Log.d(TAG, "Info cannot read result");
                                }

                                break;

                            case "dimmer get":
                                showToast = false;
                                if(!obj.isNull("Result"))
                                {
                                    String data = obj.getString("Result");
                                    LightValue_Text.setText(data);
                                    LightValue_Text.postInvalidate();
                                }
                                break;

                            default:
                                toast.setText("Unknown command : " + obj.getString("cmd"));
                                break;
                        }

                        if(showToast)toast.show();
                    }
                    else if(!obj.isNull("battery"))
                    {
                        telemetry.setData(obj);

                        if(!SensorSwitch_Mean.isChecked())
                        {
                            Temperature_Text.setText(telemetry.getTemperature());
                            Humidity_Text.setText(telemetry.getHumidity());
                            Luminance_Text.setText(telemetry.getLuminance());
                        }
                        else
                        {
                            Temperature_Text.setText(telemetry.getTemperatureMean());
                            Humidity_Text.setText(telemetry.getHumidityMean());
                            Luminance_Text.setText(telemetry.getLuminanceMean());
                        }

                        Temperature_Text.postInvalidate();
                        Humidity_Text.postInvalidate();
                        Luminance_Text.postInvalidate();
                        Motion_Text.setText(telemetry.getMotion());
                        Motion_Text.postInvalidate();
                    }
                    else
                    {
                        Log.d(TAG, "No command found in msg : " + msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }
            }
        });

        /***
         * Network functions
         */
        Network_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                if(zwaveRequest.networkAddNode(actualRoom, toast))
                {
                    progressDialog = new ProgressDialog(MainActivity.this, "Adding new device");
                    progressDialog.show();
                }
            }
        });

        Network_Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zwaveRequest.networkRemoveNode(actualRoom, toast))
                {
                    progressDialog = new ProgressDialog(MainActivity.this, "Removing device");
                    progressDialog.show();
                }
            }
        });

        Network_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zwaveRequest.networkReset(actualRoom, toast))
                {
                    progressDialog = new ProgressDialog(MainActivity.this, "Resetting network");
                    progressDialog.show();
                }
            }
        });

        LightButton_ON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zwaveRequest.dimmerOn(actualRoom, toast);
            }
        });

        LightButton_OFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zwaveRequest.dimmerOff(actualRoom, toast);
            }
        });

        LightButton_Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zwaveRequest.dimmerSet(Percentage.getText().toString(), actualRoom, toast);
            }
        });

        LightButton_Get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zwaveRequest.dimmerGet(actualRoom, toast);
            }
        });

        SensorSwitch_Mean.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Temperature_Text.setText(telemetry.getTemperatureMean());
                    Humidity_Text.setText(telemetry.getHumidityMean());
                    Luminance_Text.setText(telemetry.getLuminanceMean());
                }
                else
                {
                    Temperature_Text.setText(telemetry.getTemperature());
                    Humidity_Text.setText(telemetry.getHumidity());
                    Luminance_Text.setText(telemetry.getLuminance());
                }

                Temperature_Text.postInvalidate();
                Humidity_Text.postInvalidate();
                Luminance_Text.postInvalidate();
                Motion_Text.setText(telemetry.getMotion());
                Motion_Text.postInvalidate();
            }
        });

    }


    // You will be using "OnResume" and "OnPause" functions to resume and pause Beacons ranging (scanning)
    // See estimote documentation:  https://developer.estimote.com/android/tutorial/part-3-ranging-beacons/
    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                String msg = "Beacons: start scanning...";
                PositionText.setText(msg);
                Log.d(TAG, msg);
                beaconManager.startRanging(region);
            }
        });
        actualRoom = rooms.BEDROOM;
        if(zwaveRequest.networkInfo(actualRoom, toast))
        {
            progressDialog = new ProgressDialog(MainActivity.this, "Scanning network");
            progressDialog.show();
        }


    }


    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();

    }

}


// This class is used to filter input, you won't be using it.

class InputFilterMinMax implements InputFilter {
    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
