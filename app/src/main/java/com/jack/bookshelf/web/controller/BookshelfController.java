package com.jack.bookshelf.web.controller;

import android.text.TextUtils;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookContentBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.WebBookModel;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.web.utils.ReturnData;

import java.util.List;
import java.util.Map;

public class BookshelfController {

    public ReturnData getBookshelf() {
        List<BookShelfBean> shelfBeans = BookshelfHelp.getAllBook();
        ReturnData returnData = new ReturnData();
        if (shelfBeans.isEmpty()) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.have_not_add_novel));
        }
        return returnData.setData(shelfBeans);
    }

    public ReturnData getChapterList(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.url_cannot_be_empty_please_specify_book_url));
        }
        List<BookChapterBean> chapterList = BookshelfHelp.getChapterList(strings.get(0));
        return returnData.setData(chapterList);
    }

    public ReturnData getBookContent(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.url_cannot_be_empty_please_specify_content_url));
        }
        BookChapterBean chapter = DbHelper.getDaoSession().getBookChapterBeanDao().load(strings.get(0));
        if (chapter == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.cannot_find));
        }
        BookShelfBean bookShelfBean = BookshelfHelp.getBook(chapter.getNoteUrl());
        if (bookShelfBean == null) {
            return returnData.setErrorMsg(StringUtils.getString(R.string.cannot_find));
        }
        String content = BookshelfHelp.getChapterCache(bookShelfBean, chapter);
        if (!TextUtils.isEmpty(content)) {
            return returnData.setData(content);
        }
        try {
            BookContentBean bookContentBean = WebBookModel.getInstance().getBookContent(bookShelfBean, chapter, null).blockingFirst();
            return returnData.setData(bookContentBean.getDurChapterContent());
        } catch (Exception e) {
            return returnData.setErrorMsg(e.getMessage());
        }
    }

    public ReturnData saveBook(String postData) {
        BookShelfBean bookShelfBean = GsonUtils.parseJObject(postData, BookShelfBean.class);
        ReturnData returnData = new ReturnData();
        if (bookShelfBean != null) {
            DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            return returnData.setData("");
        }
        return returnData.setErrorMsg(StringUtils.getString(R.string.format_error));
    }
}
