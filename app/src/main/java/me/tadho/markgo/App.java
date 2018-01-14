/**

 * Created by tdh on 10/26/17.
 */

package me.tadho.markgo;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // Leak Canary init
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

        // Timber init
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            FirebaseAnalytics.getInstance(this)
                    .setAnalyticsCollectionEnabled(true);
        }
    }
}
