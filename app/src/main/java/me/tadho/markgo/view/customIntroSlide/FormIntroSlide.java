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

package me.tadho.markgo.view.customIntroSlide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import agency.tango.materialintroscreen.SlideFragment;
import me.tadho.markgo.R;
import me.tadho.markgo.view.IntroActivity;

public class FormIntroSlide extends SlideFragment{

    public EditText editText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_introslide_form, container, false);
        editText = view.findViewById(R.id.intro_name_input);
        return view;
    }

    @Override
    public int backgroundColor() {
        return R.color.teal_700;
    }

    @Override
    public int buttonsColor() {
        return R.color.teal_300;
    }

    @Override
    public boolean canMoveFurther() {
        String userName = editText.getText().toString();
        if((userName.trim().length() > 0)) {
            ((IntroActivity) getActivity()).setUserName(userName);
            return true;
        }
        return false;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.intro_form_error);
    }

}
