package com.jack.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jack.bookshelf.R;
import com.jack.bookshelf.databinding.PopMediaPlayerBinding;
import com.jack.bookshelf.help.glide.ImageLoader;
import com.jack.bookshelf.utils.TimeUtils;
import com.jack.bookshelf.utils.theme.ThemeStore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Media Player Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MediaPlayerPop extends FrameLayout {
    @SuppressLint("ConstantLocale")
    private final DateFormat timeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private final PopMediaPlayerBinding binding = PopMediaPlayerBinding.inflate(LayoutInflater.from(getContext()), this, true);
    private Callback callback;

    public MediaPlayerPop(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MediaPlayerPop(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaPlayerPop(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding.getRoot().setBackgroundColor(ThemeStore.primaryColor(context));
        binding.vwBg.setOnClickListener(null);
        setColor(binding.ivSkipPrevious.getDrawable());
        setColor(binding.ivSkipNext.getDrawable());
        setColor(binding.ivChapter.getDrawable());
        setColor(binding.ivTimer.getDrawable());
        binding.seekBar.setEnabled(false);
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (callback != null) {
                    callback.onStopTrackingTouch(seekBar.getProgress());
                }
            }
        });
    }

    private void setColor(Drawable drawable) {
        drawable.mutate();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setSeekBarEnable(boolean enable) {
        binding.seekBar.setEnabled(enable);
    }

    public void upAudioSize(int audioSize) {
        binding.seekBar.setMax(audioSize);
        binding.tvAllTime.setText(TimeUtils.millis2String(audioSize, timeFormat));
    }

    public void upAudioDur(int audioDur) {
        binding.seekBar.setProgress(audioDur);
        binding.tvDurTime.setText(TimeUtils.millis2String(audioDur, timeFormat));
    }

    public void setLlCoverBgClickListener(OnClickListener onClickListener) {
        binding.llCoverBg.setOnClickListener(onClickListener);
    }

    public void setPlayClickListener(OnClickListener onClickListener) {
        binding.fabPlayStop.setOnClickListener(onClickListener);
    }

    public void setPrevClickListener(OnClickListener onClickListener) {
        binding.ivSkipPrevious.setOnClickListener(onClickListener);
    }

    public void setNextClickListener(OnClickListener onClickListener) {
        binding.ivSkipNext.setOnClickListener(onClickListener);
    }

    public void setIvTimerClickListener(OnClickListener onClickListener) {
        binding.ivTimer.setOnClickListener(onClickListener);
    }

    public void setIvChapterClickListener(OnClickListener onClickListener) {
        binding.ivChapter.setOnClickListener(onClickListener);
    }

    public void setFabReadAloudImage(int id) {
        binding.fabPlayStop.setImageResource(id);
    }

    public void setCover(String coverPath) {
        ImageLoader.INSTANCE.load(getContext(), coverPath)
                .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                        .placeholder(R.drawable.image_cover_default))
                .into(binding.ivCover);
    }

    public interface Callback {
        void onStopTrackingTouch(int dur);
    }
}