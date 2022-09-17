package com.jack.bookshelf.widget.text;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

/**
 * Paper Text Input Layout
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperTextInputLayout extends TextInputLayout {
    public PaperTextInputLayout(Context context) {
        super(context);
        init();
    }

    public PaperTextInputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaperTextInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 取消动画
        setHintAnimationEnabled(false);
    }
}
