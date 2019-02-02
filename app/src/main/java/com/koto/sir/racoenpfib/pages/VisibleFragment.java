package com.koto.sir.racoenpfib.pages;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.services.AvisosService;

import java.util.UUID;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";
    private UUID mUUID = new UUID(0, 0);
    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Canceling notification");
            if (getResultCode() == Activity.RESULT_OK) {
                //TODO canviar el toast per una barra inferior, o alguna cosa més descriptiva
                Toast.makeText(context, R.string.new_notification, Toast.LENGTH_LONG).show();
                setResultCode(Activity.RESULT_CANCELED);
            }
            UUID uuid = (UUID) intent.getSerializableExtra(AvisosService.UNIQUE_IDENTIFIER);
            if (uuid != mUUID) {
                OnNewDataFound();
                mUUID = uuid;
            }
        }
    };

    protected abstract void OnNewDataFound();

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AvisosService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                AvisosService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}