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

package me.tadho.markgo.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.github.clans.fab.FloatingActionMenu;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.patloew.rxlocation.RxLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.tadho.markgo.R;
import me.tadho.markgo.data.FbPersistence;
import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.data.enumeration.Prefs;
import me.tadho.markgo.data.model.ClusterMarker;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.DisplayUtility;
import me.tadho.markgo.view.adapter.ClusterInfoWindowAdapter;
import timber.log.Timber;

public class MapsListFragment extends Fragment
    implements View.OnClickListener, OnMapReadyCallback{

    private ObjectAnimator scaleInX;
    private com.github.clans.fab.FloatingActionButton fab_heatmap;
    private com.github.clans.fab.FloatingActionButton fab_cluster;

    private RxLocation rxLocation;
    private LocationRequest locationRequest;
    private CompositeDisposable compositeDisposable;
    private Disposable animateMyLocationDisposable;

    private LatLng mLatLng;
    private MapView mMapView;
    private GoogleMap googleMap;
    private FloatingActionButton fabMyLocation;
    private FloatingActionMenu mFam;

    private Boolean clusterMode = true;
    private Boolean populated = false;
    private Boolean initialLatLng = false;
    private HashMap<String, Report> mapItems = new HashMap<>();
    private ClusterManager<ClusterMarker> mClusterManager;
    private TileOverlay heatMapOverlay;
    private HeatmapTileProvider heatMapProvider;
    private List<WeightedLatLng> heatMapList = new ArrayList<>();

    private DatabaseReference dbMapsRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.maps_cluster_mode);
        mLatLng = Consts.MALANG_LATLNG;
        if (getArguments() != null) {
            mLatLng = getArguments().getParcelable(Consts.LATLNG_EXTRA);
            initialLatLng = true;
            Timber.d("Argument found -> "+mLatLng);
        }
        DatabaseReference dbRef = FbPersistence.getDatabase().getReference();
        dbMapsRef = dbRef.child(Prefs.FD_REF_REPORTS);
        dbMapsRef.keepSynced(true);
        compositeDisposable = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps_list, container, false);
        if (mMapView == null) mMapView = rootView.findViewById(R.id.maps_picker_view);
        mMapView.onCreate(savedInstanceState);
        try { MapsInitializer.initialize(getActivity().getApplicationContext());}
        catch (Exception e) {
            e.printStackTrace();
            Timber.e(e.getMessage());
        }
        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFab();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_my_location :
                Timber.d("fab my location clicked");
                if (animateMyLocationDisposable != null)
                    if (!animateMyLocationDisposable.isDisposed())
                        animateMyLocationDisposable.dispose();
                animateMyLocationDisposable = setMapsCameraCompletable(0f, true).subscribe();
                compositeDisposable.add(animateMyLocationDisposable);
                break;
            case R.id.fab_mode_cluster :
                mFam.close(true);
                if (clusterMode) break;
                clusterMode = true;
                getActivity().setTitle(R.string.maps_cluster_mode);
                initiateMapsMode();
                break;
            case R.id.fab_mode_heatmap :
                mFam.close(true);
                if (!clusterMode) break;
                clusterMode = false;
                getActivity().setTitle(R.string.maps_heatmap_mode);
                initiateMapsMode();
                break;
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        try {
            boolean success = true;
            if (!DisplayUtility.isDay()) success = googleMap.setMapStyle(MapStyleOptions
                .loadRawResourceStyle(getActivity().getBaseContext(), R.raw.map_style_dark));
            if (!success) Timber.e("MapsActivityRaw", "Style parsing failed.");
        } catch (Resources.NotFoundException e) {
            Timber.e("MapsActivityRaw", "Can't find style.", e);
        }

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setMaxZoomPreference(20f);
        googleMap.setMinZoomPreference(11f);
        googleMap.setLatLngBoundsForCameraTarget(Consts.MALANG_BOUNDS);

        if (!initialLatLng) {
            setMapsCamera(mLatLng, 13.5f, false);
            compositeDisposable.add(setMapsCameraCompletable(13.5f, false).subscribe());
        } else {
            setMapsCamera(mLatLng, 16, false);
        }
        compositeDisposable.add(mapEventListenerFlowable().subscribe());
    }

    private Flowable<DataSnapshot> mapEventListenerFlowable(){
        return RxFirebaseDatabase.observeValueEvent(dbMapsRef, BackpressureStrategy.LATEST)
            .doOnNext(snaps -> {
                if (snaps.exists()) {
                    if (mapItems != null) mapItems.clear();
                    for (DataSnapshot snap : snaps.getChildren()) {
                        if (!snap.getValue(Report.class).getFixed())
                            mapItems.put(snap.getKey(), snap.getValue(Report.class));
                    }
                    if (populated) refreshMapsData();
                    else initiateMapsMode();
                }
            })
            .doOnCancel(() -> Timber.d("RxFirebase ValueEventListener onCancel triggered!"));
    }

    private void initiateMapsMode() {
        populated = false;
        if (!mapItems.isEmpty()) {
            cleanMaps();
            if (clusterMode) setupClustering();
            else setupHeatMap();
            populated = true;
        }
    }

    private void refreshMapsData(){
        if (clusterMode) {
            if (mClusterManager != null) mClusterManager.clearItems();
            populateCluster();
            mClusterManager.cluster();
        } else {
            if (heatMapList != null) heatMapList.clear();
            populateHeatMap();
            heatMapProvider.setWeightedData(heatMapList);
            heatMapOverlay.clearTileCache();
        }
    }

    private void cleanMaps(){
        if (heatMapList != null){
            heatMapList.clear();
        }
        if (heatMapOverlay != null) {
            heatMapOverlay.setVisible(false);
            heatMapOverlay.remove();
            heatMapOverlay = null;
            heatMapProvider = null;
        }
        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(null);
            mClusterManager.setOnClusterClickListener(null);
        }
        googleMap.setOnCameraIdleListener(null);
        googleMap.setOnMarkerClickListener(null);
        googleMap.setInfoWindowAdapter(null);
        googleMap.clear();
    }

    private void setupClustering(){
        if (mClusterManager==null)
            mClusterManager = new ClusterManager<>(getActivity().getBaseContext(), googleMap);
        populateCluster();

        mClusterManager.getMarkerCollection()
            .setOnInfoWindowAdapter(new ClusterInfoWindowAdapter(mapItems, getActivity()));
        googleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(cluster -> {
            float zoom = googleMap.getCameraPosition().zoom+1.5f;
            setMapsCamera(cluster.getPosition(), zoom, true);
            return true;
        });
        mClusterManager.setAnimation(true);
        mClusterManager.cluster();
    }

    private void setupHeatMap(){
        populateHeatMap();
        if (heatMapProvider==null)
            heatMapProvider = new HeatmapTileProvider.Builder()
                .radius(23)
                .weightedData(heatMapList)
                .build();
        if (DisplayUtility.isDay()) heatMapProvider.setGradient(getCustomGradient());
        heatMapProvider.setOpacity(0.55d);
        heatMapOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapProvider));
    }

    private void populateCluster(){
        for (HashMap.Entry<String,Report> mapItem : mapItems.entrySet()) {
            Double lat = mapItem.getValue().getLatitude();
            Double lng = mapItem.getValue().getLongitude();
            mClusterManager.addItem(new ClusterMarker(lat,lng,mapItem.getKey()));
        }
    }

    private void populateHeatMap(){
        for (HashMap.Entry<String,Report> mapItem : mapItems.entrySet()) {
            Double lat = mapItem.getValue().getLatitude();
            Double lng = mapItem.getValue().getLongitude();
            Double intensity = Double.valueOf(mapItem.getValue().getUpvoteCount())+1d;
            heatMapList.add(new WeightedLatLng(new LatLng(lat, lng), intensity));
        }
    }

    private Single<Boolean> myLocationObservable(){
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(1)
            .setInterval(3000);
        rxLocation = new RxLocation(getActivity().getBaseContext());
        rxLocation.setDefaultTimeout(10, TimeUnit.SECONDS);
        return rxLocation.settings()
            .checkAndHandleResolution(locationRequest);
    }

    @SuppressLint("MissingPermission")
    private Maybe<Boolean> getLastKnownLocationMaybe(Boolean isActivated) {
        if (isActivated) return rxLocation.location()
            .lastLocation()
            .map(location -> {
                LatLng latLng =new LatLng(location.getLatitude(),location.getLongitude());
                Timber.d("Getting last known location -> "+latLng);
                mLatLng = latLng;
                return false;
            })
            .onErrorReturn(e -> true);
        else return Maybe.just(true);
    }

    @SuppressLint("MissingPermission")
    private Observable<LatLng> getMyLocationObservable(Boolean isFound){
        if (isFound) return rxLocation.location()
            .updates(locationRequest)
            .map(loc -> new LatLng(loc.getLatitude(), loc.getLongitude()))
            .doOnNext(latLng -> {
                Timber.d("Getting my location -> "+latLng);
                mLatLng = latLng;
            })
            .doOnError(e -> Timber.e(e.getMessage()));
        else {
            return Observable.just(mLatLng);
        }
    }

    private Completable setMapsCameraCompletable(float zoom, Boolean animate){
        return myLocationObservable()
            .flatMapMaybe(this::getLastKnownLocationMaybe)
            .flatMapObservable(this::getMyLocationObservable)
            .flatMapCompletable(latLng -> Completable
                .fromAction(() ->
                    setMapsCamera(latLng, zoom, animate)
                )
            );
    }

    private void setMapsCamera(LatLng latLng, float zoom,  Boolean animate){
        Timber.d("Moving maps camera to -> "+latLng);
        CameraUpdate cameraUpdate;
        if (zoom > 20) zoom = 20f;
        if (zoom >= 12) cameraUpdate = CameraUpdateFactory.newCameraPosition(
            new CameraPosition.Builder().target(latLng).zoom(zoom).build());
        else cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        if (animate) googleMap.animateCamera(cameraUpdate, 800, null);
        else googleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        dbMapsRef.keepSynced(false);
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        dbMapsRef.keepSynced(false);
        fabMyLocation.setOnClickListener(null);
        fab_heatmap.setOnClickListener(null);
        fab_cluster.setOnClickListener(null);
        scaleInX.removeAllListeners();
        mFam.setIconToggleAnimatorSet(null);

        if (compositeDisposable!=null) {
            if (!compositeDisposable.isDisposed())
                compositeDisposable.dispose();
            compositeDisposable = null;
        }
        if (mapItems!=null) {
            mapItems.clear();
            mapItems = null;
        }
        if (mClusterManager!=null) {
            mClusterManager.setOnClusterClickListener(null);
            mClusterManager.clearItems();
            mClusterManager = null;
        }
        if (heatMapList!=null) {
            heatMapProvider = null;
            heatMapList.clear();
            heatMapList = null;
        }
        if (heatMapOverlay!=null) {
            heatMapOverlay.clearTileCache();
            heatMapOverlay.remove();
            heatMapOverlay = null;
        }

        googleMap.setOnMarkerClickListener(null);
        googleMap.setOnMarkerDragListener(null);
        googleMap.setOnMapClickListener(null);
        googleMap.clear();
        googleMap = null;
        mMapView.onDestroy();
    }

    private void setupFab() {
        fabMyLocation = getView().findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(this);
        mFam = getView().findViewById(R.id.fam_maps_mode);
        fab_heatmap = getView().findViewById(R.id.fab_mode_heatmap);
        fab_cluster = getView().findViewById(R.id.fab_mode_cluster);
        fab_cluster.setOnClickListener(this);
        fab_heatmap.setOnClickListener(this);
        mFam.setClosedOnTouchOutside(true);
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(mFam.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(mFam.getMenuIconView(), "scaleY", 1.0f, 0.2f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(mFam.getMenuIconView(), "scaleY", 0.2f, 1.0f);
        scaleInX = ObjectAnimator.ofFloat(mFam.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);
        scaleInX.setDuration(150);
        scaleInY.setDuration(150);
        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFam.getMenuIconView().setImageResource(mFam.isOpened() ?
                    R.drawable.ic_layers_black_24dp :
                    R.drawable.ic_close_black_24dp);
            }
        });
        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));
        mFam.setIconToggleAnimatorSet(set);
    }

    private Gradient getCustomGradient(){
        final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
        };
        final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
        };

        return new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);
    }


}
