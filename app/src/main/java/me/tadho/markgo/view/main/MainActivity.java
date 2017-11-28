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

package me.tadho.markgo.view.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.tadho.markgo.view.intro.IntroActivity;
import timber.log.Timber;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;

import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import me.tadho.markgo.data.enumeration.Preferences;
import me.tadho.markgo.utils.customModules.Fab;
import me.tadho.markgo.view.maps.MapsActivity;
import me.tadho.markgo.view.post.PostActivity;


public class MainActivity extends AppCompatActivity implements
        MainContract.View,
        View.OnClickListener {

    private MainContract.Presenter mPresenter;
    private SharedPreferences mSharedPreferences;
    private Fab mFab;
    private MaterialSheetFab<Fab> msFab;
    private int statusBarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set View-Presenter Bind
        mPresenter = new MainPresenter(this);
        // Check firstRun
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(Preferences.PREF_KEY_FIRST_RUN, true);
        Timber.d("firstRun -> "+((firstRun)?"true":"false"));
        // Call Presenter runFirstStart
        mPresenter.runFirstStart(firstRun);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        setupFab();
    }

    @Override
    public void setPresenter(@NonNull MainContract.Presenter presenter) {
        Timber.d("setPresenter() called on: presenter = [" + presenter + "]");
        mPresenter = presenter;
    }

    @Override
    public void runIntro() {
        Timber.d("should run intro");
        startActivity(new Intent(this, IntroActivity.class));
    }

    private void setupFab() {
        mFab = findViewById(R.id.mFab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.background_card);
        int fabColor = getResources().getColor(R.color.colorSecondary);

        msFab = new MaterialSheetFab<>(mFab, sheetView, overlay, sheetColor, fabColor);
        msFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = getWindow().getStatusBarColor();
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }else statusBarColor = 0;
            }

            @Override
            public void onHideSheet() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(statusBarColor);
                }
            }
        });
        findViewById(R.id.fab_sheet_item_camera).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_gallery).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_sheet_item_camera :
                Timber.d("Camera FABSheet pressed");
                msFab.hideSheet();
                startActivity(
                        new Intent(this, PostActivity.class)
                                .putExtra(Constants.TAKE_MODE, v.getId())
                );
                break;
            case R.id.fab_sheet_item_gallery :
                Timber.d("Gallery FABSheet pressed");
                msFab.hideSheet();
                startActivity(
                        new Intent(this, PostActivity.class)
                                .putExtra(Constants.TAKE_MODE, v.getId())
                );
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_action_button:
                Timber.d("Map action button pressed");
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            case R.id.reset_submenu:
                Timber.d("Reset Preferences submenu pressed");
                clearPreferences();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(msFab.isSheetVisible()){
            msFab.hideSheet();
        }else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.dialog_exit)
            .setPositiveButton(R.string.dialog_ok, (dialog, whichButton) -> {
                this.finishAffinity();
            }).setNegativeButton(R.string.dialog_cancel, null).show();
        }
    }

    private void clearPreferences(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Clear Preferences and exit app?")
        .setPositiveButton(R.string.dialog_ok, (dialog, whichButton) -> {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.apply();
            this.finishAffinity();
        }).setNegativeButton(R.string.dialog_cancel, null).show();
//
//        Snackbar.make(mFab, "Preferences Cleared", Snackbar.LENGTH_SHORT)
//                .addCallback(new Snackbar.Callback(){
//                    @Override
//                    public void onDismissed(Snackbar transientBottomBar, int event) {
//                        super.onDismissed(transientBottomBar, event);
//                        mFab.setEnabled(true);
//                    }
//                    @Override
//                    public void onShown(Snackbar sb) {
//                        super.onShown(sb);
//                        mFab.setEnabled(false);
//                    }
//                }).show();
    }
}
