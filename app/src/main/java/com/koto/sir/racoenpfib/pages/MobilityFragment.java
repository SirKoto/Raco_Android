package com.koto.sir.racoenpfib.pages;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koto.sir.racoenpfib.AbstractPagerFragments;
import com.koto.sir.racoenpfib.R;
import com.koto.sir.racoenpfib.SingleFragmentActivity;
import com.koto.sir.racoenpfib.databases.Fetchr;
import com.koto.sir.racoenpfib.databases.QueryData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MobilityFragment extends AbstractPagerFragments {
    private static final String TAG = "MobilityFragment";
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LruCache<String, Bitmap> mLruCache = new LruCache<>(10);

    public static MobilityFragment newInstance() {
        return new MobilityFragment();
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_rss_feed_black_24dp;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.mobility_fragment, container, false);

        mRecyclerView = v.findViewById(R.id.rss_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<DataRSS> list = QueryData.getDataRss();
        mRecyclerView.setAdapter(new RssAdapter(list));

        mSwipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
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
                if (isNetworkAvailable()) new DownloadRss().execute();
                else mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        return v;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class RssHolder extends RecyclerView.ViewHolder implements Html.ImageGetter {
        private AppCompatTextView title, text;
        private AppCompatImageButton link;
        private AppCompatImageView image;

        public RssHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            link = itemView.findViewById(R.id.link);
            text = itemView.findViewById(R.id.text);
            image = itemView.findViewById(R.id.image);

            text.setMovementMethod(LinkMovementMethod.getInstance());
        }

        public void bind(final DataRSS data) {
            title.setText(fromHtml(data.getTitle()));
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CustomTabsIntent.Builder()
                            .build()
                            .launchUrl(
                                    getActivity(),
                                    Uri.parse(data.getLink())
                            );
                }
            });
            text.setText(fromHtml(data.getText()));
            Log.d(TAG, "bind " + fromHtml(data.getText()));
            Log.d(TAG, "bindNormal " + data.getText());
        }

        private Spanned fromHtml(String txt) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Html.fromHtml(txt, Html.FROM_HTML_MODE_COMPACT, this, null);
            }
            return Html.fromHtml(txt, this, null);
        }

        @Override
        public Drawable getDrawable(String source) {
            LevelListDrawable d = new LevelListDrawable();

            if (isNetworkAvailable()) {
                new LoadImage().execute(source, d);
            }

            return new ColorDrawable(Color.TRANSPARENT);
        }

        class LoadImage extends AsyncTask<Object, Void, Bitmap> {

            private LevelListDrawable mDrawable;

            @Override
            protected Bitmap doInBackground(Object... params) {
                String source = (String) params[0];
                Bitmap bitmap = mLruCache.get(source);
                if (bitmap != null) return bitmap;

                mDrawable = (LevelListDrawable) params[1];
                Log.d(TAG, "doInBackground " + source);
                try {
                    InputStream is = new URL(source).openStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    mLruCache.put(source, bitmap);
                    return bitmap;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                Log.d(TAG, "onPostExecute drawable " + mDrawable);
                Log.d(TAG, "onPostExecute bitmap " + bitmap);
                if (bitmap != null) {
                    // i don't know yet a better way to refresh TextView
                    // mTv.invalidate() doesn't work as expected
//                    CharSequence t = text.getText();
//                    text.setText(t);
                    image.setImageBitmap(bitmap);
                    itemView.refreshDrawableState();
                }
            }
        }
    }

    private class RssAdapter extends RecyclerView.Adapter<RssHolder> {
        private List<DataRSS> mData;

        public RssAdapter(List<DataRSS> data) {
            mData = data;
        }

        @NonNull
        @Override
        public RssHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.mobility_item, viewGroup, false);
            return new RssHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RssHolder rssHolder, int i) {
            rssHolder.bind(mData.get(i));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private class DownloadRss extends AsyncTask<Void, Void, List<DataRSS>> {
        private static final String url = "https://www.fib.upc.edu/ca/mobilitat/rss.rss";
        private static final int IGNORE = 0;
        private static final int TITLE = 1;
        private static final int DESCRIPTION = 2;
        private static final int LINK = 3;

        @Override
        protected List<DataRSS> doInBackground(Void... voids) {
            List<DataRSS> ret = new ArrayList<>();
            try {
                String data = new Fetchr().getUrlString(url);
                // xml Parse
//                Log.d(TAG, data)
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                //TODO? InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));???????????
                xpp.setInput(new ByteArrayInputStream((data.getBytes())), null);
                DataRSS dataRss = new DataRSS();
                int eventType = xpp.getEventType();
                int currentTag = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "item":
                                dataRss = new DataRSS();
                                currentTag = IGNORE;
                                break;
                            case "title":
                                currentTag = TITLE;
                                break;
                            case "link":
                                currentTag = LINK;
                                break;
                            case "description":
                                currentTag = DESCRIPTION;
                                break;
                            default:
                                currentTag = IGNORE;
                                break;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item"))
                            ret.add(dataRss);
                        else
                            currentTag = IGNORE;
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        content = content.trim();
                        switch (currentTag) {
                            case TITLE:
                                dataRss.setTitle(content);
                                break;
                            case LINK:
                                dataRss.setLink(content);
                                break;
                            case DESCRIPTION:
                                dataRss.setText(content);
                                break;
                            default:
                                break;
                        }
                    }

                    eventType = xpp.next();
                }
            } catch (IOException e) {
                Log.e(TAG, "DownloadRss error ", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Parse error ", e);
            }

            QueryData.setDataRss(ret);
            return ret;
        }

        @Override
        protected void onPostExecute(List<DataRSS> dataRSSES) {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.d(TAG, "Data: " + dataRSSES.toString());
            mRecyclerView.setAdapter(new RssAdapter(dataRSSES));
        }

    }

    public class DataRSS {
        private String title;
        private String link;
        private String text;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}

