package com.jack.bookshelf.utils.screen;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.bookshelf.MApplication;

/**
 * Created by newbiechen on 17-5-1.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ScreenUtils {

    public static int dpToPx(int dp) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) (dp * metrics.density + 0.5f * (dp >= 0 ? 1 : -1));
    }

    public static int pxToDp(int px) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) (px / metrics.density);
    }

    public static int spToPx(int sp) {
        float fontScale = getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static int pxToSp(int px) {
        DisplayMetrics metrics = getDisplayMetrics();
        return (int) (px / metrics.scaledDensity);
    }

    /**
     * 获取手机显示App区域（头部导航栏+ActionBar+根布局）的大小，不包括虚拟按钮
     */
    public static int[] getAppSize() {
        int[] size = new int[2];
        DisplayMetrics metrics = getDisplayMetrics();
        size[0] = metrics.widthPixels;
        size[1] = metrics.heightPixels;
        return size;
    }

    /**
     * 获取整个手机屏幕的大小(包括虚拟按钮)
     * 必须在onWindowFocus方法之后使用
     */
    public static int[] getScreenSize(AppCompatActivity activity) {
        int[] size = new int[2];
        View decorView = activity.getWindow().getDecorView();
        size[0] = decorView.getWidth();
        size[1] = decorView.getHeight();
        return size;
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight() {
        Resources resources = MApplication.getInstance().getResources();
        @SuppressLint("InternalInsetResource")
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static DisplayMetrics getDisplayMetrics() {
        return MApplication
                .getInstance()
                .getResources()
                .getDisplayMetrics();
    }
}
