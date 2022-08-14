package com.jack.bookshelf.presenter.contract;

import android.widget.EditText;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.bean.SearchHistoryBean;
import com.jack.bookshelf.view.adapter.SearchBookAdapter;

import java.util.List;

public interface SearchBookContract {
    interface Presenter extends IPresenter {

        void insertSearchHistory();

        void querySearchHistory(String content);

        void cleanSearchHistory();

        void cleanSearchHistory(SearchHistoryBean searchHistoryBean);

        int getPage();

        void initPage();

        void toSearchBooks(String key, Boolean fromError);

        void stopSearch();

        void initSearchEngineS(String group);
    }

    interface View extends IView {

        void searchBook(String searchKey);

        /**
         * 成功 新增查询记录
         */
        void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

        /**
         * 成功搜索 搜索记录
         */
        void querySearchHistorySuccess(List<SearchHistoryBean> datas);

        /**
         * 首次查询成功 更新UI
         */
        void refreshSearchBook();

        /**
         * 加载更多书籍成功 更新UI
         */
        void loadMoreSearchBook(List<SearchBookBean> books);

        /**
         * 刷新成功
         */
        void refreshFinish(Boolean isAll);

        /**
         * 加载成功
         */
        void loadMoreFinish(Boolean isAll);

        /**
         * 搜索失败
         */
        void searchBookError(Throwable throwable);

        /**
         * 获取搜索内容EditText
         */
        EditText getEdtContent();

        /**
         * @return SearchBookAdapter
         */
        SearchBookAdapter getSearchBookAdapter();

    }

}
