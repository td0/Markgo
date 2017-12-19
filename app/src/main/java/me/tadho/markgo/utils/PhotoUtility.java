/*
 * Copyright (c) 2017 Tadho Muhammad <tadho@me.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tadho.markgo.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import timber.log.Timber;

public class PhotoUtility {

    private static ExifInterface exifInterface=null;

    public static Bitmap rotateBitmap(Context context, Uri uri, Bitmap bm){
        String path = getFullPathFromUri(context, uri);
        int rotationAngle = getExifOrientation(path);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    private static String getFullPathFromUri(Context context, Uri uri){
        String result;
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private static int getExifOrientation(String photoFilePath) {
        Timber.d("Camera/Gallery : reading exif orientation");
        exifInterface = createExifInterface(photoFilePath);
        String orientString = exifInterface != null ?
            exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION) : null;
        int orientation = orientString != null ?
            Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    public static LatLng getExifLocation(String photoFilePath)  {
        Timber.d("Camera/Gallery : reading exif location");
        String latDms = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String lngDms = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        Timber.d("(exif) Latitude  DMS : "+latDms+" ("+latRef+")");
        Timber.d("(exif) Longitude DMS : "+lngDms+" ("+lngRef+")");
        if(latDms==null||lngDms==null||latRef==null||lngRef==null) {
            Timber.d("(exif) gps data not found");
            return null;
        }
        Double lat = convertToDegree(latDms,latRef);
        Double lng = convertToDegree(lngDms,lngRef);
        Timber.d("(exif) Latitude  : "+lat);
        Timber.d("(exif) Longitude : "+lng);
        return new LatLng(lat,lng);
    }

    private static ExifInterface createExifInterface(String photoFilePath){
        ExifInterface ei=null;
        try {
            ei = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            Timber.e(e.getMessage());
            e.printStackTrace();
        }
        return ei;
    }

    private static Double convertToDegree(String dmsString, String dmsRef){
        Double result = 0d;
        String[] DMS = dmsString.split(",", 3);
        for (int i = 0; i < DMS.length; i++) {
            String[] dms = DMS[i].split("/", 2);
            Double dms1 = Double.valueOf(dms[0]);
            Double dms2 = Double.valueOf(dms[1]);
            result += (dms1 / dms2) / Math.pow(60, i);
        }
        return (dmsRef.equals("N")||dmsRef.equals("E"))? result : result*(-1);
    }

    public static int dp2px(final Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);
        return (int) (dp * displaymetrics.density + 0.5f);
    }

    // scale and keep aspect ratio
//    public static Bitmap scaleToFitWidth(Bitmap b, int width)
//    {
//        float factor = width / (float) b.getWidth();
//        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
//    }
//
//
//    // scale and keep aspect ratio
//    public static Bitmap scaleToFitHeight(Bitmap b, int height)
//    {
//        float factor = height / (float) b.getHeight();
//        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
//    }
//
//
//    // scale and keep aspect ratio
//    public static Bitmap scaleToFill(Bitmap b, int width, int height)
//    {
//        float factorH = height / (float) b.getWidth();
//        float factorW = width / (float) b.getWidth();
//        float factorToUse = (factorH > factorW) ? factorW : factorH;
//        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
//                (int) (b.getHeight() * factorToUse), true);
//    }
//
//
//    // scale and don't keep aspect ratio
//    public static Bitmap strechToFill(Bitmap b, int width, int height)
//    {
//        float factorH = height / (float) b.getHeight();
//        float factorW = width / (float) b.getWidth();
//        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW),
//                (int) (b.getHeight() * factorH), true);
//    }
}