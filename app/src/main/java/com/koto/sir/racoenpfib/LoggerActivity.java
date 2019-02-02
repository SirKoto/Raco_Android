package com.koto.sir.racoenpfib;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.koto.sir.racoenpfib.databases.AuthState;
import com.koto.sir.racoenpfib.databases.PagerManager;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.services.AvisosWorker;

import static com.koto.sir.racoenpfib.databases.AuthState.REDIRECT_URI;


public class LoggerActivity extends AppCompatActivity {
    public static final int REQUEST_AUTH = 1;
    private static final String TAG = "LoggerActivity";
    private static boolean b = false;

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d(TAG, "uri: " + uri.toString());
            if (uri.toString().startsWith(REDIRECT_URI)) {
                if (uri.getQueryParameter("error") != null) {
                    //en cas d'error
                    //Todo: error handling
                    Toast.makeText(this, "Error a l'init", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Log.d(TAG, "Uri not null and OK");
                    String code = uri.getQueryParameter("code");
                    new FetchNewToken().execute(code);
                }
            }
        } else {
            if (b) {
                finish();
                b = false;
            }
            b = true;
            Log.i(TAG, "Not init");
            AuthState.newInstance(this);
        }
    }

    private void onFetchResult(AuthState authState) {
        if (authState != null) {
            QueryData.setAuthState(authState);
            PagerManager.get().refresh();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (QueryData.getAuthState() != null) {
            AvisosWorker.SetRecurrentWork();
            Log.d(TAG, "Alarma activada a onDestroy de LoggerAct");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchNewToken extends AsyncTask<String, Void, AuthState> {

        @Override
        protected AuthState doInBackground(String... strings) {
            return AuthState.create(strings[0]);
        }

        @Override
        protected void onPostExecute(AuthState authState) {
            onFetchResult(authState);
        }

    }
}
