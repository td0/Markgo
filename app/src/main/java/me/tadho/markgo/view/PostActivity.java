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

package me.tadho.markgo.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.patloew.rxlocation.RxLocation;

import me.tadho.markgo.BuildConfig;
import me.tadho.markgo.utils.GlideApp;
import me.tadho.markgo.utils.PhotoUtility;
import me.tadho.markgo.view.customView.ThumbnailView;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import timber.log.Timber;


public class PostActivity extends AppCompatActivity
    implements View.OnClickListener{

    private Uri photoUri;
    private File photoFile;
    private String photoPath;
    private Bitmap photoTaken;

    private LatLng mLatLng;
    private String streetName;
    private String description;
    private String photoUploadedPath;

    private TextView tvStreetName;
    private ThumbnailView thumbnailView;
    private FloatingActionButton mFab;
    private ImageButton customLocationButton;
    private DoubleBounce spinner;

    private RxLocation rxLocation;
    private LocationRequest locationRequest;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int mode = (int) (bundle != null ? bundle.get(Constants.TAKE_MODE_EXTRA) : 0);

        initiation();
        if (mode != 0) launchTakeMode(mode);
    }

    private void initiation(){
        if (thumbnailView==null) {
            setContentView(R.layout.activity_post);
            setSupportActionBar(findViewById(R.id.toolbar));
            setTitle(getString(R.string.title_post));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            compositeDisposable = new CompositeDisposable();
            thumbnailView = findViewById(R.id.iv_preview);
            spinner = new DoubleBounce();
            spinner.setBounds(0,0,46,46);
            spinner.setColor(getResources().getColor(R.color.text_white));
            tvStreetName = findViewById(R.id.post_text_street);
            tvStreetName.setCompoundDrawables(spinner,null,null,null);
            spinner.start();
            customLocationButton = findViewById(R.id.button_set_custom_location);
            customLocationButton.setOnClickListener(this);
            mFab = findViewById(R.id.fab_submit_post);
            mFab.hide();
            streetName = Constants.STRING_NULL;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideApp.get(PostActivity.this).clearMemory();
        if (compositeDisposable!=null){
            if(!compositeDisposable.isDisposed()){
                Timber.d("RxTest onDestroy, disposing");
                compositeDisposable.dispose();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == thumbnailView.getId()){
            Intent intent = new Intent(PostActivity.this,
                    PhotoViewerActivity.class)
                .putExtra(Constants.PHOTO_PATH_EXTRA, photoPath)
                .putExtra(Constants.LOCAL_FILE_EXTRA, true);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(PostActivity.this, v,
                            getString(R.string.animation_photo_view));
            startActivity(intent, options.toBundle());
        } else if (v.getId() == customLocationButton.getId()) {
            Timber.d("Custom location button pressed");
            Intent mapPickerIntent = new Intent(PostActivity.this, MapsActivity.class)
                .putExtra(Constants.MAPS_MODE,Constants.MAPS_PICKER);
            ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(PostActivity.this, v,
                    getString(R.string.animation_maps_picker));
            startActivityForResult(mapPickerIntent, Constants.REQUEST_LOCATION_CODE);
        }
    }

    private void setThumbnailView(Uri uri){
        RequestOptions requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE);
        GlideApp.with(PostActivity.this)
            .load(uri)
            .apply(requestOptions)
            .placeholder(R.drawable.placeholder)
            .into(thumbnailView).onStop();
    }

    private File getPhotoFile() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File mediaStorageDir = new File(getExternalCacheDir(), Environment.DIRECTORY_PICTURES);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Timber.e("failed to create "+Environment.DIRECTORY_PICTURES+" directory");
                return null;
            }
            return new File(mediaStorageDir.getPath() + File.separator + Constants.postFileName);
        }
        Timber.e("Can't find External Storage / is not mounted");
        return null;
    }

    private Uri getFileProviderUri(){
        return FileProvider.getUriForFile(PostActivity.this,
            BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
    }

    public void launchTakeMode(int mode) {
        photoTaken = null;
        photoFile = getPhotoFile();
        if (photoFile == null) {
            Timber.e("Couldn't fetch photoFile");
            finish();
            return;
        }
        photoPath = photoFile.getPath();
        Timber.d("photoPath -> "+ photoPath);
        switch (mode){
            case Constants.TAKE_MODE_EXTRA_CAMERA:
                Timber.d("Call Camera here");
                launchCamera();
                break;
            case Constants.TAKE_MODE_EXTRA_GALLERY:
                Timber.d("Call Gallery here");
                launchGallery();
                break;
            default:
                throw new RuntimeException("Can't get intent extras");
        }
    }

    private void launchCamera() {
        Timber.d("Launch Camera here");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, getFileProviderUri());
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
             startActivityForResult(cameraIntent, Constants.REQUEST_CAMERA_CODE);
        }
    }

    private void launchGallery() {
        Timber.d("Launch Gallery here");
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(galleryIntent, Constants.REQUEST_GALLERY_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.d("Camera onActivityResult");
                cameraAction();
            } else { // Result was a failure
                Timber.d("Picture was not taken!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_GALLERY_CODE){
            if (resultCode == RESULT_OK) {
                Timber.d("Gallery onActivityResult");
                if (data!=null) galleryAction(data.getData());
            } else {
                Timber.d("Picture was not chosen!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_LOCATION_CODE) {
            Timber.d("Set the street name based on custom location");
        } else {
            Timber.d("Something fishy giving back onActivityResult");
            Timber.d("Request Code = "+requestCode);
            Timber.d("Result Code = "+resultCode);
        }
    }

    /*
    * CAMERA ACTION
    */
    private void cameraAction(){
        Timber.d("Camera : Loading photo with glide");
        setThumbnailView(Uri.fromFile(photoFile));
        Disposable camDisposable = Observable.just(Constants.REQUEST_CAMERA_CODE)
            .observeOn(Schedulers.io())
            .doOnNext(x->{
                Timber.d("(onNext)->RXCamera "+Thread.currentThread().getName());
                Timber.d("Gallery : Getting photo bitmap");
                photoUri = Uri.fromFile(photoFile);
                photoTaken = BitmapFactory.decodeFile(photoPath);
            })
            .compose(aggregatorObservableTransformer())
            .subscribe();
        compositeDisposable.add(camDisposable);
    }

    /*
    * GALLERY ACTION
    */
    private void galleryAction(Uri uri){
        Timber.d("Gallery : Loading photo with glide");
        setThumbnailView(uri);
        Disposable galDisposable = Observable.just(Constants.REQUEST_GALLERY_CODE)
            .observeOn(Schedulers.io())
            .doOnNext(x->{
                Timber.d("(onNext)->RXGallery "+Thread.currentThread().getName());
                Timber.d("Gallery : Getting photo bitmap");
                photoUri = uri;
                try {
                    photoTaken = MediaStore.Images.Media
                        .getBitmap(PostActivity.this.getContentResolver(), photoUri);
                } catch (IOException e) {
                    Timber.e(e.getMessage());
                    e.printStackTrace();
                }
            })
            .compose(aggregatorObservableTransformer())
            .subscribe();
        compositeDisposable.add(galDisposable);
    }

    /*
    * -> AGGREGATOR OBSERVABLE TRANSFORMER <-|
    */
    private ObservableTransformer<Integer,String> aggregatorObservableTransformer(){
        return obs -> obs
            .compose(writePhotoObservableTransformer())
            .compose(exifObservableTransformer())
            .filter(x -> x.equals(Constants.STRING_NULL))
            .flatMap(x -> getCurrentLocation());
    }

    /*
    * WRITING PHOTO FILE TO BE UPLOADED
    */
    private ObservableTransformer<Integer,Integer> writePhotoObservableTransformer(){
        return obs -> obs
            .doOnNext(x -> {
                Timber.d("(onNext)->WriteFile|Camera/Gallery "+Thread.currentThread().getName());
                Bitmap compressedBitmap = PhotoUtility
                    .rotateBitmap(PostActivity.this, photoUri, photoTaken);
                Timber.d("WriteFile|Camera/Gallery : init compress stream");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 45, bytes);
                File resizedFile = new File(photoPath);
                try {
                    Timber.d("WriteFile|Camera/Gallery : writing file...");
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                    Timber.d("WriteFile|Camera/Gallery : file written");
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e(e.getMessage());
                }
                thumbnailView.setOnClickListener(this);
            });
    }

    /*
    * READ EXIF LOCATION
    */
    private ObservableTransformer<Integer,String> exifObservableTransformer(){
        return obs -> obs
            .map(x->{
                mLatLng = PhotoUtility.getExifLocation(photoPath);
                if (mLatLng != null) setStreetName(mLatLng);
                return streetName;
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(street->{
                if (!street.equals(Constants.STRING_NULL)) {
                    spinner.stop();
                    Drawable drw = getResources()
                        .getDrawable(R.drawable.ic_location_on_red_400_24dp);
                    drw.setBounds(0,0,46,46);
                    tvStreetName.setText(street);
                    tvStreetName.setCompoundDrawables(drw,null,null,null);
                    customLocationButton.setVisibility(View.VISIBLE);
                    mFab.show();
                } else {
                    spinner.setColor(getResources().getColor(R.color.blue_300));
                    spinner.setBounds(0,0,46,46);
                    tvStreetName.setText(R.string.get_gps_location);
                    tvStreetName.setCompoundDrawables(spinner, null, null, null);
                }
            });
    }

    /*
    * GET CURRENT GPS LOCATION
    */
    private Observable<String> getCurrentLocation(){
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(1)
            .setInterval(3000);
        rxLocation = new RxLocation(PostActivity.this);
        rxLocation.setDefaultTimeout(10, TimeUnit.SECONDS);
        return rxLocation.settings().checkAndHandleResolution(locationRequest)
            .flatMapObservable(isActivated->{
                if (isActivated) {
                    Timber.d("Location settings turned on");
                    return onLocationSettingsActivatedObservable();
                }
                else return Observable.just(Constants.STRING_NULL)
                    .doOnNext(s -> {
                        Timber.d("Can't get the location, user refused to turn on location settings");
                        spinner.stop();
                        Drawable drw = getResources()
                            .getDrawable(R.drawable.ic_location_off_red_400_24dp);
                        drw.setBounds(0,0,46,46);
                        tvStreetName.setText(R.string.cant_get_location);
                        tvStreetName.setCompoundDrawables(drw,null,null,null);
                        customLocationButton.setVisibility(View.VISIBLE);
                    });
            });
    }

    /*
    * GOT LOCATION FROM GPS
    */
    @SuppressLint("MissingPermission")
    private Observable<String> onLocationSettingsActivatedObservable(){
        return rxLocation.location().updates(locationRequest)
            .map(location -> {
                mLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                setStreetName(mLatLng);
                return streetName;
            })
            .doOnNext(street->{
                Timber.d("Found location from GPS");
                if (street.equals(Constants.STRING_NULL))
                    street = getString(R.string.cant_get_street_name);
                spinner.stop();
                Drawable drw = getResources()
                    .getDrawable(R.drawable.ic_location_on_red_400_24dp);
                drw.setBounds(0,0,46,46);
                tvStreetName.setText(street);
                tvStreetName.setCompoundDrawables(drw,null,null,null);
                customLocationButton.setVisibility(View.VISIBLE);
                mFab.show();
            });
    }

    private void setStreetName(LatLng latLng){
        Timber.d("Geocode reading geocode from lat & long");
        List<Address> addresses = null;
        try {
            addresses = new Geocoder(PostActivity.this)
                .getFromLocation(latLng.latitude, latLng.longitude,1);
        } catch (IOException e) { e.printStackTrace(); }
        if(addresses == null || addresses.size() == 0 ){
            Timber.d("Geocode can't find any address");
            return;
        }
        streetName = addresses.get(0).getThoroughfare();
        Timber.d("Geocode Street Name : "+streetName);
    }
}