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
//mAuth.signOut();
//        startActivity(new Intent(this, IntroActivity.class));
//        finish();


package me.tadho.markgo.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragment;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

import me.tadho.markgo.R;
import me.tadho.markgo.data.FbPersistence;
import me.tadho.markgo.data.model.User;
import me.tadho.markgo.view.customIntroSlide.FormIntroSlide;
import timber.log.Timber;

public class IntroActivity extends MaterialIntroActivity {

    SlideFragment formSlide;
    FirebaseAuth mAuth;
    DatabaseReference rootRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FbPersistence.getDatabase().getInstance().getReference();
        usersRef = rootRef.child("Users");


        if (mAuth.getCurrentUser() == null) {
            hideNavButton();
            setupIntroSlides();
        } else startMainActivity();
    }

    private void hideNavButton(){
        hideBackButton();
        findViewById(agency.tango.materialintroscreen.R.id.button_next)
                .setVisibility(View.INVISIBLE);
    }

    public void setupIntroSlides() {
        formSlide = new FormIntroSlide();
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorMainIntro)
                .buttonsColor(R.color.colorMainIntroAccent)
                .image(R.drawable.ic_markgo_logo)
                .title(getString(R.string.intro_welcome_title))
                .description(getString(R.string.intro_welcome_description))
                .build());
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.indigo_500)
                .buttonsColor(R.color.indigo_300)
                .image(R.drawable.ic_markgo_intro_gps)
                .neededPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                        , android.Manifest.permission.ACCESS_COARSE_LOCATION})
                .description(getString(R.string.intro_permission_description))
                .build());
        addSlide( formSlide );
    }

    private void startMainActivity(){
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }

    public void onAuthSuccess(FirebaseUser user, String name){
        String uid = user.getUid();
        writeNewUser(uid, name);
        startMainActivity();
        rootRef.child("UsersList").child(uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Timber.d("Create new user");
                    writeNewUser(uid, name);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void writeNewUser(String uid, String name){
        User user = new User(name);
        usersRef.child(uid).setValue(user);
        rootRef.child("UsersList").child(uid).setValue(true);
    }

    @Override
    public void onBackPressed() {}

}
