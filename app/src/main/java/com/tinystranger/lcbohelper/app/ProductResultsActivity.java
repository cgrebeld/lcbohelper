package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductResultsActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<List<LCBOEntity>>
{
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
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.product_result_row,
                        parent, false);
            }
            View row = convertView;
            //Log.d("db", "getView " + position);
            /*
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.product_result_row,
                    parent, false);
*/
            LCBOEntity item = getItem(position);
            ImageView view = (ImageView)row.findViewById(R.id.productImage);
            if (!Utils.thumbnailCache.containsKey(item.itemNumber)) {
                BitmapWorkerTask task = new BitmapWorkerTask(activity, item, position);
                task.execute(item.itemNumber);
                //activity.tasks.add(task);
                view.setImageBitmap(Utils.getDefaultThumbnail(activity));
                Utils.thumbnailCache.put(item.itemNumber,Utils.getDefaultThumbnail(activity));
            } else {
                view.setImageBitmap(Utils.thumbnailCache.get(item.itemNumber));
                Utils.scaleImage(view);
            }
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
        final Activity me = this;

        ((ListView)findViewById(R.id.productResultListView)).setOnItemClickListener(new
                AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        LCBOEntity item = mAdapter.getItem(position);
                        ProductDetailActivity.lastResult = item;
                        ProductDetailActivity.lastBitmap = Utils.thumbnailCache.get(item.itemNumber);
                        if (ProductDetailActivity.lastBitmap == null)
                            ProductDetailActivity.lastBitmap = Utils.getDefaultThumbnail(me);
                        Intent i = new Intent(getApplicationContext(),ProductDetailActivity.class);
                        startActivity(i);
                    }
                });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Bundle args = new Bundle();
        int maxResults = 40;
        String search = getIntent().getStringExtra("search");
        //search = search.replaceAll("[^A-Za-z0-9 ]", "");
        search = search.replaceAll("(^| )[^ ]*[^A-Za-z ][^ ]*(?=$| )", "");

        String url = null;
        try {
            url = String.format(
                    "http://lcboapi.com/products?q=%s"
//                    "http://stage.lcbo.com/lcbo-webapp/productsearch.do?itemKeyword=%s&numProducts=%d"
//            , URLEncoder.encode(search, "UTF-8"), maxResults);
              , URLEncoder.encode(search, "UTF-8"));
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
    public Loader<List<LCBOEntity>> onCreateLoader(int id, Bundle args) {
          return new LCBOAPILoader(this, args, LCBOAPIParser.QueryType.kProducts);
//        return new LCBOXmlLoader(this, args, LCBOQueryParser.QueryType.kProducts);
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

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }

}
