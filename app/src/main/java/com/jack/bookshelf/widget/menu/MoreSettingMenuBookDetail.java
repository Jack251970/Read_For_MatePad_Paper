package com.jack.bookshelf.widget.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.screen.view.PopupWindowUtil;

/**
 * PopupMenu in Book Detail Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MoreSettingMenuBookDetail extends PopupWindow {
    private final View view;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    public MoreSettingMenuBookDetail(Context context, Boolean ifAllowUpdate, @NonNull OnItemClickListener itemClick) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.view = LayoutInflater.from(context).inflate(R.layout.menu_more_setting_book_detail, null);
        // 菜单文字初始化
        if (!ifAllowUpdate) {
            ((TextView) view.findViewById(R.id.tv_manage_update_book_detail)).setText(R.string.allow_update);
            ((ImageView) view.findViewById(R.id.iv_manage_update_book_detail)).setImageResource(R.drawable.ic_update_book_enable);
        }
        this.setContentView(view);
        // 菜单点击事件
        view.findViewById(R.id.ll_refresh_book_detail).setOnClickListener(v -> {
            dismiss();
            itemClick.refreshBook();
        });
        view.findViewById(R.id.ll_manage_update_book_detail).setOnClickListener(v -> {
            MoreSettingMenuBookDetail.this.dismiss();
            itemClick.manageUpdate();
        });
        view.findViewById(R.id.ll_edit_book_source_book_detail).setOnClickListener(v -> {
            dismiss();
            itemClick.editBookSource();
        });
        view.findViewById(R.id.ll_copy_book_url_detail).setOnClickListener(v -> {
            dismiss();
            itemClick.copyBookUrl();
        });
        setFocusable(true);
        setTouchable(true);
    }

    public void show(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, windowPos[0] - 30, windowPos[1] + 10);
    }

    public interface OnItemClickListener {
        void refreshBook();

        void manageUpdate();

        void editBookSource();

        void copyBookUrl();
    }
}
