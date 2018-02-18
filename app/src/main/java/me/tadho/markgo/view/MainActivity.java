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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.CompositeDisposable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.tbruyelle.rxpermissions2.RxPermissions;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;

import me.tadho.markgo.data.FbPersistence;
import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Prefs;
import me.tadho.markgo.utils.DisplayUtility;
import me.tadho.markgo.view.adapter.ViewPagerAdapter;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private FirebaseAuth mAuth;
    private CompositeDisposable compositeDisposable;

    private FloatingActionMenu mFam;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private DatabaseReference userStatusRef;
    private int userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();
        userStatus = 0;
        userStatusRef = FbPersistence.getDatabase().getReference()
            .child(Prefs.FD_REF_USERS).child(uid).child("status");
        setupTab();
        setupFab();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStatusRef.removeEventListener(statusListener());
        if (compositeDisposable!=null){
            if(!compositeDisposable.isDisposed()){
                Timber.d("RxTest onDestroy, disposing");
                compositeDisposable.dispose();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFab() {
        mFam = findViewById(R.id.mFam);
        userStatusRef.addValueEventListener(statusListener());

        compositeDisposable = new CompositeDisposable();
        Timber.d("Setting up Floating Action Menu");
        FloatingActionButton fabGallery = findViewById(R.id.fabGallery);
        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        mFam.setClosedOnTouchOutside(true);

        Disposable fabGalleryDisposable = RxView.clicks(fabGallery)
                .compose(new RxPermissions(MainActivity.this)
                        .ensure(Manifest.permission.READ_EXTERNAL_STORAGE))
                .subscribe(granted->{
                    Timber.d("Floating Action Button Gallery clicked");
                    mFam.toggle(true);
                    if (granted){
                        Timber.d("Read external permission granted");
                        Intent intent = new Intent(this, PostActivity.class)
                                .putExtra(Consts.TAKE_MODE_EXTRA, Consts.TAKE_MODE_EXTRA_GALLERY);
                        startActivity(intent);
                    } else {
                        Timber.d("Permission rejected");
                        showPermissionReasoning();
                    }
                });
        Disposable fabCameraDisposable = RxView.clicks(fabCamera)
                .subscribe(v->{
                    Timber.d("Floating Action Button Camera clicked");
                    mFam.toggle(true);
                    Intent intent = new Intent(MainActivity.this, PostActivity.class)
                            .putExtra(Consts.TAKE_MODE_EXTRA, Consts.TAKE_MODE_EXTRA_CAMERA);
                    startActivity(intent);
                });
        compositeDisposable.addAll(fabGalleryDisposable,fabCameraDisposable);
    }

    private void setupTab(){
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPagerAdapter = new ViewPagerAdapter(
            getSupportFragmentManager(),MainActivity.this);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);
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
            case R.id.logout_submenu:
                Timber.d("Logout Prefs submenu pressed");
                signOut();
                return true;
            // TODO: Change name
//            case R.id.changename_submenu:
//                Timber.d("Change name submenu pressed");
//                DisplayUtility.customAlertDialog(MainActivity.this)
//                    .setMessage("report count has been reset to 0")
//                    .show();
//                return true;
            case R.id.about_submenu:
                Timber.d("About submenu pressed");
                new LibsBuilder()
                    .withFields(Libs.toStringArray(R.string.class.getFields()))
                    .withAboutIconShown(true)
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    .withAboutAppName(getString(R.string.app_name))
                    .withActivityTitle(getString(R.string.activity_about_title))
                    .withAboutDescription(getString(R.string.activity_about_description))
                    .withLicenseShown(true)
                    .start(MainActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFam.isOpened()) {
            mFam.close(true);
        } else {
            AlertDialog.Builder alert = DisplayUtility.customAlertDialog(this);
            alert.setTitle(R.string.dialog_exit)
                    .setPositiveButton(R.string.dialog_ok, (dialog, whichButton) -> this.finishAffinity())
                    .setNegativeButton(R.string.dialog_cancel, null).show();
        }
    }

    private void showPermissionReasoning(){
        AlertDialog.Builder alert = DisplayUtility.customAlertDialog(this);
        alert.setTitle(R.string.dialog_permission_denied)
                .setMessage(R.string.dialog_storage_permission_reasoning)
                .setPositiveButton(R.string.dialog_ok,null)
                .show();
    }

    private void signOut() {
        AlertDialog.Builder alert = DisplayUtility.customAlertDialog(this);
        alert.setTitle(R.string.dialog_confirm_signout)
            .setPositiveButton(R.string.dialog_ok, (dialog, whichButton) -> {
                mAuth.signOut();
                startActivity(new Intent(this, IntroActivity.class));
                finish();
            }).setNegativeButton(R.string.dialog_cancel, null).show();
    }

    private ValueEventListener statusListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setUserStatus(dataSnapshot.getValue(Integer.class));
                if (getUserStatus() > 0) {
                    mFam.setMenuButtonColorNormalResId(R.color.colorSecondary);
                    mFam.setMenuButtonColorPressedResId(R.color.colorSecondaryDark);
                    mFam.setMenuButtonColorRippleResId(R.color.colorSecondaryDark);
                    mFam.setOnMenuButtonClickListener(view -> mFam.toggle(true));
                } else {
                    mFam.setMenuButtonColorNormalResId(R.color.grey_500);
                    mFam.setMenuButtonColorPressedResId(R.color.grey_500);
                    mFam.setMenuButtonColorRippleResId(R.color.grey_500);
                    mFam.setOnMenuButtonClickListener(view -> Snackbar
                        .make(view, R.string.snackbar_blocked, Snackbar.LENGTH_SHORT)
                        .show());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void setUserStatus(int status) {
        userStatus = status;
    }
    public int getUserStatus() {
        return userStatus;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
