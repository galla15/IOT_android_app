package com.example.iot_hes.iotlab;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/*
This class provides a loading window
 */
class ProgressDialog {

    private AlertDialog.Builder builder;
    private View layoutInflater = null;
    private TextView messageTextView;
    private AlertDialog progressDialog;
    private boolean running = false;

    public ProgressDialog(Context context, String msg){
        builder = new AlertDialog.Builder(context);
        layoutInflater = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        messageTextView = layoutInflater.findViewById(R.id.messageTextView);
        messageTextView.setText(msg);

        builder.setView(layoutInflater);
        builder.setCancelable(false);
        progressDialog = builder.create();
    }

    public void show()
    {
        running = true;
        progressDialog.show();
    }

    public void stop()
    {
        running = false;
        progressDialog.dismiss();
    }
}

