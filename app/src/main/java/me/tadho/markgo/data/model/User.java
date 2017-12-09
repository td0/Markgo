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

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;

// [START blog_user_class]
@IgnoreExtraProperties
public class User implements Serializable {
    private String name;
    private int reportCount;
    private int status;
    private HashMap<String, Object> joinDate;

    public User(){

    }

    public User(String name){
        this.name = name;
        reportCount = 0;
        status = 1;
    }

    public String getName() {
        return name;
    }

    public int getReportCount() {
        return reportCount;
    }

    public int getStatus() {
        return status;
    }

    public HashMap<String, Object> getJoinDate(){
        if (joinDate != null) return joinDate;

        HashMap<String, Object> timeStampNow = new HashMap<>();
        timeStampNow.put("timestamp", ServerValue.TIMESTAMP);
        return timeStampNow;
    }

    @Exclude
    public long getJoinDateLong() {
        return (long)joinDate.get("timestamp");
    }
}
