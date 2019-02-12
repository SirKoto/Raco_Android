package com.koto.sir.racoenpfib.pages;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.AbstractPagerFragments;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.databases.QueryData;
import com.koto.sir.racoenpfib.models.CalendarClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CalendarFragment extends AbstractPagerFragments {
    private static final String TAG = "CalendarFragment";
    private RecyclerView mRecyclerView;
    private List<CalendarClasses> mClasses;
    private View mView;
    private boolean mToCheck;

    public static CalendarFragment newInstance() {

        return new CalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.calendar_fragment, container, false);

        mClasses = QueryData.getCalendar();
        if (mClasses == null)
            mClasses = new ArrayList<>(0);


        mRecyclerView = mView.findViewById(R.id.calendar_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));//GridLayoutManager(getActivity(), 5));
        setupAdapter();

        return mView;
    }

    private void setupAdapter() {
        Log.d(TAG, "setupAdapter");
        if (isAdded()) {
            Log.d(TAG, "Mclasses " + mClasses);
            mRecyclerView.setAdapter(new TimetableAdapter(mClasses));
        }
    }

    @Override
    public int getIcon() {
        return R.drawable.twotone_calendar_today_black_24;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RacoEnpFibApp.getAppContext());
        mToCheck = preferences.getBoolean(RacoEnpFibApp.getAppContext().getResources().getString(R.string.check_calendar_on), true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mToCheck) new FetchCalendar().execute();
    }

    private class TimetableHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mID;
        private AppCompatTextView mGrup;
        private AppCompatTextView mTipus;
        private AppCompatTextView mHora;
        private AppCompatTextView mAula;
        private MaterialCardView mDayCard;
        private AppCompatTextView mDay;

        TimetableHolder(View itemView) {
            super(itemView);
            mID = itemView.findViewById(R.id.id_letter_assig);
            mGrup = itemView.findViewById(R.id.grup_assig);
            mTipus = itemView.findViewById(R.id.tipus_grup_assig);
            mTipus = itemView.findViewById(R.id.tipus_grup_assig);
            mHora = itemView.findViewById(R.id.time_assig);
            mAula = itemView.findViewById(R.id.aula_assig);
            mDayCard = itemView.findViewById(R.id.card_day);
            mDay = mDayCard.findViewById(R.id.textview_day);
        }

        void onBind(CalendarClasses classes, boolean setDay, int day) {
            String hours = classes.getInici() + "-" + (classes.getInici() + classes.getDurada());
            mHora.setText(hours);
            mAula.setText(classes.getAules());
            mID.setText(classes.getCodiAssig());
            mGrup.setText(classes.getGrup());
            mTipus.setText(classes.getTipus());
            if (setDay) {
                mDayCard.setVisibility(View.VISIBLE);
                int id;
                switch (day) {
                    case 0:
                        id = R.string.day1;
                        break;
                    case 1:
                        id = R.string.day2;
                        break;
                    case 2:
                        id = R.string.day3;
                        break;
                    case 3:
                        id = R.string.day4;
                        break;
                    case 4:
                        id = R.string.day5;
                        break;
                    default:
                        return;
                }
                mDay.setText(id);
            } else
                mDayCard.setVisibility(View.GONE);
        }
    }

    private class TimetableAdapter extends RecyclerView.Adapter<TimetableHolder> {
        private List<CalendarClasses> mCalendarClasses;
        private int[] iniDays;
//        private int maxRows = 0;

        public TimetableAdapter(List<CalendarClasses> calendarClasses) {
            mCalendarClasses = calendarClasses;
            iniDays = new int[5];
            for (int i = 0; i < 5; ++i) iniDays[i] = -1;
            for (int i = 0; i < mCalendarClasses.size(); ++i) {
                int d = mCalendarClasses.get(i).getDia();
                if (iniDays[d - 1] == -1) iniDays[d - 1] = i;
            }
        }


        @NonNull
        @Override
        public TimetableHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_timetable_item, viewGroup, false);
            return new TimetableHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TimetableHolder timetableHolder, int i) {
            int j = 0;
            boolean found = false;
            for (; j < 5 && !found; ++j) {
                if (i == iniDays[j]) found = true;
            }
            timetableHolder.onBind(mCalendarClasses.get(i), found, j - 1);
        }

        @Override
        public int getItemCount() {
            return mCalendarClasses.size();//5 * maxRows;
        }
    }


    private class FetchCalendar extends AsyncTask<Void, Void, List<CalendarClasses>> {
        private static final String URL = "https://api.fib.upc.edu/v2/jo/classes/";
        private static final String TAG = "Calendar.FetchCalendar";

        @Override
        protected List<CalendarClasses> doInBackground(Void... voids) {
            Log.d(TAG, "fetching classes");
            String dataJson = new Fetchr().getDataUrlJson(URL);
            Log.d(TAG, "dataJSON " + dataJson);
            List<CalendarClasses> data;
            try {
                JSONObject jsonObject = new JSONObject(dataJson);
                int n = jsonObject.getInt("count");
                Log.d(TAG, "N: " + n);
                data = new ArrayList<>(n);
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i = 0; i < results.length(); ++i) {
                    JSONObject classeJon = results.getJSONObject(i);
                    CalendarClasses classe = new CalendarClasses();

                    classe.setCodiAssig(classeJon.getString("codi_assig"));
                    classe.setGrup(classeJon.getString("grup"));
                    classe.setInici(classeJon.getString("inici"));
                    classe.setTipus(classeJon.getString("tipus"));
                    classe.setAules(classeJon.getString("aules"));
                    classe.setDia(classeJon.getInt("dia_setmana"));
                    classe.setDurada(classeJon.getInt("durada"));

                    data.add(classe);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error en el Json", e);
                return new ArrayList<>(0);
            }
            return data;
        }

        @Override
        protected void onPostExecute(final List<CalendarClasses> calendarClasses) {
            Log.d(TAG, "classes fetched " + calendarClasses);
            if (calendarClasses.size() != 0) {
                if (mClasses.size() == 0) {
                    mClasses = calendarClasses;
                    QueryData.setCalendar(mClasses);
                    setupAdapter();
                }
                if (!mClasses.equals(calendarClasses)) {
                    Snackbar
                            .make(mView, "Different timetable found", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Load", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mClasses = calendarClasses;
                                    QueryData.setCalendar(mClasses);
                                    setupAdapter();
                                }
                            })
                            .show();
                }

            }
        }
    }
}
