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

package me.tadho.markgo.data.enumeration;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public final class Consts {
    public static final String TAKE_MODE_EXTRA = "takeFrom";
    public static final String STRING_NOT_AVAILABLE = "404-N/A";

    // POST
    public static final int PHOTO_QUALITY = 23;
    public static final int TAKE_MODE_EXTRA_GALLERY = 3;
    public static final int TAKE_MODE_EXTRA_CAMERA = 29;
    public static final String PHOTO_PATH_EXTRA = "imagePath";
    public static final String LOCAL_FILE_EXTRA = "localFile";
    public static final String LATLNG_EXTRA = "currentLocation";
    public static final String POST_MAPS_BUNDLE = "mapsBundle";

    // MAPS
    public static final LatLng MALANG_LATLNG = new LatLng(-7.9666204, 112.6326321);
    public static final LatLngBounds MALANG_BOUNDS = new LatLngBounds(new LatLng(-8.20401459d,112.42446899),
        new LatLng(-7.85657986d,112.80075073d));
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";


    //    Request Code
    public static final int REQUEST_CODE_CAMERA = 11;
    public static final int REQUEST_CODE_GALLERY = 22;
    public static final int REQUEST_LOCATION_CODE = 33;

    //    File name
    public static final String POST_FILE_NAME = "post";
    public static final String POST_FILE_EXT = ".jpg";

    //    Intro Consts
    public static final int REG_STATE_GET_CODE = 1;
    public static final int REG_STATE_GET_AUTH = 2;
    public static final long DEBOUNCE_TIMEOUT = 500;
    public static final long VERIFY_PHONE_TIMEOUT = 60;
    //    Maps Modes
    public static final String MAPS_MODE = "mapsMode";
    public static final char MAPS_PICKER = 'p';
    public static final char MAPS_VIEWER = 'v';
    public static final char MAPS_LIST = 'n';
    //    Maps Filters
    public static final int MAPS_FILTER_ALL = 1;
    public static final int MAPS_FILTER_BROKEN = 2;
    public static final int MAPS_FILTER_FIXED = 3;


    //    TODO : can't figure out how I could take string resource frome outside of the Activity/Fragment
    public static final String STRING_INFO_WINDOW_DESCRIPTION = "Description : ";

}
