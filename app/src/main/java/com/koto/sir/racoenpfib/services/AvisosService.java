package com.koto.sir.racoenpfib.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.koto.sir.racoenpfib.MainActivity;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AuthState;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.models.Avis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Deprecated
public class AvisosService extends IntentService {
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String UNIQUE_IDENTIFIER = "IDENTIFIER_NOTIFICATION";
    public static final String ACTION_SHOW_NOTIFICATION = "com.koto.sir.racoenpfib.SHOW_NOTIFICATION";
    public static final String GROUP_KEY = "com.koto.sir.racoenpfib.GROUP_KEY";
    public static final String PERM_PRIVATE = "com.koto.sir.racoenpfib.PRIVATE";
    public static final String CHANEL_ID = "com.koto.sir.racoenpfib.chanel_id_notification_avisos";
    private static final String TAG = "AvisosService";
    private static final String URL = "https://api.fib.upc.edu/v2/jo/avisos/";
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10);
    private static final long POLL_INTERVAL_MS_FAST = TimeUnit.MINUTES.toMillis(1);


    public AvisosService() {
        super(TAG);
    }


    public static void setServiceAlarmOn(Context context, boolean isOn) {
        Log.i(TAG, "SetServiceAlarmOn now: " + isOn);
        Intent i = AvisosService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (isOn)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS_FAST,
                    pi);
        else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryData.setAlarmaAvisos(isOn);
    }

    public static void toggleAlarm(Context context, boolean fastPoll) {
        Log.i(TAG, "toggleAlarm Fast: " + fastPoll);
        if (isServiceOn(context)) {
            Log.i(TAG, "toggleAlarg, service is ON");
            Intent i = AvisosService.newIntent(context);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pi);

            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    fastPoll ? POLL_INTERVAL_MS_FAST : POLL_INTERVAL_MS,
                    pi);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AvisosService.class);
    }

    public static boolean isServiceOn(Context context) {
        Intent i = AvisosService.newIntent(context);
        return null !=
                PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");
        if (!isNetworkAvailableAndConnected()) return;
        String dataJson = new Fetchr().getDataUrlJson(URL);
        Log.d(TAG, "dataJson: " + dataJson);
        final long last = QueryData.getLastUpdatedAvis();
        long newer = last;
        List<Avis> renovats = new ArrayList<>();
        try {
            JSONArray avisosArray = new JSONObject(dataJson).getJSONArray("results");

            for (int i = 0; i < avisosArray.length(); ++i) {
                JSONObject avisJson = avisosArray.getJSONObject(i);
                long tmp = AvisosLab.stringToDate(avisJson.getString("data_modificacio")).getTime();
                if (tmp > last) {
                    if (tmp > newer) newer = tmp;
                    //carreguem i guardem l'objecte
                    Avis avis = new Avis(UUID.randomUUID());
                    Log.d(TAG, "avis per avis " + avisJson.toString());
                    AvisosLab.parseAvis(avis, avisJson);
                    renovats.add(avis);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error en la creacio d'avisos", e);
            //Desem que es provable que hi hagui error ja que no hi ha login
            return;
        }
        //Guardar tots els nous avisos modificats
        Log.d(TAG, "avisos renovats: " + renovats.toString());
        for (Avis avis : renovats) {
            AvisosLab.get(this).addAvis(avis);
        }
        if (newer != last) {

            QueryData.setLastUpdatedAvis(newer);

            //CREEM LA NOTIFICACIO
            UUID uuid = UUID.randomUUID();
            for (Avis avis : renovats) {
                Intent i = MainActivity.newIntent(this, avis.getUid());
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

                Notification notification = new NotificationCompat.Builder(this, CHANEL_ID)
                        .setTicker(avis.getTitol())
                        .setSmallIcon(R.drawable.twotone_shutter_speed_24)
                        .setContentTitle(avis.getAssignatura())
                        .setContentText(avis.getTitol())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(Html.fromHtml("<b>" + avis.getTitol() + "</b><br>" + avis.getText())))
                        .setWhen(avis.getDataModificacio().getTime())
                        .setGroup(GROUP_KEY)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                Log.d(TAG, "showBackgroundNotification " + avis.getTitol());
                showBackgroundNotification(0, notification, uuid);
            }


        }

//        AvisosLab.get(this).deleteDuplicateRows();
    }

    private void showBackgroundNotification(int requestCode, Notification notification, UUID uuid) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        i.putExtra(UNIQUE_IDENTIFIER, uuid);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK,
                null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnected();
    }
}
