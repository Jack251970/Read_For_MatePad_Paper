package com.jack.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;

/**
 * 放入书架界面
 * Copyright (c) 2017. 章钦豪. All rights reserved.
 * Edited by Jack Ye
 */

public class CheckAddShelfPop extends PopupWindow {
    private final Context mContext;
    private final View view;
    private final OnItemClickListener itemClick;
    private final String bookName;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    public CheckAddShelfPop(Context context, @NonNull String bookName, @NonNull OnItemClickListener itemClick) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.bookName = bookName;
        this.itemClick = itemClick;
        view = LayoutInflater.from(mContext).inflate(R.layout.mo_dialog_two, null);
        this.setContentView(view);
        initView();
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
    }

    private void initView() {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvBookName = view.findViewById(R.id.tv_msg);
        tvTitle.setText(R.string.add_to_shelf);
        tvBookName.setText(mContext.getString(R.string.check_add_bookshelf, bookName));
        TextView tvExit = view.findViewById(R.id.tv_cancel);
        tvExit.setText(R.string.no);
        tvExit.setOnClickListener(v -> {
            dismiss();
            itemClick.clickExit();
        });
        TextView tvAddShelf = view.findViewById(R.id.tv_done);
        tvAddShelf.setText(R.string.dialog_confirm);
        tvAddShelf.setOnClickListener(v -> itemClick.clickAddShelf());
    }

    public interface OnItemClickListener {
        void clickExit();

        void clickAddShelf();
    }
}
