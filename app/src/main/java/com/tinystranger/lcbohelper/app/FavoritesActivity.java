package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FavoritesActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;

    private class CustomListAdapter extends ArrayAdapter<LCBOEntity> {

        Activity activity;
        public CustomListAdapter(Activity activity, Context context, int textViewResourceId,
                                 List<LCBOEntity> objects) {
            super(context, textViewResourceId, objects);
            this.notifyDataSetChanged();
            this.activity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.favorites_row,
                        parent, false);
            }
            View row = convertView;

            LCBOEntity item = getItem(position);
            ImageView view = (ImageView)row.findViewById(R.id.productImage);
            if (!Utils.thumbnailCache.containsKey(item.itemNumber)) {
                BitmapWorkerTask task = new BitmapWorkerTask(activity, item, position, R.id.favoritesList);
                task.execute(item.itemNumber);

                view.setImageBitmap(Utils.getDefaultThumbnail(activity));
                Utils.thumbnailCache.put(item.itemNumber,Utils.getDefaultThumbnail(activity));
            } else {
                view.setImageBitmap(Utils.thumbnailCache.get(item.itemNumber));
                Utils.scaleImage(view);
            }
            TextView txt = (TextView) row.findViewById(R.id.productName);
            txt.setText(item.itemName);
            RatingBar bar = (RatingBar) row.findViewById(R.id.favoriteRatingBar);
            bar.setRating(item.userRating);
            txt = (TextView) row.findViewById(R.id.volume);
            txt.setText(item.productSize);
            txt = (TextView) row.findViewById(R.id.price);
            txt.setText(item.price);
            return row;
        }
    }

    CustomListAdapter mAdapter;

    public class CustomComparator implements Comparator<LCBOEntity> {
        @Override
        public int compare(LCBOEntity o1, LCBOEntity o2) {
            return (int)Math.floor(o2.userRating - o1.userRating);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_favorites);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new CustomListAdapter(this, this, R.layout.favorites_row, new ArrayList<LCBOEntity>());
        ((ListView)findViewById(R.id.favoritesList)).setAdapter(mAdapter);

        // fill the list
        ArrayList<LCBOEntity> Favs = new ArrayList<LCBOEntity>(Utils.getRatingsHashMap(this).values());
        if (Favs != null) {
            Collections.sort(Favs, new CustomComparator());
            for (LCBOEntity el : Favs) {
                if (el.userRating > 0)
                    mAdapter.add(el);
            }
        }

        ((ListView)findViewById(R.id.favoritesList)).setOnItemClickListener(new
            AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    LCBOEntity item = mAdapter.getItem(position);
                    ProductDetailActivity.lastResult = item;
                    ProductDetailActivity.lastBitmap = Utils.thumbnailCache.get(item.itemNumber);
                    if (ProductDetailActivity.lastBitmap == null)
                        ProductDetailActivity.lastBitmap = Utils.getDefaultThumbnail(FavoritesActivity.this);
                    Intent i = new Intent(getApplicationContext(),ProductDetailActivity.class);
                    startActivity(i);
                }
            });

        if (savedInstanceState == null) {
            if (mAdapter.getCount() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Set a star rating on a product and it will show up here!")
                        .setTitle("No Rated Drinks");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FavoritesActivity.this.finish();
                    }
                });
                builder.show();

            }
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
        String shareBody = "";
        ArrayList<LCBOEntity> Favs = new ArrayList<LCBOEntity>(Utils.getRatingsHashMap(this).values());
        if (Favs != null) {
            for (LCBOEntity ent : Favs) {
                if (ent.userRating > 0) {
                    shareBody += ent.itemName + " (" + ent.itemNumber + ") " + ent.userRating + " stars\n";
                }
            }
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Favorites from LCBO Helper Android");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        return sharingIntent;
    }
}
