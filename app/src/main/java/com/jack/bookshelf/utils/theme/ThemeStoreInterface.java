package com.jack.bookshelf.utils.theme;


import androidx.annotation.ColorInt;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
interface ThemeStoreInterface {
    ThemeStore primaryColor(@ColorInt int color);

    ThemeStore primaryColorDark(@ColorInt int color);

    ThemeStore accentColor(@ColorInt int color);

    ThemeStore backgroundColor(@ColorInt int color);

    void apply();
}
