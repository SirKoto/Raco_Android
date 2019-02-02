package com.koto.sir.racoenpfib.pages;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.models.Avis;
import com.koto.sir.racoenpfib.services.AvisosWorker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class AvisosListFragment extends VisibleFragment {
    public static final DateFormat sFormater = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static final String TAG = "AvisosListFragment";
    private static final String ARG_NAME_ASSIG = "com.koto.sir.AvisosListFragment.NomAssig";
    private String nomAssig;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OnFragmentListlneedsTransaction mCallback;
    private AppCompatTextView mTextView;

    public static AvisosListFragment newInstance(String assig) {
        Bundle args = new Bundle();
        args.putString(ARG_NAME_ASSIG, assig);
        AvisosListFragment fragment = new AvisosListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nomAssig = getArguments().getString(ARG_NAME_ASSIG);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getActivity())
                    .inflateTransition(android.R.transition.move));
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        postponeEnterTransition();
        View view = inflater.inflate(R.layout.avis_list_fragment, container, false);
        mTextView = view.findViewById(R.id.title_card_avis);

        mTextView.setText(nomAssig);

        view.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setExitTransition(new Explode().setDuration(1000));
                mCallback.onBackPressedFromList(mTextView);
            }
        });

        mRecyclerView = view.findViewById(R.id.avisos_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //new AvisosLoader().execute(nomAssig);
//        setupAdapter(new ArrayList<Avis>(0));


        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_avis);
        Resources resources = getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSwipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorSecondary, null),
                    resources.getColor(R.color.colorSecondaryVariant, null));
        } else
            mSwipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorSecondary),
                    resources.getColor(R.color.colorSecondaryVariant));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryDownloadingAgain();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        postponeEnterTransition();
        setupAdapter(AvisosLab.get(RacoEnpFibApp.getAppContext()).getAvisos(nomAssig));
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "OnPreDraw");
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    private void tryDownloadingAgain() {
//        getActivity().startService(AvisosService.newIntent(getActivity()));
        OneTimeWorkRequest reload = new OneTimeWorkRequest.Builder(AvisosWorker.class).build();
        WorkManager.getInstance().enqueue(reload);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //TODO: check si encara esta funcionant el service o posar el Broadcast
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 200);
    }

    private void setupAdapter(List<Avis> avisList) {
        Log.d(TAG, "setupAdapter PRE " + avisList.size());
        if (isAdded()) {
            mRecyclerView.setAdapter(new AvisListAdapter(avisList));
            Log.d(TAG, "setupAdapter " + avisList.size());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = getParentFragment();
        if (fragment instanceof OnFragmentListlneedsTransaction) {
//            setExitTransition(new Fade(Fade.OUT));
            mCallback = (OnFragmentListlneedsTransaction) fragment;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    protected void OnNewDataFound() {
        Log.d(TAG, "OnNewDataFound");
        setupAdapter(AvisosLab.get(RacoEnpFibApp.getAppContext()).getAvisos(nomAssig));
    }


    public interface OnFragmentListlneedsTransaction {
        void onBackPressedFromList(AppCompatTextView title);

        void transactionToDetail(Avis avis, View view, View viewFrag);
    }


    private class AvisItemHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mTitle;
        private AppCompatTextView mDate_i;
        private AppCompatTextView mDate_m;
        private AppCompatImageView mNewAvis;
        private AppCompatImageView mHasDocument;

        public AvisItemHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.nom_assig);
            mDate_i = itemView.findViewById(R.id.data_inserit);
            mDate_m = itemView.findViewById(R.id.data_modificat);
            mNewAvis = itemView.findViewById(R.id.image_no_vist);
            mHasDocument = itemView.findViewById(R.id.image_te_document);
        }

        public void bind(final Avis avis) {
            mTitle.setText(avis.getTitol());
            mDate_i.setText(sFormater.format(avis.getDataInsercio()));
            mDate_m.setText(sFormater.format(avis.getDataModificacio()));
            mNewAvis.setVisibility(avis.isVist() ? View.INVISIBLE : View.VISIBLE);
            mHasDocument.setVisibility(avis.getAdjunts().size() == 0 ? View.INVISIBLE : View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setExitTransition(new Fade(Fade.OUT).setDuration(150));
//                    setReenterTransition(new Fade(Fade.IN).setStartDelay(300).setDuration(500));
                    mCallback.transactionToDetail(avis, itemView, getView().findViewById(R.id.card_relative_layour));
                }
            });
            ViewCompat.setTransitionName(itemView, avis.getUid().toString());
        }
    }

    private class AvisListAdapter extends RecyclerView.Adapter<AvisItemHolder> {
        private List<Avis> mAvisList;

        AvisListAdapter(List<Avis> avisos) {
            mAvisList = avisos;
        }

        @NonNull
        @Override
        public AvisItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.avis_list_item, viewGroup, false);
            return new AvisItemHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AvisItemHolder avisItemHolder, int i) {
            avisItemHolder.bind(mAvisList.get(i));
        }

        @Override
        public int getItemCount() {
            return mAvisList.size();
        }
    }


}
