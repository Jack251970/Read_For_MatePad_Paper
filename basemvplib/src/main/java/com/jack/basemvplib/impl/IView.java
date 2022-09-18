package com.jack.basemvplib.impl;

import android.content.Context;

import androidx.annotation.StringRes;

public interface IView {
    Context getContext();

    void toast(String msg);

    void toast(@StringRes int id);
}
