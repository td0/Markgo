package me.tadho.markgo.view;

import android.support.annotation.NonNull;

/**
 * Created by tdh on 10/26/17.
 */

public interface BaseView<T> {
    void setPresenter(@NonNull T presenter);
}
