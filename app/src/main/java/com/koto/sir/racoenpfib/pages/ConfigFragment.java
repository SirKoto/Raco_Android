package com.koto.sir.racoenpfib.pages;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koto.sir.racoenpfib.AbstractPagerFragments;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.PagerManager;

import java.util.ArrayList;
import java.util.List;

public class ConfigFragment extends AbstractPagerFragments {
    private static final int[] sImageRes = new int[4];
    private static final int[] sTitleRes = new int[4];
    private static final int[] sInfoRes = new int[4];

    static {
        sTitleRes[PagerManager.CONFIG_PAGE] = R.string.configuration;
        sInfoRes[PagerManager.CONFIG_PAGE] = R.string.configure_info;
        sImageRes[PagerManager.CONFIG_PAGE] = R.drawable.ic_build_black_24dp;

        sTitleRes[PagerManager.LAYOUT_MENU] = R.string.dashboard;
        sInfoRes[PagerManager.LAYOUT_MENU] = R.string.dashboard_info;
        sImageRes[PagerManager.LAYOUT_MENU] = R.drawable.ic_dashboard_black_24dp;

        sTitleRes[PagerManager.CALENDAR_PAGE] = R.string.calendar;
        sInfoRes[PagerManager.CALENDAR_PAGE] = R.string.calendar_info;
        sImageRes[PagerManager.CALENDAR_PAGE] = R.drawable.ic_baseline_calendar_today_24px;

        sTitleRes[PagerManager.RSS_MOVILITAT_PAGE] = R.string.rss;
        sInfoRes[PagerManager.RSS_MOVILITAT_PAGE] = R.string.rss_info;
        sImageRes[PagerManager.RSS_MOVILITAT_PAGE] = R.drawable.ic_rss_feed_black_24dp;
    }


    RecyclerView mRecyclerView;


    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.config_fragment, container, false);
        mRecyclerView = v.findViewById(R.id.recycler_view_config);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(new ConfigAdapter(PagerManager.get().getConfig()));

        return v;
    }

    @Override
    public int getIcon() {
        return R.drawable.twotone_home_black_24;
    }

    private class ConfigHolder extends RecyclerView.ViewHolder {
        AppCompatImageView mCercle;
        AppCompatTextView mTitle;
        AppCompatTextView mInfo;

        public ConfigHolder(@NonNull View itemView) {
            super(itemView);
            mCercle = itemView.findViewById(R.id.image_config);
            mTitle = itemView.findViewById(R.id.title_config);
            mInfo = itemView.findViewById(R.id.config_info);

            itemView.setClickable(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                itemView.setForeground(getSelectedItemDrawable());
            }
        }

        public void bind(int i) {
            mCercle.setImageResource(sImageRes[i]);
            mTitle.setText(sTitleRes[i]);
            mInfo.setText(sInfoRes[i]);
        }
        private Drawable getSelectedItemDrawable() {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray ta = getActivity().obtainStyledAttributes(attrs);
            Drawable selectedItemDrawable = ta.getDrawable(0);
            ta.recycle();
            return selectedItemDrawable;
        }
    }

    private class ConfigAdapter extends RecyclerView.Adapter<ConfigHolder> {
        List<Integer> mInfo;

        public ConfigAdapter(List<Integer> info) {
            mInfo = info;
        }

        @NonNull
        @Override
        public ConfigHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.config_card, viewGroup, false);
            return new ConfigHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ConfigHolder configHolder, int i) {
            configHolder.bind(mInfo.get(i));
        }

        @Override
        public int getItemCount() {
            return mInfo.size();
        }
    }


}
