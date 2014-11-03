package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    private static HashMap<String, LCBOEntity> RatingsHashMap;
    private static Bitmap defaultThumbnail = null;

    public static Bitmap getDefaultThumbnail(Activity Activity) {
        if (null == defaultThumbnail) {
            defaultThumbnail = BitmapFactory.decodeResource(Activity.getResources(),
                    R.drawable.default_thumbnail);
        }
        return defaultThumbnail;
    }

    public static HashMap<String, LCBOEntity> getRatingsHashMap(Activity Activity)
    {
        if (RatingsHashMap == null)
        {
            readRatings(Activity);
        }
        return RatingsHashMap;
    }

    public static void readRatings(Activity Activity)
    {
        try {
            FileInputStream fileInputStream = Activity.openFileInput("ratings.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Utils.RatingsHashMap = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            Utils.RatingsHashMap = new HashMap<String, LCBOEntity>();
        }
    }

    public static void writeRatings(Activity Activity)
    {
        try {
            FileOutputStream fos = Activity.openFileOutput("ratings.ser", Context.MODE_PRIVATE);
            ObjectOutputStream s = new ObjectOutputStream(fos);
            s.writeObject(Utils.RatingsHashMap);
            s.close();
        } catch (Exception e) {

        }
    }
    public static void scaleImage(ImageView view)
    {
        Drawable drawing = view.getDrawable();
        if (drawing == null) {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding_x = 54;
        int bounding_y = 54;

        float xScale = ((float) bounding_x) / width;
        float yScale = ((float) bounding_y) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();
        BitmapDrawable result = new BitmapDrawable(view.getResources(), scaledBitmap);

        view.setImageDrawable(result);
/*
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
*/
    }

    public static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "LCBO/2.5 CFNetwork/672.1.14 Darwin/14.0.0");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "en-us");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();

        if ("gzip".equals(conn.getContentEncoding())) {
            stream = new GZIPInputStream(stream);
        }
        return stream;
    }
}
