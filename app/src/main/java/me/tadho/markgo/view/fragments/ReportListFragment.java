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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import me.tadho.markgo.R;
import me.tadho.markgo.data.FbPersistence;
import me.tadho.markgo.data.enumeration.Prefs;
import me.tadho.markgo.data.enumeration.UpdatePaths;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.view.adapter.ReportViewHolder;
import timber.log.Timber;

public abstract class ReportListFragment extends Fragment{

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    DatabaseReference timelineRef;
    DatabaseReference myReportRef;
    DatabaseReference userUpvotesRef;
    RecyclerView mRecycler;
    LinearLayoutManager mManager;
    FirebaseRecyclerAdapter<Report, ReportViewHolder> mAdapter;
    HashMap<String, Boolean> userUpvotes;

    public ReportListFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUpvotes = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FbPersistence.getDatabase().getReference();
        timelineRef = dbRef.child(Prefs.FD_REF_REPORTS);
        myReportRef = dbRef.child(Prefs.FD_REF_USERREPORTS).child(getUid());
        userUpvotesRef = dbRef.child(Prefs.FD_REF_USERUPVOTES).child(getUid());
        timelineRef.keepSynced(true);
        myReportRef.keepSynced(true);
        userUpvotesRef.keepSynced(true);

        userUpvotesRef.addValueEventListener(valueEventListener());
    }

    private ValueEventListener valueEventListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userUpvotes = (HashMap<String, Boolean>) dataSnapshot.getValue();
                } else userUpvotes.clear();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                final String reportUid = model.getReporterId();
                Boolean upvote = userUpvotes.get(reportKey) != null;
                if (model.getReporterId().equals(getUid())) upvote = true;
                holder.bindToReportItem(getActivity(), model,
                    onClickListener(reportUid, reportKey), upvote);
            }
        };
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mManager.findLastCompletelyVisibleItemPosition();
            }
        });

        mRecycler.setAdapter(mAdapter);
    }

    private View.OnClickListener onClickListener(String reportUid, String reportKey){
        return v -> {
            if (v.getId() == R.id.report_menu){
                Timber.d("Report Menu clicked! -> "+reportKey);
            } else if (v.getId() == R.id.upvote_icon) {
                if (!reportUid.equals(getUid()))
                    updateVote(reportUid, reportKey, userUpvotes.get(reportKey) == null);
            }
        };
    }

    private void updateVote(String reportUid, String reportKey, Boolean isAdded){
        DatabaseReference reportRef = dbRef.child(Prefs.FD_REF_USERREPORTS).child(reportUid);
        Map<String, Object> userUpvotesPath = UpdatePaths.getVoteReportPaths(reportKey, isAdded);
        userUpvotesRef.updateChildren(userUpvotesPath, (error, ref) -> {
            if (error == null)
                UpdatePaths.runVoteTransactions(reportKey, isAdded, timelineRef, reportRef);
        });
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
        userUpvotesRef.removeEventListener(valueEventListener());
        timelineRef.keepSynced(false);
        myReportRef.keepSynced(false);
        userUpvotesRef.keepSynced(false);
    }

    protected String getUid(){
        return mAuth.getUid();
    }

    public abstract Query getQuery(DatabaseReference qRef);
}
