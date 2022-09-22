package com.jack.bookshelf.widget.text;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.screen.ScreenUtils;

/**
 * Accent Background TextView
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AccentBgTextView extends AppCompatTextView {
    public AccentBgTextView(Context context) {
        super(context);
        init(context, null);
    }

    public AccentBgTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AccentBgTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 设置文字颜色
        setTextColor(Color.WHITE);
        // 设置背景
        setBackground(Selector.shapeBuild()
                .setCornerRadius(ScreenUtils.dpToPx(3))
                .setDefaultBgColor(Color.BLACK).create());
        // 设置Padding
        setPadding(ScreenUtils.dpToPx(4),ScreenUtils.dpToPx(1),ScreenUtils.dpToPx(4),ScreenUtils.dpToPx(1));
    }
}

