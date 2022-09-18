package com.jack.bookshelf.presenter;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.bookshelf.bean.LocBookShelfBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.model.ImportBookModel;
import com.jack.bookshelf.presenter.contract.ImportBookContract;
import com.jack.bookshelf.utils.RxUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Import Book Presenter
 * Copyright (c) 2017. 章钦豪. All rights reserved.
 * Edited by Jack251970
 */

public class ImportBookPresenter extends BasePresenterImpl<ImportBookContract.View> implements ImportBookContract.Presenter {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void importBooks(List<File> books) {
        Observable.fromIterable(books)
                .flatMap(file -> ImportBookModel.getInstance().importBook(file))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(LocBookShelfBean value) {
                        if (value.getNew()) {
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value.getBookShelfBean());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.addError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mView.addSuccess();
                    }
                });
    }

    @Override
    public void detachView() {
        compositeDisposable.dispose();
    }
}
