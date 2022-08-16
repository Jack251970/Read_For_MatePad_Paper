package com.jack.bookshelf.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.jack.bookshelf.R;

/**
 * 对话框
 * Edited by Jack251970
 */

public class MoDialogHUD {
    private final Context context;
    private ViewGroup decorView;    // activity的根View
    private ViewGroup rootView;     // mSharedView的根View
    private MoDialogView mSharedView;
    private OnDismissListener dismissListener;

    private Boolean canBack = false;

    public MoDialogHUD(Context context) {
        this.context = context;
        initViews();
        initCenter();
    }

    private void initCenter() {
        mSharedView.setGravity(Gravity.CENTER);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    private void initBottom() {
        mSharedView.setGravity(Gravity.BOTTOM);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    private void initMarRightTop() {
        mSharedView.setGravity(Gravity.RIGHT | Gravity.TOP);
        if (mSharedView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSharedView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMargins(0, 0, 0, 0);
                mSharedView.setLayoutParams(layoutParams);
            }
            mSharedView.setPadding(0, 0, 0, 0);
        }
    }

    private void initViews() {
        decorView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = new FrameLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.setLayoutParams(layoutParams);
        rootView.setClickable(true);
        rootView.setBackgroundColor(context.getResources().getColor(R.color.btn_bg_press_tp));

        mSharedView = new MoDialogView(context);

    }

    private Animation getInAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_in);
    }

    private Animation getOutAnimation() {
        return AnimationUtils.loadAnimation(context, R.anim.moprogress_out);
    }

    private boolean isShowing() {
        return rootView.getParent() != null;
    }

    private void onAttached() {
        decorView.addView(rootView);
        if (mSharedView.getParent() != null)
            ((ViewGroup) mSharedView.getParent()).removeView(mSharedView);
        rootView.addView(mSharedView);
    }

    public void dismissImmediately() {
        dismiss();
    }

    // 无消失动画的退出
    public void dismiss() {
        if (dismissListener != null) {
            dismissListener.onDismiss();
            dismissListener = null;
        }
        if (mSharedView != null && rootView != null && mSharedView.getParent() != null) {
            new Handler().post(() -> {
                rootView.removeView(mSharedView);
                decorView.removeView(rootView);
            });
        }
    }

    public Boolean isShow() {
        return (mSharedView != null && mSharedView.getParent() != null);
    }

    /**
     * 返回键事件
     */
    public Boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShowing()) {
                if (canBack) {
                    dismissImmediately();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 加载动画
     */
    public void showLoading(String msg) {
        initCenter();
        canBack = false;
        rootView.setOnClickListener(null);
        if (!isShowing()) {
            onAttached();
        }
        mSharedView.showLoading(msg);
    }

    /**
     * 单个按钮的提示信息
     */
    public void showInfo(String msg) {
        initCenter();
        canBack = true;
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, v -> dismissImmediately());
        if (!isShowing()) {
            onAttached();
        }
    }

    /**
     * 单个按钮的提示信息
     */
    public void showInfo(String msg, String btnText, View.OnClickListener listener) {
        initCenter();
        canBack = true;
        rootView.setOnClickListener(null);
        mSharedView.showInfo(msg, btnText, listener);
        if (!isShowing()) {
            onAttached();
        }
    }

    /**
     * 两个不同等级的按钮
     */
    public void showTwoButton(String title,String msg, String b_f, View.OnClickListener c_f, String b_s, View.OnClickListener c_s, boolean canBack) {
        initCenter();
        this.canBack = canBack;
        rootView.setOnClickListener(v -> dismissImmediately());
        mSharedView.showTwoButton(title, msg, b_f, c_f, b_s, c_s);
        if (!isShowing()) {
            onAttached();
        }
    }

    /**
     * 显示一段文本
     */
    public void showText(String text) {
        initCenter();
        canBack = true;
        rootView.setOnClickListener(v -> dismissImmediately());
        mSharedView.showText(text);
        if (!isShowing()) {
            onAttached();
        }
    }

    /**
     * 显示asset Markdown
     * 仅在非E-Ink模式下才显示淡入淡出动画
     */
    public void showAssetMarkdown(String assetFileName) {
        // 初始化界面
        initCenter();
        // 设置是否可以通过返回键退出
        canBack = true;
        // 设置事件
        rootView.setOnClickListener(v -> dismissImmediately());
        // 显示Markdown文档
        mSharedView.showAssetMarkdown(assetFileName);
        if (!isShowing()) {
            onAttached();
        }
    }

    public void showImageText(Bitmap bitmap, String text) {
        initCenter();
        canBack = true;
        rootView.setOnClickListener(v -> dismissImmediately());
        mSharedView.showImageText(bitmap, text);
        if (!isShowing()) {
            onAttached();
        }
    }

    private interface OnDismissListener {
        void onDismiss();
    }
}
