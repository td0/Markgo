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

import me.tadho.markgo.data.enumeration.Preferences;
import timber.log.Timber;

public class IntroPresenter implements IntroContract.Presenter {

    private final IntroContract.View mView;

    IntroPresenter(IntroContract.View mView){
        if (mView != null) {
            this.mView = mView;
            mView.setPresenter(this);
        } else {
            throw new RuntimeException("Cant bind view");
        }
    }

    @Override
    public void start() {
        mView.setupIntroSlides();
    }

    @Override
    public void introFinish() {
        Timber.d("intro Finished, setting up data");
        mView.changePreferences(Preferences.PREF_KEY_FIRST_RUN, false);
        mView.changePreferences(Preferences.PREF_KEY_USER_NAME, mView.getUserName());
    }
}
