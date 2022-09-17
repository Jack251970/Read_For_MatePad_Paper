package com.jack.bookshelf.utils.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ColorUtils;

import java.lang.reflect.Field;

/**
 * @author afollestad, plusCubed
 */
public final class TintHelper {

    @SuppressLint("PrivateResource")
    @ColorInt
    private static int getDefaultRippleColor(@NonNull Context context, boolean useDarkRipple) {
        // Light ripple is actually translucent black, and vice versa
        return ContextCompat.getColor(context, useDarkRipple ?
                R.color.ripple_material_light : R.color.ripple_material_dark);
    }

    @NonNull
    private static ColorStateList getDisabledColorStateList(@ColorInt int normal, @ColorInt int disabled) {
        return new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_enabled}
        }, new int[]{
                disabled,
                normal
        });
    }

    public static void setTintSelector(@NonNull View view, @ColorInt final int color, final boolean darker, final boolean useDarkTheme) {
        final boolean isColorLight = ColorUtils.isColorLight(color);
        final int disabled = ContextCompat.getColor(view.getContext(), useDarkTheme ? R.color.ate_button_disabled_dark : R.color.ate_button_disabled_light);
        final int pressed = ColorUtils.shiftColor(color, darker ? 0.9f : 1.1f);
        final int activated = ColorUtils.shiftColor(color, darker ? 1.1f : 0.9f);
        final int rippleColor = getDefaultRippleColor(view.getContext(), isColorLight);
        final int textColor = Color.BLACK;
        final ColorStateList sl;
        if (view instanceof Button) {
            sl = getDisabledColorStateList(color, disabled);
            if (view.getBackground() instanceof RippleDrawable) {
                RippleDrawable rd = (RippleDrawable) view.getBackground();
                rd.setColor(ColorStateList.valueOf(rippleColor));
            }

            // Disabled text color state for buttons, may get overridden later by ATE tags
            final Button button = (Button) view;
            button.setTextColor(getDisabledColorStateList(textColor, ContextCompat.getColor(view.getContext(), useDarkTheme ? R.color.ate_button_text_disabled_dark : R.color.ate_button_text_disabled_light)));
        } else if (view instanceof FloatingActionButton) {
            // FloatingActionButton doesn't support disabled state?
            sl = new ColorStateList(new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            }, new int[]{
                    color,
                    pressed
            });

            final FloatingActionButton fab = (FloatingActionButton) view;
            fab.setRippleColor(rippleColor);
            fab.setBackgroundTintList(sl);
            if (fab.getDrawable() != null)
                fab.setImageDrawable(createTintedDrawable(fab.getDrawable(), textColor));
            return;
        } else {
            sl = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                            new int[]{android.R.attr.state_enabled, android.R.attr.state_activated},
                            new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}
                    },
                    new int[]{
                            disabled,
                            color,
                            pressed,
                            activated,
                            activated
                    }
            );
        }

        Drawable drawable = view.getBackground();
        if (drawable != null) {
            drawable = createTintedDrawable(drawable, sl);
            ViewUtil.setBackgroundCompat(view, drawable);
        }

        if (view instanceof TextView && !(view instanceof Button)) {
            final TextView tv = (TextView) view;
            tv.setTextColor(Color.BLACK);
        }
    }

    public static void setTintAuto(final @NonNull View view, final @ColorInt int color,
                                   boolean background) {
        setTintAuto(view, color, background, ATHUtil.isWindowBackgroundDark(view.getContext()));
    }

    public static void setTintAuto(final @NonNull View view, final @ColorInt int color,
                                   boolean background, final boolean isDark) {
        if (!background) {
            if (view instanceof SeekBar)
                setTint((SeekBar) view, color, isDark);
            else if (view instanceof AppCompatEditText)
                setTint((AppCompatEditText) view, color, isDark);
            else if (view instanceof ImageView)
                setTint((ImageView) view, color);
            else if (view instanceof SearchView) {
                int[] iconIdS = new int[]{androidx.appcompat.R.id.search_button, androidx.appcompat.R.id.search_close_btn,};
                for (int iconId : iconIdS) {
                    ImageView icon = view.findViewById(iconId);
                    if (icon != null) {
                        setTint(icon, color);
                    }
                }

            } else {
                background = true;
            }

            if (!background && view.getBackground() instanceof RippleDrawable) {
                // Ripples for the above views (e.g. when you tap and hold a switch or checkbox)
                RippleDrawable rd = (RippleDrawable) view.getBackground();
                @SuppressLint("PrivateResource") final int unchecked = ContextCompat.getColor(view.getContext(),
                        isDark ? R.color.ripple_material_dark : R.color.ripple_material_light);
                final int checked = ColorUtils.adjustAlpha(color, 0.4f);
                final ColorStateList sl = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_activated, -android.R.attr.state_checked},
                                new int[]{android.R.attr.state_activated},
                                new int[]{android.R.attr.state_checked}
                        },
                        new int[]{
                                unchecked,
                                checked,
                                checked
                        }
                );
                rd.setColor(sl);
            }
        }
        if (background) {
            // Need to tint the background of a view
            if (view instanceof FloatingActionButton || view instanceof Button) {
                setTintSelector(view, color, false, isDark);
            } else if (view.getBackground() != null) {
                Drawable drawable = view.getBackground();
                if (drawable != null) {
                    drawable = createTintedDrawable(drawable, color);
                    ViewUtil.setBackgroundCompat(view, drawable);
                }
            }
        }
    }

    public static void setTint(@NonNull SeekBar seekBar, @ColorInt int color, boolean useDarker) {
        final ColorStateList s1 = getDisabledColorStateList(color,
                ContextCompat.getColor(seekBar.getContext(), useDarker ? R.color.ate_control_disabled_dark : R.color.ate_control_disabled_light));
        seekBar.setThumbTintList(s1);
        seekBar.setProgressTintList(s1);
    }

    @SuppressLint("RestrictedApi")
    public static void setTint(@NonNull AppCompatEditText editText, @ColorInt int color, boolean useDarker) {
        final ColorStateList editTextColorStateList = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_enabled, -android.R.attr.state_pressed, -android.R.attr.state_focused},
                new int[]{}
        }, new int[]{
                ContextCompat.getColor(editText.getContext(), useDarker ? R.color.ate_text_disabled_dark : R.color.ate_text_disabled_light),
                ContextCompat.getColor(editText.getContext(), useDarker ? R.color.ate_control_normal_dark : R.color.ate_control_normal_light),
                color
        });
        editText.setSupportBackgroundTintList(editTextColorStateList);
        setCursorTint(editText, color);
    }

    public static void setTint(@NonNull ImageView image, @ColorInt int color) {
        image.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    // This returns a NEW Drawable because of the mutate() call. The mutate() call is necessary because Drawables with the same resource have shared states otherwise.
    @CheckResult
    @Nullable
    public static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable == null) return null;
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    // This returns a NEW Drawable because of the mutate() call. The mutate() call is necessary because Drawables with the same resource have shared states otherwise.
    @CheckResult
    @Nullable
    public static Drawable createTintedDrawable(@Nullable Drawable drawable, @NonNull ColorStateList sl) {
        if (drawable == null) return null;
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, sl);
        return drawable;
    }

    @SuppressLint("SoonBlockedPrivateApi")
    public static void setCursorTint(@NonNull EditText editText, @ColorInt int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0] = createTintedDrawable(drawables[0], color);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = createTintedDrawable(drawables[1], color);
            fCursorDrawable.set(editor, drawables);
        } catch (Exception ignored) {
        }
    }
}