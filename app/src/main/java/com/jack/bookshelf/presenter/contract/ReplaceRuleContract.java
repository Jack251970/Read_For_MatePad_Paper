package com.jack.bookshelf.presenter.contract;


import com.google.android.material.snackbar.Snackbar;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.bean.ReplaceRuleBean;

import java.util.List;

public interface ReplaceRuleContract {
    interface Presenter extends IPresenter {

        void saveData(List<ReplaceRuleBean> replaceRuleBeans);

        void delData(ReplaceRuleBean replaceRuleBean);

        void delData(List<ReplaceRuleBean> replaceRuleBeans);

        void importDataSLocal(String uri);

        void importDataS(String text);
    }

    interface View extends IView {

        void refresh();

        Snackbar getSnackBar(String msg, int length);

    }

}
