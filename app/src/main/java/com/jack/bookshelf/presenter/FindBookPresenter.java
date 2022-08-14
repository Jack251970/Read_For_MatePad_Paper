//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.jack.bookshelf.presenter;

import static com.jack.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jack.basemvplib.BasePresenterImpl;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.FindKindBean;
import com.jack.bookshelf.bean.FindKindGroupBean;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.model.analyzeRule.AnalyzeRule;
import com.jack.bookshelf.presenter.contract.FindBookContract;
import com.jack.bookshelf.utils.ACache;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public class FindBookPresenter extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {
    private Disposable disposable;
    private AnalyzeRule analyzeRule;
    private final String findError = "发现规则语法错误";

    @SuppressWarnings("unchecked")
    @Override
    public void initData() {
        if (disposable != null) return;
        ACache aCache = ACache.get(mView.getContext(), "findCache");
        Single.create((SingleOnSubscribe<List<RecyclerViewData>>) e -> {
            List<RecyclerViewData> group = new ArrayList<>();
            boolean showAllFind = MApplication.getConfigPreferences().getBoolean("showAllFind", true);
            List<BookSourceBean> sourceBeans = new ArrayList<>(showAllFind ? BookSourceManager.getAllBookSourceBySerialNumber() : BookSourceManager.getSelectedBookSourceBySerialNumber());
            for (BookSourceBean sourceBean : sourceBeans) {
                Pair<FindKindGroupBean, List<FindKindBean>> pair = sourceBean.getFindList();
                if (pair != null) {
                    group.add(new RecyclerViewData(pair.first, pair.second, false));
                }
            }
            e.onSuccess(group);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<RecyclerViewData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(List<RecyclerViewData> recyclerViewData) {
                        mView.upData(recyclerViewData);
                        disposable.dispose();
                        disposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        disposable.dispose();
                        disposable = null;
                    }
                });
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl, BookSourceBean bookSourceBean) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", new AnalyzeRule(null, bookSourceBean));
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }

}