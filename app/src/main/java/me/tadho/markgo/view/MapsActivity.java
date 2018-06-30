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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.view.fragments.MapsListFragment;
import me.tadho.markgo.view.fragments.MapsPickerFragment;
import timber.log.Timber;

public class MapsActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;
    private Menu menu;
    private LatLng mLatLng;
    private Bundle extras;
    private Fragment fragment;
    private char mapsMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("MapsActivity onCreate()");
        setContentView(R.layout.activity_maps);
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(getString(R.string.title_maps));
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mapsMode = Consts.MAPS_LIST;
        extras = getIntent().getExtras();
        if (extras != null) {
            mLatLng = extras.getParcelable(Consts.LATLNG_EXTRA);
            mapsMode = extras.getChar(Consts.MAPS_MODE);
        }
        Timber.d("mapsMode = "+ mapsMode);
        loadMapsMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_maps_menu, menu);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
            R.array.maps_filter_spinner, R.layout.layout_spinner_custom_textview);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MenuItem menuItem = menu.findItem(R.id.maps_menu_filter);
        Spinner spinner = (Spinner) menuItem.getActionView();
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position+1) {
                    case Consts.MAPS_FILTER_ALL :
                        if (fragment != null) ((MapsListFragment)fragment).setFilterMode(Consts.MAPS_FILTER_ALL);
                        break;
                    case Consts.MAPS_FILTER_BROKEN :
                        if (fragment != null) ((MapsListFragment)fragment).setFilterMode(Consts.MAPS_FILTER_BROKEN);
                        break;
                    case Consts.MAPS_FILTER_FIXED :
                        if (fragment != null) ((MapsListFragment)fragment).setFilterMode(Consts.MAPS_FILTER_FIXED);
                        break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (mapsMode != Consts.MAPS_LIST)
            menu.setGroupVisible(R.id.maps_menu_filter_group, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void loadMapsMode(){
        switch (mapsMode){
            case Consts.MAPS_PICKER:
                setTitle(getString(R.string.title_maps_picker));
                Timber.d("Maps location picker mode");
                fragment = new MapsPickerFragment();
                if (mLatLng!=null) setFragmentLatLng(fragment);
                loadFragment(fragment);
                break;
            case Consts.MAPS_VIEWER:
                Timber.d("Maps location viewer mode");
                break;
            default:
                setTitle(getString(R.string.title_maps));
                Timber.d("Maps location list mode");
                fragment = new MapsListFragment();
                if (mLatLng!=null) setFragmentLatLng(fragment);
                loadFragment(fragment);
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
        bundle.putParcelable(Consts.LATLNG_EXTRA, mLatLng);
        fragment.setArguments(bundle);
    }

    public void sendActivityResult(LatLng latLng){
        Intent resultExtra = new Intent();
        resultExtra.putExtra(Consts.LATLNG_EXTRA, latLng);
        setResult(RESULT_OK,resultExtra);
        supportFinishAfterTransition();
    }

}
