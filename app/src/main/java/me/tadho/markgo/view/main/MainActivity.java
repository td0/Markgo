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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import me.tadho.markgo.R;
import me.tadho.markgo.BuildConfig;
import me.tadho.markgo.data.enumeration.Preferences;
import me.tadho.markgo.view.maps.MapsActivity;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter mPresenter;
    private SharedPreferences mSharedPreferences;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate() called on: savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new MainPresenter(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(Preferences.PREF_KEY_FIRST_RUN, true);
        Timber.d("firstRun : "+((firstRun)?"true":"false"));
        mPresenter.start();

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("first_run", firstRun);
            bundle.putString("screen", getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }

        mPresenter.runFirstStart(firstRun);
        mPresenter.runInDebug(BuildConfig.DEBUG);

        mFab = findViewById(R.id.mFab);
        mFab.setOnClickListener( v -> {
            Timber.d("Moving to maps intent");
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
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
    public void subscribeTopic(String key) {
        Timber.d("subscribeTopic() called on: key = [" + key + "]");
        FirebaseMessaging.getInstance().subscribeToTopic(key);
    }

    @Override
    public void unsubscribeTopic(String key) {
        Timber.d("subscribeTopic() called on: key = [" + key + "]");
        FirebaseMessaging.getInstance().subscribeToTopic(key);
    }
}
