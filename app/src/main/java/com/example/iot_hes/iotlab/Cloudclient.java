package com.example.iot_hes.iotlab;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.things.iotcore.ConnectionParams;
import com.google.android.things.iotcore.IotCoreClient;
import com.google.android.things.iotcore.TelemetryEvent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Cloudclient {
    private static final String TAG = "Cloud_client";
    private IotCoreClient client;
    private ConnectionParams params;

    private final String project_id = "smartbuilding-297507";
    private final String cloud_region = "europe-west1";
    private KeyPair key;

    private String reg_id;
    private String dev_id;

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

    public Cloudclient(Context context, String registry_id, String device_id) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        Log.d(TAG, "Creating new client");
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
        client.publishTelemetry(event);


    }
}
