package com.tinystranger.lcbohelper.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class ProductResultsActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<List<LCBOEntity>>
{
    static Bitmap defaultThumbnail = null;
    static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    private final Semaphore bitmapDownloadSemaphore = new Semaphore(3, true);
    boolean loaded;

    private class CustomListAdapter extends ArrayAdapter<LCBOEntity> {

        ProductResultsActivity activity;
        public CustomListAdapter(ProductResultsActivity activity, Context context, int textViewResourceId,
                                 List<LCBOEntity> objects) {
            super(context, textViewResourceId, objects);
            this.notifyDataSetChanged();
            this.activity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Log.d("db", "getView " + position);
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.product_result_row,
                    parent, false);

            LCBOEntity item = getItem(position);
            ImageView view = (ImageView)row.findViewById(R.id.productImage);
            if (!thumbnailCache.containsKey(item.itemNumber)) {
                BitmapWorkerTask task = new BitmapWorkerTask(activity, item.itemNumber, position);
                task.execute(item.itemNumber);
                //activity.tasks.add(task);
                view.setImageBitmap(defaultThumbnail);
            } else
                view.setImageBitmap(thumbnailCache.get(item.itemNumber));

            TextView txt = (TextView) row.findViewById(R.id.productName);
            txt.setText(item.itemName);
            txt = (TextView) row.findViewById(R.id.quantity);
            txt.setText(String.valueOf(item.productQuantity) + " Available");
            txt = (TextView) row.findViewById(R.id.volume);
            txt.setText(item.productSize);
            txt = (TextView) row.findViewById(R.id.price);
            txt.setText(item.price);
            return row;
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        ProductResultsActivity activity;
        String itemNumber;
        int position;

        public BitmapWorkerTask(ProductResultsActivity aActivity, String aItemNumber, int position) {
            activity = aActivity;
            itemNumber = aItemNumber;
            this.position = position;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bm = null;
            synchronized (activity) {
                try {
                    bitmapDownloadSemaphore.acquire();
                    Log.d("db", "Fetch " + params[0]);
                    bm = fetchThumbnail(params[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    bitmapDownloadSemaphore.release();
                }
            }
            return bm;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            ListView lv = (ListView)findViewById(R.id.productResultListView);
            View row = lv.getChildAt(position);
            if (row != null) {
                ImageView view = (ImageView) row.findViewById(R.id.productImage);
                if (view != null) {
                    if (bitmap != null) {
                        view.setImageBitmap(bitmap);
                        thumbnailCache.put(itemNumber, bitmap);
                        Log.d("db", "Fetch ok");
                    } else {
                        thumbnailCache.put(itemNumber, defaultThumbnail);
                    }
                }
            }
        }
    }

    CustomListAdapter mAdapter;
    //int nextPositionToLoadImage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_results);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        ((ListView)findViewById(R.id.productResultListView)).setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        TextView title = (TextView)findViewById(R.id.resultsTitle);
        title.setText("Search Results for " + "\"" + getIntent().getStringExtra("search") + "\"");

        mAdapter = new CustomListAdapter(this, this, R.layout.product_result_row, new ArrayList<LCBOEntity>());

        ((ListView)findViewById(R.id.productResultListView)).setAdapter(mAdapter);

        if (null == defaultThumbnail) {
            defaultThumbnail = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_thumbnail);
        }

        ((ListView)findViewById(R.id.productResultListView)).setOnItemClickListener(new
                AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        LCBOEntity item = mAdapter.getItem(position);
                        ProductDetailActivity.lastResult = item;
                        ProductDetailActivity.lastBitmap = thumbnailCache.get(item.itemNumber);
                        if (ProductDetailActivity.lastBitmap == null)
                            ProductDetailActivity.lastBitmap = defaultThumbnail;
                        Intent i = new Intent(getApplicationContext(),ProductDetailActivity.class);
                        startActivity(i);
                    }
                });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Bundle args = new Bundle();
        int maxResults = 40;
        String search = getIntent().getStringExtra("search");
        String url = null;
        try {
            url = String.format(
                    "http://stage.lcbo.com/lcbo-webapp/productsearch.do?itemKeyword=%s&numProducts=%d"
            , URLEncoder.encode(search, "UTF-8"), maxResults);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        args.putString("url", url);

        loaded = false;

        Loader loader = getSupportLoaderManager().initLoader(0, args, this);
        // call forceLoad() to start processing
        loader.forceLoad();
    }

    // Called when a new Loader needs to be created
    public LCBOXmlLoader onCreateLoader(int id, Bundle args) {

        return new LCBOXmlLoader(this, args, LCBOQueryParser.QueryType.kProducts);
    }

    public class CustomComparator implements Comparator<LCBOEntity> {
        @Override
        public int compare(LCBOEntity o1, LCBOEntity o2) {
            return o1.priceNumber.intValue() - o2.priceNumber.intValue();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<LCBOEntity>> loader, List<LCBOEntity> data)
    {
        loaded = true;

        if (data != null) {
            Collections.sort(data, new CustomComparator());

            for (LCBOEntity el : data) {
                if (el.productQuantity > 0)
                    mAdapter.add(el);
            }
            Log.d("db", "load got " + data.size() + " results, " + mAdapter.getCount() + " retained");
        }

        if (mAdapter.getCount() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No matching products found!")
                    .setTitle("Sorry");
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ProductResultsActivity.this.finish();
                }
            });
            builder.show();

        }
    }

    private Bitmap fetchThumbnail(String productNumber)
    {
        String url = String.format(
                "http://lcbo.com/assets/products/40x40/%07d.jpg"
                , Integer.parseInt(productNumber));
        Bitmap bm = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            bm = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (FileNotFoundException e) {
            Log.d("db", "Fetch failed");
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }
    private void setResultThumbnail(int position, Bitmap bm)
    {
        ListView lv = (ListView)findViewById(R.id.productResultListView);
        if (position < lv.getCount()) {
            View row = lv.getChildAt(position);
            if (row != null) {
                ImageView view = (ImageView) row.findViewById(R.id.productImage);
                view.setImageBitmap(bm);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }

}
