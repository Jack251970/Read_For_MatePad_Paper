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
    private final Context context;
    private final View view;
    private final ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnThisListener thisListener;

    public static Regex fontRegex = new Regex("(?i).*\\.[ot]tf");

    @SuppressLint("InflateParams")
    public FontSelectorDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
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
                ((ImageView)view.findViewById(R.id.iv_indicator_font_harmony_regular)).setImageResource(R.drawable.ic_select_menu_selected);
                break;
            case 1:
                ((ImageView)view.findViewById(R.id.iv_indicator_font_harmony_bold)).setImageResource(R.drawable.ic_select_menu_selected);
                break;
        }
    }

    private void bindView() {
        view.findViewById(R.id.tv_font_self_choose).setOnClickListener(v -> {
            dismiss();
            thisListener.forBottomButton();
        });
        view.findViewById(R.id.tv_font_harmony_regular).setOnClickListener(v -> {
            dismiss();
            thisListener.forMenuItem(0);
        });
        view.findViewById(R.id.tv_font_harmony_bold).setOnClickListener(v -> {
            dismiss();
            thisListener.forMenuItem(1);
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

        void forBottomButton();
    }
}