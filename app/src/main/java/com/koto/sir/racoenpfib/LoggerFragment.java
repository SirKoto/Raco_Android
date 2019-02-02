package com.koto.sir.racoenpfib;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LoggerFragment extends AbstractPagerFragments {
    private static final String TAG = "LoggerFragment";
    private static final int REQUEST_LOGGER = 11231;

    private Button mLogInButton;

    public static LoggerFragment newInstance() {
        return new LoggerFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGGER) {
            Log.d(TAG, "S'ha creat el loger, toca provar d'accedir i renovar-lo");
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.logger_fragment, container, false);

        mLogInButton = v.findViewById(R.id.loggin_button);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getActivity()
                        .getSystemService(CONNECTIVITY_SERVICE);

                boolean b = (cm.getActiveNetworkInfo() != null)
                        && cm.getActiveNetworkInfo().isConnected();
                if (b)
                    startActivityForResult(new Intent(getActivity(), LoggerActivity.class), REQUEST_LOGGER);
                else
                    Toast.makeText(getActivity(), R.string.not_internet, Toast.LENGTH_LONG).show();
            }
        });


        return v;
    }

    //TODO: Aquests metodes son temporals, ja que LoggerFragment en teoria no disposa de Dades
    @Override
    public int getIcon() {
        return android.R.drawable.btn_star;
    }

}
