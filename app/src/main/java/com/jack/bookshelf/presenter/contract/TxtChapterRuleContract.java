package com.jack.bookshelf.presenter.contract;

import com.google.android.material.snackbar.Snackbar;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.bean.TxtChapterRuleBean;

import java.util.List;

public interface TxtChapterRuleContract {
    interface Presenter extends IPresenter {

        void saveData(List<TxtChapterRuleBean> txtChapterRuleBeans);

        void delData(TxtChapterRuleBean txtChapterRuleBean);

        void delData(List<TxtChapterRuleBean> txtChapterRuleBeans);

        void importDataSLocal(String uri);

        void importDataS(String text);
    }

    interface View extends IView {

        void refresh();

        Snackbar getSnackBar(String msg, int length);

    }
}
