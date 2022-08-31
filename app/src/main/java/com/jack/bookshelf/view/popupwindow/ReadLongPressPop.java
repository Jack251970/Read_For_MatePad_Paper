package com.jack.bookshelf.view.popupwindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.jack.bookshelf.databinding.PopReadLongPressBinding;
import com.jack.bookshelf.utils.DensityUtil;

/**
 * Read LongPress Pop Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ReadLongPressPop extends FrameLayout {

    private final PopReadLongPressBinding binding = PopReadLongPressBinding
            .inflate(LayoutInflater.from(getContext()), this, true);
    private OnBtnClickListener clickListener;
    // private ReadBookActivity activity;
    // private final ReadBookControl readBookControl = ReadBookControl.getInstance();

    public ReadLongPressPop(Context context) {
        super(context);
        init(context);
    }

    public ReadLongPressPop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadLongPressPop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding.getRoot().setOnClickListener(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(0,0, getMeasuredWidth(), getMeasuredHeight()),
                DensityUtil.dp2px(getContext(),4), DensityUtil.dp2px(getContext(),4), Path.Direction.CW);
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
    }

    public void setListener(@NonNull OnBtnClickListener clickListener) {
        // this.activity = readBookActivity;
        this.clickListener = clickListener;
        initData();
        bindEvent();
    }

    private void initData() {}

    private void bindEvent() {
        // 复制
        binding.tvCp.setOnClickListener(v -> clickListener.copySelect());
        // 替换
        binding.tvReplace.setOnClickListener(v -> clickListener.replaceSelect());
        // 标记广告
        binding.tvReplaceAd.setOnClickListener(v -> clickListener.replaceSelectAd());
    }

    public interface OnBtnClickListener {
        void copySelect();

        void replaceSelect();

        void replaceSelectAd();
    }
}