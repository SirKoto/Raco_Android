package com.koto.sir.racoenpfib.services;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.koto.sir.racoenpfib.MainActivity;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.models.Avis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class AvisosWorker extends Worker {
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String UNIQUE_IDENTIFIER = "IDENTIFIER_NOTIFICATION";
    public static final String ACTION_SHOW_NOTIFICATION = "com.koto.sir.racoenpfib.SHOW_NOTIFICATION";
    public static final String GROUP_KEY = "com.koto.sir.racoenpfib.avisosworker.GROUP_KEY";
    public static final String PERM_PRIVATE = "com.koto.sir.racoenpfib.PRIVATE";
    public static final String CHANEL_ID = "com.koto.sir.racoenpfib.chanel_id_notification_avisos";


    private static final String URL = "https://api.fib.upc.edu/v2/jo/avisos/";
    private static final String TAG = "AvisosWorker";
    private static ReentrantLock sLock = new ReentrantLock();

    public AvisosWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void SetRecurrentWork() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AvisosWorker.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!isNetworkAvailableAndConnected()) return Result.failure();
        sLock.lock();
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
            return Result.failure();
        }
        //Guardar tots els nous avisos modificats
        Log.d(TAG, "avisos renovats: " + renovats.toString());
        for (Avis avis : renovats) {
            AvisosLab.get(getApplicationContext()).addAvis(avis);
        }
        if (newer != last) {

            QueryData.setLastUpdatedAvis(newer);
            sLock.unlock();

            //CREEM LA NOTIFICACIO
            UUID uuid = UUID.randomUUID();
            for (Avis avis : renovats) {
                Log.d(TAG, "Avis uuid " + avis.getUid().toString());
                Intent i = MainActivity.newIntent(getApplicationContext(), avis.getUid());
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), avis.getUid().hashCode(), i, 0);

                Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANEL_ID)
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
                showBackgroundNotification(avis.getUid().hashCode(), notification, uuid);
            }


        } else
            sLock.unlock();

        return Result.success();
    }

    private void showBackgroundNotification(int requestCode, Notification notification, UUID uuid) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        i.putExtra(UNIQUE_IDENTIFIER, uuid);
        getApplicationContext().sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK,
                null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);

        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnected();
    }
}
