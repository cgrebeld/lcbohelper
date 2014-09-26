package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;

class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    Activity activity;
    LCBOEntity item;
    int position;
    private static Semaphore bitmapDownloadSemaphore = new Semaphore(3, true);

    public BitmapWorkerTask(Activity aActivity, LCBOEntity aItem, int position) {
        activity = aActivity;
        item = aItem;
        this.position = position;
        Log.d("LCB", "BitmapWorkerTask ctor" + position);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bm = null;
        synchronized (activity) {
            try {
                bitmapDownloadSemaphore.acquire();
                Log.d("db", "Fetch " + params[0] + " semaphore = " + bitmapDownloadSemaphore.availablePermits());
                bm = fetchThumbnail(item);
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
        ListView lv = (ListView)activity.findViewById(R.id.productResultListView);
        int visibleIndex = position - lv.getFirstVisiblePosition();
        View row = lv.getChildAt(visibleIndex);
        if (row != null) {
            ImageView view = (ImageView) row.findViewById(R.id.productImage);
            if (view != null) {
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                    Utils.scaleImage(view);
                    Utils.thumbnailCache.put(item.itemNumber, bitmap);
                    Log.d("LCB", "post execute ok");
                } else {
                    Utils.thumbnailCache.put(item.itemNumber, Utils.getDefaultThumbnail(activity));
                    Log.d("LCB", "post execute 2");
                }
            } else {
                Log.d("LCB", "post execute 3");
            }
        } else {
            Log.d("LCB", "post execute 4");
        }
    }

    private Bitmap fetchThumbnail(LCBOEntity aEntity)
    {
        String url;
        if (aEntity.image_thumb_url == null) {
            url = String.format(
                    "http://lcbo.com/assets/products/40x40/%07d.jpg"
                    , Integer.parseInt(aEntity.itemNumber));
        } else {
            url = aEntity.image_thumb_url;
        }
        Bitmap bm = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            bm = BitmapFactory.decodeStream(connection.getInputStream());
            Log.d("LCB", "Fetch ok");
        } catch (FileNotFoundException e) {
            Log.d("db", "Fetch failed");
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }
}