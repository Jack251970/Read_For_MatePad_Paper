package com.jack.bookshelf.presenter.contract;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;

public interface MainContract {

    interface View extends IView {

        void initImmersionBar();

        void recreate();

        void toast(String msg);

        void toast(int strId);

        int getGroup();
    }

    interface Presenter extends IPresenter {

        void addBookUrl(String bookUrl);
    }
}
