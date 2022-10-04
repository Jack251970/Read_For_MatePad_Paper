package com.jack.bookshelf.model.task;

import com.jack.bookshelf.base.BaseModelImpl;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.WebChapterBean;
import com.jack.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.jack.bookshelf.model.content.BookChapterList;

import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnalyzeNextUrlTask {
    private final WebChapterBean webChapterBean;
    private Callback callback;
    private final BookShelfBean bookShelfBean;
    private final Map<String, String> headerMap;
    private final BookChapterList bookChapterList;

    public AnalyzeNextUrlTask(BookChapterList bookChapterList, WebChapterBean webChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        this.bookChapterList = bookChapterList;
        this.webChapterBean = webChapterBean;
        this.bookShelfBean = bookShelfBean;
        this.headerMap = headerMap;
    }

    public AnalyzeNextUrlTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void analyzeUrl(AnalyzeUrl analyzeUrl) {
        BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                .flatMap(stringResponse ->
                        bookChapterList.analyzeChapterList(stringResponse.body(), bookShelfBean, headerMap))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        callback.addDisposable(d);
                    }

                    @Override
                    public void onNext(List<BookChapterBean> bookChapterBeans) {
                        callback.analyzeFinish(webChapterBean, bookChapterBeans);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    public interface Callback {
        void addDisposable(Disposable disposable);

        void analyzeFinish(WebChapterBean bean, List<BookChapterBean> bookChapterBeans);

        void onError(Throwable throwable);
    }
}
