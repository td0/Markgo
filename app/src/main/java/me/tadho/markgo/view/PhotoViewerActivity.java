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
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import me.tadho.markgo.utils.GlideApp;

public class PhotoViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra(Constants.PHOTO_PATH_EXTRA);
        boolean isLocalFile = intent.getBooleanExtra(Constants.LOCAL_FILE_EXTRA, false);
        PhotoView photoView = findViewById(R.id.iv_preview);

        if (isLocalFile){
            File file = new File(imagePath);
            Uri fileUri = Uri.fromFile(file);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            GlideApp.with(PhotoViewerActivity.this)
                    .load(fileUri)
                    .apply(requestOptions)
                    .into(photoView);
        } else {
            GlideApp.with(PhotoViewerActivity.this)
                    .load(imagePath)
                    .into(photoView);
        }

        photoView.setOnPhotoTapListener((view, x, y) -> supportFinishAfterTransition());
        photoView.setOnOutsidePhotoTapListener(imageView -> supportFinishAfterTransition());
    }
}