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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import me.tadho.markgo.BuildConfig;
import me.tadho.markgo.utils.BitmapScaler;
import me.tadho.markgo.utils.ThumbnailView;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import timber.log.Timber;


public class PostActivity extends AppCompatActivity
    implements View.OnClickListener{

    private File photoFile;
    private String photoPath;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ThumbnailView thumbnailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setTitle(getString(R.string.title_post));
        thumbnailView = findViewById(R.id.iv_preview);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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

    public void getTakeMode() {
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            switch ((int) bundle.get(Constants.TAKE_MODE_EXTRA)){
                case R.id.fab_sheet_item_camera:
                    Timber.d("Call Camera here");
                    launchCamera();
                    break;
                case R.id.fab_sheet_item_gallery:
                    Timber.d("Call Gallery here");
                    launchGallery();
                    break;
                default:
                    throw new RuntimeException("Can't get intent extras");
            }
        }
    }

    public File getPhotoFileUri(String fileName) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File mediaStorageDir = new File(getExternalCacheDir(), Environment.DIRECTORY_PICTURES);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Timber.d("failed to create "+Environment.DIRECTORY_PICTURES+" directory");
                return null;
            }
            return new File(mediaStorageDir.getPath() + File.separator + fileName+".jpg");
        }
        return null;
    }

    private void launchCamera() {
        Timber.d("Launch Camera here");
        photoFile = getPhotoFileUri(Constants.fileName);
        photoPath = photoFile.getPath();
        Uri fileProvider = FileProvider.getUriForFile(PostActivity.this,
                BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
             startActivityForResult(cameraIntent, Constants.REQUEST_CAMERA_CODE);
        }
    }

    private void launchGallery() {
        Timber.d("Launch Gallery here");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.d("Camera onActivityResult");
                cameraAction();
            } else { // Result was a failure
                Timber.d("Picture wasn't taken!");
                finish();
            }
        } else if (requestCode == Constants.REQUEST_GALLERY_CODE){
            if (resultCode == RESULT_OK) {
                Timber.d("Gallery onActivityResult");
            } else {
                Timber.d("Image wasn't chosen");
                finish();
            }
        }
    }

    private void cameraAction(){
        int rotationDegree = getExifOrientation(photoPath);
        Bitmap takenImage = BitmapFactory.decodeFile(photoPath);

        Disposable runCamDisposable = Observable.just(1)
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(x -> {
                Timber.d("(onSubscribe)->RXCamera "+Thread.currentThread().getName());
                Timber.d("Camera : scaling picture");
                Bitmap previewBitmap = BitmapScaler.scaleToFitHeight(takenImage, 800);
                previewBitmap = rotateBitmap(previewBitmap,rotationDegree);
                Timber.d("Camera : attaching picture to view");
                thumbnailView.setImageBitmap(previewBitmap);
            })
            .observeOn(Schedulers.io())
            .doOnComplete( () -> {
                Timber.d("(onNext)->RXCamera "+Thread.currentThread().getName());
                Bitmap compressedBitmap = rotateBitmap(takenImage, rotationDegree);
                Timber.d("Camera : init compress stream");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                File resizedFile = new File(photoPath);
                try {
                    Timber.d("Camera : writing file...");
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                    Timber.d("Camera : file overwritten");
                    thumbnailView.setOnClickListener(this);
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e(e.getMessage());
                }
            }).subscribe();
        compositeDisposable.add(runCamDisposable);

    }

    private int getExifOrientation(String photoFilePath) {
        Timber.d("Camera : reading exif data");
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            Timber.e(e.getMessage());
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    private Bitmap rotateBitmap(Bitmap bm, int rotationAngle){
        Timber.d("Camera : Rotating bitmap");
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            Intent intent = new Intent(PostActivity.this, ImageViewerActivity.class);
            intent.putExtra(Constants.IMAGE_PATH_EXTRA, photoPath);
            intent.putExtra(Constants.LOCAL_FILE_EXTRA, true);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(PostActivity.this, v, "image");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(intent, options.toBundle());
            }
        }
    }
}
