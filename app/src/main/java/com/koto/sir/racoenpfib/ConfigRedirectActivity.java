package com.koto.sir.racoenpfib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.koto.sir.racoenpfib.databases.PagerManager;
import com.koto.sir.racoenpfib.pages.CalendarFragment;
import com.koto.sir.racoenpfib.pages.ConfigFragment;

public class ConfigRedirectActivity extends SingleFragmentActivity {
    private static final String TAG = "ConfigRedirectActivity";
    private static final String EXTRA_TAG = "com.koto.sir.ConfigRedirecActivity.EXTRA_TAG";
    private int mTag = 0;

    public static Intent newIntent(Context context, int tag) {
        Intent intent = new Intent(context, ConfigRedirectActivity.class);
        intent.putExtra(EXTRA_TAG, tag);
        return intent;
    }


    @Override
    protected Fragment createFragment() {
        mTag = getIntent().getIntExtra(EXTRA_TAG, -1);
        Fragment fragment = null;
        switch (mTag) {
            case PagerManager.CONFIG_PAGE:
                //TODO
                fragment = null;
                break;
            case PagerManager.CALENDAR_PAGE:
                fragment = CalendarFragment.newInstance();
                break;
            case PagerManager.RSS_MOVILITAT_PAGE:
                fragment = null;
                break;
            case PagerManager.LAYOUT_MENU:
                fragment = null;
                break;
            default:
        }
        return fragment;
    }


    @Override
    protected void onStart() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(getResources()
                .getString(ConfigFragment.sTitleRes[mTag]));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorOnPrimary));
//        toolbar.setSubtitleTextColor(getResources().getColor(R.color.colorOnPrimary));

        final Drawable upArrow;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            upArrow = getDrawable(R.drawable.abc_ic_ab_back_material);
        } else upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);

        try {
            upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(upArrow);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error on upArrow", e);
            toolbar.setVisibility(View.GONE);
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
