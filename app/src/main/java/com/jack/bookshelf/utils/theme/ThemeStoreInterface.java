package com.jack.bookshelf.utils.theme;


import androidx.annotation.ColorInt;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
interface ThemeStoreInterface {

    // Primary colors

    ThemeStore primaryColor(@ColorInt int color);

    ThemeStore primaryColorDark(@ColorInt int color);

    // Accent colors

    ThemeStore accentColor(@ColorInt int color);

    // Primary text color

    ThemeStore textColorPrimary(@ColorInt int color);

    ThemeStore textColorPrimaryInverse(@ColorInt int color);

    // Secondary text color

    ThemeStore textColorSecondary(@ColorInt int color);

    ThemeStore textColorSecondaryInverse(@ColorInt int color);

    ThemeStore backgroundColor(@ColorInt int color);

    void apply();
}
