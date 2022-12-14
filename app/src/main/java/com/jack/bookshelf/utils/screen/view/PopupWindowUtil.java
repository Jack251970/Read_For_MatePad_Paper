package com.jack.bookshelf.utils.screen.view;

import android.view.View;

import com.jack.bookshelf.utils.screen.ScreenUtils;

/**
 * PopupWindow Util
 * Edited by Jack251970
 */

public class PopupWindowUtil {
    /**
     * 计算PopupWindows弹出位置
     * y方向就在anchorView的上面和下面对齐显示，x方向就与屏幕右边对齐显示
     * @param anchorView 弹出菜单的view
     * @param contentView  PopupWindow的view
     * @return PopupWindow 左上角的xOff,yOff坐标
     */
    public static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int[] windowPos = new int[2];
        final int[] anchorLoc = new int[2];
        final int[] screenSize = ScreenUtils.getAppSize();
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的宽高
        final int screenWidth = screenSize[0];
        final int screenHeight = screenSize[1];
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        windowPos[0] = screenWidth - windowWidth;
        if (isNeedShowUp) {
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }
}
