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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.tadho.markgo.R;
import me.tadho.markgo.data.enumeration.Constants;
import me.tadho.markgo.data.enumeration.Preferences;
import me.tadho.markgo.data.model.User;
import me.tadho.markgo.view.customView.FormIntroSlide;
import timber.log.Timber;

public class IntroActivity extends MaterialIntroActivity {

    private FirebaseAuth mAuth;
    private String verificationId;
    private DatabaseReference rootRef;
    private FormIntroSlide formSlide;
    private Task<AuthResult> signInTask;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            onFinish();
        } else {
            rootRef = FirebaseDatabase.getInstance().getReference();
            compositeDisposable = new CompositeDisposable();
            hideNavButton();
            setupIntroSlides();
        }
    }

    private void hideNavButton(){
        hideBackButton();
        findViewById(agency.tango.materialintroscreen.R.id.button_next)
            .setVisibility(View.INVISIBLE);
    }

    public void setupIntroSlides() {
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
            .neededPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION})
            .description(getString(R.string.intro_permission_description))
            .build());
        formSlide = new FormIntroSlide();
        addSlide( formSlide );
    }

    @Override
    public void onFinish() {
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {}

    public void getVerificationCode(String phone){
        PhoneAuthProvider
            .getInstance()
            .verifyPhoneNumber(phone, Constants.VERIFY_PHONE_TIMEOUT,
                TimeUnit.SECONDS,
                this,
                authCallback());
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks authCallback(){
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Timber.d("Verification Complete");
                EditText codeEdit = findViewById(R.id.intro_code_input);
                codeEdit.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Timber.e(e.getMessage());
                formSlide.initiation();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    showMessage(e.getMessage());
                } else if (e instanceof FirebaseAuthException){
                    showMessage(getString(R.string.fbauth_error_not_authorized));
                } else if (e instanceof FirebaseTooManyRequestsException){
                    showMessage(getString(R.string.fbauth_error_quota_reached));
                } else if (e instanceof FirebaseApiNotAvailableException){
                    showMessage(getString(R.string.fbauth_error_service_unavailable));
                } else {
                    showMessage(getString(R.string.fbauth_error_other));
                }
            }

            @Override
            public void onCodeSent(String verification, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verification, forceResendingToken);
                Timber.d("onCodeSent Running!");
                formSlide.setCantMoveFurtherMessage(getString(R.string.intro_form_snackbar_error3));
                verificationId = verification;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Timber.e(getString(R.string.fbauth_error_request_timeout));
            }
        };
    }

    public void signInWithCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                Timber.d("Phone Sign In success");
                FirebaseUser user = authResult.getUser();
                String name = ((EditText)findViewById(R.id.intro_name_input))
                    .getText().toString().trim();
                onAuthSuccess(user, name);
            })
            .addOnFailureListener(e -> {
                Timber.e(e.getMessage());
                showMessage(e.getMessage());
            });
    }

    public void onAuthSuccess(FirebaseUser userAuth, String name){
        Timber.d("Running onAuthSuccess");
        String uid = userAuth.getUid();
        User user = new User(name);
        DatabaseReference userListRef = rootRef.child("UsersList").child(uid);

        Completable userRegisterCompletable = RxFirebaseDatabase
            .observeSingleValueEvent(userListRef)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable(snap -> {
               if (!snap.exists()) {
                   Timber.d("Create new user..");
                   return setFBUserCompletable(uid, user);
               }
               return setFBNameCompletable(uid, name);
            });
        compositeDisposable.add(userRegisterCompletable.subscribe());
    }

    private Completable setFBUserCompletable(String uid, User user) {
        DatabaseReference ref = rootRef.child(Preferences.FD_REF_USERS).child(uid);
        return RxFirebaseDatabase.setValue(ref, user)
            .andThen(setFBUserListCompletable(uid, user.getName()));
    }

    private Completable setFBUserListCompletable(String uid, String name){
        DatabaseReference ref = rootRef.child(Preferences.FD_REF_USERSLIST).child(uid);
        return RxFirebaseDatabase.setValue(ref, true)
            .doOnComplete(() -> {
                savePreferences(name, 0);
                onFinish();
            });
    }

    private Completable setFBNameCompletable(String uid, String name){
        DatabaseReference userRef = rootRef.child(Preferences.FD_REF_USERS).child(uid);
        DatabaseReference nameRef = userRef.child(Preferences.FD_REF_NAME);
        DatabaseReference countRef = userRef.child(Preferences.FD_REF_REPORTCOUNT);
        return  RxFirebaseDatabase.observeSingleValueEvent(countRef)
            .flatMapCompletable(snap ->
                RxFirebaseDatabase.setValue(nameRef,name)
                    .doOnComplete(() -> {
                       savePreferences(name, snap.getValue(Integer.class));
                       onFinish();
                    })
            );
    }

    private void savePreferences(String name, int reportCount){
        Timber.d("Saving SharedPreferences");
        Timber.d("name = "+name+"; reportCount = "+reportCount);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Preferences.PREF_KEY_USER_NAME, name);
        editor.putInt(Preferences.PREF_KEY_REPORT_COUNT, reportCount);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy Called");
        if (compositeDisposable!=null) {
            if (!compositeDisposable.isDisposed()){
                compositeDisposable.dispose();
            }
        }
        if (signInTask != null) {
            signInTask = null;
        }
    }
}
