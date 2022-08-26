package com.jack.bookshelf.view.popupwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.jack.bookshelf.R;
import com.jack.bookshelf.databinding.PopReadMenuBinding;
import com.jack.bookshelf.service.ReadAloudService;

/**
 * 阅读界面->菜单界面（底部）
 * Edited by Jack251970
 */

public class ReadBottomMenu extends FrameLayout {

    private final PopReadMenuBinding binding = PopReadMenuBinding.inflate(LayoutInflater.from(getContext()), this, true);
    private Callback callback;

    public ReadBottomMenu(Context context) {
        super(context);
        init(context);
    }

    public ReadBottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadBottomMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding.vwBg.setOnClickListener(null);
        binding.vwNavigationBar.setOnClickListener(null);
    }

    public void setListener(Callback callback) {
        this.callback = callback;
        bindEvent();
    }

    private void bindEvent() {
        binding.llReadAloudTimer.setOnClickListener(view -> callback.dismiss());
        binding.llFloatingButton.setOnClickListener(view -> callback.dismiss());

        // 阅读进度
        binding.hpbReadProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                callback.skipToPage(seekBar.getProgress());
            }
        });

        // 朗读定时
        binding.fabReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(getContext(), 10));

        // 朗读
        binding.fabReadAloud.setOnClickListener(view -> callback.onMediaButton());

        // 长按停止朗读
        binding.fabReadAloud.setOnLongClickListener(view -> {
            if (ReadAloudService.running) {
                callback.toast(R.string.aloud_stop);
                ReadAloudService.stop(getContext());
            } else {
                callback.toast(R.string.read_aloud);
            }
            return true;
        });

        // 自动翻页
        binding.fabAutoPage.setOnClickListener(view -> callback.autoPage());
        binding.fabAutoPage.setOnLongClickListener(view -> {
            callback.toast(R.string.auto_next_page);
            return true;
        });

        // 上一章
        binding.tvPre.setOnClickListener(view -> callback.skipPreChapter());

        // 下一章
        binding.tvNext.setOnClickListener(view -> callback.skipNextChapter());

        // 目录
        binding.llCatalog.setOnClickListener(view -> callback.openChapterList());

        // 界面
        binding.llFont.setOnClickListener(view -> callback.openReadInterface());

        // 设置
        binding.llSetting.setOnClickListener(view -> callback.openMoreSetting());

        binding.tvReadAloudTimer.setOnClickListener(null);
    }

    public void setFabReadAloudImage(int id) {
        binding.fabReadAloud.setImageResource(id);
    }

    public void setReadAloudTimer(boolean visibility) {
        if (visibility) {
            binding.llReadAloudTimer.setVisibility(VISIBLE);
        } else {
            binding.llReadAloudTimer.setVisibility(GONE);
        }
    }

    public void setReadAloudTimer(String text) {
        binding.tvReadAloudTimer.setText(text);
    }

    public void setFabReadAloudText(String text) {
        binding.fabReadAloud.setContentDescription(text);
    }

    public SeekBar getReadProgress() {
        return binding.hpbReadProgress;
    }

    public void setTvPre(boolean enable) {
        binding.tvPre.setEnabled(enable);
    }

    public void setTvNext(boolean enable) {
        binding.tvNext.setEnabled(enable);
    }

    public void setAutoPage(boolean autoPage) {
        if (autoPage) {
            binding.fabAutoPage.setImageResource(R.drawable.ic_auto_page_stop);
            binding.fabAutoPage.setContentDescription(getContext().getString(R.string.auto_next_page_stop));
        } else {
            binding.fabAutoPage.setImageResource(R.drawable.ic_auto_page);
            binding.fabAutoPage.setContentDescription(getContext().getString(R.string.auto_next_page));
        }
    }

    public interface Callback {
        void skipToPage(int page);

        void onMediaButton();

        void autoPage();

        void skipPreChapter();

        void skipNextChapter();

        void openChapterList();

        void openReadInterface();

        void openMoreSetting();

        void toast(int id);

        void dismiss();
    }

}
