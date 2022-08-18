package com.jack.bookshelf.view.dialog;

import static com.jack.bookshelf.utils.StringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;

/**
 * Alert Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AlertDialog extends PopupWindow{
    private final Context context;
    private final View mainView;
    private TextView tvTitle;
    private TextView tvNegativeButton;
    private TextView tvPositiveButton;
    private OnItemClickListener itemClick;
    public static final int NO_TITLE = 0;

    public static AlertDialog builder(Context context, View mainView, int type) {
        return new AlertDialog(context, mainView, type);
    }

    public AlertDialog setMessage(int strId) {
        return setMessage(getString(strId));
    }

    public AlertDialog setMessage(String title) {
        tvTitle.setText(title);
        return this;
    }

    public AlertDialog setNegativeButton (int strId) {
        return setNegativeButton(getString(strId));
    }

    public AlertDialog setNegativeButton (String text) {
        tvNegativeButton.setText(text);
        tvNegativeButton.setOnClickListener(v -> {
            dismiss();
            itemClick.forNegativeButton();
        });
        return this;
    }

    public AlertDialog setPositiveButton (int strId) {
        return setPositiveButton(getString(strId));
    }

    public AlertDialog setPositiveButton (String text) {
        tvPositiveButton.setText(text);
        tvPositiveButton.setOnClickListener(v -> {
            dismiss();
            itemClick.forPositiveButton();
        });
        return this;
    }

    public AlertDialog setOnclick(@NonNull OnItemClickListener itemClick) {
        this.itemClick = itemClick;
        return this;
    }

    @SuppressLint({"InflateParams"})
    public AlertDialog(Context context, View mainView, int type) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.mainView = mainView;
        if (type == NO_TITLE) {
            View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_no_title, null);
            this.setContentView(view);
            bindView(view);
        }
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.tv_alert_dialog_no_title_tips);
        tvNegativeButton = view.findViewById(R.id.tv_alert_dialog_no_title_negative_button);
        tvPositiveButton = view.findViewById(R.id.tv_alert_dialog_no_title_positive_button);
    }

    public void show() {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forNegativeButton();

        void forPositiveButton();
    }
}