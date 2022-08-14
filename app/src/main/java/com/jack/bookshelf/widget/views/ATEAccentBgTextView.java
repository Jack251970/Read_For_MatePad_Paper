package com.jack.bookshelf.widget.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.utils.ColorUtils;
import com.jack.bookshelf.utils.ScreenUtils;
import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.theme.ThemeStore;

public class ATEAccentBgTextView extends AppCompatTextView {
    public ATEAccentBgTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ATEAccentBgTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEAccentBgTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackground(Selector.shapeBuild()
                .setCornerRadius(ScreenUtils.dpToPx(3))
                .setDefaultBgColor(ThemeStore.accentColor(context))
                .setPressedBgColor(ColorUtils.darkenColor(ThemeStore.accentColor(context)))
                .create());
        setTextColor(Color.WHITE);
    }
}
