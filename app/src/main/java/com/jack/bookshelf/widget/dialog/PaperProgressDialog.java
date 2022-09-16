package com.jack.bookshelf.widget.dialog;

import static com.jack.bookshelf.utils.StringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.widget.bar.PaperProgressBar;

/**
 * Paper Progress Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperProgressDialog extends PopupWindow{
    private final Context context;
    private TextView tvTitle;
    private TextView tvButton;
    private LinearLayout llTwoButton;
    private TextView tvLeftButton;
    private TextView tvRightButton;
    private PaperProgressBar progressBar;
    private TextView progressNumber;
    private int max = 100;

    public static PaperProgressDialog builder(Context context) { return new PaperProgressDialog(context); }

    public PaperProgressDialog setTitle(int strId) {
        return setTitle(getString(strId));
    }

    public PaperProgressDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public PaperProgressDialog setProgressMax(int max) {
        this.max = max;
        progressBar.setMax(max);
        return this;
    }

    public PaperProgressDialog setProgress(int progress) {
        progressBar.setProgress(progress, false);
        progressNumber.setText(getPercentText(progress));
        return this;
    }

    private String getPercentText(int progress) {
        return "" + (int)(100.0 * progress / max + 0.5) + " %";
    }

    public PaperProgressDialog setButton(int strId) {
        return setButton(getString(strId));
    }

    public PaperProgressDialog setButton(String text) {
        llTwoButton.setVisibility(View.GONE);
        tvButton.setVisibility(View.VISIBLE);
        tvButton.setText(text);
        return this;
    }

    public PaperProgressDialog setButton(int leftButton, int rightButton) {
        return setButton(getString(leftButton), getString(rightButton));
    }

    public PaperProgressDialog setButton(String leftButton, String rightButton) {
        tvLeftButton.setText(leftButton);
        tvRightButton.setText(rightButton);
        return this;
    }

    public PaperProgressDialog setOnclick(@NonNull OnItemClickListener itemClick) {
        if (tvButton.getVisibility() == View.VISIBLE) {
            tvButton.setOnClickListener(v -> {
                dismiss();
                setProgress(0);
                itemClick.forButton(0);
            });
        } else {
            tvLeftButton.setOnClickListener(v -> {
                dismiss();
                setProgress(0);
                itemClick.forButton(0);
            });
            tvRightButton.setOnClickListener(v -> {
                dismiss();
                setProgress(0);
                itemClick.forButton(1);
            });
        }
        return this;
    }

    @SuppressLint({"InflateParams"})
    public PaperProgressDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.tv_title_progress_dialog);
        tvButton = view.findViewById(R.id.tv_button_progress_dialog);
        llTwoButton = view.findViewById(R.id.ll_two_button_progress_dialog);
        tvLeftButton = view.findViewById(R.id.tv_negative_button_progress_dialog);
        tvRightButton = view.findViewById(R.id.tv_positive_button_progress_dialog);
        progressBar = view.findViewById(R.id.pgb_progress_bar_progress_dialog);
        progressNumber = view.findViewById(R.id.tv_progress_number_progress_dialog);
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forButton(int item);
    }
}