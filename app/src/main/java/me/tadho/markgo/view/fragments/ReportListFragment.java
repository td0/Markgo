/*
 * Copyright (c) 2018 Tadho Muhammad <tadho@me.com>
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

package me.tadho.markgo.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import me.tadho.markgo.R;
import me.tadho.markgo.data.FbPersistence;
import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.data.enumeration.Prefs;
import me.tadho.markgo.data.enumeration.RefPaths;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.DisplayUtility;
import me.tadho.markgo.view.PhotoViewerActivity;
import me.tadho.markgo.view.adapter.ReportViewHolder;
import timber.log.Timber;

public abstract class ReportListFragment extends Fragment{

    private String mName;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private DatabaseReference timelineRef;
    private DatabaseReference myReportRef;
    private DatabaseReference userUpvotesRef;
    private DatabaseReference userFixedIssueRef;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<Report, ReportViewHolder> mAdapter;
    private HashMap<String, Boolean> userUpvotes;
    private HashMap<String, Boolean> userFixedIssue;

    private Report report;

    public ReportListFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUpvotes = new HashMap<>();
        userFixedIssue = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FbPersistence.getDatabase().getReference();
        timelineRef = dbRef.child(Prefs.FD_REF_REPORTS);
        myReportRef = dbRef.child(Prefs.FD_REF_USERREPORTS).child(getUid());
        userUpvotesRef = dbRef.child(Prefs.FD_REF_USERUPVOTES).child(getUid());
        userFixedIssueRef = dbRef.child(Prefs.FD_REF_USERFIXEDISSUES).child(getUid());
        timelineRef.keepSynced(true);
        myReportRef.keepSynced(true);
        userUpvotesRef.keepSynced(true);
        userFixedIssueRef.keepSynced(true);
        userUpvotesRef.addValueEventListener(upvoteEventListener());
        userFixedIssueRef.addValueEventListener(fixedEventListener());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mName = sp.getString(Prefs.PREF_KEY_USER_NAME, Consts.STRING_NOT_AVAILABLE);
    }

    private ValueEventListener upvoteEventListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userUpvotes = (HashMap<String, Boolean>) dataSnapshot.getValue();
                } else userUpvotes.clear();
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private ValueEventListener fixedEventListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    userFixedIssue = (HashMap<String, Boolean>) dataSnapshot.getValue();
                else userFixedIssue.clear();
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_timeline, container, false);

        mRecycler = rootView.findViewById(R.id.recycler_timeline);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query timelineQuery = getQuery(dbRef);
        FirebaseRecyclerOptions<Report> options = new FirebaseRecyclerOptions.Builder<Report>()
            .setQuery(timelineQuery, Report.class)
            .build();

        mAdapter = new FirebaseRecyclerAdapter<Report, ReportViewHolder>(options) {
            @Override
            public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ReportViewHolder(inflater.inflate(R.layout.layout_item_report, parent, false));
            }
            @Override
            protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull Report model) {
                final String reportKey = getRef(position).getKey();
                Boolean upvote = userUpvotes.get(reportKey) != null;
                if (model.getReporterId().equals(getUid())) upvote = true;
                holder.bindToReportItem(getActivity(), model,
                    onClickListener(reportKey, model), upvote);
            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                if (type == ChangeEventType.ADDED) {
                    if (snapshot.getValue(Report.class).getReporterId().equals(getUid())){
                        mRecycler.scrollToPosition(newIndex);
                    }
                }
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private View.OnClickListener onClickListener(String reportKey, Report report){
        String reporterId = report.getReporterId();
        return v -> {
            if (v.getId() == R.id.report_menu){
                Timber.d("Report Menu clicked! -> "+reportKey);
                Context wrapper = new ContextThemeWrapper(getActivity(), R.style.popupMenuStyle);
                PopupMenu popup = new PopupMenu(wrapper, v);
                int menuResId;
                if (reporterId.equals(getUid())) menuResId = R.menu.my_report_menu;
                else menuResId = R.menu.timeline_report_menu;
                popup.getMenuInflater().inflate(menuResId, popup.getMenu());
                if (userFixedIssue.get(reportKey) != null)
                    popup.getMenu().findItem(R.id.menu_issue_fixed).setTitle(R.string.menu_cancel_fixed_issue);
                popup.setOnMenuItemClickListener(onMenuItemClickListener(reportKey, report));
                popup.show();
            } else if (v.getId() == R.id.upvote_icon) {
                if (!reporterId.equals(getUid()))
                    updateVote(reporterId, reportKey, userUpvotes.get(reportKey) == null);
            } else if (v.getId() == R.id.report_photo) {
                Timber.d("Photo clicked");
                Intent intent = new Intent(getActivity(),PhotoViewerActivity.class)
                    .putExtra(Consts.PHOTO_PATH_EXTRA, report.getImageUrl())
                    .putExtra(Consts.LOCAL_FILE_EXTRA, false);
                ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), v,
                        getActivity().getString(R.string.animation_photo_view));
                startActivity(intent, options.toBundle());
            }
        };
    }

    private void updateVote(String reportUid, String reportKey, Boolean isAdded){
        DatabaseReference reportRef = dbRef.child(Prefs.FD_REF_USERREPORTS).child(reportUid);
        Map<String, Object> userUpvotesPath = RefPaths.getVoteReportPaths(reportKey, isAdded);
        userUpvotesRef.updateChildren(userUpvotesPath, (error, ref) -> {
            if (error == null)
                RefPaths.runVoteTransactions(reportKey, isAdded, reportRef, timelineRef);
        });
    }

    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener(String key, Report report){
        return (item -> {
            switch (item.getItemId()){
                case R.id.menu_delete_report:
                    menuDeleteReport(key);
                    return true;
                case R.id.menu_issue_fixed:
                    menuIssueFixed(key);
                    return true;
                case R.id.menu_issue_abuse:
                    menuIssueAbuse(key, report);
                    return true;
                default:
                    return false;
            }
        });
    }

    private void menuDeleteReport(String key){
        DisplayUtility.customAlertDialog(getActivity())
            .setTitle(R.string.dialog_delete_report)
            .setPositiveButton(R.string.dialog_delete,((dialog, which) -> {
                Map<String,Object> deleteReportPaths = RefPaths.getDeleteReportPaths(getUid(), key);
                dbRef.updateChildren(deleteReportPaths);
                String photoStorageRef = RefPaths.getPhotoStorageRef(getUid())+key+".jpg";
                FirebaseStorage.getInstance()
                    .getReference(photoStorageRef)
                    .delete();
            }))
            .setNegativeButton(R.string.dialog_cancel,null)
            .show();
    }

    private void menuIssueFixed(String key){
        Map<String, Object> fixedIssuePaths;
        FloatingActionMenu fam = getActivity().findViewById(R.id.mFam);
        int dialogMsgId;
        if (userFixedIssue.get(key) == null) {
            fixedIssuePaths = RefPaths.getFixedPaths(getUid(), key);
            dialogMsgId = R.string.dialog_issuing_fixed_road;
        } else {
            fixedIssuePaths = RefPaths.getCancelFixedPaths(getUid(), key);
            dialogMsgId = R.string.dialog_cancelling_fixed_road;
        }
        Snackbar.make(fam, dialogMsgId, Snackbar.LENGTH_SHORT).show();
        dbRef.updateChildren(fixedIssuePaths);
    }

    private  void menuIssueAbuse(String key, Report report){
        LayoutInflater li = LayoutInflater.from(getActivity());
        View abuseDialogLayout = li.inflate(R.layout.dialog_abuse_reasoning, null);
        final EditText reasoningET = abuseDialogLayout.findViewById(R.id.reasoning_input);
        DisplayUtility.customAlertDialog(getActivity())
            .setView(abuseDialogLayout)
            .setNegativeButton(R.string.dialog_cancel,null)
            .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                String reasoning = reasoningET.getText().toString();
                Map<String, Object> abuseIssuePaths;
                abuseIssuePaths = RefPaths.getAbuseIssuePaths(getUid(), mName, report,
                    key, reasoning);
                dbRef.updateChildren(abuseIssuePaths);
            })
            .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null ){
            mAdapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) mAdapter.stopListening();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userUpvotesRef.removeEventListener(upvoteEventListener());
        userFixedIssueRef.removeEventListener(fixedEventListener());
        timelineRef.keepSynced(false);
        myReportRef.keepSynced(false);
        userUpvotesRef.keepSynced(false);
        userFixedIssueRef.keepSynced(false);
        userUpvotes.clear();
    }

    protected String getUid(){
        return mAuth.getUid();
    }

    public abstract Query getQuery(DatabaseReference qRef);
}
