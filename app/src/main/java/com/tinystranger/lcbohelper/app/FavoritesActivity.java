package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
            //Log.d("db", "getView " + position);
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.product_result_row,
                    parent, false);

            LCBOEntity item = getItem(position);
            ImageView view = (ImageView)row.findViewById(R.id.productImage);
            if (!Utils.thumbnailCache.containsKey(item.itemNumber)) {
                BitmapWorkerTask task = new BitmapWorkerTask(activity, item, position);
                task.execute(item.itemNumber);
                //activity.tasks.add(task);
                view.setImageBitmap( Utils.getDefaultThumbnail(activity));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_favorites);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
