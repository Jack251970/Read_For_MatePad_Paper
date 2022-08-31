package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivityReadStyleBinding;
import com.jack.bookshelf.help.ReadBookControl;
import com.jack.bookshelf.help.permission.Permissions;
import com.jack.bookshelf.help.permission.PermissionsCompat;
import com.jack.bookshelf.utils.ActivityExtensionsKt;
import com.jack.bookshelf.utils.BitmapUtil;
import com.jack.bookshelf.utils.ContextExtensionsKt;
import com.jack.bookshelf.utils.MeUtils;
import com.jack.bookshelf.utils.RealPathUtil;
import com.jack.bookshelf.widget.filepicker.picker.FilePicker;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Unit;

/**
 * 阅读样式设置界面
 * Edited by Jack251970
 */

public class ReadStyleActivity extends MBaseActivity<IPresenter> implements ColorPickerDialogListener {
    private final int ResultSelectBg = 103;
    private final int SELECT_TEXT_COLOR = 201;
    private final int SELECT_BG_COLOR = 301;

    private ActivityReadStyleBinding binding;
    private final ReadBookControl readBookControl = ReadBookControl.getInstance();
    private int textDrawableIndex;
    private int textColor;
    private int bgColor;
    private Drawable bgDrawable;
    private int bgCustom;
    private boolean darkStatusIcon;
    private String bgPath;
    private BgImgListAdapter bgImgListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        binding = ActivityReadStyleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.llContent.setPadding(0, ContextExtensionsKt.getStatusBarHeight(this), 0, 0);
        setTextKind(readBookControl);
    }

    @Override
    protected void initImmersionBar() {
        ActivityExtensionsKt.fullScreen(this);
        int flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        flag = flag | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(flag);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        ActivityExtensionsKt.setLightStatusBar(this, darkStatusIcon);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        Intent intent = getIntent();
        textDrawableIndex = intent.getIntExtra("index", 0);
        bgCustom = readBookControl.getBgCustom(textDrawableIndex);
        textColor = readBookControl.getTextColor(textDrawableIndex);
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        bgDrawable = readBookControl.getBgDrawable(textDrawableIndex, getContext(), width, height);
        bgColor = readBookControl.getBgColor(textDrawableIndex);
        darkStatusIcon = readBookControl.getDarkStatusIcon(textDrawableIndex);
        bgPath = readBookControl.getBgPath(textDrawableIndex);
        upText();
        upBg();
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        // 状态栏图标颜色设置
        binding.swDarkStatusIcon.setChecked(darkStatusIcon);
        binding.swDarkStatusIcon.setOnCheckedChangeListener((compoundButton, b) -> {
            darkStatusIcon = b;
            initImmersionBar();
            saveStyle();
        });
        // 文字背景点击事件
        binding.llContent.setOnClickListener((view) -> {
            if (binding.llBottom.getVisibility() == View.GONE) {
                binding.llBottom.setVisibility(View.VISIBLE);
            } else {
                binding.llBottom.setVisibility(View.GONE);
            }
        });
        // 选择文字颜色
        binding.tvSelectTextColor.setOnClickListener(view -> {
            ColorPickerDialog.newBuilder()
                    .setColor(textColor)
                    .setShowAlphaSlider(false)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setDialogId(SELECT_TEXT_COLOR)
                    .show(ReadStyleActivity.this);});
        // 选择背景颜色
        binding.tvSelectBgColor.setOnClickListener(view ->
                ColorPickerDialog.newBuilder()
                        .setColor(bgColor)
                        .setShowAlphaSlider(false)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setDialogId(SELECT_BG_COLOR)
                        .show(ReadStyleActivity.this));
        // 背景图列表
        bgImgListAdapter = new BgImgListAdapter(this);
        bgImgListAdapter.initList();
        binding.bgImgList.setAdapter(bgImgListAdapter);
        binding.bgImgList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i == 0) {
                // 选择背景图片
                selectImage();
            } else {
                // 设置预置背景图片
                bgPath = bgImgListAdapter.getItemAssetsFile(i - 1);
                setAssetsBg(bgPath);
                saveStyle();
            }
        });
        // 选择背景图片
        binding.tvSelectBgImage.setOnClickListener(view -> selectImage());
        // 恢复默认
        binding.tvDefault.setOnClickListener(view -> {
            bgCustom = 0;
            textColor = readBookControl.getDefaultTextColor(textDrawableIndex);
            bgDrawable = readBookControl.getDefaultBgDrawable(textDrawableIndex, this);
            upText();
            upBg();
            saveStyle();
        });
    }

    /**
     * 获取存储权限
     */
    private void selectImage() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.bg_image_per)
                .onGranted((requestCode) -> {
                    selectImageDialog();
                    return Unit.INSTANCE;
                })
                .request();
    }

    /**
     * 进入系统图片格式文件选择
     */
    private void selectImageDialog() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, ResultSelectBg);
            // 在onActivityResult中返回选取的图片
        } catch (Exception e) {
            FilePicker picker = new FilePicker(this, FilePicker.FILE);
            picker.setBackgroundColor(getResources().getColor(R.color.background));
            picker.setTopBackgroundColor(getResources().getColor(R.color.background));
            picker.setItemHeight(30);
            picker.setOnFilePickListener(this::setCustomBg);
            picker.show();
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read_style_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // 菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存配置
     */
    private void saveStyle() {
        readBookControl.setTextColor(textDrawableIndex, textColor);
        readBookControl.setBgCustom(textDrawableIndex, bgCustom);
        readBookControl.setBgColor(textDrawableIndex, bgColor);
        readBookControl.setDarkStatusIcon(textDrawableIndex, darkStatusIcon);
        if (bgCustom == 2 || bgCustom == 3) {
            readBookControl.setBgPath(textDrawableIndex, bgPath);
        }
        readBookControl.initTextDrawableIndex();
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
    }

    private void setTextKind(ReadBookControl readBookControl) {
        binding.tvContent.setTextSize(readBookControl.getTextSize());
    }

    private void upText() {
        binding.tvContent.setTextColor(textColor);
    }

    private void upBg() {
        binding.llContent.setBackground(bgDrawable);
    }

    /**
     * 自定义背景
     */
    public void setCustomBg(String bgPath) {
        try {
            Resources resources = this.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            Bitmap bitmap = BitmapUtil.getFitSampleBitmap(bgPath, width, height);
            bgCustom = 2;
            bgDrawable = new BitmapDrawable(getResources(), bitmap);
            upBg();
        } catch (Exception e) {
            e.printStackTrace();
            toast(e.getMessage(), ERROR);
        }
    }

    public void setAssetsBg(String path) {
        try {
            Resources resources = ReadStyleActivity.this.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;

            Bitmap bitmap = MeUtils.getFitAssetsSampleBitmap(ReadStyleActivity.this.getAssets(), path, width, height);
            bgCustom = 3;
            bgDrawable = new BitmapDrawable(getResources(), bitmap);
            upBg();
        } catch (Exception e) {
            e.printStackTrace();
            toast(e.getMessage(), ERROR);
        }
    }

    /**
     * 图片选择回调函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ResultSelectBg) {
            if (resultCode == RESULT_OK && null != data) {
                try {
                    bgPath = RealPathUtil.getPath(this, data.getData());
                    setCustomBg(bgPath);
                    saveStyle();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Callback that is invoked when a color is selected from the color picker dialog.
     * @param dialogId The dialog id used to create the dialog instance.
     * @param color    The selected color
     */
    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case SELECT_TEXT_COLOR:
                textColor = color;
                upText();
                saveStyle();
                break;
            case SELECT_BG_COLOR:
                bgCustom = 1;
                bgColor = color;
                bgDrawable = new ColorDrawable(bgColor);
                upBg();
                saveStyle();
        }
    }

    /**
     * Callback that is invoked when the color picker dialog was dismissed.
     * @param dialogId The dialog id used to create the dialog instance.
     */
    @Override
    public void onDialogDismissed(int dialogId) {}

    private static class BgImgListAdapter extends BaseAdapter {
        private final Context context;
        private final LayoutInflater mInflater;
        private List<String> assetsFiles;
        final BitmapFactory.Options options = new BitmapFactory.Options();

        BgImgListAdapter(Context context) {
            this.context = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            options.inJustDecodeBounds = false;
            options.inSampleSize = 4;
        }

        void initList() {
            AssetManager am = context.getAssets();
            String[] path;
            try {
                path = am.list("bg");  //获取所有,填入目录获取该目录下所有资源
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            assetsFiles = new ArrayList<>();
            Collections.addAll(assetsFiles, path);
        }

        @Override
        public int getCount() {
            return assetsFiles.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        String getItemAssetsFile(int position) {
            return "bg/" + assetsFiles.get(position);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_read_bg, null);
                holder.mImage = convertView.findViewById(R.id.iv_bg);
                holder.mTitle = convertView.findViewById(R.id.tv_desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            if (position == 0) {
                holder.mTitle.setText(R.string.select_image);
                holder.mTitle.setTextColor(Color.parseColor("#101010"));
                holder.mImage.setImageResource(R.drawable.ic_image);
            } else {
                String path = assetsFiles.get(position - 1);
                holder.mTitle.setText(getFileName(path));
                holder.mTitle.setTextColor(Color.parseColor("#101010"));
                try {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.mImage.getDrawable();
                    //如果图片还未回收，先强制回收该图片
                    if (bitmapDrawable != null && !bitmapDrawable.getBitmap().isRecycled()) {
                        bitmapDrawable.getBitmap().recycle();
                    }
                    //该变现实的图片
                    Bitmap bmp = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), getItemAssetsFile(position - 1), 256, 256);
                    holder.mImage.setImageBitmap(bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.mImage.setImageBitmap(null);
                }
            }
            return convertView;
        }

        String getFileName(String path) {
            int start = path.lastIndexOf("/");
            int end = path.lastIndexOf(".");
            if (end < 0) end = path.length();
            return path.substring(start + 1, end);
        }

        private static class ViewHolder {
            private TextView mTitle ;
            private ImageView mImage;
        }

    }
}
