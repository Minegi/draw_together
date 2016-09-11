package com.samsung.hackathon.drawtogether.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapUtils {
    public static Bitmap getResizedBitmap(final Bitmap bm,
                                          final int newWidth, final int newHeight) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        final float scaledWidth = ((float)newWidth) / width;
        final float scaledHeight = ((float)newHeight) / height;

        final Matrix matrix = new Matrix();
        matrix.postScale(scaledWidth, scaledHeight);

        final Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap cropBitmap(final Bitmap bm, final int width, final int height,
                                    final float density) {
        return Bitmap.createBitmap(bm, 0, 0, width, height * (int)density);

    }
}
