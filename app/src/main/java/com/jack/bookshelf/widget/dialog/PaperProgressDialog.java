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
        return (int)(100 * progress / max) + " %";
    }

    public PaperProgressDialog setButton(int strId) {
        return setButton(getString(strId));
    }

    public PaperProgressDialog setButton(String text) {
        tvButton.setText(text);
        return this;
    }

    public PaperProgressDialog setOnclick(@NonNull OnItemClickListener itemClick) {
        tvButton.setOnClickListener(v -> {
            dismiss();
            setProgress(0);
            itemClick.forButton();
        });
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
        progressBar = view.findViewById(R.id.pgb_progress_bar_progress_dialog);
        progressNumber = view.findViewById(R.id.tv_progress_number_progress_dialog);
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forButton();
    }
}