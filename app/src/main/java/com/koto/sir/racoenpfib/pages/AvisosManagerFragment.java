package com.koto.sir.racoenpfib.pages;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koto.sir.racoenpfib.AbstractPagerFragments;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.AvisosLab;
import com.koto.sir.racoenpfib.models.Avis;

import java.util.UUID;

public class AvisosManagerFragment extends AbstractPagerFragments
        implements AvisosFragment.OnFragmentGeneralNeedTransaction,
        AvisosListFragment.OnFragmentListlneedsTransaction,
        AvisosDetailFragment.OnFragmentDetaillneedsTransaction {
    private static final String TAG = "AvisosManagerFragment";
    private static final String BACK_GENERAL_LIST = "back_general_list";
    private static final String BACK_LIST_DETAIL = "back_list_detail";
    private static final int GENERAL_VIEW = 1;
    private static final int DETAIL_VIEW = 15;
    private static final String INIT_FRAGMENT = "avisos.manager.koto.sir.data.fragment";
    private static final String ARG_DETAIL = "avisos.manager.koto.sir.dadesaUsar";
    private int mState;
    private int mSizeStack = 0;
    private UUID mUUID;

    public static AvisosManagerFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        //TODO: Posar les inicialitzacions dels diferents fragments
        if (uuid == null) args.putInt(INIT_FRAGMENT, GENERAL_VIEW);
        else {
            args.putInt(INIT_FRAGMENT, DETAIL_VIEW);
            args.putSerializable(ARG_DETAIL, uuid);
        }
        AvisosManagerFragment fragment = new AvisosManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mState = arguments.getInt(INIT_FRAGMENT);
        if (mState == DETAIL_VIEW)
            mUUID = (UUID) arguments.getSerializable(ARG_DETAIL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Fragment childFragment;

        if (mState == DETAIL_VIEW) {
            Avis avis = AvisosLab.get(getActivity()).getAvis(mUUID);
            if (avis == null) childFragment = AvisosFragment.newInstance();
            else
                childFragment = AvisosDetailFragment.newInstance(avis);
        } else
            childFragment = AvisosFragment.newInstance();

        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment)
                .commit();

    }

    @Override
    public int getIcon() {
        return R.drawable.twotone_notifications_black_24;
    }


    @Override
    public void transactionToDetail(Avis avis, View view, View viewFrag) {
        Log.d(TAG, "TransactioToDetail " + avis.getTitol());
        Fragment fragment = AvisosDetailFragment.newInstance(avis);
        fragment.setEnterTransition(new Fade(Fade.IN).setStartDelay(500));
        fragment.setReturnTransition(new Fade(Fade.OUT).setDuration(200));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.move));
            fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.move).setStartDelay(200));
        }
//        fragment.setSharedElementReturnTransition(new ChangeBounds());

        ViewCompat.setTransitionName(viewFrag, "nomRandomTemporal");
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction
                .addSharedElement(view, "paridaNumero2")
                .addSharedElement(viewFrag, "nomRandomTemporal")
                .replace(R.id.fragment_container, fragment)
//                .setReorderingAllowed(true)
                .addToBackStack(BACK_LIST_DETAIL)
                .commit();
        mSizeStack++;
    }

    @Override
    public void transactionToDetailFromAvisosFragment(UUID uuid) {
        Avis avis = AvisosLab.get(getActivity()).getAvis(uuid);
        Fragment fragment = AvisosDetailFragment.newInstance(avis);
        fragment.setEnterTransition(new Fade(Fade.IN).setStartDelay(200));
        fragment.setReturnTransition(new Fade(Fade.OUT));

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        mSizeStack++;
    }

    @Override
    public void transactionToList(String title, int position, View itemView) {
        Log.d(TAG, "transactionToList " + title);
        Fragment fragment = AvisosListFragment.newInstance(title);
        fragment.setEnterTransition(new Fade(Fade.IN).setDuration(500).setStartDelay(200));
        fragment.setReturnTransition(new Fade(Fade.OUT).setDuration(500));
//        fragment.setEnterTransition(new AutoTransition());
        fragment.postponeEnterTransition();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction
//                .setReorderingAllowed(true)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .addSharedElement(itemView.findViewById(R.id.title_card_avis), title)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        mSizeStack++;
    }

    @Override
    public void onBackPressedFromList(AppCompatTextView title) {
        String title_string = (String) title.getText();
        mSizeStack--;

        Log.d(TAG, "onBackPressedFromList " + title_string);
//        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        getChildFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressedFromDetail(Avis avis) {
        Log.d(TAG, "BackFromDetail " + (mState == DETAIL_VIEW));
        if (mState == DETAIL_VIEW) {
            Fragment fragment = AvisosFragment.newInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM).setDuration(300).setStartDelay(300));
            }
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
            mState = GENERAL_VIEW;
        } else {
            mSizeStack--;
            getChildFragmentManager().popBackStack();
        }
    }

    public boolean onBackPressed() {
        if (mState == DETAIL_VIEW) {
            onBackPressedFromDetail(null);
            return true;
        }
        if (mSizeStack == 0) return false;
        getChildFragmentManager().popBackStack();
        mSizeStack--;
        return true;
    }
}
