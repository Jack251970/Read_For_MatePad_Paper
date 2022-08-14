package com.jack.bookshelf.presenter.contract;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.List;

public interface FindBookContract {
    interface Presenter extends IPresenter {

        void initData();

    }

    interface View extends IView {

        void upData(List<RecyclerViewData> group);

    }
}
