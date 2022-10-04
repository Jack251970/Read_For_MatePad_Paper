package com.jack.bookshelf.web.controller;

import android.text.TextUtils;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.web.utils.ReturnData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SourceController {

    public ReturnData saveSource(String postData) {
        BookSourceBean bookSourceBean = GsonUtils.parseJObject(postData, BookSourceBean.class);
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.book_source_and_url_cannot_be_empty));
        }
        BookSourceManager.addBookSource(bookSourceBean);
        return returnData.setData("");
    }

    public ReturnData saveSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        List<BookSourceBean> okSources = new ArrayList<>();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }
            BookSourceManager.addBookSource(bookSourceBean);
            okSources.add(bookSourceBean);
        }
        return (new ReturnData()).setData(okSources);
    }

    public ReturnData getSource(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.url_cannot_be_empty_please_specify_book_source_url));
        }
        BookSourceBean bookSourceBean = BookSourceManager.getBookSourceByUrl(strings.get(0));
        if (bookSourceBean == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.cannot_find_book_source_please_check_url));
        }
        return returnData.setData(bookSourceBean);
    }

    public ReturnData getSources() {
        List<BookSourceBean> bookSourceBeans = BookSourceManager.getAllBookSource();
        ReturnData returnData = new ReturnData();
        if (bookSourceBeans.size() == 0) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.book_source_empty));
        }
        return returnData.setData(BookSourceManager.getAllBookSource());
    }

    public ReturnData deleteSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            BookSourceManager.removeBookSource(bookSourceBean);
        }
        return (new ReturnData()).setData(StringUtils.getString(R.string.have_execute));
    }
}
