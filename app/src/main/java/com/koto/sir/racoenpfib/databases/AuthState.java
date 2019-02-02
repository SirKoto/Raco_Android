package com.koto.sir.racoenpfib.databases;

import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class AuthState {
    private static final String TAG = "AuthState";

    private static final String CLIENT_ID = "fXVCB6ZjPNZK8Zx98dAZkGgxFO2A0Ir97dqapDXO";
    private static final String SECRET_ID = "vyUaogVm44ssM8FyM4q3DBmGIxxf6eezgpdbD3vRVB2320zO7qOU05uEdxnkyEYIj36WvcxaYpB914jQ0h5ebPIxB0lnyAubpYTf10jvDs27gW4Ovvak7PYV9tTBknGV";
    public static final String REDIRECT_URI = "apifib://racoenp";
    private static final Uri ENDPOINT_Auth = Uri.parse("https://api.fib.upc.edu/v2/o/authorize/");
    private static final String ENDPOINT_Token = "https://api.fib.upc.edu/v2/o/token/";
    private static final String ENDPOINT_Revoke = "https://api.fib.upc.edu/v2/o/revoke_token/";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String GRANT_TYPE2 = "refresh_token";
    private static final String SCOPE = "read";

    private String mToken;
    private String mRefreshToken;
    private long mTime;

    public AuthState(String token, String refreshToken, int time) {
        mToken = token;
        mRefreshToken = refreshToken;
        mTime = time * 100 + Calendar.getInstance().getTimeInMillis() - 5000;
    }

    public static void newInstance(Context context) {
        Uri.Builder builder = ENDPOINT_Auth.buildUpon();
        Uri url = builder
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", SCOPE)
//                .appendQueryParameter("state", "CACAtuaCCA")
                .appendQueryParameter("approval_prompt", "auto")
                .build();
        Log.d(TAG, "newInstance -> url: " + url.toString());

        new CustomTabsIntent.Builder().build().launchUrl(context, url);
    }

    public static AuthState create(String authCode) {
        Uri uri = new Uri.Builder()
                .appendQueryParameter("grant_type", GRANT_TYPE)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("code", authCode)
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("client_secret", SECRET_ID)
                .build();
        Log.d(TAG, "create -> url: " + uri.toString());
        Log.d(TAG, "Endpoint :" + ENDPOINT_Token);
        return new Fetchr().createAuthState(ENDPOINT_Token.toString(), uri.toString().substring(1));
    }

    public boolean renewToken() {
        Uri uri = new Uri.Builder()
                .appendQueryParameter("grant_type", GRANT_TYPE2)
                .appendQueryParameter("refresh_token", mRefreshToken)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("client_secret", SECRET_ID)
                .build();
        Log.d(TAG, "create renew -> url: " + uri.toString());
        AuthState tmp = new Fetchr().createAuthState(ENDPOINT_Token.toString(), uri.toString().substring(1));
        if (tmp == null) {
            return false;
        }
        setSelf(tmp);
        QueryData.setAuthState(this);
        return true;
    }

    public String getToken(){
        if (!isValidToken()) {
            if(!renewToken()){
                deleteToken();
                return "";
            }
        }
        return mToken;
    }

    public void deleteToken() {
        Uri uri = new Uri.Builder()
                .appendQueryParameter("token", mToken)
                .appendQueryParameter("client_id", CLIENT_ID)
                .build();
        try {
            new Fetchr().getUrlBytesPost(ENDPOINT_Revoke, uri.toString().substring(1));
            QueryData.setAuthState(null);
        } catch (IOException e) {
            Log.e(TAG, "Error deleting", e);
        }
        Log.i(TAG, "Token deleted");
    }

    private void setSelf(AuthState authState) {
        mRefreshToken = authState.mRefreshToken;
        mTime = authState.mTime;
        mToken = authState.mToken;
    }

    private boolean isValidToken() {
        return mTime > Calendar.getInstance().getTimeInMillis();
    }


}
