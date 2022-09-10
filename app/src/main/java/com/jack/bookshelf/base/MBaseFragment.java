package com.jack.bookshelf.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jack.basemvplib.BaseFragment;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.utils.ToastsKt;

/**
 * MBase Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public abstract class MBaseFragment<T extends IPresenter> extends BaseFragment<T> implements IView {
    public final SharedPreferences preferences = MApplication.getConfigPreferences();
    protected T mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mPresenter = initInjector();
        attachView();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachView();
    }

    /**
     * P层绑定   若无则返回null;
     */
    protected abstract T initInjector();

    /**
     * P层绑定V层
     */
    private void attachView() {
        if (null != mPresenter) {
            mPresenter.attachView(this);
        }
    }

    /**
     * P层解绑V层
     */
    private void detachView() {
        if (null != mPresenter) {
            mPresenter.detachView();
        }
    }

    public void toast(String msg) {
        ToastsKt.toast(this.getActivity(),msg,Toast.LENGTH_SHORT);
    }

    public void toast(int strId) {
        ToastsKt.toast(this.getActivity(),strId,Toast.LENGTH_SHORT);
    }
}
