package com.koto.sir.racoenpfib;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koto.sir.racoenpfib.databases.PagerManager;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.pages.AvisosManagerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PagerFragment extends Fragment implements PagerManager.CallbackManager {
    private static final String TAG = "PagerFragment";
    private static final int OFFSCREEN_PAGE_LIMIT = 2;
    private static final String ARG_UUID = "com.koto.sir.ARG_UUDI";
    private ViewPager mViewPager;
    private List<AbstractPagerFragments> mPages = new ArrayList<>();
    private boolean mNeedsRefresh, mAvisosPageSelected = false;
    private UUID mUUID;


    public static PagerFragment newInstance(UUID uuid) {
        PagerFragment fragment = new PagerFragment();
        if (uuid != null) {
            Bundle arg = new Bundle();
            arg.putSerializable(ARG_UUID, uuid);
            fragment.setArguments(arg);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PagerManager.get().assignCallback(this);

        Bundle arg = getArguments();
        if (arg != null) {
            mUUID = (UUID) arg.getSerializable(ARG_UUID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mNeedsRefresh) {
            Log.d(TAG, "onRefreshBoolean");
            loadPages();
            mViewPager.getAdapter().notifyDataSetChanged();
            mNeedsRefresh = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mUUID != null) {
            mViewPager.setCurrentItem(PagerManager.get().getAvisPos());
            loadPages();
            mUUID = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pager_fragment, container, false);
        mViewPager = v.findViewById(R.id.view_pager_main);

        loadPages();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int i) {
                Log.d(TAG, "get Item " + i);
                //ToDo: Fer que es creein els fragments només a aqui, i no a loadPages()
                return mPages.get(i);
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                int d = mPages.indexOf(object);
                return d == -1 ? FragmentStatePagerAdapter.POSITION_NONE : d;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                int res = mPages.get(position).getIcon();
                Drawable image;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    image = getActivity().getResources()
                            .getDrawable(res, null);
                } else {
                    image = getActivity().getResources()
                            .getDrawable(res);
                }
                image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                SpannableString sb = new SpannableString(" ");
                ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return sb;
            }

            @Override
            public int getCount() {
                return mPages.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mAvisosPageSelected = position == PagerManager.get().getAvisPos();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);

        TabLayout tabLayout = new TabLayout(getActivity());
        tabLayout.setupWithViewPager(mViewPager, true);

        mNeedsRefresh = false;


        return v;
    }

    public boolean onBackPressed() {
        if (mAvisosPageSelected) {
            AvisosManagerFragment fragment = (AvisosManagerFragment) mPages.get(PagerManager.get().getAvisPos());
            return fragment.onBackPressed();
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bundle arg = getArguments();
        if (arg != null) arg.clear();
    }

    public void loadPages() {
        mPages.clear();
        if (mUUID != null) Log.d(TAG, "loadPages: " + mUUID.toString());
        else Log.d(TAG, "loadPages UUID null");
        mPages.addAll(PagerManager.get().getFragments(mUUID));

    }

    @Override
    public void onPagesChanged() {
        Log.d(TAG, "OnPagesChanged Callback");
//        loadPages();
//        mViewPager.getAdapter().notifyDataSetChanged();
        mNeedsRefresh = true;
    }
}
