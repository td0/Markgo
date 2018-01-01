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

package me.tadho.markgo.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import me.tadho.markgo.R;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.GlideApp;
import me.tadho.markgo.utils.PhotoUtility;
import timber.log.Timber;

public class ClusterInfoWindowAdapter
    implements GoogleMap.InfoWindowAdapter {

    private View mContent;
    private HashMap<String,Report> mReports;
    private Context mContext;
    private LayoutInflater inflater;
    private AppCompatImageView ivThumbnail;
    private AppCompatTextView tvTitle;
    private AppCompatTextView tvDesc;
    private AppCompatTextView tvUpvotes;
    private int dimension;


    public ClusterInfoWindowAdapter(HashMap<String,Report> reports, Context context){
        this.mReports = reports;
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.mContent = inflater.inflate(R.layout.layout_maps_infowindow_content, null);

        dimension = PhotoUtility.dp2px(mContext, 120);
        ivThumbnail = mContent.findViewById(R.id.infowindow_thumbnail);
        tvTitle = mContent.findViewById(R.id.infowindow_title);
        tvDesc = mContent.findViewById(R.id.infowindow_desc);
        tvUpvotes = mContent.findViewById(R.id.infowindow_upvotes);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return  null;
    }

    @SuppressLint("CheckResult")
    @Override
    public View getInfoContents(Marker marker) {
        String key = marker.getTitle();
        Report item = mReports.get(key);
        String vote = String.valueOf(item.getUpvoteCount()+1);
        String desc = mContext.getString(R.string.info_window_description)+item.getDescription();

        tvTitle.setText(item.getReporterName());
//        tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvUpvotes.setText(vote);
        tvDesc.setText(desc);
        if (item.getDescription().isEmpty()) tvDesc.setVisibility(View.GONE);
        else tvDesc.setVisibility(View.VISIBLE);
        ivThumbnail.setImageResource(R.drawable.placeholder);
        GlideApp.with(mContext)
            .asBitmap()
            .load(item.getImageUrl())
            .override(dimension,dimension)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    ivThumbnail.setImageBitmap(resource);
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                        marker.showInfoWindow();
                    }
                }
            });

        return mContent;
    }


}
