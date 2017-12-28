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

package me.tadho.markgo.view.customView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import com.jakewharton.rxbinding2.widget.RxTextView;
import agency.tango.materialintroscreen.SlideFragment;

import me.tadho.markgo.data.enumeration.Consts;
import me.tadho.markgo.utils.DisplayUtility;
import me.tadho.markgo.R;
import me.tadho.markgo.view.IntroActivity;
import timber.log.Timber;

public class FormIntroSlide extends SlideFragment
        implements View.OnClickListener {

    private TextInputLayout nameInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout codeInputLayout;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText codeEditText;
    private Button submitButton;
    private Button signinButton;
    private Button resendButton;

    private Subject<Integer> verifyStateSubject = PublishSubject.create();
    private CompositeDisposable compositeDisposable;
    private String cantMoveFurtherMessage;
    private boolean submitButtonError;
    private boolean signinButtonError;
    private int verifyState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_introslide_form, container, false);
        nameInputLayout = view.findViewById(R.id.intro_input_name_layout);
        phoneInputLayout = view.findViewById(R.id.intro_input_phone_layout);
        codeInputLayout = view.findViewById(R.id.intro_input_code_layout);
        nameEditText = view.findViewById(R.id.intro_name_input);
        phoneEditText = view.findViewById(R.id.intro_phone_input);
        codeEditText = view.findViewById(R.id.intro_code_input);
        submitButton = view.findViewById(R.id.intro_submit_button);
        signinButton = view.findViewById(R.id.intro_signin_button);
        resendButton = view.findViewById(R.id.intro_resend_button);
        submitButton.setOnClickListener(this);
        signinButton.setOnClickListener(this);
        resendButton.setOnClickListener(this);
        view.findViewById(R.id.intro_form_layout)
            .setOnFocusChangeListener((v,hasFocus) -> {
                if (hasFocus) DisplayUtility.hideKeyboard(getContext(),v);
            });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiation();
        Timber.d("onActivityCreated verifyState -> "+verifyState);
    }

    public void setCantMoveFurtherMessage(String message){
        cantMoveFurtherMessage = message;
    }

    @Override
    public int backgroundColor() {
        return R.color.teal_500;
    }

    @Override
    public int buttonsColor() {
        return R.color.teal_300;
    }

    @Override
    public boolean canMoveFurther() {
        return false;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return cantMoveFurtherMessage;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        killCompositeDisposable();
    }

    @Override
    public void onClick(View v) {
        DisplayUtility.hideKeyboard(getContext(), v);
        if (v.getId() == R.id.intro_submit_button){
            Timber.d("Get Verification button pressed");
            if (!submitButtonError){
                cantMoveFurtherMessage = getString(R.string.intro_form_snackbar_error2);
                setVerifyState(Consts.REG_STATE_GET_AUTH);
                showMessage(getString(R.string.fbauth_enter_get_verification));
                ((IntroActivity)getActivity())
                    .getVerificationCode(phoneEditText.getText().toString());
            }
            else showMessage(cantMoveFurtherMessage);
        }
        else if (v.getId() == R.id.intro_signin_button) {
            Timber.d("Sign In button pressed");
            if (!signinButtonError){
                showMessage("Signing in...");
                ((IntroActivity)getActivity())
                    .signInWithCode(codeEditText.getText().toString());
            }
            else showMessage(cantMoveFurtherMessage);
        }
        else if (v.getId() == R.id.intro_resend_button){
            Timber.d("Resend button pressed");
            setVerifyState(Consts.REG_STATE_GET_CODE);
        }
    }

    public void initiation(){
        killCompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        submitButtonError = true;
        signinButtonError = true;
        cantMoveFurtherMessage = getString(R.string.intro_form_snackbar_error1);
        setFormValidation();
        setVerifyState(Consts.REG_STATE_GET_CODE);
    }

    private void setFormValidation() {
        Observable<CharSequence> nameEditObservable = RxTextView.textChanges(nameEditText);
        Observable<CharSequence> phoneEditObservable = RxTextView.textChanges(phoneEditText);
        Observable<CharSequence> codeEditObservable = RxTextView.textChanges(codeEditText);

        Disposable nameDisposable = nameEditObservable
            .doOnNext(charSequence -> hideNameError())
            .debounce(Consts.DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(charSequence -> !TextUtils.isEmpty(charSequence))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(charSequence -> {
                if (!validateName(charSequence.toString())) {
                    cantMoveFurtherMessage = getString(R.string.intro_form_snackbar_error1);
                    showNameError();
                } else hideNameError();
            });
        Disposable phoneDisposable = phoneEditObservable
            .doOnNext(charSequence -> hidePhoneError())
            .debounce(Consts.DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(charSequence -> !TextUtils.isEmpty(charSequence))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(charSequence -> {
                if (!validatePhone(charSequence.toString())) {
                    cantMoveFurtherMessage = getString(R.string.intro_form_snackbar_error1);
                    showPhoneError();
                } else hidePhoneError();
            });
        Disposable codeDisposable = codeEditObservable
            .doOnNext(charSequence -> hideCodeError())
            .debounce(Consts.DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(charSequence -> !TextUtils.isEmpty(charSequence))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(charSequence -> {
                if (!validateCode(charSequence.toString())) {
                    showCodeError();
                    signinButtonError = true;
                } else {
                    hideCodeError();
                    signinButtonError = false;
                }
            });
        Disposable buttonDisposable = Observable
            .combineLatest(nameEditObservable, phoneEditObservable,
            (nameObs, phoneObs) -> validateName(nameObs.toString())
                    && validatePhone(phoneObs.toString())
                    && verifyState == Consts.REG_STATE_GET_CODE)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(valid -> {
                if (valid && verifyState == Consts.REG_STATE_GET_CODE) {
                    Timber.d("buttonDisposable Verify State : 1");
                    submitButtonError = false;
                } else submitButtonError = true;
            });
        Observable<Long> resendTimerObservable = Observable
            .intervalRange(0,31,0,1, TimeUnit.SECONDS)
            .map(interval -> 30-interval)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(reversed -> {
                String s = "Resend ("+reversed+")";
                resendButton.setText(s);
            })
            .doOnComplete(()->{
                resendButton.setText(R.string.intro_button_Resend);
                resendButton.setEnabled(true);
            });
        Disposable verifyStateDisposable = verifyStateSubject
            .doOnNext(state -> {
                if (state == Consts.REG_STATE_GET_CODE) {
                    Timber.d("Verify State 1 : getting verification code");
                    codeEditText.setText("");
                    nameEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    phoneEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                    codeInputLayout.setVisibility(View.INVISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                    signinButton.setVisibility(View.INVISIBLE);
                    resendButton.setVisibility(View.INVISIBLE);
                    resendButton.setEnabled(false);
                } else if (state == Consts.REG_STATE_GET_AUTH) {
                    Timber.d("Verify State 2 : authenticating");
                    nameEditText.setInputType(InputType.TYPE_NULL);
                    phoneEditText.setInputType(InputType.TYPE_NULL);
                    codeInputLayout.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.INVISIBLE);
                    signinButton.setVisibility(View.VISIBLE);
                    resendButton.setVisibility(View.VISIBLE);
                }
            })
            .filter(state -> state== Consts.REG_STATE_GET_AUTH)
            .flatMap(state -> resendTimerObservable)
            .subscribe();
        compositeDisposable.addAll(nameDisposable, phoneDisposable, codeDisposable,
            buttonDisposable, verifyStateDisposable);
    }

    private void killCompositeDisposable(){
        if (compositeDisposable!=null) {
            if (!compositeDisposable.isDisposed()) compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    private void showMessage(String s){
        ((IntroActivity)getActivity()).showMessage(s);
    }

    //////////

    private void enableError(TextInputLayout textInputLayout) {
        if (textInputLayout.getChildCount() == 2)
            textInputLayout.getChildAt(1).setVisibility(View.VISIBLE);
    }
    private void disableError(TextInputLayout textInputLayout) {
        if (textInputLayout.getChildCount() == 2)
            textInputLayout.getChildAt(1).setVisibility(View.GONE);
    }

    private boolean validateName(String name) {
        return name.matches("^\\p{L}+[\\p{L}\\p{Z}\\p{P}]*");
    }
    private boolean validatePhone(String phoneNumber){
        if (TextUtils.isEmpty(phoneNumber)) return false;
        Matcher matcher = Patterns.PHONE.matcher(phoneNumber);
        return matcher.matches() && phoneNumber.length()>=9;
    }
    private boolean validateCode(String code){
        return code.matches("^[0-9]{5,7}$");
    }

    private void showNameError(){
        enableError(nameInputLayout);
        nameInputLayout.setError(getString(R.string.intro_invalid_name));
    }
    private void hideNameError(){
        disableError(nameInputLayout);
        nameInputLayout.setError(null);
    }
    private void showPhoneError(){
        enableError(phoneInputLayout);
        phoneInputLayout.setError(getString(R.string.intro_invalid_phone));
    }
    private void hidePhoneError(){
        disableError(phoneInputLayout);
        phoneInputLayout.setError(null);
    }
    private void showCodeError(){
        enableError(codeInputLayout);
        codeInputLayout.setError(getString(R.string.intro_invalid_code));
    }
    private void hideCodeError(){
        disableError(codeInputLayout);
        codeInputLayout.setError(null);
    }
    private void setVerifyState(int state){
        verifyState = state;
        verifyStateSubject.onNext(state);
    }
}
