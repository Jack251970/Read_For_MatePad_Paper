package com.jack.bookshelf.view.fragment.base;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.base.MBaseFragment;
import com.jack.bookshelf.view.fragment.adapter.FileSystemAdapter;

import java.io.File;
import java.util.List;

/**
 * Created by newbiechen on 17-7-10.
 * FileSystemActivity的基础Fragment类
 */

public abstract class BaseFileFragment extends MBaseFragment<IPresenter> {

    protected FileSystemAdapter mAdapter;
    protected OnFileCheckedListener mListener;
    protected boolean isCheckedAll;

    public void setChecked(boolean checked) {
        isCheckedAll = checked;
    }

    // 当前fragment是否全选
    public boolean isCheckedAll() {
        return isCheckedAll;
    }

    // 设置当前列表为全选
    public void setCheckedAll(boolean checkedAll) {
        if (mAdapter == null) return;
        isCheckedAll = checkedAll;
        mAdapter.setCheckedAll(checkedAll);
    }

    // 获取被选中的数量
    public int getCheckedCount() {
        if (mAdapter == null) return 0;
        return mAdapter.getCheckedCount();
    }

    // 获取被选中的文件列表
    public List<File> getCheckedFiles() {
        return mAdapter != null ? mAdapter.getCheckedFiles() : null;
    }

    // 获取文件的总数
    public int getFileCount() {
        return mAdapter != null ? mAdapter.getItemCount() : 0;
    }

    // 获取可点击的文件的数量
    public int getCheckableCount() {
        if (mAdapter == null) return 0;
        return mAdapter.getCheckableCount();
    }

    // 设置文件点击监听事件
    public void setOnFileCheckedListener(OnFileCheckedListener listener) {
        mListener = listener;
    }

    // 文件点击监听
    public interface OnFileCheckedListener {
        void onItemCheckedChange(boolean isChecked);

        void onCategoryChanged();
    }
}
