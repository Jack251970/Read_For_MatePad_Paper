package com.jack.bookshelf.utils.theme;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class ATH {
    public static void setTint(@NonNull View view, @ColorInt int color) {
        TintHelper.setTintAuto(view, color, false);
    }
}