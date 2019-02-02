package com.koto.sir.racoenpfib.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.koto.sir.racoenpfib.databases.QueryData;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        if (QueryData.getAlarmaAvisos())
            AvisosService.setServiceAlarmOn(context, true);
    }

}
