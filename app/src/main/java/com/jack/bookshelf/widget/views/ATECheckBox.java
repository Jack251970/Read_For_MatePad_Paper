package com.jack.bookshelf.widget.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.jack.bookshelf.utils.theme.ATH;
import com.jack.bookshelf.utils.theme.ThemeStore;

/**
 * CheckBox
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ATECheckBox extends AppCompatCheckBox {

    public ATECheckBox(Context context) {
        super(context);
        init(context);
    }

    public ATECheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ATECheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(Color.TRANSPARENT);  // 取消点击背景
        ATH.setTint(this, ThemeStore.accentColor(context));
    }
}
