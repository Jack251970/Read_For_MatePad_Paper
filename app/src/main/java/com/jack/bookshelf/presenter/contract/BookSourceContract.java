package com.jack.bookshelf.presenter.contract;

import com.google.android.material.snackbar.Snackbar;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.bean.BookSourceBean;

import java.util.List;

public interface BookSourceContract {

    interface Presenter extends IPresenter {

        void saveData(BookSourceBean bookSourceBean);

        void saveData(List<BookSourceBean> bookSourceBeans);

        void delData(BookSourceBean bookSourceBean);

        void delData(List<BookSourceBean> bookSourceBeans);

        void importBookSource(String url);

        void importBookSourceLocal(String path);

        void checkBookSource(List<BookSourceBean> sourceBeans);
    }

    interface View extends IView {

        void refreshBookSource();

        Snackbar getSnackBar(String msg, int length);

        void showSnackBar(String msg, int length);

        void setResult(int resultCode);

        int getSort();
    }

}
