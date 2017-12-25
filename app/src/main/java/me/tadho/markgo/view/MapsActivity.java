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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import me.tadho.markgo.view.fragments.MapsPickerFragment;
import timber.log.Timber;

public class MapsActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;
    private LatLng mLatLng;
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("MapsActivity onCreate()");
        setContentView(R.layout.activity_maps);
        setTitle(getString(R.string.title_maps));
        setSupportActionBar(findViewById(R.id.toolbar));
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        char mapsMode = 0;
        extras = getIntent().getExtras();
        if (extras != null) {
            mapsMode = extras.getChar(Constants.MAPS_MODE);
        }
        loadMapsMode(mapsMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMapsMode(char mapsMode){
        switch (mapsMode){
            case Constants.MAPS_PICKER:
                Timber.d("Maps location picker mode");
                Fragment fragment = new MapsPickerFragment();
                mLatLng = extras.getParcelable(Constants.LATLNG_EXTRA);
                if (mLatLng!=null) setFragmentLatLng(fragment);
                loadFragment(fragment);
                break;
            case Constants.MAPS_VIEWER:
                Timber.d("Maps location viewer mode");
                break;
            default:
                Timber.d("Maps location list mode");
        }
    }

    private void loadFragment(Fragment fragment){
        if (mFragmentManager == null) mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
            .add(R.id.frame_main, fragment)
            .commit();
    }

    private void setFragmentLatLng(Fragment fragment){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.LATLNG_EXTRA, mLatLng);
        fragment.setArguments(bundle);
    }

    public void sendActivityResult(LatLng latLng){
        Intent resultExtra = new Intent();
        resultExtra.putExtra(Constants.LATLNG_EXTRA, latLng);
        setResult(RESULT_OK,resultExtra);
        supportFinishAfterTransition();
    }

}
