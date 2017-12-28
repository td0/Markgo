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

package me.tadho.markgo.view.customView;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import me.tadho.markgo.R;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.GlideApp;

public class ClusterInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private View mView;
    private View mContent;
    private HashMap<String,Report> mReports;
    private Context mContext;
    private LayoutInflater inflater;

    public ClusterInfoWindowAdapter(HashMap<String,Report> reports, Context context){
        this.mReports = reports;
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.mView = inflater.inflate(R.layout.layout_maps_infowindow, null);
        this.mContent = inflater.inflate(R.layout.layout_maps_infowindow_content, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        String key = marker.getTitle();
        Report item = mReports.get(key);

        AppCompatImageView ivThumbnail = mContent.findViewById(R.id.infowindow_thumbnail);
        AppCompatTextView tvTitle = mContent.findViewById(R.id.infowindow_title);
        AppCompatTextView tvDesc = mContent.findViewById(R.id.infowindow_desc);
        AppCompatTextView tvUpvotes = mContent.findViewById(R.id.infowindow_upvotes);

        tvTitle.setText(item.getReporterName());
        tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvUpvotes.setText("upvote : "+ item.getUpvoteCount());
        tvDesc.setText("description : "+ item.getDescription());
        if (item.getDescription().isEmpty()) tvDesc.setVisibility(View.INVISIBLE);
        else tvDesc.setVisibility(View.VISIBLE);

        GlideApp.with(mContext)
            .load(item.getImageUrl())
            .into(ivThumbnail);

        return mContent;
    }
}
