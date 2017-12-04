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

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import timber.log.Timber;

import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;

public class PostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setTitle(getString(R.string.title_post));

        getTakeMode();
    }

    public void getTakeMode() {
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            switch ((int) bundle.get(Constants.TAKE_MODE)){
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

    public void launchCamera() { Timber.d("Launch Camera here"); }

    public void launchGallery() {
        Timber.d("Launch Gallery here");
    }

}
