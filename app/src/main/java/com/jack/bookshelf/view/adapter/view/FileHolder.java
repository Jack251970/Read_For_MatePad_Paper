package com.jack.bookshelf.view.adapter.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.constant.AppConstant;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.FileHelp;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.view.adapter.base.ViewHolderImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * File Item Holder
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class FileHolder extends ViewHolderImpl<File> {
    private ImageView mIvIcon;
    private ImageView mIvIfAdd;
    private CheckBox mCbSelect;
    private TextView mTvName;
    private LinearLayout mLlBrief;
    private TextView mTvTag;
    private TextView mTvSize;
    private TextView mTvDate;
    private TextView mTvSubCount;

    private final HashMap<File, Boolean> mSelectedMap;

    public FileHolder(HashMap<File, Boolean> selectedMap) {
        mSelectedMap = selectedMap;
    }

    @Override
    public void initView() {
        mIvIcon = findById(R.id.file_iv_icon);
        mCbSelect = findById(R.id.file_cb_select);
        mTvName = findById(R.id.file_tv_name);
        mLlBrief = findById(R.id.file_ll_brief);
        mTvTag = findById(R.id.file_tv_tag);
        mTvSize = findById(R.id.file_tv_size);
        mTvDate = findById(R.id.file_tv_date);
        mTvSubCount = findById(R.id.file_tv_sub_count);
        mIvIfAdd = findById(R.id.file_iv_if_add);
    }

    @Override
    public void onBind(File data, int pos) {
        //判断是文件还是文件夹
        if (data.isDirectory()) {
            setFolder(data);
        } else {
            setFile(data);
        }
        mCbSelect.setClickable(false);
    }

    /**
     * 文件类型的显示设置
     */
    private void setFile(File file) {
        // 书架是否加入与是否可以选择
        if (BookshelfHelp.getBook(file.getAbsolutePath()) != null) {
            mCbSelect.setVisibility(View.GONE);
            mIvIfAdd.setVisibility(View.VISIBLE);
        } else {
            mCbSelect.setChecked(Boolean.TRUE.equals(mSelectedMap.get(file)));
            mCbSelect.setVisibility(View.VISIBLE);
        }
        // 文件图片
        mTvTag.setVisibility(View.VISIBLE);
        mTvTag.setText(file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase());
        // 文件名字、大小、日期
        mTvName.setText(file.getName());
        mTvSize.setText(FileHelp.getFileSize(file.length()));
        mTvDate.setText(StringUtils.dateConvert(file.lastModified(), AppConstant.FORMAT_FILE_DATE));
        // 文件其余信息
        mLlBrief.setVisibility(View.VISIBLE);
        mTvSubCount.setVisibility(View.GONE);
    }

    /**
     * 文件夹类型的显示设置
     */
    public void setFolder(File folder) {
        // 文件夹图片
        mIvIcon.setVisibility(View.VISIBLE);
        mTvTag.setVisibility(View.GONE);
        // 是否可以选择
        mCbSelect.setVisibility(View.GONE);
        // 文件夹名字
        mTvName.setText(folder.getName());
        // 文件夹介绍
        mLlBrief.setVisibility(View.GONE);
        mTvSubCount.setVisibility(View.VISIBLE);
        // 文件夹所含项目数量
        mTvSubCount.setText(getContext().getString(R.string.nb_file_sub_count, Objects.requireNonNull(folder.list()).length));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_file;
    }
}
