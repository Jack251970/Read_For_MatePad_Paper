package com.jack.bookshelf.widget.textview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.utils.ScreenUtils;
import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.theme.ThemeStore;

/**
 * Paper TextView with Black Background
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperAccentBgTextView extends AppCompatTextView {
    public PaperAccentBgTextView(Context context) {
        super(context);
        init(context);
    }

    public PaperAccentBgTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaperAccentBgTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackground(Selector.shapeBuild()
                .setCornerRadius(ScreenUtils.dpToPx(3))
                .setDefaultBgColor(ThemeStore.accentColor(context))
                .create());
        setTextColor(Color.WHITE);
    }
}
