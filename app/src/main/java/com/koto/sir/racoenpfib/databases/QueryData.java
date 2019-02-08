package com.koto.sir.racoenpfib.databases;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.models.CalendarClasses;
import com.koto.sir.racoenpfib.pages.MobilityFragment;

import java.util.ArrayList;
import java.util.List;

public class QueryData {
    private static final String TAG = "QueryData";
    private static final String PREF_AUTH_STATE = "search_AuthState";
    private static final String PREF_LIST_PAGES = "list_pages";
    private static final String PREF_CALENDAR = "calendar_classes";
    private static final String LAST_UPDATED_AVIS = "last_updated_avis";
    private static final String MOBILITY_RSS = "mob_rss";
    private static final String ALARM_AVISOS_ON = "alarma_avisos_on";

    public static boolean getAlarmaAvisos() {
        return PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getBoolean(ALARM_AVISOS_ON, false);
    }

    public static void setAlarmaAvisos(boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putBoolean(ALARM_AVISOS_ON, isOn)
                .apply();
    }

    public static long getLastUpdatedAvis() {
        return PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getLong(LAST_UPDATED_AVIS, 0);
    }

    public static void setLastUpdatedAvis(long data) {
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putLong(LAST_UPDATED_AVIS, data)
                .apply();
    }

    @NonNull
    public static List<MobilityFragment.DataRSS> getDataRss() {
        String jsonData = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getString(MOBILITY_RSS, null);
        if (jsonData == null)
            return new ArrayList<MobilityFragment.DataRSS>(0);
        Gson gson = new Gson();
        List<MobilityFragment.DataRSS> data = gson.fromJson(jsonData, new TypeToken<List<MobilityFragment.DataRSS>>() {
        }.getType());
        return data == null ? new ArrayList<MobilityFragment.DataRSS>(0) : data;
    }

    public static void setDataRss(List<MobilityFragment.DataRSS> dataRss) {
        Gson gson = new Gson();
        String data = gson.toJson(dataRss, new TypeToken<List<MobilityFragment.DataRSS>>() {
        }.getType());
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putString(MOBILITY_RSS, data)
                .apply();
    }

    public static List<CalendarClasses> getCalendar() {
        String jsonData = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getString(PREF_CALENDAR, null);
        if (jsonData == null)
            return null;
        Gson gson = new Gson();
        return gson.fromJson(jsonData, new TypeToken<List<CalendarClasses>>() {
        }.getType());
    }

    public static void setCalendar(List<CalendarClasses> pages) {
        Gson gson = new Gson();
        String data = gson.toJson(pages, new TypeToken<List<CalendarClasses>>() {
        }.getType());
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putString(PREF_CALENDAR, data)
                .apply();
        Log.d(TAG, data);
    }


    public static List<Integer> getListPages() {
        String jsonData = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getString(PREF_LIST_PAGES, null);
        if (jsonData == null)
            return null;
        Gson gson = new Gson();
        return gson.fromJson(jsonData, new TypeToken<List<Integer>>() {
        }.getType());
    }

    public static void setListPages(List<Integer> pages) {
        Gson gson = new Gson();
        String data = gson.toJson(pages, new TypeToken<List<Integer>>() {
        }.getType());
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putString(PREF_LIST_PAGES, data)
                .apply();
        Log.d(TAG, data);
    }

    public static AuthState getAuthState() {
        String jsonData = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .getString(PREF_AUTH_STATE, null);
        if (jsonData == null)
            return null;
        Gson gson = new Gson();
        return gson.fromJson(jsonData, AuthState.class);
    }

    public static void setAuthState(AuthState authState) {
        Gson gson = new Gson();
        String data = gson.toJson(authState);
        PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext())
                .edit().putString(PREF_AUTH_STATE, data)
                .apply();
        Log.d(TAG, data);
    }

}
