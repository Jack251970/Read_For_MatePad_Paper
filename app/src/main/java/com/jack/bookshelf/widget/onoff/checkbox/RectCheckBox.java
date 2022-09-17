package com.jack.bookshelf.widget.onoff.checkbox;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.screen.ScreenUtils;

/**
 * Rectangle Check Box
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class RectCheckBox extends AppCompatCheckBox {
    int padding = ScreenUtils.dpToPx(5);

    public RectCheckBox(Context context) {
        super(context);
        init();
    }

    public RectCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 取消点击背景
        setBackgroundColor(Color.TRANSPARENT);
        // 设置文字颜色
        setHintTextColor(Color.BLACK);
        // 设置按钮图案
        setButtonDrawable(R.drawable.selector_rect_check_box_background);
        // 设置边距
        setPadding(padding, padding, padding, padding);
    }
}
