package com.example.gatherup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

class MyNetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "MyNetworkReceiver";
    //private Context context;
    private Snackbar snackbar;

    public MyNetworkReceiver(Context context) {
        //this.context = context;
        this.snackbar = Snackbar.make(((Activity)context).findViewById(android.R.id.content), "Network connection is not available", Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if(isConnected) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
                Log.d(TAG, "Internet connection is connected");
            } else {
                if (snackbar != null) {
                    snackbar.show();
                }
                Log.d(TAG, "Internet connection is not connected");
            }
        }
    }
}
