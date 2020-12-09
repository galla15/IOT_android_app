package com.example.iot_hes.iotlab;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.things.iotcore.ConnectionParams;
import com.google.android.things.iotcore.IotCoreClient;
import com.google.android.things.iotcore.TelemetryEvent;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Cloudclient {
    private static final String TAG = "Cloud_client";
    private IotCoreClient client;
    private ConnectionParams params;

    private final String serverUri = "ssl://mqtt.googleapis.com:8883";
    private final String project_id = "smartbuilding-297507";
    private final String cloud_region = "europe-west1";
    private KeyPair key;

    private String reg_id;
    private String dev_id;

    private MqttAndroidClient mqttClient;
    private  IMqttToken connectionToken = null;
    private MqttConnectOptions connectOptions;
    private  String clientId = "android_app";

    private String read_InputStream(InputStream is) throws IOException {
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while (((str = reader.readLine()) != null))
        {
            sb.append(str);
        }

        return sb.toString();
    }

    private KeyPair read_keys(Context context) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        KeyPair keys;

        InputStream in = context.getResources().openRawResource(R.raw.rsa_public);
        String public_key = read_InputStream(in);

        public_key = public_key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(),"")
                    .replace("-----END PUBLIC KEY-----", "");
        //Log.d(TAG, public_key);

        in = context.getResources().openRawResource(R.raw.rsa_private);
        String private_key = read_InputStream(in);
        private_key = private_key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(),"")
                    .replace("-----END PRIVATE KEY-----", "");
        //Log.d(TAG, private_key);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        byte [] pub_encoded = Base64.decode(public_key, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub_encoded);
        PublicKey pub_key = kf.generatePublic(keySpec);

        byte [] priv_encoded = Base64.decode(private_key, Base64.DEFAULT);
        PKCS8EncodedKeySpec p_keySpec = new PKCS8EncodedKeySpec(priv_encoded);
        PrivateKey pri_key = kf.generatePrivate(p_keySpec);

        return new KeyPair(pub_key, pri_key);
    }

    static MqttCallback mCallback;
    static long MINUTES_PER_HOUR = 60;

    /** Create a Cloud IoT Core JWT for the given project id, signed with the given RSA key. */
    private String createJwtRsa(Context context, String projectId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Date now = new Date();
        // Create a JWT to authenticate this device. The device will be disconnected after the token
        // expires, and will have to reconnect with a new token. The audience field should always be set
        // to the GCP project id.
        JwtBuilder jwtBuilder =
                Jwts.builder()
                        .setIssuedAt(now)
                        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                        .setAudience(projectId);

        /*byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");*/

        KeyPair keys = read_keys(context);


        return jwtBuilder.signWith(keys.getPrivate(), SignatureAlgorithm.RS256).compact();
    }


    private String getClientID()
    {
        return "projects/" + project_id + "/locations/" + cloud_region + "/registries/"
                + reg_id + "/devices/" + dev_id;
    }

    private String getCommandTopic()
    {
        return "/devices/" + dev_id + "/commands";
    }

    private String getStateTopic()
    {
        return "/devices/" + dev_id + "/state";
    }

    private  String getConfigTopic()
    {
        return "/devices/" + dev_id + "/config";
    }

    private String getEventTopic()
    {
        return  "/devices/" + dev_id + "/events";
    }

    public Cloudclient(Context context, String registry_id, String device_id) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        reg_id = registry_id;
        dev_id = device_id;

        mqttClient = new MqttAndroidClient(context, serverUri,getClientID(), new MemoryPersistence());
        mqttClient.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI)
            {
                if(reconnect)
                {
                    Log.d(TAG, "Reconnected to : " + serverURI);
                }
                else
                {
                    Log.d(TAG, "Connected to : " + serverURI);
                    //send_cmd("KK");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The connection was lost : " + Log.getStackTraceString(cause));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "Incoming message : " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Message delivered");
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });


        connectOptions = new MqttConnectOptions();
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        Properties sslProps = new Properties();
        sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
        connectOptions.setSSLProperties(sslProps);

        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(false);
        connectOptions.setUserName("unused");

        InputStream is = context.getResources().openRawResource(R.raw.rsa_private);
        String private_key = read_InputStream(is);
        String passwd = createJwtRsa(context, project_id);
        connectOptions.setPassword(passwd.toCharArray());

        /*Log.d(TAG, "Creating new client");
        reg_id = registry_id;
        dev_id = device_id;
        key = read_keys(context);

        Log.d(TAG, "Building connection params");
        params = new ConnectionParams.Builder()
                .setProjectId(project_id)
                .setRegistry(reg_id, cloud_region)
                .setDeviceId(dev_id)
                .build();

        Log.d(TAG, "Building iotcore client");
        client = new IotCoreClient.Builder()
                .setConnectionParams(params)
                .setKeyPair(key)
                .build();

        client.connect();

        client.publishDeviceState("Hello".getBytes());

        TelemetryEvent event = new TelemetryEvent("blind open \'4/1\'".getBytes(), "/devices/" + dev_id, TelemetryEvent.QOS_AT_LEAST_ONCE);
        client.publishTelemetry(event);*/

    }

    private IMqttToken connect()
    {
        IMqttToken token = null;
        try{
            token = mqttClient.connect(connectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttClient.setBufferOpts(disconnectedBufferOptions);
                    //subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect to : " + serverUri + "\n" +
                            "Error : " + exception.getMessage());
                }
            });
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
        return token;
    }

    public void send_cmd(String cmd) {

        if(!mqttClient.isConnected()) {
            connectionToken = connect();
        }

        connectionToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                //Im connected
                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload("Hello from android".getBytes(StandardCharsets.UTF_8.name()));
                    message.setQos(0);
                    String topic = getEventTopic();
                    mqttClient.publish(topic, message, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d(TAG, "Publish success");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d(TAG, "Publish failed : " + exception +"\n" + Log.getStackTraceString(exception));
                        }
                    });
                } catch (MqttException | UnsupportedEncodingException e) {
                    Log.d(TAG, "Publish exception : " + Log.getStackTraceString(e));
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d(TAG, Log.getStackTraceString(exception));
            }
        });


    }

}
