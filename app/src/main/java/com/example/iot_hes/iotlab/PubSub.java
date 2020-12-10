package com.example.iot_hes.iotlab;

import android.content.Context;
import android.util.Log;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;


public class PubSub implements MessageReceiver {

    private Publisher publisher;
    private Subscriber subscriber;
    private String project_id;
    private ServiceAccountCredentials pub_credentials;
    private Credentials sub_credentials;
    private final String TAG = "PubSub";
    private Function<String, Void> sub_cb;

    private boolean setPubCredentials(Context context) {
        boolean result = true;
        InputStream is = context.getResources().openRawResource(R.raw.user_service);
        try {
            pub_credentials = ServiceAccountCredentials.fromStream(is);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean setSubCredentials(Context context) {
        boolean result = true;
        InputStream is = context.getResources().openRawResource(R.raw.user_service);
        try {
            sub_credentials = ServiceAccountCredentials.fromStream(is);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public PubSub(Context context, String project_id)
    {

        this.project_id = project_id;

        if(!setPubCredentials(context))
        {
            Log.d(TAG, "Failed to obtain credentials : Publish");
            return;
        }
        if(!setSubCredentials(context))
        {
            Log.d(TAG, "Failed to obtain credentials : Subscribe");
            return;
        }

        Log.d(TAG, "PubSub object created successfully");

    }

    public void publish(String topic, String payload) throws InterruptedException {
        String _topic = "projects/" + project_id + "/topics/" + topic;

        try {
            publisher = Publisher.newBuilder(_topic)
                    .setCredentialsProvider(FixedCredentialsProvider.create(pub_credentials))
                    .build();
        } catch (IOException e) {
            Log.d(TAG, "Exception while creating publisher : " + e.getMessage());
            e.printStackTrace();
        }

        PubsubMessage msg = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(payload))
                .build();
        Log.d(TAG, "Publishing to : " + _topic);
        ApiFuture<String> msgIdFuture = publisher.publish(msg);
        ApiFutures.addCallback(msgIdFuture, new ApiFutureCallback<String>() {
                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "Publish failed : " + t.getMessage());
                        t.printStackTrace();
                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "Publish success with id : " + result);
                    }
                },
                MoreExecutors.directExecutor());
    }

    public void subscribe(String topic, Function<String, Void> func)
    {
        ProjectSubscriptionName sub_name = ProjectSubscriptionName.of(this.project_id, topic);
        this.sub_cb = func;

        this.subscriber = Subscriber.newBuilder(sub_name, this)
                .setCredentialsProvider(FixedCredentialsProvider.create(sub_credentials))
                .build();
        subscriber.startAsync().awaitRunning();
        //subscriber.awaitTerminated();
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        Log.d(TAG, "Received :" + message.getData().toStringUtf8());
        consumer.ack();
        sub_cb.apply(message.getData().toStringUtf8());
    }
}
