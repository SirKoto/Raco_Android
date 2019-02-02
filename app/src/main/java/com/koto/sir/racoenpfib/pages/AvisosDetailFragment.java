package com.koto.sir.racoenpfib.pages;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.models.Adjunt;
import com.koto.sir.racoenpfib.models.Avis;


public class AvisosDetailFragment extends Fragment {
    private static final String ARG_AVIS = "arg_avis";

    private Avis mAvis;
    private OnFragmentDetaillneedsTransaction mCallback;

    public static AvisosDetailFragment newInstance(Avis avis) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_AVIS, avis);
        AvisosDetailFragment fragment = new AvisosDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAvis = (Avis) getArguments().getSerializable(ARG_AVIS);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setTransitionName(view.findViewById(R.id.item_general), "paridaNumero2");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.avis_detail_fragment, container, false);

        AppCompatTextView text = v.findViewById(R.id.text_detail_avis);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            text.setText(Html.fromHtml(mAvis.getText(), Html.FROM_HTML_MODE_LEGACY));
        else
            text.setText(Html.fromHtml(mAvis.getText()));
        text.setMovementMethod(LinkMovementMethod.getInstance());

        v.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getFragmentManager().popBackStack();
                mCallback.onBackPressedFromDetail(mAvis);
            }
        });


        v.findViewById(R.id.image_no_vist).setVisibility(mAvis.isVist() ? View.INVISIBLE : View.VISIBLE);

        ((AppCompatTextView) v.findViewById(R.id.nom_assig)).setText(mAvis.getTitol());
        ((AppCompatTextView) v.findViewById(R.id.title_card_avis)).setText(mAvis.getAssignatura());

        v.findViewById(R.id.image_te_document).setVisibility(mAvis.getAdjunts().size() == 0 ? View.INVISIBLE : View.VISIBLE);

        ((AppCompatTextView) v.findViewById(R.id.data_inserit)).setText(AvisosListFragment.sFormater.format(mAvis.getDataInsercio()));
        ((AppCompatTextView) v.findViewById(R.id.data_modificat)).setText(AvisosListFragment.sFormater.format(mAvis.getDataModificacio()));


        //posem les descarregues
        if (mAvis.getAdjunts().size() == 0)
            v.findViewById(R.id.files_separator).setVisibility(View.GONE);
        else {
            v.findViewById(R.id.files_separator).setVisibility(View.VISIBLE);

            LinearLayout ll = v.findViewById(R.id.linear_layout_detail);

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (15 * scale + 0.5f);

            for (final Adjunt adjunt : mAvis.getAdjunts()) {
                AppCompatTextView textView = new AppCompatTextView(getActivity());
                textView.setText(adjunt.getNom());
                textView.setLayoutParams(
                        new LinearLayoutCompat.LayoutParams(
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                textView.setPaddingRelative(dpAsPixels, 5, 5, dpAsPixels);
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.twotone_cloud_download_black_36, 0);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Fetchr().downloadFile(getActivity(), adjunt);
                    }
                });

                ll.addView(textView);
            }

        }


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = getParentFragment();
        if (fragment instanceof OnFragmentDetaillneedsTransaction) {
            mCallback = (OnFragmentDetaillneedsTransaction) fragment;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mAvis.isVist()) {
            //Marcar com a vist!
            Animation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(500);
            animation.setStartOffset(1000);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    getView().findViewById(R.id.image_no_vist).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            getView().findViewById(R.id.image_no_vist).startAnimation(animation);

            mAvis.setVist(true);
            new SaverCutre().execute(mAvis);
        }
    }

    interface OnFragmentDetaillneedsTransaction {
        void onBackPressedFromDetail(Avis avis);
    }

    private static class SaverCutre extends AsyncTask<Avis, Void, Void> {
        @Override
        protected Void doInBackground(Avis... avis) {
            AvisosLab.get(RacoEnpFibApp.getAppContext()).updateAvis(avis[0]);
            return null;
        }
    }
}

