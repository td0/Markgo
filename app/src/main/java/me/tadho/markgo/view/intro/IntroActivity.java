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

package me.tadho.markgo.view.intro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;


import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragment;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

import me.tadho.markgo.R;
import me.tadho.markgo.view.intro.customSlide.FormIntroSlide;

public class IntroActivity extends MaterialIntroActivity implements IntroContract.View {

    private IntroContract.Presenter mPresenter;
    private SharedPreferences mSharedPreferences;
    private SlideFragment formSlide;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new IntroPresenter(this);
        mPresenter.start();

    }

    @Override
    public void setPresenter(@NonNull IntroContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
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
                .backgroundColor(R.color.indigo_700)
                .buttonsColor(R.color.indigo_300)
                .image(R.drawable.ic_markgo_intro_gps)
                .neededPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
                .description(getString(R.string.intro_permission_description))
                .build());
        addSlide(formSlide);
    }

    @Override
    public void changePreferences(String key, boolean value) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    public void changePreferences(String key, String value) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void onFinish() {
        mPresenter.introFinish();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName){
        this.userName=userName;
    }

    @Override
    public void onBackPressed() {}
}
