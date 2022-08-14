package com.jack.bookshelf.view.popupmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.popupwindow.PopupWindowsUtil;

/**
 * PopupMenu in Main Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack Ye
 */

public class MoreSettingMenuMain extends PopupWindow {
    private final View view;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    public MoreSettingMenuMain(Context context, @NonNull OnItemClickListener itemClick) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.view = LayoutInflater.from(context).inflate(R.layout.more_setting_menu_main, null);
        this.setContentView(view);
        // 菜单点击事件
        view.findViewById(R.id.mpp_tv_download_all_main).setOnClickListener(v -> {
            dismiss();
            itemClick.downloadAll();
        });
        view.findViewById(R.id.mpp_tv_arrange_bookshelf_main).setOnClickListener(v -> {
            dismiss();
            itemClick.arrangeBookshelf();
        });
        view.findViewById(R.id.mpp_tv_sequence_rule_main).setOnClickListener(v -> {
            dismiss();
            itemClick.selectSequenceRule();
        });
        view.findViewById(R.id.mpp_tv_start_web_service_main).setOnClickListener(v -> {
            dismiss();
            itemClick.startWebService();
        });
        setFocusable(true);
        setTouchable(true);
    }

    public void show(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowsUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, windowPos[0] - 40, windowPos[1] + 10);
    }

    public interface OnItemClickListener {
        void downloadAll();

        void arrangeBookshelf();

        void selectSequenceRule();

        void startWebService();
    }
}
