package com.koto.sir.racoenpfib.databases;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import android.util.Log;

import com.koto.sir.racoenpfib.models.Adjunt;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fetchr {
    private static final String TAG = "Fetchr";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        return getUrlBytes(urlSpec, null, null);
    }

    public byte[] getUrlBytes(String urlSpec, String token, String mime_type) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", mime_type);
        }
        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {


            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    //TODO EL TOKEN HA ESTAT ESBORRAT. WHAT TO DO?????????????????????
                }
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public byte[] getUrlBytesPost(String urlSpec, String content) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            Log.d(TAG, "Url: " + urlSpec + " ////Content " + content);
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(content);
            wr.flush();
            wr.close();
            Log.d(TAG, "Post sent with result before: " + connection.getResponseCode() + " " + connection.getResponseMessage());

            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream out;
                Log.d(TAG, "Post sent with result: " + connection.getResponseCode());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
                }

                out = new ByteArrayOutputStream();
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public AuthState createAuthState(String urlSpec, String content) {
        try {
            String data = new String(getUrlBytesPost(urlSpec, content));
            Log.d(TAG, "Data retrieved " + data);
            JSONObject jsonObject;
            jsonObject = new JSONObject(data);
            String token = jsonObject.getString("access_token");
            int timeRemaining = jsonObject.getInt("expires_in");
            String refreshToken = jsonObject.getString("refresh_token");
            return new AuthState(token, refreshToken, timeRemaining);

        } catch (IOException e) {
            Log.e(TAG, "not AuthCreated form url " + urlSpec, e);
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "not Json Auth not created from" + urlSpec, e);
            return null;
        }
    }

    public String getDataUrlJson(String url) {
        try {
            AuthState authState = QueryData.getAuthState();
            Log.d(TAG, "getdataurl autstate==null" + (authState == null));
            if (authState == null) return "";
            String token = authState.getToken();
            if (token.equals("")) throw new IOException("Empty Token");
            return new String(getUrlBytes(url, token, "application/json"));
        } catch (IOException e) {
            Log.e(TAG, "Error reading data with token", e);
            return "";
        }
    }

    public void downloadFile(Context context, Adjunt adjunt) {

        //TODO CHECK SI HI HA INTERNET


        Uri url = Uri.parse(adjunt.getUrl());
        Log.d(TAG, "DownloadFile " + adjunt.toString());
        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setMimeType(adjunt.getMimeType());

        AuthState authState = QueryData.getAuthState();
        if (authState == null) return;
        String token = authState.getToken();
        if (token.equals("")) return;
        request.addRequestHeader("Authorization", "Bearer " + token);

        request.setTitle(adjunt.getNom());

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, adjunt.getNom());

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
