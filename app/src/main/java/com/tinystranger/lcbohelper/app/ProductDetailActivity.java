package com.tinystranger.lcbohelper.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class ProductDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<List<LCBOEntity>>{

    ProgressBar spinner;
    private ShareActionProvider mShareActionProvider;

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        ProductDetailActivity activity;
        String itemNumber;

        public BitmapWorkerTask(ProductDetailActivity aActivity, String aItemNumber) {
            activity = aActivity;
            itemNumber = aItemNumber;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Log.d("db", "Fetch " + params[0]);
            return fetchThumbnail(params[0]);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView view = (ImageView) activity.findViewById(R.id.detailImage);
            if (view != null && bitmap != null) {
                if (view != null) {
                    view.setImageBitmap(bitmap);
                }
            }
        }
        private Bitmap fetchThumbnail(String productNumber)
        {
            Bitmap bm = null;
            try {
                String url = String.format(
                    "http://lcbo.com/app/images/products/thumbs/%07d.jpg"
                    , Integer.parseInt(productNumber));
                bm = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
            } catch (FileNotFoundException e) {
                // pass
            } catch (NumberFormatException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }
    }

    boolean loaded;
    public static LCBOEntity lastResult;
    public static Bitmap lastBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView view = (ImageView) findViewById(R.id.detailImage);
        if (view != null && lastBitmap != null) {
            view.setImageBitmap(lastBitmap);
        }

        spinner = (ProgressBar)findViewById(R.id.storesProgressBar);
        spinner.setVisibility(View.GONE);

        updateData();

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Bundle args = new Bundle();
        String search = lastResult.itemNumber;
        String url = String.format(
                "http://stage.lcbo.com/lcbo-webapp/productdetail.do?itemNumber=%s"
                , search);
        args.putString("url", url);

        loaded = false;

        Loader loader = getSupportLoaderManager().initLoader(0, args, this);
        // call forceLoad() to start processing
        loader.forceLoad();
    }

    // Launcher for location details
    private class BtnHandler implements View.OnClickListener
    {
        LCBOEntity entity;
        BtnHandler(LCBOEntity entity)
        {
            this.entity = entity;
        }
        @Override
        public void onClick(View view) {
            LocationDetailActivity.location = entity;
            Intent i = new Intent(getApplicationContext(),LocationDetailActivity.class);
            startActivity(i);
        }
    };

    // Called when a new Loader needs to be created
    public LCBOXmlLoader onCreateLoader(int id, Bundle args) {

        BitmapWorkerTask task = new BitmapWorkerTask(this, lastResult.itemNumber);
        task.execute(lastResult.itemNumber);

        if (MainActivity.mCurrentLocation != null)
        {
            spinner.setVisibility(View.VISIBLE);
            LocationFetchTask locTask = new LocationFetchTask(MainActivity.mCurrentLocation, lastResult.itemNumber, new LocationFetchTask.LocationListener() {
                @Override
                public void onLocationsFetched(List<LCBOEntity> locations) {
                    if (locations != null)
                    {
                        LayoutInflater inflater = getLayoutInflater();
                        LinearLayout parent = (LinearLayout)findViewById(R.id.detailStoresLayout);
                        for(LCBOEntity item : locations) {
                            View row = inflater.inflate(R.layout.detail_location_result_row,
                                    parent, false);

                            TextView txt = (TextView) row.findViewById(R.id.locationName);
                            txt.setText(item.locationName);
                            txt = (TextView) row.findViewById(R.id.locationQuantity);
                            txt.setText(String.valueOf(item.productQuantity) + " Available");
                            txt = (TextView) row.findViewById(R.id.locationAddress1);
                            txt.setText(item.locationAddress1);
                            txt = (TextView) row.findViewById(R.id.locationDistance);
                            txt.setText(String.valueOf(item.distance) + " km Away");
                            row.setOnClickListener(new BtnHandler(item));
                            parent.addView(row);
                        }
                    }
                    spinner.setVisibility(View.GONE);
                }
            });
            locTask.execute(MainActivity.mCurrentLocation);
        }

        return new LCBOXmlLoader(this, args, LCBOQueryParser.QueryType.kProducts);
    }

    @Override
    public void onLoadFinished(Loader<List<LCBOEntity>> loader, List<LCBOEntity> data) {
        loaded = true;
        if (data.size() > 0)
            lastResult = data.get(0);
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No matching products found!")
                    .setTitle("Sorry");
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ProductDetailActivity.this.finish();
                }
            });
            builder.show();
        }
        updateData();

    }

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }

    void updateData()
    {
        ImageView view = (ImageView)findViewById(R.id.detailImage);

        TextView txt = (TextView)findViewById(R.id.detailName);
        txt.setText(lastResult.itemName);

        txt = (TextView) findViewById(R.id.detailPrice);
        txt.setText(lastResult.price);

        txt = (TextView) findViewById(R.id.detailVolume);
        txt.setText(lastResult.productSize);

        txt = (TextView) findViewById(R.id.detailRegion);
        String region = "";
        if (null != lastResult.producingRegion)
            region += lastResult.producingRegion;
        if (null != lastResult.producingCountry)
            region += ", " + lastResult.producingCountry;
        if (region.length() != 0)
            region = "From " + region;
        txt.setText(region);

        txt = (TextView) findViewById(R.id.detailStockType);
        txt.setText("Stock Type is " + lastResult.stockType);

        txt = (TextView) findViewById(R.id.detailSweetnessDescriptor);
        if (lastResult.sweetnessDescriptor != null)
            txt.setText("Sweetness is " + lastResult.sweetnessDescriptor);
        else
            txt.setText("");

        txt = (TextView) findViewById(R.id.detailWineStyle);
        txt.setText("");
        if (null != lastResult.wineStyle) {
            txt.setText("Wine Style is " + Html.fromHtml(lastResult.wineStyle));
        }

        txt = (TextView) findViewById(R.id.detailItemDescription);
        txt.setText("");
        if (null != lastResult.itemDescription) {
            txt.setText(Html.fromHtml(lastResult.itemDescription));
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.product_detail, menu);
        // Get the menu item.
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());
        return super.onCreateOptionsMenu(menu);
    }

    /** Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        String shareBody = lastResult.itemName + "\n\n" + String.format("http://www.lcbo.com/lcbo-ear/lcbo/product/details.do?language=EN&itemNumber=%s", lastResult.itemNumber);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "I found this with the LCBO Helper Android");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        return sharingIntent;
    }
}
