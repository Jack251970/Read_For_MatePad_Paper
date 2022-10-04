package com.jack.bookshelf.model.content;

import static android.text.TextUtils.isEmpty;

import android.text.TextUtils;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.model.analyzeRule.AnalyzeByRegex;
import com.jack.bookshelf.model.analyzeRule.AnalyzeRule;
import com.jack.bookshelf.utils.StringUtils;

import io.reactivex.Observable;

class BookInfo {
    private final String tag;
    private final String sourceName;
    private final BookSourceBean bookSourceBean;

    BookInfo(String tag, String sourceName, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            String baseUrl = bookShelfBean.getNoteUrl();
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error) + baseUrl));
                return;
            } else {
                Debug.printLog(tag, StringUtils.getString(R.string.get_info_page_success));
                Debug.printLog(tag, "└" + baseUrl);
            }
            bookShelfBean.setTag(tag);

            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            bookInfoBean.setNoteUrl(baseUrl);   //id
            bookInfoBean.setTag(tag);
            bookInfoBean.setOrigin(sourceName);
            bookInfoBean.setBookSourceType(bookSourceBean.getBookSourceType()); // 是否为有声读物

            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean, bookSourceBean);
            analyzer.setContent(s, baseUrl);

            // 获取详情页预处理规则
            String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
            boolean isRegex = false;
            if (!isEmpty(ruleInfoInit)) {
                // 仅使用java正则表达式提取书籍详情
                if (ruleInfoInit.startsWith(":")) {
                    isRegex = true;
                    ruleInfoInit = ruleInfoInit.substring(1);
                    Debug.printLog(tag, StringUtils.getString(R.string.preprocess_info));
                    AnalyzeByRegex.getInfoOfRegex(s, ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                } else {
                    Object object = analyzer.getElement(ruleInfoInit);
                    if (object != null) {
                        analyzer.setContent(object);
                    }
                }
            }
            if (!isRegex) {
                Debug.printLog(tag, StringUtils.getString(R.string.preprocess_info));
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) analyzer.setContent(object);
                Debug.printLog(tag, StringUtils.getString(R.string.preprocess_info_success));

                Debug.printLog(tag, StringUtils.getString(R.string.get_book_name));
                String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
                if (!isEmpty(bookName)) bookInfoBean.setName(bookName);
                Debug.printLog(tag, "└" + bookName);

                Debug.printLog(tag, StringUtils.getString(R.string.get_book_author));
                String bookAuthor = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor()));
                if (!isEmpty(bookAuthor)) bookInfoBean.setAuthor(bookAuthor);
                Debug.printLog(tag, "└" + bookAuthor);

                Debug.printLog(tag, StringUtils.getString(R.string.get_book_kind));
                String bookKind = analyzer.getString(bookSourceBean.getRuleBookKind());
                Debug.printLog(tag, 111, "└" + bookKind);

                Debug.printLog(tag, StringUtils.getString(R.string.get_latest_chapter));
                String bookLastChapter = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
                if (!isEmpty(bookLastChapter)) bookShelfBean.setLastChapterName(bookLastChapter);
                Debug.printLog(tag, "└" + bookLastChapter);

                Debug.printLog(tag, StringUtils.getString(R.string.get_book_introduction));
                String bookIntroduce = analyzer.getString(bookSourceBean.getRuleIntroduce());
                if (!isEmpty(bookIntroduce)) bookInfoBean.setIntroduce(bookIntroduce);
                Debug.printLog(tag, 1, "└" + bookIntroduce, true, true);

                Debug.printLog(tag, StringUtils.getString(R.string.get_book_cover));
                String bookCoverUrl = analyzer.getString(bookSourceBean.getRuleCoverUrl(), true);
                if (!isEmpty(bookCoverUrl)) bookInfoBean.setCoverUrl(bookCoverUrl);
                Debug.printLog(tag, "└" + bookCoverUrl);

                Debug.printLog(tag, StringUtils.getString(R.string.get_catalog_url));
                String bookCatalogUrl = analyzer.getString(bookSourceBean.getRuleChapterUrl(), true);
                if (isEmpty(bookCatalogUrl)) bookCatalogUrl = baseUrl;
                bookInfoBean.setChapterUrl(bookCatalogUrl);

                //如果目录页和详情页相同,暂存页面内容供获取目录用
                if (bookCatalogUrl.equals(baseUrl)) bookInfoBean.setChapterListHtml(s);
                Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
                bookShelfBean.setBookInfoBean(bookInfoBean);
                Debug.printLog(tag, StringUtils.getString(R.string.parse_info_page_success));
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }
}
