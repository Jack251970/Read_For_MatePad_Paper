//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.jack.bookshelf.presenter;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.DownloadBookBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.WebBookModel;
import com.jack.bookshelf.model.content.WebBook;
import com.jack.bookshelf.presenter.contract.BookListContract;
import com.jack.bookshelf.service.DownloadService;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class BookListPresenter extends BasePresenterImpl<BookListContract.View> implements BookListContract.Presenter {
    private int threadsNum = 6;
    private int refreshIndex;
    private List<BookShelfBean> bookShelfBeans;
    private int group;
    private boolean hasUpdate = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void queryBookShelf(final Boolean needRefresh, final int group) {
        this.group = group;
        if (needRefresh) {
            hasUpdate = false;
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList;
            if (group == 0) {
                bookShelfList = BookshelfHelp.getAllBook();
            } else {
                bookShelfList = BookshelfHelp.getBooksByGroup(group - 1);
            }
            e.onNext(bookShelfList == null ? new ArrayList<>() : bookShelfList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(@NonNull List<BookShelfBean> value) {
                        bookShelfBeans = value;
                        mView.refreshBookShelf(bookShelfBeans);
                        if (needRefresh && NetworkUtils.isNetWorkAvailable()) {
                            startRefreshBook();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }
                });
    }

    private void downloadAll(int downloadNum, boolean onlyNew) {
        if (bookShelfBeans == null || mView.getContext() == null) {
            return;
        }
        AsyncTask.execute(() -> {
            for (BookShelfBean bookShelfBean : new ArrayList<>(bookShelfBeans)) {
                if (!bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG) && (!onlyNew || bookShelfBean.getHasUpdate())) {
                    List<BookChapterBean> chapterBeanList = BookshelfHelp.getChapterList(bookShelfBean.getNoteUrl());
                    if (chapterBeanList.size() >= bookShelfBean.getDurChapter()) {
                        for (int start = bookShelfBean.getDurChapter(); start < chapterBeanList.size(); start++) {
                            if (!chapterBeanList.get(start).getHasCache(bookShelfBean.getBookInfoBean())) {
                                DownloadBookBean downloadBook = new DownloadBookBean();
                                downloadBook.setName(bookShelfBean.getBookInfoBean().getName());
                                downloadBook.setNoteUrl(bookShelfBean.getNoteUrl());
                                downloadBook.setCoverUrl(bookShelfBean.getBookInfoBean().getCoverUrl());
                                downloadBook.setStart(start);
                                downloadBook.setEnd(downloadNum > 0 ? Math.min(chapterBeanList.size() - 1, start + downloadNum - 1) : chapterBeanList.size() - 1);
                                downloadBook.setFinalDate(System.currentTimeMillis());
                                DownloadService.addDownload(mView.getContext(), downloadBook);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void startRefreshBook() {
        if (mView.getContext() != null) {
            threadsNum = mView.getPreferences().getInt(mView.getContext().getString(R.string.pk_threads_num), 16);
            if (bookShelfBeans != null && bookShelfBeans.size() > 0) {
                refreshIndex = -1;
                for (int i = 1; i <= threadsNum; i++) {
                    refreshBookshelf();
                }
            }
        }
    }

    private synchronized void refreshBookshelf() {
        refreshIndex++;
        if (refreshIndex < bookShelfBeans.size()) {
            BookShelfBean bookShelfBean = bookShelfBeans.get(refreshIndex);
            if (!bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG) && bookShelfBean.getAllowUpdate() && bookShelfBean.getGroup() != 3) {
                int chapterNum = bookShelfBean.getChapterListSize();
                bookShelfBean.setLoading(true);
                mView.refreshBook(bookShelfBean.getNoteUrl());
                WebBookModel.getInstance().getChapterList(bookShelfBean)
                        .flatMap(chapterBeanList -> saveBookToShelfO(bookShelfBean, chapterBeanList))
                        .compose(RxUtils::toSimpleSingle)
                        .subscribe(new Observer<BookShelfBean>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(@NonNull BookShelfBean value) {
                                if (value.getErrorMsg() != null) {
                                    mView.toast(value.getErrorMsg());
                                    value.setErrorMsg(null);
                                }
                                bookShelfBean.setLoading(false);
                                if (chapterNum < bookShelfBean.getChapterListSize())
                                    hasUpdate = true;
                                mView.refreshBook(bookShelfBean.getNoteUrl());
                                refreshBookshelf();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                if (!(e instanceof WebBook.NoSourceThrowable)) {
                                    bookShelfBean.setLoading(false);
                                    mView.refreshBook(bookShelfBean.getNoteUrl());
                                    refreshBookshelf();
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {
                refreshBookshelf();
            }
        } else if (refreshIndex >= bookShelfBeans.size() + threadsNum - 1) {
            if (hasUpdate && mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_auto_download), false)) {
                downloadAll(10, true);
                hasUpdate = false;
            }
            queryBookShelf(false, group);
        }
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean, List<BookChapterBean> chapterBeanList) {
        return Observable.create(e -> {
            if (!chapterBeanList.isEmpty()) {
                BookshelfHelp.delChapterList(bookShelfBean.getNoteUrl());
                BookshelfHelp.saveBookToShelf(bookShelfBean);
                DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(chapterBeanList);
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK), @Tag(RxBusTag.HAD_REMOVE_BOOK), @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)})
    public void hadAddOrRemoveBook(BookShelfBean bookShelfBean) {
        queryBookShelf(false, group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_GROUP)})
    public void updateGroup(Integer group) {
        this.group = group;
        mView.updateGroup(group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.REFRESH_BOOK_LIST)})
    public void reFlashBookList(Boolean needRefresh) {
        queryBookShelf(needRefresh, group);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.DOWNLOAD_ALL)})
    public void downloadAll(Integer downloadNum) {
        downloadAll(downloadNum, false);
    }
}
