package com.jack.bookshelf.utils.theme;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
interface ThemeStorePrefKeys {
    String CONFIG_PREFS_KEY_DEFAULT = "[[kabouzeid_app-theme-helper]]";
    String IS_CONFIGURED_KEY = "is_configured";
    String IS_CONFIGURED_VERSION_KEY = "is_configured_version";
    String VALUES_CHANGED = "values_changed";

    String KEY_PRIMARY_COLOR = "primary_color";
    String KEY_PRIMARY_COLOR_DARK = "primary_color_dark";
    String KEY_ACCENT_COLOR = "accent_color";

    String KEY_TEXT_COLOR_PRIMARY = "text_color_primary";
    String KEY_TEXT_COLOR_SECONDARY = "text_color_secondary";

    String KEY_BACKGROUND_COLOR = "backgroundColor";

    String KEY_AUTO_GENERATE_PRIMARYDARK = "auto_generate_primarydark";
}