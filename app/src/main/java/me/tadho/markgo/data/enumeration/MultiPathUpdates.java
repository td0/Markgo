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

import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.data.model.ReportMaps;
import timber.log.Timber;

public final class MultiPathUpdates {

    // Post Report
    public static Map<String, Object> getPostReportPaths(String key, Report report, int reportCount){
        String uid = report.getReporterId();
        ReportMaps reportMaps = new ReportMaps(report.getCoordinate());
        Map<String, Object> object = new HashMap<>();
        object.put(pathJoin(Prefs.FD_REF_REPORTS ,key), report);
        object.put(pathJoin(Prefs.FD_REF_USERREPORTS, uid, key), report);
        object.put(pathJoin(Prefs.FD_REF_USERS, uid, Prefs.FD_REF_REPORTCOUNT),
            reportCount);
        object.put(pathJoin(Prefs.FD_REF_REPORTMAPS, key), reportMaps);
        return object;
    }

    // Vote Report
    public static Map<String, Object> getVoteReportPaths(String voterUid, String reporterUid, String key, int voteCount){
        Map<String, Object> object = new HashMap<>();
        voteCount++;
        return updateReportVotes(voterUid, reporterUid, key, voteCount);
    }

    // Devote Report
    public static Map<String, Object> getDevoteReportPaths(String voterUid, String reporterUid, String key, int voteCount){
        voteCount--;
        return updateReportVotes(voterUid, reporterUid, key, voteCount);
    }

    private static Map<String, Object> updateReportVotes(String voterUid, String reporterUid, String key, int voteCount) {
        Map<String, Object> object = new HashMap<>();
        object.put(pathJoin(Prefs.FD_REF_USERUPVOTES, voterUid, key), true);
        object.put(pathJoin(Prefs.FD_REF_REPORTS, key, Prefs.FD_REF_UPVOTECOUNT), voteCount);
        object.put(pathJoin(Prefs.FD_REF_USERREPORTS, reporterUid, key, Prefs.FD_REF_UPVOTECOUNT), voteCount);
        object.put(pathJoin(Prefs.FD_REF_REPORTMAPS, key, Prefs.FD_REF_UPVOTECOUNT), voteCount);
        return object;
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
