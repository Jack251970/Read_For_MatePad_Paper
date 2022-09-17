package com.jack.bookshelf.utils.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ColorUtils;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */

public final class ThemeStore implements ThemeStorePrefKeys, ThemeStoreInterface {

    private final Context mContext;
    private final SharedPreferences.Editor mEditor;

    public static ThemeStore editTheme(@NonNull Context context) {
        return new ThemeStore(context);
    }

    @SuppressLint("CommitPrefEdits")
    private ThemeStore(@NonNull Context context) {
        mContext = context;
        mEditor = prefs(context).edit();
    }

    @Override
    public ThemeStore primaryColor(@ColorInt int color) {
        mEditor.putInt(KEY_PRIMARY_COLOR, color);
        if (autoGeneratePrimaryDark(mContext))
            primaryColorDark(ColorUtils.darkenColor(color));
        return this;
    }

    @Override
    public ThemeStore primaryColorDark(@ColorInt int color) {
        mEditor.putInt(KEY_PRIMARY_COLOR_DARK, color);
        return this;
    }

    @Override
    public ThemeStore accentColor(@ColorInt int color) {
        mEditor.putInt(KEY_ACCENT_COLOR, color);
        return this;
    }

    @Override
    public ThemeStore backgroundColor(int color) {
        mEditor.putInt(KEY_BACKGROUND_COLOR, color);
        return this;
    }

    // Commit method

    @SuppressWarnings("unchecked")
    @Override
    public void apply() {
        mEditor.putLong(VALUES_CHANGED, System.currentTimeMillis())
                .putBoolean(IS_CONFIGURED_KEY, true)
                .apply();
    }

    // Static getters

    @CheckResult
    @NonNull
    static SharedPreferences prefs(@NonNull Context context) {
        return context.getSharedPreferences(CONFIG_PREFS_KEY_DEFAULT, Context.MODE_PRIVATE);
    }

    @CheckResult
    @ColorInt
    public static int primaryColor(@NonNull Context context) {
        return prefs(context).getInt(KEY_PRIMARY_COLOR, ATHUtil.resolveColor(context, R.attr.colorPrimary, Color.parseColor("#455A64")));
    }

    @CheckResult
    @ColorInt
    public static int accentColor(@NonNull Context context) {
        return prefs(context).getInt(KEY_ACCENT_COLOR, ATHUtil.resolveColor(context, R.attr.colorAccent, Color.parseColor("#263238")));
    }

    @CheckResult
    @ColorInt
    public static int textColorPrimary(@NonNull Context context) {
        return prefs(context).getInt(KEY_TEXT_COLOR_PRIMARY, ATHUtil.resolveColor(context, android.R.attr.textColorPrimary));
    }

    @CheckResult
    @ColorInt
    public static int textColorSecondary(@NonNull Context context) {
        return prefs(context).getInt(KEY_TEXT_COLOR_SECONDARY, ATHUtil.resolveColor(context, android.R.attr.textColorSecondary));
    }

    @CheckResult
    @ColorInt
    public static int backgroundColor(@NonNull Context context) {
        return prefs(context).getInt(KEY_BACKGROUND_COLOR, ATHUtil.resolveColor(context, android.R.attr.colorBackground));
    }

    @CheckResult
    public static boolean autoGeneratePrimaryDark(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_AUTO_GENERATE_PRIMARYDARK, true);
    }

    @SuppressLint("CommitPrefEdits")
    public static boolean isConfigured(Context context, @IntRange(from = 0, to = Integer.MAX_VALUE) long version) {
        final SharedPreferences prefs = prefs(context);
        final long lastVersion = prefs.getLong(IS_CONFIGURED_VERSION_KEY, -1);
        if (version > lastVersion) {
            prefs.edit().putLong(IS_CONFIGURED_VERSION_KEY, version).apply();
            return false;
        }
        return true;
    }
}