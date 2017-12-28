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

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patloew.rxlocation.RxLocation;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.utils.DisplayUtility;
import me.tadho.markgo.view.MapsActivity;
import timber.log.Timber;

public class MapsPickerFragment extends Fragment implements
    View.OnClickListener, OnMapReadyCallback{

    private Disposable pickLocationDisposable;
    private MapView mMapView;
    private GoogleMap googleMap;
    private LatLng mLatLng;
    private LatLng initialLatLng;
    private Marker pickerMarker;
    private RxLocation rxLocation;
    private LocationRequest locationRequest;

    private FloatingActionButton fabSubmit;
    private FloatingActionButton fabMyLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Getting fragment argument");
        initialLatLng = new LatLng(0,0);
        mLatLng = Consts.MALANG_LATLNG;
        if (getArguments() != null) {
            mLatLng = getArguments().getParcelable(Consts.LATLNG_EXTRA);
            initialLatLng = mLatLng;
            Timber.d("Argument found -> "+mLatLng);
        }
    }

    @Nullable
    @Override
    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps_picker, container, false);
        if (mMapView == null) mMapView = rootView.findViewById(R.id.maps_picker_view);
        mMapView.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {e.printStackTrace();}
        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabSubmit = getView().findViewById(R.id.fab_submit_location);
        fabMyLocation = getView().findViewById(R.id.fab_my_location);
        fabSubmit.setOnClickListener(this);
        fabMyLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_submit_location :
                Timber.d("Submit Location button clicked");
                if (!initialLatLng.equals(mLatLng)) ((MapsActivity)getActivity()).sendActivityResult(mLatLng);
                else getActivity().onBackPressed();
                break;
            case R.id.fab_my_location :
                Timber.d("My Location button clicked");
                pickLocationDisposable = myLocationSingle().subscribe();
                break;
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        if (!DisplayUtility.isDay()) {
            try {
                boolean success = googleMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(getActivity().getBaseContext(), R.raw.maps_style_dark));
                if (!success) {
                    Timber.e("MapsActivityRaw", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Timber.e("MapsActivityRaw", "Can't find style.", e);
            }
        }
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(true);
        googleMap.setLatLngBoundsForCameraTarget(Consts.MALANG_BOUNDS);

        MarkerOptions markerOptions = new MarkerOptions()
            .position(mLatLng)
            .draggable(true);
        pickerMarker = googleMap.addMarker(markerOptions);
        googleMap.setOnMarkerClickListener(marker -> {
            Timber.d("Marker clicked, playing dead!");
            return true;
        });
        googleMap.setOnMapClickListener(latLng -> {
            pickerMarker.setPosition(latLng);
            mLatLng = latLng;
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                mLatLng = marker.getPosition();
            }
        });

        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(mLatLng).zoom(17).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private Single myLocationSingle() {
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(1)
            .setInterval(3000);
        rxLocation = new RxLocation(getActivity().getBaseContext());
        rxLocation.setDefaultTimeout(10, TimeUnit.SECONDS);
        return rxLocation.settings()
            .checkAndHandleResolution(locationRequest)
            .flatMap(this::getMyLocationSingle);
    }

    @SuppressLint("MissingPermission")
    private Single getMyLocationSingle(Boolean isActivated) {
        if (isActivated) return rxLocation.location()
            .updates(locationRequest)
            .map(loc -> new LatLng(loc.getLatitude(),loc.getLongitude()))
            .take(1)
            .single(mLatLng)
            .doOnSuccess(latLng -> {
                Timber.d("Getting my location -> "+latLng);

                Timber.d("Setting marker on my location");
                pickerMarker.setPosition(latLng);

                Timber.d("Animating camera on my location");
                CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(17).build();
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    800,null);
                mLatLng = latLng;
            })
            .doOnError(e -> {
                Timber.e("Failed to get location updates");
                Timber.e(e.getMessage());
            });
        Timber.d("location isn't activated, return dummy single");
        return Single.just(mLatLng);
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
    public void onDestroy() {
        super.onDestroy();
        if (pickLocationDisposable != null) {
            pickLocationDisposable.dispose();
            pickLocationDisposable = null;
        }
        pickerMarker.remove();
        pickerMarker = null;
        googleMap.setOnMarkerClickListener(null);
        googleMap.setOnMarkerDragListener(null);
        googleMap.setOnMapClickListener(null);
        googleMap.clear();
        googleMap = null;
        fabSubmit.setOnClickListener(null);
        fabMyLocation.setOnClickListener(null);
        mMapView.getMapAsync(null);
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
