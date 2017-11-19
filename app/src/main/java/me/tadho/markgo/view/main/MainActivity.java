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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Preferences;
import me.tadho.markgo.view.maps.MapsActivity;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter mPresenter;
    private SharedPreferences mSharedPreferences;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set View-Presenter Bind
        mPresenter = new MainPresenter(this);
        // Check firstRun
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(Preferences.PREF_KEY_FIRST_RUN, true);
        Timber.d("firstRun -> "+((firstRun)?"true":"false"));
        // Call Presenter runFirstStart
        mPresenter.runFirstStart(firstRun);
        // Set FAB
        mFab = findViewById(R.id.mFab);
        mFab.setOnClickListener( v -> {
            Timber.d("FAB Pressed");
            Snackbar.make(v, "FAB Pressed", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        });
    }

    @Override
    public void setPresenter(@NonNull MainContract.Presenter presenter) {
        Timber.d("setPresenter() called on: presenter = [" + presenter + "]");
        mPresenter = presenter;
    }

    @Override
    public void changePreferences(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
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
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Do you want to exit?");
        // alert.setMessage("Message");

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            this.finishAffinity();
        });

        alert.setNegativeButton("Cancel", null);

        alert.show();
    }

    private void clearPreferences(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();

        Snackbar.make(mFab, "Preferences Cleared", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
}
