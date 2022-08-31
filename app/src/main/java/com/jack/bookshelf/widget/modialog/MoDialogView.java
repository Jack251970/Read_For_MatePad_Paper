package com.jack.bookshelf.widget.modialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ReadAssets;
import com.jack.bookshelf.widget.RotateLoading;

import ru.noties.markwon.Markwon;

/**
 * MoDialog View
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MoDialogView extends LinearLayout {
    private final Context context;

    public MoDialogView(Context context) {
        this(context, null);
    }

    public MoDialogView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(VERTICAL);
    }

    /**
     * 转圈载入
     */
    public void showLoading(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.mo_dialog_loading, this, true);
        TextView msgTv = findViewById(R.id.msg_tv);
        if (text != null && text.length() > 0) {
            msgTv.setText(text);
        }
        RotateLoading rlLoading = findViewById(R.id.rl_loading);
        rlLoading.start();
    }

    /**
     * 显示一段文本
     */
    public void showText(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.mo_dialog_text_large, this, true);
        TextView textView = findViewById(R.id.tv_can_copy);
        textView.setText(text);
    }

    /**
     * 显示Markdown文本
     */
    public void showAssetMarkdown(String assetFileName) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.mo_dialog_markdown, this, true);
        TextView tvMarkdown = findViewById(R.id.tv_markdown);
        Markwon.create(tvMarkdown.getContext()).setMarkdown(tvMarkdown, ReadAssets.getText(context, assetFileName));
    }
}