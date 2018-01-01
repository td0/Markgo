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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

import me.tadho.markgo.data.model.Report;
import timber.log.Timber;

public final class UpdatePaths {

    // Post Report
    public static Map<String, Object> getPostReportPaths(String key, Report report, int reportCount){
        String uid = report.getReporterId();
        Map<String, Object> object = new HashMap<>();
        object.put(pathJoin(Prefs.FD_REF_REPORTS ,key), report);
        object.put(pathJoin(Prefs.FD_REF_USERREPORTS, uid, key), report);
        object.put(pathJoin(Prefs.FD_REF_USERS, uid, Prefs.FD_REF_REPORTCOUNT),
            reportCount);
        return object;
    }

    // Vote Report
    public static Map<String, Object> getVoteReportPaths(String key, Boolean isAdded){
        HashMap<String, Object> object = new HashMap<>();
        if (isAdded) object.put(key, true);
        else object.put(key, null);
        return object;
    }

    public static void runVoteTransactions(String reportKey, Boolean isAdded, DatabaseReference... refs){
        for (DatabaseReference ref : refs) {
            ref.child(reportKey).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Report r = mutableData.getValue(Report.class);
                    if (r == null) return Transaction.success(mutableData);
                    if (isAdded) r.setUpvoteCount(r.getUpvoteCount() + 1);
                    else r.setUpvoteCount(r.getUpvoteCount() - 1);
                    mutableData.setValue(r);
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
            });
        }
    }

    private static String pathJoin(String... strings){
        final String SLASH = "/";
        StringBuilder combinedPath = new StringBuilder("");
        for (String string: strings){
            combinedPath.append(string);
            combinedPath.append(SLASH);
        }
        Timber.d("Combined path -> "+combinedPath.toString());
        return combinedPath.toString();
    }
}
