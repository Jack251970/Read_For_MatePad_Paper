package com.jack.bookshelf.presenter;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.help.DocumentHelper;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.presenter.contract.BookSourceContract;
import com.jack.bookshelf.service.CheckSourceService;
import com.jack.bookshelf.utils.StringUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Book Source Presenter
 * Created by GKF on 2017/12/18.
 * Edited by Jack251970
 */

public class BookSourcePresenter extends BasePresenterImpl<BookSourceContract.View> implements BookSourceContract.Presenter {
    private BookSourceBean delBookSource;
    private Snackbar progressSnackBar;

    @Override
    public void saveData(BookSourceBean bookSourceBean) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean));
    }

    @Override
    public void saveData(List<BookSourceBean> bookSourceBeans) {
        AsyncTask.execute(() -> {
            if (mView.getSort() == 0) {
                for (int i = 1; i <= bookSourceBeans.size(); i++) {
                    bookSourceBeans.get(i - 1).setSerialNumber(i);
                }
            }
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(bookSourceBeans);
        });
    }

    @Override
    public void delData(BookSourceBean bookSourceBean) {
        this.delBookSource = bookSourceBean;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getDaoSession().getBookSourceBeanDao().delete(bookSourceBean);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.getSnackBar(delBookSource.getBookSourceName() + StringUtils.getString(R.string.have_deleted), Snackbar.LENGTH_LONG)
                                .setAction(R.string.restore, view -> restoreBookSource(delBookSource))
                                .setActionTextColor(Color.WHITE)
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(R.string.delete_fail);
                        mView.refreshBookSource();
                    }
                });
    }

    @Override
    public void delData(List<BookSourceBean> bookSourceBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            for (BookSourceBean sourceBean : bookSourceBeans) {
                DbHelper.getDaoSession().getBookSourceBeanDao().delete(sourceBean);
            }
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.toast(R.string.delete_success);
                        mView.refreshBookSource();
                        mView.setResult(RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(R.string.delete_fail);
                    }
                });
    }

    private void restoreBookSource(BookSourceBean bookSourceBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookSourceManager.addBookSource(bookSourceBean);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.refreshBookSource();
                        mView.setResult(RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void importBookSource(String text) {
        mView.showSnackBar(StringUtils.getString(R.string.is_importing_book_source), Snackbar.LENGTH_INDEFINITE);
        Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(text);
        if (observable != null) {
            observable.subscribe(getImportObserver());
        } else {
            mView.showSnackBar(StringUtils.getString(R.string.format_error), Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void importBookSourceLocal(String path) {
        if (TextUtils.isEmpty(path)) {
            mView.toast(R.string.read_file_error);
            return;
        }
        String json;
        DocumentFile file;
        try {
            file = DocumentFile.fromFile(new File(path));
        } catch (Exception e) {
            mView.toast(path +StringUtils.getString(R.string.cannot_open));
            return;
        }
        json = DocumentHelper.readString(file);
        if (!isEmpty(json)) {
            mView.showSnackBar(StringUtils.getString(R.string.is_importing_book_source), Snackbar.LENGTH_INDEFINITE);
            importBookSource(json);
        } else {
            mView.toast(R.string.read_file_error);
        }
    }

    private MyObserver<List<BookSourceBean>> getImportObserver() {
        return new MyObserver<>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onNext(List<BookSourceBean> bookSourceBeans) {
                if (bookSourceBeans.size() > 0) {
                    mView.refreshBookSource();
                    mView.showSnackBar(String.format(StringUtils.getString(R.string.import_number_book_source_success), bookSourceBeans.size()), Snackbar.LENGTH_SHORT);
                    mView.setResult(RESULT_OK);
                } else {
                    mView.showSnackBar(StringUtils.getString(R.string.format_error), Snackbar.LENGTH_SHORT);
                }
            }

            @Override
            public void onError(Throwable e) {
                mView.showSnackBar(e.getMessage(), Snackbar.LENGTH_SHORT);
            }
        };
    }

    @Override
    public void checkBookSource(List<BookSourceBean> sourceBeans) {
        CheckSourceService.start(mView.getContext(), sourceBeans);
    }

    /////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    /////////////////////RxBus////////////////////////

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHECK_SOURCE_STATE)})
    public void upCheckSourceState(String msg) {
        mView.refreshBookSource();
        if (progressSnackBar == null) {
            progressSnackBar = mView.getSnackBar(msg, Snackbar.LENGTH_INDEFINITE);
            progressSnackBar.setActionTextColor(Color.WHITE);
            progressSnackBar.setAction(mView.getContext().getString(R.string.cancel), view -> CheckSourceService.stop(mView.getContext()));
        } else {
            progressSnackBar.setText(msg);
        }
        if (!progressSnackBar.isShown()) {
            progressSnackBar.show();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHECK_SOURCE_FINISH)})
    public void checkSourceFinish(String msg) {
        mView.showSnackBar(msg, Snackbar.LENGTH_SHORT);
    }
}
