package com.jack.bookshelf.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.jack.bookshelf.R;
import com.jack.bookshelf.help.ReadBookControl;

import kotlin.text.Regex;

/**
 * Font Selector Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class FontSelectorDialog extends PopupWindow {
    private final View view;
    private final ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnThisListener thisListener;

    @SuppressLint("InflateParams")
    public FontSelectorDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.view = LayoutInflater.from(context).inflate(R.layout.dialog_font_selector, null);
        this.setContentView(view);
        initView();
        bindView();
        setFocusable(true);
        setTouchable(true);
    }

    private void initView() {
        switch (readBookControl.getFontItem()) {
            case 0:
                ((ImageView)view.findViewById(R.id.iv_indicator_font_harmony_bold)).setImageResource(R.drawable.ic_select_menu_selected);
                break;
            case 1:
                ((ImageView)view.findViewById(R.id.iv_indicator_font_source_han)).setImageResource(R.drawable.ic_select_menu_selected);
                break;
            case 2:
                ((ImageView)view.findViewById(R.id.iv_indicator_font_kai_ti)).setImageResource(R.drawable.ic_select_menu_selected);
                break;
        }
    }

    private void bindView() {
        view.findViewById(R.id.tv_font_self_choose).setOnClickListener(v -> {
            dismiss();
        });
        // 鸿蒙粗体
        view.findViewById(R.id.tv_font_harmony_bold).setOnClickListener(v -> {
            dismiss();
            thisListener.forMenuItem(0);
        });
        // 思源黑体
        view.findViewById(R.id.tv_font_source_han).setOnClickListener(v -> {
            dismiss();
            thisListener.forMenuItem(1);
        });
        // 微软楷体
        view.findViewById(R.id.tv_font_kai_ti).setOnClickListener(v -> {
            dismiss();
            thisListener.forMenuItem(2);
        });
    }

    public FontSelectorDialog setListener(OnThisListener thisListener) {
        this.thisListener = thisListener;
        return this;
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnThisListener {
        void forMenuItem(int item);
    }
}