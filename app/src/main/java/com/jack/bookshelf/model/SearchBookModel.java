package com.jack.bookshelf.model;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Search Book Model
 * Created by GKF on 2018/1/16.
 */

public class SearchBookModel {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService;
    private final Scheduler scheduler;
    private long startThisSearchTime;
    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final int threadsNum;
    private final int search_result_filter_grade;
    private int page = 0;
    private int searchEngineIndex;
    private int searchSuccessNum;
    private CompositeDisposable compositeDisposable;
    private final OnSearchListener searchListener;

    public SearchBookModel(OnSearchListener searchListener) {
        this(searchListener, BookSourceManager.getSelectedBookSource());
    }

    public SearchBookModel(OnSearchListener searchListener, List<BookSourceBean> sourceBeanList) {
        this.searchListener = searchListener;
        threadsNum = MApplication.getConfigPreferences().getInt(MApplication.getInstance().getString(R.string.pk_threads_num), 16);
        executorService = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
        search_result_filter_grade = MApplication.getConfigPreferences().getInt(MApplication.getInstance().getString(R.string.pk_search_result_filter_grade), 0);
        if (sourceBeanList == null) {
            initSearchEngineS(BookSourceManager.getSelectedBookSource());
        } else {
            initSearchEngineS(sourceBeanList);
        }
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngineS(@NonNull List<BookSourceBean> sourceBeanList) {
        searchEngineS.clear();
        for (BookSourceBean bookSourceBean : sourceBeanList) {
            if (bookSourceBean.getEnable()) {
                SearchEngine se = new SearchEngine();
                se.setTag(bookSourceBean.getBookSourceUrl());
                se.setHasMore(true);
                searchEngineS.add(se);
            }
        }
    }

    public void searchReNew() {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        page = 0;
        for (SearchEngine searchEngine : searchEngineS) {
            searchEngine.setHasMore(true);
        }
    }

    public void stopSearch() {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        handler.post(() -> {
            searchListener.refreshFinish(true);
            searchListener.loadMoreFinish(true);
        });
    }

    private void searchBookError(Throwable throwable) {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        handler.post(() -> {
            searchListener.refreshFinish(true);
            searchListener.loadMoreFinish(true);
            searchListener.searchBookError(throwable);
        });
    }

    public void onDestroy() {
        stopSearch();
        executorService.shutdown();
    }

    public void setSearchTime(long searchTime) {
        this.startThisSearchTime = searchTime;
    }

    public void search(final String content, final long searchTime, List<BookShelfBean> bookShelfS, Boolean fromError) {
        if (searchTime != startThisSearchTime) {
            return;
        }
        if (!fromError) {
            page++;
        }
        if (page == 0) {
            page = 1;
        }
        if (page == 1) {
            handler.post(searchListener::refreshSearchBook);
        }
        if (searchEngineS.size() == 0) {
            searchBookError(new Throwable(StringUtils.getString(R.string.have_not_choose_any_book_source)));
            return;
        }
        searchSuccessNum = 0;
        searchEngineIndex = -1;
        for (int i = 0; i < threadsNum; i++) {
            searchOnEngine(content, bookShelfS, searchTime);
        }
    }

    private synchronized void searchOnEngine(final String content, List<BookShelfBean> bookShelfS, final long searchTime) {
        if (searchTime != startThisSearchTime) {
            return;
        }
        searchEngineIndex++;
        long startTime = System.currentTimeMillis();
        if (searchEngineIndex < searchEngineS.size()) {
            final SearchEngine searchEngine = searchEngineS.get(searchEngineIndex);
            if (searchEngine.getHasMore()) {
                WebBookModel.getInstance()
                        .searchBook(content, page, searchEngine.getTag())
                        .subscribeOn(scheduler)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(List<SearchBookBean> searchBookBeans) {
                                if (searchTime == startThisSearchTime) {
                                    searchSuccessNum++;
                                    if (searchBookBeans.size() > 0) {
                                        for (SearchBookBean temp : searchBookBeans) {
                                            int searchTime = (int) (System.currentTimeMillis() - startTime) / 1000;
                                            temp.setSearchTime(searchTime);
                                            for (BookShelfBean bookShelfBean : bookShelfS) {
                                                if (Objects.equals(bookShelfBean.getNoteUrl(), temp.getNoteUrl())) {
                                                    temp.setIsCurrentSource(true);
                                                    break;
                                                }
                                            }
                                        }

                                        if (search_result_filter_grade > 0) {
                                            for (int index = 0; index < searchBookBeans.size(); index++) {
                                                SearchBookBean temp = searchBookBeans.get(index);
                                                String r = temp.getName() + temp.getAuthor();
                                                char[] s = content.replaceAll("\\s", "").toCharArray();
                                                int j = 9 - search_result_filter_grade;

                                                for (char c : s) {
                                                    if (r.indexOf(c) < 0) {
                                                        j--;
                                                    }
                                                    if (j < 0) {
                                                        searchBookBeans.remove(index);
                                                        index--;
                                                        break;
                                                    }
                                                }
                                            }

                                        }
                                        searchListener.loadMoreSearchBook(searchBookBeans);
                                    } else {
                                        searchEngine.setHasMore(false);
                                    }
                                    searchOnEngine(content, bookShelfS, searchTime);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                searchEngine.setHasMore(false);
                                searchOnEngine(content, bookShelfS, searchTime);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {
                searchOnEngine(content, bookShelfS, searchTime);
            }
        } else {
            if (searchEngineIndex >= searchEngineS.size() + threadsNum - 1) {
                if (searchSuccessNum == 0 && searchListener.getItemCount() == 0) {
                    if (page == 1) {
                        searchBookError(new Throwable(StringUtils.getString(R.string.have_not_find_any_content)));
                    } else {
                        searchBookError(new Throwable(StringUtils.getString(R.string.have_not_find_more_content)));
                    }
                } else {
                    if (page == 1) {
                        handler.post(() -> searchListener.refreshFinish(false));
                    }
                    for (SearchEngine engine : searchEngineS) {
                        if (engine.hasMore) {
                            handler.post(() -> searchListener.loadMoreFinish(false));
                            return;
                        }
                    }
                    handler.post(() -> searchListener.loadMoreFinish(true));
                }
            }
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public interface OnSearchListener {
        void refreshSearchBook();

        void refreshFinish(Boolean isAll);

        void loadMoreFinish(Boolean isAll);

        void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList);

        void searchBookError(Throwable throwable);

        int getItemCount();
    }

    private static class SearchEngine {

        private String tag;
        private Boolean hasMore;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        Boolean getHasMore() {
            return hasMore;
        }

        void setHasMore(Boolean hasMore) {
            this.hasMore = hasMore;
        }
    }
}
