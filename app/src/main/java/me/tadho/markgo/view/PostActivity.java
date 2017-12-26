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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatEditText;
import android.text.format.Time;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import durdinapps.rxfirebase2.RxFirebaseStorage;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.patloew.rxlocation.RxLocation;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import me.tadho.markgo.BuildConfig;
import me.tadho.markgo.data.enumeration.Preferences;
import me.tadho.markgo.data.model.Report;
import me.tadho.markgo.utils.DisplayUtility;
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
    private String userName;
    private int reportCount;

    private TextView tvStreetName;
    private ThumbnailView thumbnailView;
    private FloatingActionButton mFab;
    private ImageButton customLocationButton;
    private DoubleBounce spinner;
    private ProgressDialog progress;

    private RxLocation rxLocation;
    private LocationRequest locationRequest;
    private CompositeDisposable compositeDisposable;
    private Subject<Boolean> customLocationButtonPressed = PublishSubject.create();

    private FirebaseAuth mAuth;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int mode = (int) (bundle != null ? bundle.get(Constants.TAKE_MODE_EXTRA) : 0);
        mAuth = FirebaseAuth.getInstance();
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

            streetName = Constants.STRING_NOT_AVAILABLE;
            compositeDisposable = new CompositeDisposable();
            thumbnailView = findViewById(R.id.iv_preview);
            mFab = findViewById(R.id.fab_submit_post);
            mFab.setOnClickListener(this);
            mFab.hide();

            spinner = new DoubleBounce();
            spinner.setBounds(0,0,46,46);
            spinner.setColor(getResources().getColor(R.color.text_white));
            tvStreetName = findViewById(R.id.post_text_street);
            tvStreetName.setCompoundDrawables(spinner,null,null,null);
            spinner.start();

            customLocationButton = findViewById(R.id.button_set_custom_location);
            customLocationButton.setOnClickListener(this);
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
    @SuppressLint("RestrictedApi")
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
            customLocationButtonPressed.onNext(true);
            mFab.hide();

            Bundle extras = new Bundle();
            Intent mapPickerIntent = new Intent(PostActivity.this, MapsActivity.class);
            extras.putChar(Constants.MAPS_MODE,Constants.MAPS_PICKER);
            if (mLatLng != null) extras.putParcelable(Constants.LATLNG_EXTRA, mLatLng);
            mapPickerIntent.putExtras(extras);

            ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(PostActivity.this, v,
                    getString(R.string.animation_maps_picker));
            startActivityForResult(mapPickerIntent,Constants.REQUEST_LOCATION_CODE,options.toBundle());
        } else if (v.getId() == mFab.getId()) {
            postReport();
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
            return new File(mediaStorageDir.getPath() + File.separator
                + Constants.POST_FILE_NAME + Constants.POST_FILE_EXT);
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
                Timber.d("Launching Camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, getFileProviderUri());
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
                }
                break;
            case Constants.TAKE_MODE_EXTRA_GALLERY:
                Timber.d("Launching Gallery");
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (galleryIntent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(galleryIntent, Constants.REQUEST_CODE_GALLERY);
                }
                break;
            default:
                throw new RuntimeException("Can't get intent extras");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {
                Timber.d("Camera onActivityResult");
                cameraAction();
            } else { // Result was a failure
                Timber.d("Picture was not taken!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_CODE_GALLERY){
            if (resultCode == RESULT_OK) {
                Timber.d("Gallery onActivityResult");
                if (data!=null) galleryAction(data.getData());
            } else {
                Timber.d("Picture was not chosen!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_LOCATION_CODE) {
            if (resultCode==RESULT_OK) {
                Timber.d("Set the street name based on custom location");
                spinner.start();
                tvStreetName.setCompoundDrawables(spinner, null, null, null);
                tvStreetName.setText(R.string.get_street_name);
                Disposable customLocationDisposable = Completable.fromAction(() -> {
                        mLatLng = data.getParcelableExtra(Constants.LATLNG_EXTRA);
                        setStreetName(mLatLng);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> setLocationFound(streetName, false))
                    .subscribe();
                compositeDisposable.add(customLocationDisposable);
            }
            if (!streetName.equals(Constants.STRING_NOT_AVAILABLE)) mFab.show();
        } else {
            Timber.w("Request Code = "+requestCode);
            Timber.w("Result Code = "+resultCode);
        }
    }

    /*
    * CAMERA ACTION
    */
    private void cameraAction(){
        Timber.d("Camera : Loading photo with glide");
        setThumbnailView(Uri.fromFile(photoFile));
        Disposable camDisposable = Observable.just(Constants.STRING_NOT_AVAILABLE)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(x->{
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
        Disposable galDisposable = Observable.just(Constants.STRING_NOT_AVAILABLE)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(x -> {
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
    * <-|-> AGGREGATOR OBSERVABLE TRANSFORMER <-|->
    */
    private ObservableTransformer<String,String> aggregatorObservableTransformer(){
        return observable -> observable
            .compose(writePhotoObservableTransformer())
            .compose(exifObservableTransformer())
            .filter(x -> x.equals(Constants.STRING_NOT_AVAILABLE))
            .flatMap(x -> getCurrentLocation());
    }

    /*
    * WRITING PHOTO FILE TO BE UPLOADED
    */
    private ObservableTransformer<String,String> writePhotoObservableTransformer(){
        return observable -> observable
            .doOnNext(x -> {
                Timber.d("(onNext)->WriteFile|Camera/Gallery "+Thread.currentThread().getName());
                Bitmap compressedBitmap = PhotoUtility
                    .rotateBitmap(PostActivity.this, photoUri, photoTaken);
                Timber.d("WriteFile|Camera/Gallery : init compress stream");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 37, bytes);
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
    private ObservableTransformer<String,String> exifObservableTransformer(){
        return observable -> observable
            .map(x -> {
                mLatLng = PhotoUtility.getExifLocation(photoPath);
                if (mLatLng != null) setStreetName(mLatLng);
                return streetName;
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(street -> {
                if (!street.equals(Constants.STRING_NOT_AVAILABLE)) {
                    setLocationFound(street,true);
                    customLocationButton.setVisibility(View.VISIBLE);
                    mFab.show();
                } else {
                    spinner.setColor(getResources().getColor(R.color.blue_300));
                    spinner.setBounds(0,0,46,46);
                    tvStreetName.setText(R.string.get_gps_location);
                    tvStreetName.setCompoundDrawables(spinner, null, null, null);
                }
                customLocationButton.setVisibility(View.VISIBLE);
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
        return rxLocation.settings()
            .checkAndHandleResolution(locationRequest)
            .flatMapObservable(isActivated -> {
                if (isActivated) {
                    Timber.d("Location settings turned on");
                    return onLocationSettingsActivatedObservable();
                }
                return onLocationNotFoundObservable();
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
            .doOnNext(street -> {
                Timber.d("Found location from GPS");
                setLocationFound(street, false);
                customLocationButton.setVisibility(View.VISIBLE);
                mFab.show();
            })
            .takeUntil(customLocationButtonPressed)
            .doOnComplete(() -> {
                if (streetName.equals(Constants.STRING_NOT_AVAILABLE)) {
                    setLocationNotFound();
                    Timber.d("Location fetching interupted");
                }
            });
    }

    private Observable<String> onLocationNotFoundObservable(){
        return Observable.just(Constants.STRING_NOT_AVAILABLE)
            .doOnNext(s -> setLocationNotFound());
    }

    private void setLocationFound(String street, Boolean isExif){
        if (street.equals(Constants.STRING_NOT_AVAILABLE))
            street = getString(R.string.cant_get_street_name);
        spinner.stop();
        Drawable drw = getResources()
            .getDrawable(R.drawable.ic_location_on_red_400_24dp);
        drw.setBounds(0,0,46,46);
        String streetText = isExif? getString(R.string.location_tag_exif):getString(R.string.location_tag_gps);
        streetText = streetText+" "+street;
        tvStreetName.setText(streetText);
        tvStreetName.setCompoundDrawables(drw,null,null,null);
        mFab.show();
    }

    private void setLocationNotFound(){
        spinner.stop();
        Drawable drw = getResources()
            .getDrawable(R.drawable.ic_location_off_red_400_24dp);
        drw.setBounds(0,0,46,46);
        tvStreetName.setText(R.string.cant_get_location);
        tvStreetName.setCompoundDrawables(drw,null,null,null);
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


    /*
    * FIREBAAAAASE
    */
    private void postReport(){
        AppCompatEditText descEditText = findViewById(R.id.text_input_description);
        String description = descEditText.getText().toString();
        String uid = mAuth.getUid();
        Timber.d("Posting, user ID -> "+uid);

        initProgressBar();
        progress.show();
        mFab.hide();

        HashMap<String, Object> coord = new HashMap<String, Object>();
        coord.put("latitude", mLatLng.latitude);
        coord.put("longitude", mLatLng.longitude);
        sp = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);
        userName = sp.getString(Preferences.PREF_KEY_USER_NAME, Constants.STRING_NOT_AVAILABLE);

        compositeDisposable.add(uploadPhoto(uid)
            .flatMapCompletable(snap -> {
                Report report = new Report(uid, userName, streetName,
                    description, snap.getDownloadUrl().toString(), coord);
                return saveReportDataCompletable(report);
            })
            .subscribe());
    }

    private Maybe<UploadTask.TaskSnapshot> uploadPhoto(String uid){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
            .child(Preferences.FS_REF_ROOT).child(uid+"/"+fileNameGenerator());
        Timber.d("reference : "+storageRef.getPath());
        return RxFirebaseStorage
            .putFile(storageRef, Uri.fromFile(photoFile))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess(snap -> {
                progress.setMessage("Posting Report");
                Timber.d("Download URL "+snap.getDownloadUrl());
            })
            .doOnError(e -> {
                mFab.show();
                progress.dismiss();
                DisplayUtility.customAlertDialog(PostActivity.this)
                    .setTitle("Upload Failed")
                    .setMessage("Unable to upload the photo for some reasons")
                    .setPositiveButton("Dismiss",null)
                    .show();
                Timber.e(e.getMessage());
            });
    }

    private Completable saveReportDataCompletable(Report report){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String key = dbRef.child("Reports").push().getKey();
        String uid = report.getReporterId();
        reportCount = sp.getInt(Preferences.PREF_KEY_REPORT_COUNT, 0);
        reportCount++;
        Map<String, Object> reportUpdates = new HashMap<>();
        reportUpdates.put(Preferences.FD_REF_REPORTS + "/" + key, report);
        reportUpdates.put(Preferences.FD_REF_USERREPORTS + "/" + uid + "/" + key, report);
        reportUpdates.put(Preferences.FD_REF_USERS + "/" + uid + "/"
            + Preferences.FD_REF_REPORTCOUNT, reportCount);
        GeoFire geoFire = new GeoFire(dbRef.child(Preferences.FD_REF_GEOFIRENODE));
        GeoLocation geoLocation = new GeoLocation(mLatLng.latitude,mLatLng.longitude);

        return RxFirebaseDatabase.updateChildren(dbRef, reportUpdates)
            .doOnError(e -> {
                Timber.d(e.getMessage());
                progress.dismiss();
                DisplayUtility.customAlertDialog(PostActivity.this)
                    .setTitle("Failed Posting Report")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Dismiss", null)
                    .show();
                mFab.show();
            })
            .doOnComplete(() -> {
                progress.setMessage("restructuring location...");
                geoFire.setLocation(key,geoLocation,geoFireCompletionListener());
            });
    }

    private String fileNameGenerator(){
        Time t = new Time();
        t.setToNow();
        long timestamp=System.currentTimeMillis();
        return "/"+t.year+t.month+t.monthDay+t.hour+t.minute+t.second+":"
            +timestamp+Constants.POST_FILE_EXT;
    }
    private GeoFire.CompletionListener geoFireCompletionListener(){
        return (gKey, err) -> {
            if (err == null) {
                Timber.d("Updating SharedPreferences reportCount");
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(Preferences.PREF_KEY_REPORT_COUNT, reportCount);
                editor.apply();
                progress.dismiss();
                finish();
            } else {
                progress.dismiss();
                Timber.e(err.getMessage());
            }
        };
    }

    private void initProgressBar(){
        progress = new ProgressDialog(this);
        progress.setMessage("Uploading Photo");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
    }
}