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

package me.tadho.markgo.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {
    private String reporterId;
    private String reporterName;
    private String streetName;
    private String description;
    private String imageUrl;
    private int upvoteCount;
    private String status;
    private HashMap<String, Object> coordinate;
    private HashMap<String, Object> date;

    public Report(){

    }

    public Report(String uid, String uname, String strname, String desc, String imgUrl, HashMap<String, Object> coord){
        this.reporterId = uid;
        this.reporterName = uname;
        this.streetName = strname;
        this.description = desc;
        this.imageUrl = imgUrl;
        this.upvoteCount = 0;
        this.status = "reported";
        this.coordinate = coord;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getDescription() {
        return description;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public String getStatus() {
        return status;
    }

    public HashMap<String, Object> getDate() {
        if (date != null) return date;

        HashMap<String, Object> timeStampNow = new HashMap<>();
        timeStampNow.put("reportTime", ServerValue.TIMESTAMP);
        return timeStampNow;
    }

    @Exclude
    public long getReportTime(){
        return (long)date.get("reportTime");
    }

    public HashMap<String, Object> getCoordinate() {
        return coordinate;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
