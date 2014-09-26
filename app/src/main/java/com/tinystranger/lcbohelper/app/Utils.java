package com.tinystranger.lcbohelper.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.HashMap;

public class Utils {
    public static HashMap<String, Bitmap> thumbnailCache = new HashMap<String, Bitmap>();
    private static Bitmap defaultThumbnail = null;

    public static Bitmap getDefaultThumbnail(Activity Activity) {
        if (null == defaultThumbnail) {
            defaultThumbnail = BitmapFactory.decodeResource(Activity.getResources(),
                    R.drawable.default_thumbnail);
        }
        return defaultThumbnail;
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
}
