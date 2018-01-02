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

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Calendar;
import java.util.Locale;

import me.tadho.markgo.R;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.GlideApp;
import me.tadho.markgo.view.customView.ThumbnailView;

public class ReportViewHolder extends RecyclerView.ViewHolder {

    private TextView reporterNameTV;
    private TextView streetNameTV;
    private TextView descriptionTV;
    private TextView upvoteCountTV;
    private TextView dateTV;
    private ThumbnailView photoIV;
    private ImageView upvoteIconIV;
    private ImageView menuIV;
    private TextView fixedFlairTV;

    public ReportViewHolder(View itemView) {
        super(itemView);

        reporterNameTV = itemView.findViewById(R.id.reporter_name);
        streetNameTV = itemView.findViewById(R.id.report_street_name);
        descriptionTV = itemView.findViewById(R.id.report_description);
        upvoteCountTV = itemView.findViewById(R.id.report_upvote_count);
        dateTV = itemView.findViewById(R.id.report_date);
        photoIV = itemView.findViewById(R.id.report_photo);
        upvoteIconIV = itemView.findViewById(R.id.upvote_icon);
        menuIV = itemView.findViewById(R.id.report_menu);
        fixedFlairTV = itemView.findViewById(R.id.fixed_flair);
    }

    public void bindToReportItem(Context ctx, Report report, View.OnClickListener onClickListener, boolean upvoted) {
        String date = getDate(report.getReportTime());
        String desc = report.getDescription();
        GlideApp.with(ctx)
            .load(report.getImageUrl())
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(photoIV);
        reporterNameTV.setText(report.getReporterName());
        dateTV.setText(date);
        streetNameTV.setText(report.getStreetName());
        streetNameTV.setPaintFlags(streetNameTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        upvoteCountTV.setText(String.valueOf(report.getUpvoteCount()+1));
        if (desc.isEmpty()) descriptionTV.setVisibility(View.GONE);
        else {
            descriptionTV.setText(desc);
            descriptionTV.setVisibility(View.VISIBLE);
        }
        upvoteIconIV.setOnClickListener(null);
        menuIV.setOnClickListener(null);
        photoIV.setOnClickListener(null);
        photoIV.setOnClickListener(onClickListener);
        if (!report.getFixed()) {
            if (upvoted) upvoteIconIV.setImageResource(R.drawable.ic_star_yellow_24dp);
            else upvoteIconIV.setImageResource(R.drawable.ic_star_border_yellow_24dp);
            upvoteCountTV.setVisibility(View.VISIBLE);
            upvoteIconIV.setVisibility(View.VISIBLE);
            fixedFlairTV.setVisibility(View.GONE);
            upvoteIconIV.setOnClickListener(onClickListener);
            menuIV.setOnClickListener(onClickListener);
        } else {
            upvoteCountTV.setVisibility(View.GONE);
            upvoteIconIV.setVisibility(View.GONE);
            fixedFlairTV.setVisibility(View.VISIBLE);

        }


    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd MMM yyyy", cal).toString();
    }

}
