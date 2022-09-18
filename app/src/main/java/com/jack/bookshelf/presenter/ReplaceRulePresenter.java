package com.jack.bookshelf.presenter;

import static android.text.TextUtils.isEmpty;

import android.graphics.Color;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.bean.ReplaceRuleBean;
import com.jack.bookshelf.help.DocumentHelper;
import com.jack.bookshelf.model.ReplaceRuleManager;
import com.jack.bookshelf.presenter.contract.ReplaceRuleContract;
import com.jack.bookshelf.utils.StringUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Replace Rule Presenter
 * Created by GKF on 2017/12/18.
 * Edited by Jack251970
 */

public class ReplaceRulePresenter extends BasePresenterImpl<ReplaceRuleContract.View> implements ReplaceRuleContract.Presenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int i = 0;
            for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
                i++;
                replaceRuleBean.setSerialNumber(i + 1);
            }
            ReplaceRuleManager.addDataS(replaceRuleBeans);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            ReplaceRuleManager.delData(replaceRuleBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(Boolean replaceRuleBeans) {
                        mView.refresh();
                        mView.getSnackBar(replaceRuleBean.getReplaceSummary() + StringUtils.getString(R.string.have_deleted), Snackbar.LENGTH_LONG)
                                .setAction(R.string.restore, view -> restoreData(replaceRuleBean))
                                .setActionTextColor(Color.WHITE)
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            ReplaceRuleManager.delDataS(replaceRuleBeans);
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

    private void restoreData(ReplaceRuleBean replaceRuleBean) {
        ReplaceRuleManager.saveData(replaceRuleBean)
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        mView.refresh();
                    }
                });
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
