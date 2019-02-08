package com.koto.sir.racoenpfib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.koto.sir.racoenpfib.databases.AuthState;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.pages.SettingsFragment;
import com.koto.sir.racoenpfib.services.AvisosWorker;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private BroadcastReceiver mOnTokenUnavaliable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View v = findViewById(R.id.fragment_container);
            Snackbar.make(v, "El token no es pot usar", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Log in", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AuthState authState = QueryData.getAuthState();
                            if (authState != null)
                                new SettingsFragment.DeleteToken().execute(authState);
                            QueryData.setAuthState(null);

                            ConnectivityManager cm = (ConnectivityManager) SingleFragmentActivity.this
                                    .getSystemService(CONNECTIVITY_SERVICE);

                            boolean b = (cm.getActiveNetworkInfo() != null)
                                    && cm.getActiveNetworkInfo().isConnected();
                            if (b) {
                                startActivity(
                                        new Intent(SingleFragmentActivity.this,
                                                LoggerActivity.class));
                            } else
                                Toast.makeText(SingleFragmentActivity.this, R.string.not_internet, Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }
    };


    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            if (fragment == null) finish();
            else
                fm.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Fetchr.ACTION_TOKEN_UNAUTHORIZED);
        registerReceiver(mOnTokenUnavaliable, filter,
                AvisosWorker.PERM_PRIVATE, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mOnTokenUnavaliable);
    }
}
