package com.jack.bookshelf.widget.dialog;

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
 * Paper Alert Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperAlertDialog extends PopupWindow{
    private final Context context;
    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvAppendMessage;
    private TextView tvNegativeButton;
    private TextView tvPositiveButton;

    public static final int NO_APPEND_MESSAGE = 0, ONLY_CENTER_TITLE = 1, ALL = 2;

    public static PaperAlertDialog builder(Context context) { return new PaperAlertDialog(context); }

    public PaperAlertDialog setType(int type) {
        switch (type) {
            case ONLY_CENTER_TITLE:
                tvTitle.setGravity(Gravity.CENTER);
                tvMessage.setVisibility(View.GONE);
                break;
            case ALL:
                tvAppendMessage.setVisibility(View.VISIBLE);
                break;
            default:
                return this;
        }
        return this;
    }

    public PaperAlertDialog setTitle(int strId) {
        return setTitle(getString(strId));
    }

    public PaperAlertDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public PaperAlertDialog setMessage(int strId) {
        return setMessage(getString(strId));
    }

    public PaperAlertDialog setMessage(String message) {
        tvMessage.setText(message);
        return this;
    }

    public PaperAlertDialog setAppendMessage(int strId) {
        return setAppendMessage(getString(strId));
    }

    public PaperAlertDialog setAppendMessage(String appendMessage) {
        tvAppendMessage.setText(appendMessage);
        return this;
    }

    public PaperAlertDialog setNegativeButton(int strId) {
        return setNegativeButton(getString(strId));
    }

    public PaperAlertDialog setNegativeButton(String text) {
        tvNegativeButton.setText(text);
        return this;
    }

    public PaperAlertDialog setPositiveButton(int strId) {
        return setPositiveButton(getString(strId));
    }

    public PaperAlertDialog setPositiveButton(String text) {
        tvPositiveButton.setText(text);
        return this;
    }

    public PaperAlertDialog setOnclick(@NonNull OnItemClickListener itemClick) {
        tvPositiveButton.setOnClickListener(v -> {
            dismiss();
            itemClick.forPositiveButton();
        });
        tvNegativeButton.setOnClickListener(v -> {
            dismiss();
            itemClick.forNegativeButton();
        });
        return this;
    }

    @SuppressLint({"InflateParams"})
    public PaperAlertDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_paper, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.tv_title_alert_dialog);
        tvMessage = view.findViewById(R.id.tv_message_alert_dialog);
        tvAppendMessage = view.findViewById(R.id.tv_append_message_alert_dialog);
        tvNegativeButton = view.findViewById(R.id.tv_negative_button_alert_dialog);
        tvPositiveButton = view.findViewById(R.id.tv_positive_button_alert_dialog);
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forNegativeButton();

        void forPositiveButton();
    }
}