package com.jack.bookshelf.widget.page;

import android.annotation.SuppressLint;
import android.view.View;

import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookContentBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.databinding.ActivityBookReadBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.WebBookModel;
import com.jack.bookshelf.model.content.WebBook;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Net Page Loader
 * Edited by Jack251970
 */

public class PageLoaderNet extends PageLoader {
    private final List<String> downloadingChapterList = new ArrayList<>();
    private final ExecutorService executorService;
    private final Scheduler scheduler;

    PageLoaderNet(PageView pageView, BookShelfBean bookShelfBean, Callback callback) {
        super(pageView, bookShelfBean, callback);
        executorService = Executors.newFixedThreadPool(20);
        scheduler = Schedulers.from(executorService);
    }

    @Override
    public void refreshChapterList() {
        if (!callback.getChapterList().isEmpty()) {
            isChapterListPrepare = true;
            // 打开章节
            skipToChapter(book.getDurChapter(), book.getDurChapterPage());
        } else {
            WebBookModel.getInstance().getChapterList(book)
                    .compose(RxUtils::toSimpleSingle)
                    .subscribe(new MyObserver<>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(List<BookChapterBean> chapterBeanList) {
                            isChapterListPrepare = true;
                            // 目录加载完成
                            if (!chapterBeanList.isEmpty()) {
                                BookshelfHelp.delChapterList(book.getNoteUrl());
                                callback.onCategoryFinish(chapterBeanList);
                            }
                            // 加载并显示当前章节
                            skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof WebBook.NoSourceThrowable) {
                                mPageView.autoChangeSource();
                            } else {
                                loadChapterError(e.getMessage());
                            }
                        }
                    });
        }
    }


    public void changeSourceFinish(BookShelfBean bookShelfBean) {
        if (bookShelfBean == null) {
            openChapter(book.getDurChapter());
        } else {
            this.book = bookShelfBean;
            refreshChapterList();
        }
    }

    @SuppressLint("DefaultLocale")
    private synchronized void loadContent(final int chapterIndex) {
        if (downloadingChapterList.size() >= 20) return;
        if (chapterIndex >= callback.getChapterList().size()
                || DownloadingList(listHandle.CHECK, callback.getChapterList().get(chapterIndex).getDurChapterUrl()))
            return;
        if (null != book && callback.getChapterList().size() > 0) {
            Observable.create((ObservableOnSubscribe<Integer>) e -> {
                if (shouldRequestChapter(chapterIndex)) {
                    DownloadingList(listHandle.ADD, callback.getChapterList().get(chapterIndex).getDurChapterUrl());
                    e.onNext(chapterIndex);
                }
                e.onComplete();
            })
                    .flatMap(index -> WebBookModel.getInstance().getBookContent(book, callback.getChapterList().get(chapterIndex), null))
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onNext(BookContentBean bookContentBean) {
                            DownloadingList(listHandle.REMOVE, bookContentBean.getDurChapterUrl());
                            finishContent(bookContentBean.getDurChapterIndex());
                        }

                        @Override
                        public void onError(Throwable e) {
                            DownloadingList(listHandle.REMOVE, callback.getChapterList().get(chapterIndex).getDurChapterUrl());
                            if (chapterIndex == book.getDurChapter()) {
                                if (e instanceof WebBook.NoSourceThrowable) {
                                    mPageView.autoChangeSource();
                                } else {
                                    loadChapterError(e.getMessage());
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 编辑下载列表
     */
    private synchronized boolean DownloadingList(listHandle editType, String value) {
        if (editType == listHandle.ADD) {
            downloadingChapterList.add(value);
            return true;
        } else if (editType == listHandle.REMOVE) {
            downloadingChapterList.remove(value);
            return true;
        } else {
            return downloadingChapterList.contains(value);
        }
    }

    /**
     * 章节下载完成
     */
    private void finishContent(int chapterIndex) {
        if (chapterIndex == mCurChapterPos) {
            super.parseCurChapter();
        }
        if (chapterIndex == mCurChapterPos - 1) {
            super.parsePrevChapter();
        }
        if (chapterIndex == mCurChapterPos + 1) {
            super.parseNextChapter();
        }
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    public void refreshDurChapter() {
        if (callback.getChapterList().isEmpty()) {
            updateChapter();
            return;
        }
        if (callback.getChapterList().size() - 1 < mCurChapterPos) {
            mCurChapterPos = callback.getChapterList().size() - 1;
        }
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(book.getBookInfoBean().getName(), book.getTag()),
                mCurChapterPos, callback.getChapterList().get(mCurChapterPos).getDurChapterName());
        skipToChapter(mCurChapterPos, 0);
    }

    @Override
    protected String getChapterContent(BookChapterBean chapter) {
        return BookshelfHelp.getChapterCache(book, chapter);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected boolean noChapterData(BookChapterBean chapter) {
        return !BookshelfHelp.isChapterCached(book.getBookInfoBean().getName(), book.getTag(), chapter, book.isAudio());
    }

    private boolean shouldRequestChapter(Integer chapterIndex) {
        return NetworkUtils.isNetWorkAvailable() && noChapterData(callback.getChapterList().get(chapterIndex));
    }

    // 装载上一章节的内容
    @Override
    void parsePrevChapter() {
        if (mCurChapterPos >= 1) {
            loadContent(mCurChapterPos - 1);
        }
        super.parsePrevChapter();
    }

    // 装载当前章内容。
    @Override
    void parseCurChapter() {
        for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterListSize()); i++) {
            loadContent(i);
        }
        super.parseCurChapter();
    }

    // 装载下一章节的内容
    @Override
    void parseNextChapter() {
        for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterListSize()); i++) {
            loadContent(i);
        }
        super.parseNextChapter();
    }

    @Override
    public void updateChapter(ActivityBookReadBinding binding, PageLoader mPageLoader) {
        mPageView.getActivity().toast(StringUtils.getString(R.string.catalog_updating));
        WebBookModel.getInstance().getChapterList(book)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<BookChapterBean> chapterBeanList) {
                        isChapterListPrepare = true;
                        if (chapterBeanList.size() > callback.getChapterList().size()) {
                            mPageView.getActivity().toast(StringUtils.getString(R.string.update_finish_with_new_chapter));
                            callback.onCategoryFinish(chapterBeanList);
                        } else {
                            mPageView.getActivity().toast(StringUtils.getString(R.string.update_finish_without_new_chapter));
                        }
                        // 加载并显示当前章节
                        skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                        // 刷新显示内容
                        if (binding.tvChapterName.getVisibility() == View.VISIBLE) {
                            binding.tvChapterName.setText(chapterBeanList.get(mPageLoader.getCurChapterPos()).getDurChapterName());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadChapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    @Override
    public void updateChapter() {
        mPageView.getActivity().toast(StringUtils.getString(R.string.catalog_updating));
        WebBookModel.getInstance().getChapterList(book)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<BookChapterBean> chapterBeanList) {
                        isChapterListPrepare = true;
                        if (chapterBeanList.size() > callback.getChapterList().size()) {
                            mPageView.getActivity().toast(StringUtils.getString(R.string.update_finish_with_new_chapter));
                            callback.onCategoryFinish(chapterBeanList);
                        } else {
                            mPageView.getActivity().toast(StringUtils.getString(R.string.update_finish_without_new_chapter));
                        }
                        // 加载并显示当前章节
                        skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadChapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void closeBook() {
        super.closeBook();
        executorService.shutdown();
    }

    public enum listHandle {
        ADD, REMOVE, CHECK
    }
}
