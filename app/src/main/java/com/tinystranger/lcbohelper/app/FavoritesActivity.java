package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
                BitmapWorkerTask task = new BitmapWorkerTask(activity, item, position);
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
        ArrayList<LCBOEntity> Favs = new ArrayList<LCBOEntity>(MainActivity.RatingsHashMap.values());
        Collections.sort(Favs, new CustomComparator());
        for (LCBOEntity el : Favs) {
            if (el.userRating > 0)
                mAdapter.add(el);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
