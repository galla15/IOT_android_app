package com.example.iot_hes.iotlab;

import android.content.Context;
import android.util.Log;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.io.InputStream;

public class PubSub {

    private Publisher publisher;
    private Subscriber subscriber;
    private String project_id;
    private ProjectTopicName _topic;
    private Credentials pub_credentials;
    private Credentials sub_credentials;
    private final String TAG = "PubSub";

    private boolean setCredentials(Context context)
    {
        boolean result = true;
        InputStream is = context.getResources().openRawResource(R.raw.user_service);

        try {
            pub_credentials = GoogleCredentials.fromStream(is)
                    .createScoped("https://pubsub.googleapis.com/google.pubsub.v1.Publisher");
            sub_credentials = GoogleCredentials.fromStream(is)
                    .createScoped("https://pubsub.googleapis.com/google.pubsub.v1.Subscriber");
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    public PubSub(Context context, String project_id)
    {
        this.project_id = project_id;

        if(!setCredentials(context))
        {
            Log.d(TAG, "Failed to obtain credentials");
        }
        else
        {
            Log.d(TAG, "PubSub object created successfully");
        }

    }

    public void publish(String topic, String payload)
    {
        _topic = ProjectTopicName.of(this.project_id, topic);
        try {
            publisher = Publisher.newBuilder(_topic)
                    .setCredentialsProvider(FixedCredentialsProvider.create(pub_credentials))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PubsubMessage msg = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(payload)).build();
        ApiFuture<String> msgIdFuture = publisher.publish(msg);
        ApiFutures.addCallback(msgIdFuture, new ApiFutureCallback<String>() {
            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Publish success with id : " + result);
            }
        }, MoreExecutors.directExecutor());
    }
}
