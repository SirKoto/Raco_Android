package com.koto.sir.racoenpfib.pages;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.services.AvisosWorker;

import java.util.UUID;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";
    private static UUID mUUID = new UUID(0, 0);

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "Canceling notification");
            boolean b = false;
            if (getResultCode() == Activity.RESULT_OK) {
                setResultCode(Activity.RESULT_CANCELED);
                b = true;
            }
            UUID uuid = (UUID) intent.getSerializableExtra(AvisosWorker.UNIQUE_IDENTIFIER);
            if (uuid != mUUID) {
                Log.d(TAG, "callingOnNewDataFOund");
                OnNewDataFound();
                mUUID = uuid;

                if (b) {
                    //TODO canviar el toast per una barra inferior, o alguna cosa més descriptiva
                    Toast.makeText(context, R.string.new_notification, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    protected abstract void OnNewDataFound();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated, register " + (this instanceof AvisosFragment));
        super.onViewCreated(view, savedInstanceState);
        IntentFilter filter = new IntentFilter(AvisosWorker.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                AvisosWorker.PERM_PRIVATE, null);
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView, unregister " + (this instanceof AvisosFragment));
        super.onDestroyView();
        getActivity().unregisterReceiver(mOnShowNotification);

    }
}
