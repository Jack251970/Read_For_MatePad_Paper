package com.jack.basemvplib.impl;

import android.content.Context;

public interface IView {
    Context getContext();

    void toast(String msg);

    void toast(int id);
}
