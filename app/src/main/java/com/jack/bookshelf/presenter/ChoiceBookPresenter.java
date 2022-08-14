//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.jack.bookshelf.presenter;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.model.WebBookModel;
import com.jack.bookshelf.presenter.contract.ChoiceBookContract;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChoiceBookPresenter extends BasePresenterImpl<ChoiceBookContract.View> implements ChoiceBookContract.Presenter {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String tag;
    private final String url;
    private final String title;

    private int page = 1;
    private long startThisSearchTime;

    public ChoiceBookPresenter(final Intent intent) {
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        tag = intent.getStringExtra("tag");
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> temp = DbHelper.getDaoSession().getBookShelfBeanDao().queryBuilder().list();
            if (temp == null)
                temp = new ArrayList<>();
            e.onNext(temp);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BookShelfBean>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<BookShelfBean> value) {
                        initPage();
                        toSearchBooks(null);
                        mView.startRefreshAnim();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void initPage() {
        this.page = 1;
        this.startThisSearchTime = System.currentTimeMillis();
    }

    @Override
    public void toSearchBooks(String key) {
        final long tempTime = startThisSearchTime;
        searchBook(tempTime);
    }

    private void searchBook(final long searchTime) {
        WebBookModel.getInstance().findBook(url, page, tag)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<SearchBookBean> value) {
                        if (searchTime == startThisSearchTime) {
                            if (page == 1) {
                                mView.refreshSearchBook(value);
                                mView.refreshFinish(value.size() <= 0);
                            } else {
                                mView.loadMoreSearchBook(value);
                            }
                            page++;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        mView.searchBookError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public String getTitle() {
        return title;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

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

}