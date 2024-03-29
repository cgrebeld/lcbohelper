package com.tinystranger.lcbohelper.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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
                view.setImageBitmap(bitmap);
                scaleImage(view);
                //view.setOnClickListener(new ImageClickHandler(bitmap));
            }
        }
        private Bitmap fetchThumbnail(String productNumber)
        {
            Bitmap bm = null;
            try {
                String url;
                if (ProductDetailActivity.lastResult != null &&
                        ProductDetailActivity.lastResult.image_url != null)
                {
                    url = ProductDetailActivity.lastResult.image_url;
                }
                else if (ProductDetailActivity.lastResult != null &&
                        ProductDetailActivity.lastResult.image_thumb_url != null)
                {
                    url = ProductDetailActivity.lastResult.image_thumb_url;
                } else {
                    url = String.format(
                            "http://lcbo.com/app/images/products/thumbs/%07d.jpg"
                            , Integer.parseInt(productNumber));
                }
                bm = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
            } catch (FileNotFoundException e) {
                // pass
            } catch (NumberFormatException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

        private void scaleImage(ImageView view)
        {
            Drawable drawing = view.getDrawable();
            if (drawing == null) {
                return;
            }
            Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bounding_x = 340;
            int bounding_y = 340;

            float xScale = ((float) bounding_x) / width;
            float yScale = ((float) bounding_y) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(yScale, yScale);

            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            BitmapDrawable result = new BitmapDrawable(getResources(), scaledBitmap);

            view.setImageDrawable(result);
        }
    }

    boolean loaded;
    public static LCBOEntity lastResult;
    public static Bitmap lastBitmap;
    private static List<LCBOEntity> lastLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView view = (ImageView) findViewById(R.id.detailImage);
        if (view != null && lastBitmap != null) {
            view.setImageBitmap(lastBitmap);
        }

        updateData();

        spinner = (ProgressBar) findViewById(R.id.storesProgressBar);
        spinner.setVisibility(View.GONE);

        updateData();

        if (savedInstanceState == null) {
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            Bundle args = new Bundle();
            String url = String.format(
                    "http://lcboapi.com/products/%s"
                    , lastResult.itemNumber);
            args.putString("url", url);

            loaded = false;

            Loader loader = getSupportLoaderManager().initLoader(0, args, this);
            // call forceLoad() to start processing
            loader.forceLoad();
        } else {
            if (lastLocations != null && !lastLocations.isEmpty()) {
                addLastLocationsData();
            }
        }
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
    }

    // Launcher for location details
    private class ImageClickHandler implements View.OnClickListener
    {
        Bitmap otherThumb;
        ImageClickHandler(Bitmap bigThumb)
        {
            this.otherThumb = bigThumb;
        }
        @Override
        public void onClick(View view) {
            if (otherThumb != null) {
                ImageView img = (ImageView) view;
                Bitmap currentThumb = ((BitmapDrawable) img.getDrawable()).getBitmap();
                img.setImageBitmap(otherThumb);
                img.setMinimumWidth(Math.max(otherThumb.getWidth(), 143));
                img.setMinimumHeight(Math.max(otherThumb.getHeight(), 127));
                otherThumb = currentThumb;
            }
        }
    }

    // Called when a new Loader needs to be created
    public AsyncTaskLoader<List<LCBOEntity>> onCreateLoader(int id, Bundle args) {

        if (lastResult.image_thumb_url == null || lastResult.image_thumb_url.isEmpty()) {
            BitmapWorkerTask task = new BitmapWorkerTask(this, lastResult.itemNumber);
            task.execute(lastResult.itemNumber);
        }

        if (MainActivity.mCurrentLocation != null)
        {
            spinner.setVisibility(View.VISIBLE);
            LocationFetchTask locTask = new LocationFetchTask(MainActivity.mCurrentLocation, lastResult.itemNumber, new LocationFetchTask.LocationListener() {
                @Override
                public void onLocationsFetched(List<LCBOEntity> locations) {
                    lastLocations = locations;
                    addLastLocationsData();
                    spinner.setVisibility(View.GONE);
                }
            });
            locTask.execute(MainActivity.mCurrentLocation);
        }

        return new LCBOAPILoader(this, args, LCBOAPIParser.QueryType.kProductDetail);
    }

    void addLastLocationsData()
    {
        if (lastLocations != null)
        {
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout parent = (LinearLayout)findViewById(R.id.detailStoresLayout);
            for(LCBOEntity item : lastLocations) {
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
    }

    @Override
    public void onLoadFinished(Loader<List<LCBOEntity>> loader, List<LCBOEntity> data) {
        loaded = true;
        if (data != null && data.size() > 0) {
            lastResult = data.get(0);
            updateData();
        } else {
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
        //AppRater.app_launched(getApplicationContext());
    }

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }

    void updateData()
    {
        TextView txt = (TextView)findViewById(R.id.detailName);
        txt.setText(lastResult.itemName);

        txt = (TextView) findViewById(R.id.detailProductNumber);
        txt.setText( Html.fromHtml("<b>LCBO#</b> " + lastResult.itemNumber));

        txt = (TextView) findViewById(R.id.detailPrice);
        if (lastResult.regularPrice != null && !lastResult.price.equalsIgnoreCase(lastResult.regularPrice)) {
            txt.setText( Html.fromHtml("<b>" + lastResult.price + "</b> <i>WAS " + lastResult.regularPrice + "</i>"));

        } else {
            txt.setText( Html.fromHtml("<b>" + lastResult.price + "</b>"));
        }

        txt = (TextView) findViewById(R.id.detailVolume);
        txt.setText(lastResult.productSize);

        txt = (TextView) findViewById(R.id.detailRegion);
        String region = "";
        if (null != lastResult.producingRegion && !lastResult.producingRegion.equalsIgnoreCase("Region Not Specified"))
            region = lastResult.producingRegion;
        if (null != lastResult.producingCountry) {
            if (region.length() > 0)
                region += ", ";
            region += lastResult.producingCountry;
            region = region.replace(", Region Not Specified", "");
        }
        if (region.length() != 0)
            region = "<b>From</b> " + region;
        txt.setText(Html.fromHtml(region));

        txt = (TextView) findViewById(R.id.detailStockType);
        if (lastResult.wineVerietal != null && !lastResult.wineVerietal.isEmpty()) {
            String s = "<b>Varietal</b> is " + lastResult.wineVerietal;
            if (lastResult.stock_type != null && !lastResult.stock_type.equalsIgnoreCase("LCBO"))
                s += " (" + lastResult.stock_type + ")";
            txt.setText(Html.fromHtml(s));
        } else {
            txt.setText(lastResult.stock_type);
            if (lastResult.primary_category != null)
                txt.setText(txt.getText() + " . " + lastResult.primary_category);
            if (lastResult.secondary_category != null)
                txt.setText(txt.getText() + " . " + lastResult.secondary_category);
            if (lastResult.tertiary_category != null)
                txt.setText(txt.getText() + " . " + lastResult.tertiary_category);
        }

        txt = (TextView) findViewById(R.id.detailSweetnessDescriptor);
        if (lastResult.sweetnessDescriptor != null)
            txt.setText( Html.fromHtml("<b>Sweetness</b> is " + lastResult.sweetnessDescriptor));
        else
            txt.setText("");

        txt = (TextView) findViewById(R.id.detailWineStyle);
        txt.setText("");
        if (null != lastResult.wineStyle) {
            txt.setText( Html.fromHtml("<b>Style</b> is " + Html.fromHtml(lastResult.wineStyle)));
        }

        txt = (TextView) findViewById(R.id.detailServingSuggestion);
        txt.setText("");
        if (null != lastResult.serving_suggestion) {
            txt.setText(Html.fromHtml("<b>Pairing:</b> " + lastResult.serving_suggestion));
        }

        txt = (TextView) findViewById(R.id.detailItemDescription);
        txt.setText("");
        if (null != lastResult.itemDescription) {
            txt.setText(Html.fromHtml(lastResult.itemDescription));
        }

        RatingBar bar = (RatingBar)findViewById(R.id.detailRatingBar);
        if (Utils.getRatingsHashMap(this).containsKey(lastResult.itemNumber)) {
            bar.setRating(Utils.getRatingsHashMap(this).get(lastResult.itemNumber).userRating);
        }
        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean fromUser) {
                if (fromUser) {
                    HashMap<String, LCBOEntity> RatingsMap = Utils.getRatingsHashMap(ProductDetailActivity.this);
                    if (RatingsMap.containsKey(lastResult.itemNumber)) {
                        LCBOEntity Ent = RatingsMap.get(lastResult.itemNumber);
                        Ent.userRating = v;
                    } else {
                        lastResult.userRating = v;
                        RatingsMap.put(lastResult.itemNumber, lastResult);
                    }
                    Utils.writeRatings(ProductDetailActivity.this);
                }
            }
        });
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
