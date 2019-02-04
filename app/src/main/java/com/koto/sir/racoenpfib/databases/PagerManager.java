package com.koto.sir.racoenpfib.databases;

import android.util.Log;

import com.koto.sir.racoenpfib.LoggerFragment;
import com.koto.sir.racoenpfib.AbstractPagerFragments;
import com.koto.sir.racoenpfib.pages.AvisosFragment;
import com.koto.sir.racoenpfib.pages.AvisosManagerFragment;
import com.koto.sir.racoenpfib.pages.CalendarFragment;
import com.koto.sir.racoenpfib.pages.ConfigFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PagerManager {
    public static final int CALENDAR_PAGE = 1;
    public static final int AVISOS_PAGE = -1;
    public static final int LAYOUT_MENU = 2;
    public static final int RSS_MOVILITAT_PAGE = 3;
    public static final int CONFIG_PAGE = 0;
    public static final int NUM_MAX_PAGES = 6;
    private static final String TAG = "PagerManager";
    private static PagerManager sPagerManager;
    private List<Integer> mPagesIds = null;
    private CallbackManager mCallback;
    private int mAvisPos;

    private PagerManager() {
        mPagesIds = QueryData.getListPages();
        if (mPagesIds == null) {
            mPagesIds = new ArrayList<>();
            mPagesIds.add(CALENDAR_PAGE);
            mPagesIds.add(AVISOS_PAGE);
            mPagesIds.add(CONFIG_PAGE);
        }
        Log.d(TAG, "PagerManager creadora" + mPagesIds.toString());
    }

    public static PagerManager get() {
        if (sPagerManager == null) {
            sPagerManager = new PagerManager();
        }
        return sPagerManager;
    }

    public int getSizeInts() {
        return mPagesIds.size();
    }

    public int getPagesId(int pos) {
        if (0 <= pos && pos <= mPagesIds.size()) {
            return mPagesIds.get(pos);
        }
        return 0;
    }

    public void swap(int ini, int fi) {
        Collections.swap(mPagesIds, ini, fi);
        mCallback.onPagesChanged();
    }

    public void assignCallback(CallbackManager callback) {
        mCallback = callback;
    }

    public void refresh() {
        mCallback.onPagesChanged();
    }

    public void deletePage(int page) {
        /*if (page == CONFIG_PAGE) return;
        for (int i = 0; i < mPagesIds.size(); ++i)
            if (mPagesIds.get(i) == page) {
                mPagesIds.remove(i);
                break;
            }*/
        mPagesIds.remove(page);
        QueryData.setListPages(mPagesIds);
        mCallback.onPagesChanged();
    }

    public void movePage(int page, int position) {
        if (position >= mPagesIds.size()) return;
        int pos = 0;
        boolean found = false;
        while (pos < mPagesIds.size()) {
            if (mPagesIds.get(pos) == page) {
                found = true;
                break;
            }
            pos++;
        }
        if (!found || pos == position) return;

        mPagesIds.remove(pos);
        if (pos < position)
            mPagesIds.set(position - 1, page);
        else
            mPagesIds.set(position, page);

        QueryData.setListPages(mPagesIds);
        mCallback.onPagesChanged();
    }

    public void addPage(int page) {
        if (mPagesIds.size() > NUM_MAX_PAGES) return;
        boolean found = false;
        int pos = 0;
        while (pos < mPagesIds.size()) {
            if (mPagesIds.get(pos) == page) {
                found = true;
                break;
            }
            pos++;
        }
        if (found)
            mPagesIds.remove(pos);
        mPagesIds.add(page);

        QueryData.setListPages(mPagesIds);
        mCallback.onPagesChanged();
    }

    public int getAvisPos() {
        return mAvisPos;
    }

    public List<Integer> getConfig() {
        List<Integer> ret = new ArrayList<>();
        if (!mPagesIds.contains(CALENDAR_PAGE))
            ret.add(CALENDAR_PAGE);

        if (!mPagesIds.contains(RSS_MOVILITAT_PAGE))
            ret.add(RSS_MOVILITAT_PAGE);

        ret.add(LAYOUT_MENU);
        ret.add(CONFIG_PAGE);
        return ret;
    }

    public List<AbstractPagerFragments> getFragments(UUID uuid) {
        ArrayList<AbstractPagerFragments> results = new ArrayList<>();
        boolean b = QueryData.getAuthState() == null;
        if (b) results.add(LoggerFragment.newInstance());
        Log.d(TAG, "Boolean de token " + b);
        for (int i = 0; i < mPagesIds.size(); ++i) {
            switch (mPagesIds.get(i)) {
                case CONFIG_PAGE:
                    results.add(ConfigFragment.newInstance());
                    break;
                case CALENDAR_PAGE:
                    results.add(CalendarFragment.newInstance());
                    break;
                case AVISOS_PAGE:
                    mAvisPos = i;
                    results.add(AvisosManagerFragment.newInstance(uuid));
                    break;
                default:
            }
        }
        Log.d(TAG, "Get Fragments " + results);
        return results;
    }

    public interface CallbackManager {
        void onPagesChanged();
    }
}
