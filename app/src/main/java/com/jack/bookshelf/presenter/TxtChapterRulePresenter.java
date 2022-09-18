package com.jack.bookshelf.presenter;

import static android.text.TextUtils.isEmpty;

import android.graphics.Color;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.TxtChapterRuleBean;
import com.jack.bookshelf.help.DocumentHelper;
import com.jack.bookshelf.model.ReplaceRuleManager;
import com.jack.bookshelf.model.TxtChapterRuleManager;
import com.jack.bookshelf.presenter.contract.TxtChapterRuleContract;
import com.jack.bookshelf.utils.StringUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Txt Chapter Rule Presenter
 * Edited by Jack251970
 */

public class TxtChapterRulePresenter extends BasePresenterImpl<TxtChapterRuleContract.View> implements TxtChapterRuleContract.Presenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<TxtChapterRuleBean> txtChapterRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int i = 0;
            for (TxtChapterRuleBean ruleBean : txtChapterRuleBeans) {
                i++;
                ruleBean.setSerialNumber(i + 1);
            }
            DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(txtChapterRuleBeans);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(TxtChapterRuleBean txtChapterRuleBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            TxtChapterRuleManager.del(txtChapterRuleBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean replaceRuleBeans) {
                        mView.refresh();
                        mView.getSnackBar(txtChapterRuleBean.getName() + StringUtils.getString(R.string.have_deleted), Snackbar.LENGTH_LONG)
                                .setAction(R.string.restore, view -> restoreData(txtChapterRuleBean))
                                .setActionTextColor(Color.BLACK)
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delData(List<TxtChapterRuleBean> txtChapterRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            TxtChapterRuleManager.del(txtChapterRuleBeans);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.toast(R.string.delete_success);
                        mView.refresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(R.string.delete_fail);
                    }
                });
    }

    private void restoreData(TxtChapterRuleBean txtChapterRuleBean) {
        TxtChapterRuleManager.save(txtChapterRuleBean);
        mView.refresh();
    }

    @Override
    public void importDataSLocal(String path) {
        String json;
        DocumentFile file = DocumentFile.fromFile(new File(path));
        json = DocumentHelper.readString(file);
        if (!isEmpty(json)) {
            importDataS(json);
        } else {
            mView.toast(R.string.read_file_error);
        }
    }

    @Override
    public void importDataS(String text) {
        Observable<Boolean> observable = ReplaceRuleManager.importReplaceRule(text);
        if (observable != null) {
            observable.subscribe(new MyObserver<>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    mView.refresh();
                    mView.toast(R.string.import_success);
                }

                @Override
                public void onError(Throwable e) {
                    mView.toast(R.string.format_error);
                }
            });
        } else {
            mView.toast(R.string.import_fail);
        }
    }
}
