package com.jack.bookshelf.presenter.contract;

import android.content.Intent;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.SearchBookBean;

import java.util.List;

public interface BookDetailContract {
    interface Presenter extends IPresenter {
        void initData(Intent intent);

        int getOpenFrom();

        SearchBookBean getSearchBook();

        BookShelfBean getBookShelf();

        List<BookChapterBean> getChapterList();

        Boolean getInBookShelf();

        void initBookFormSearch(SearchBookBean searchBookBean);

        void getBookShelfInfo();

        void addToBookShelf();

        void removeFromBookShelf();

        void changeBookSource(SearchBookBean searchBookBean);
    }

    interface View extends IView {
        /**
         * 更新书籍详情UI
         */
        void updateView();

        /**
         * 数据获取失败
         */
        void getBookShelfError();

        void readBook();

        void finish();

        void toast(String msg);
    }
}
