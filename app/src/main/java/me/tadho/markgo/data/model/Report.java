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

import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import me.tadho.markgo.data.enumeration.Consts;


public class Report implements Serializable{
    private String reporterId;
    private String reporterName;
    private String streetName;
    private String description;
    private String imageUrl;
    private Integer upvoteCount;
    private Boolean fixed;
    private Double latitude;
    private Double longitude;
    private Map<String, Double> coordinate;
    private Map<String, Object> date;

    public Report(){

    }

    public Report(String uid, String uname, String street, String desc, String imgUrl, Map<String, Double> coord){
        this.reporterId = uid;
        this.reporterName = uname;
        this.streetName = street;
        this.description = desc;
        this.imageUrl = imgUrl;
        this.coordinate = coord;
        this.upvoteCount = 0;
        this.fixed = false;
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

    public Integer getUpvoteCount() {
        return upvoteCount;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public Map<String, Object> getDate() {
        if (date != null) return date;

        HashMap<String, Object> timeStampNow = new HashMap<>();
        timeStampNow.put("reportTime", ServerValue.TIMESTAMP);
        return timeStampNow;
    }

    @Exclude
    public long getReportTime(){
        return (long)date.get("reportTime");
    }
    @Exclude
    public Double getLatitude(){
        return coordinate.get(Consts.KEY_LATITUDE);
    }
    @Exclude
    public Double getLongitude(){
        return coordinate.get(Consts.KEY_LONGITUDE);
    }

    @Exclude
    public void setUpvoteCount(int count){
        this.upvoteCount = count;
    }

    public Map<String, Double> getCoordinate() {
        return coordinate;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
