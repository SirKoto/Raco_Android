package com.koto.sir.racoenpfib.services;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receiver result: " + getResultCode());


        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext());
        boolean b = preferences.getBoolean(RacoEnpFibApp.getAppContext().getResources().getString(R.string.notifications_on), true);
        Log.d(TAG, "Preferences " + b);

        int requestCode = intent.getIntExtra(AvisosWorker.REQUEST_CODE, 0);
        Notification notification = intent.getParcelableExtra(AvisosWorker.NOTIFICATION);
        Log.d(TAG, "Notification " + notification);
        if (b) NotificationManagerCompat.from(context)
                .notify(requestCode, notification);

    }
}
