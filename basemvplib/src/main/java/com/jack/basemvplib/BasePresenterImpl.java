package com.jack.basemvplib;

import androidx.annotation.NonNull;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;

public abstract class BasePresenterImpl<T extends IView> implements IPresenter {
    protected T mView;

    @Override
    public void attachView(@NonNull IView iView) {
        mView = (T) iView;
    }
}
