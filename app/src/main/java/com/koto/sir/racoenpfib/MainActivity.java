package com.koto.sir.racoenpfib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.services.AvisosService;

import java.util.UUID;


public class MainActivity extends SingleFragmentActivity {
    private static final String TAG = "MainActivity";
    private static final String UUID_EXTRA = "com.koto.sir.uuid_extra";

    public static Intent newIntent(Context context, UUID uuid) {
        //TODO
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(UUID_EXTRA, uuid);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID uuid = (UUID) getIntent().getSerializableExtra(UUID_EXTRA);
        return PagerFragment.newInstance(uuid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AvisosService.toggleAlarm(RacoEnpFibApp.getAppContext(), true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //TODO NO MIRAR AIXO SEMPRE; NOMES SI EL CANAL NO ESTÀ CREAT
            NotificationChannel channel = new NotificationChannel(AvisosService.CHANEL_ID, "Avisos poll", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Poll the API to get new notifications");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        AvisosService.toggleAlarm(RacoEnpFibApp.getAppContext(), false);
    }
}
