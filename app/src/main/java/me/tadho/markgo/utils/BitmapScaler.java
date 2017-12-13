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

package me.tadho.markgo.utils;

import android.graphics.Bitmap;

public class BitmapScaler
{
    // scale and keep aspect ratio
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    // scale and keep aspect ratio
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }


    // scale and keep aspect ratio
    public static Bitmap scaleToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                (int) (b.getHeight() * factorToUse), true);
    }


    // scale and don't keep aspect ratio
    public static Bitmap strechToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getHeight();
        float factorW = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW),
                (int) (b.getHeight() * factorH), true);
    }
}