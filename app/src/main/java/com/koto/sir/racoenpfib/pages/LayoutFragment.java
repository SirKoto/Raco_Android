package com.koto.sir.racoenpfib.pages;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.databases.PagerManager;

public class LayoutFragment extends Fragment {
    private static final String TAG = "LayoutFragment";
    private RecyclerView mRecyclerView;
    private ItemTouchHelper.Callback mCallback;

    {
        mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                PagerManager.get().swap(viewHolder.getAdapterPosition(), viewHolder1.getAdapterPosition());
                return true;
            }

            @Override
            public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                mRecyclerView.getAdapter().notifyItemMoved(fromPos, toPos);
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int pagesId = PagerManager.get().getPagesId(viewHolder.getAdapterPosition());
                if (pagesId == PagerManager.CONFIG_PAGE || pagesId == PagerManager.AVISOS_PAGE)
                    return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int j = viewHolder.getAdapterPosition();
                PagerManager.get().deletePage(j);
                mRecyclerView.getAdapter().notifyItemRemoved(j);
            }
        };
    }

    public static LayoutFragment newInstance() {
        return new LayoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_layout, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_view_layout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new LayoutAdapter());
        new ItemTouchHelper(mCallback).attachToRecyclerView(mRecyclerView);

        for (int i = 0; i < PagerManager.get().getSizeInts(); ++i)
            Log.d(TAG, "onCreate " + PagerManager.get().getPagesId(i));

        return v;
    }

    private class LayoutHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        AppCompatImageView mImage;

        public LayoutHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text1);
            mImage = itemView.findViewById(R.id.image);
        }

        public void bind(int text) {
            if (text == PagerManager.AVISOS_PAGE) {
                mTextView.setText("Avisos");
                Drawable d = getResources().getDrawable(R.drawable.twotone_notifications_black_24);
                mImage.setImageDrawable(d);
            } else if (text == PagerManager.CONFIG_PAGE) {
                mTextView.setText("Home");
                Drawable d = getResources().getDrawable(R.drawable.twotone_home_black_24);
                mImage.setImageDrawable(d);
            } else {
                String s = getResources().getString(ConfigFragment.sTitleRes[text]);
                mTextView.setText(s);
                Drawable d = getResources().getDrawable(ConfigFragment.sImageRes[text]);
                mImage.setImageDrawable(d);
            }
        }
    }

    private class LayoutAdapter extends RecyclerView.Adapter<LayoutHolder> {
        @NonNull
        @Override
        public LayoutHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_item, viewGroup, false);
            return new LayoutHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LayoutHolder layoutHolder, int i) {
            Log.d(TAG, "onBind " + i + " representa " + PagerManager.get().getPagesId(i));
            layoutHolder.bind(PagerManager.get().getPagesId(i));
        }

        @Override
        public int getItemCount() {
            return PagerManager.get().getSizeInts();
        }
    }


}
