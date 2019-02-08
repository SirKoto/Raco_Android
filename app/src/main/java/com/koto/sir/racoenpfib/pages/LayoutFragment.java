package com.koto.sir.racoenpfib.pages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
    private FloatingActionButton mPlus, mCalendar, mRss;
    private boolean mIsOpen = false;

    {
        mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Bitmap icon;
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.twotone_delete_black_24);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        paint.setColor(getResources().getColor(R.color.colorSecondaryVariant, null));
                    }
                    if (dX > 0) {
                        /* Set your color for positive displacement */

                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), paint);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + 16,
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                paint);
                    } else {
                        /* Set your color for negative displacement */

                        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    }

                    final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
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

    @Override
    public void onStop() {
        PagerManager.get().commitChanges();
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_layout, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_view_layout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new LayoutAdapter());
        new ItemTouchHelper(mCallback).attachToRecyclerView(mRecyclerView);

        mRss = v.findViewById(R.id.rss_mobilitat);
        mRss.setOnClickListener(new ButtonClickListener(PagerManager.RSS_MOVILITAT_PAGE));
        mRss.hide();
        mCalendar = v.findViewById(R.id.calendari);
        mCalendar.setOnClickListener(new ButtonClickListener(PagerManager.CALENDAR_PAGE));
        mCalendar.hide();
        mPlus = v.findViewById(R.id.floating_action_button);
        mPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO FER QUE EL PRIMER COP QUE S'OBRE HI HA UNA ANIMACIO(EL SHOW NO HO FA)
                if (!mIsOpen)
                    showMenu();
                else hideMenu();
            }
        });
        return v;
    }

    private void showMenu() {
        if (!PagerManager.get().isActive(PagerManager.RSS_MOVILITAT_PAGE)) mRss.show();
        if (!PagerManager.get().isActive(PagerManager.CALENDAR_PAGE)) mCalendar.show();
        mIsOpen = true;
    }

    private void hideMenu() {
        mRss.hide();
        mCalendar.hide();
        mIsOpen = false;
    }

    private class ButtonClickListener implements View.OnClickListener {
        private int dataType;

        public ButtonClickListener(int dataType) {
            this.dataType = dataType;
        }

        @Override
        public void onClick(View v) {
            if (!PagerManager.get().addPage(dataType)) return;
            hideMenu();
            mRecyclerView.getAdapter().notifyItemInserted(PagerManager.get().getSizeInts() - 1);
        }
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
