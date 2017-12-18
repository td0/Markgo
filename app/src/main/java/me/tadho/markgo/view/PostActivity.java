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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import me.tadho.markgo.BuildConfig;
import me.tadho.markgo.utils.GlideApp;
import me.tadho.markgo.utils.PhotoUtility;
import me.tadho.markgo.view.customView.ThumbnailView;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import timber.log.Timber;


public class PostActivity extends AppCompatActivity
    implements View.OnClickListener{

    private File photoFile;
    private String photoPath;
    private Bitmap photoTaken;
    private int rotationDegree;
    private FloatingActionButton mFab;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ThumbnailView thumbnailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTakeMode();
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
                    PhotoViewerActivity.class);
            intent.putExtra(Constants.PHOTO_PATH_EXTRA, photoPath);
            intent.putExtra(Constants.LOCAL_FILE_EXTRA, true);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(PostActivity.this, v,
                            getString(R.string.animation_photo_view));
            startActivity(intent, options.toBundle());
        }
    }

    public void getTakeMode() {
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            photoFile = getPhotoFile();
            photoPath = photoFile.getPath();
            photoTaken = null;
            Timber.d("photoPath -> "+ photoPath);
            switch ((int) bundle.get(Constants.TAKE_MODE_EXTRA)){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setupView();
        if (requestCode == Constants.REQUEST_CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.d("Camera onActivityResult");
                cameraAction();
            } else {
                // Result was a failure
                Timber.d("Picture was not taken!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_GALLERY_CODE){
            if (resultCode == RESULT_OK) {
                Timber.d("Gallery onActivityResult");
                if (data!=null) galleryAction(data.getData());
            } else {
                Timber.d("Picture was not chosen");
                finish();
            }
        }
    }

    private void cameraAction(){
        rotationDegree = getExifOrientation(photoPath);
        photoTaken = BitmapFactory.decodeFile(photoPath);
        Disposable camDisposable = Observable.just(1)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(x->{
                Timber.d("(onNext)->RXCamera "+Thread.currentThread().getName());
                Timber.d("Camera : scaling picture");
                Bitmap previewBitmap = PhotoUtility.scaleToFitHeight(photoTaken, 800);
                previewBitmap = rotateBitmap(previewBitmap,rotationDegree);
                Timber.d("Camera : Setting up photo thumbnail into view");
                thumbnailView.setImageBitmap(previewBitmap);
            })
            .compose(writePhotoFileObservable())
            .subscribe();
        compositeDisposable.add(camDisposable);
    }

    private void galleryAction(Uri photoUri){
        Disposable galDisposable = Observable.just(2)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(x->{
                Timber.d("Gallery : Getting photo bitmap");
                try {
                    photoTaken = MediaStore.Images.Media
                            .getBitmap(PostActivity.this.getContentResolver(), photoUri);
                } catch (IOException e) {
                    Timber.e(e.getMessage());
                    e.printStackTrace();
                }
                Timber.d("Gallery : get full path from Uri");
                String photoFullPath = PhotoUtility
                        .getFullPathFromUri(PostActivity.this, photoUri);
                rotationDegree = getExifOrientation(photoFullPath);
                Timber.d("(onNext)->RXGallery "+Thread.currentThread().getName());
                Timber.d("Gallery : scaling picture");
                Bitmap previewBitmap = PhotoUtility.scaleToFitHeight(photoTaken, 800);
                previewBitmap = rotateBitmap(previewBitmap,rotationDegree);
                Timber.d("Gallery : Setting up photo thumbnail into view");
                thumbnailView.setImageBitmap(previewBitmap);
            })
            .compose(writePhotoFileObservable())
            .subscribe();
        compositeDisposable.add(galDisposable);
    }

    private ObservableTransformer<Integer,Integer> writePhotoFileObservable(){
        return observable -> observable.observeOn(Schedulers.io())
                .doOnComplete(()->{
                    Timber.d("(onNext)->WriteFile|Camera/Gallery "+Thread.currentThread().getName());
                    Bitmap compressedBitmap = rotateBitmap(photoTaken, rotationDegree);
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

//    private void setThumbnailView(Uri uri){
//        RequestOptions requestOptions = new RequestOptions()
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .skipMemoryCache(true);
//        GlideApp.with(PostActivity.this)
//            .load(uri)
//            .placeholder(R.drawable.placeholder)
//            .apply(requestOptions)
//            .into(thumbnailView);
//    }

    private int getExifOrientation(String photoFilePath) {
        Timber.d("Camera/Gallery : reading exif data");
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            Timber.e(e.getMessage());
            e.printStackTrace();
        }
        String orientString = exif != null ? exif.getAttribute(ExifInterface.TAG_ORIENTATION) : null;
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    private Bitmap rotateBitmap(Bitmap bm, int rotationAngle){
        Timber.d("Camera/Gallery : Rotating bitmap");
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    private void setupView(){
        if (thumbnailView==null) {
            setContentView(R.layout.activity_post);
            setSupportActionBar(findViewById(R.id.toolbar));
            setTitle(getString(R.string.title_post));
            thumbnailView = findViewById(R.id.iv_preview);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            mFab = findViewById(R.id.fab_submit_post);
            mFab.hide();
        }
    }
}
