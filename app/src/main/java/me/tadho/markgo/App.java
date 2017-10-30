package me.tadho.markgo;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by tdh on 10/26/17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
