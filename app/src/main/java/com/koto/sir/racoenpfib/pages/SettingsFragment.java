package com.koto.sir.racoenpfib.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AuthState;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.services.AvisosWorker;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private DialogInterface.OnClickListener dialogDeleteListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //clicat borrar
                    AvisosLab.get(getActivity()).deleteData();
                    Toast.makeText(getActivity(), "Data deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VisibleFragment.ACTION_JUST_RELOAD);
                    RacoEnpFibApp.getAppContext().sendBroadcast(intent, AvisosWorker.PERM_PRIVATE);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No fer res
                    break;
            }
        }
    };
    private DialogInterface.OnClickListener dialogDeleteTokenListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //clicat borrar
                    AuthState authState = QueryData.getAuthState();
                    if (authState != null) new DeleteToken().execute(authState);
                    QueryData.setAuthState(null);
                    Toast.makeText(getActivity(), "Token deleted", Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No fer res
                    break;
            }
        }
    };

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        if (preference.getKey().equals(getResources().getString(R.string.key_delete_avisos))) {
            Log.i(TAG, "Preference clicked Avisos");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle("Are you sure?")
                    .setMessage("This will delete all the data, and it will become impossible to restore")
                    .setPositiveButton("Delete", dialogDeleteListener)
                    .setNegativeButton("Cancel", dialogDeleteListener)
                    .show();
            return true;
        } else if (preference.getKey().equals(getResources().getString(R.string.key_delete_token))) {
            Log.i(TAG, "Preference clicked Token");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle("Are you sure?")
                    .setMessage("This will delete your token, and you will need to log in again")
                    .setPositiveButton("Delete", dialogDeleteTokenListener)
                    .setNegativeButton("Cancel", dialogDeleteTokenListener)
                    .show();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public static class DeleteToken extends AsyncTask<AuthState, Void, Void> {
        @Override
        protected Void doInBackground(AuthState... authStates) {
            authStates[0].deleteToken();
            return null;
        }
    }
}
