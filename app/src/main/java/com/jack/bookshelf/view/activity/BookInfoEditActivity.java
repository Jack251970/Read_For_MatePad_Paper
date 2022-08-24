package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivityBookInfoEditBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.permission.Permissions;
import com.jack.bookshelf.help.permission.PermissionsCompat;
import com.jack.bookshelf.utils.RealPathUtil;
import com.jack.bookshelf.utils.SoftInputUtil;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.widget.modialog.MoDialogHUD;

import java.util.Objects;

import kotlin.Unit;

/**
 * Book Info Edit Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookInfoEditActivity extends MBaseActivity<IPresenter> {
    private final int ResultSelectCover = 103;
    private final int ResultEditCover = 104;

    private ActivityBookInfoEditBinding binding;
    private String noteUrl;
    private BookShelfBean book;
    private MoDialogHUD moDialogHUD;

    public static void startThis(Context context, String noteUrl) {
        Intent intent = new Intent(context, BookInfoEditActivity.class);
        intent.putExtra("noteUrl", noteUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString("noteUrl"))) {
            noteUrl = savedInstanceState.getString("noteUrl");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteUrl", noteUrl);
    }

    /**
     * 布局载入
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityBookInfoEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tilBookName.setHint(getString(R.string.book_name));
        binding.tilBookAuthor.setHint(getString(R.string.author));
        binding.tilCoverUrl.setHint(getString(R.string.cover_path));
        binding.tilBookJj.setHint(getString(R.string.book_intro));
        moDialogHUD = new MoDialogHUD(this);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        if (!TextUtils.isEmpty(getIntent().getStringExtra("noteUrl"))) {
            noteUrl = getIntent().getStringExtra("noteUrl");
        }
        if (!TextUtils.isEmpty(noteUrl)) {
            book = BookshelfHelp.getBook(noteUrl);
            if (book != null) {
                binding.tieBookName.setText(book.getBookInfoBean().getName());
                binding.tieBookAuthor.setText(book.getBookInfoBean().getAuthor());
                binding.tieBookJj.setText(book.getBookInfoBean().getIntroduce());
                if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                    binding.tieCoverUrl.setText(book.getBookInfoBean().getCoverUrl());
                } else {
                    binding.tieCoverUrl.setText(book.getCustomCoverPath());
                }
            }
            initCover();
        }
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        super.bindEvent();
        binding.ivBackBookInfoEdit.setOnClickListener(v -> {
            SoftInputUtil.hideIMM(getCurrentFocus());
            finish();
        });
        binding.ivSaveBookInfoEdit.setOnClickListener(v -> saveInfo());
        binding.tvSelectCover.setOnClickListener(view -> selectCover());
        binding.tvChangeCover.setOnClickListener(view -> {
            Intent intent = new Intent(BookInfoEditActivity.this, BookCoverEditActivity.class);
            intent.putExtra("name", book.getBookInfoBean().getName());
            intent.putExtra("author", book.getBookInfoBean().getAuthor());
            startActivityForResult(intent, ResultEditCover);
        });
        binding.tvRefreshCover.setOnClickListener(view -> {
            book.setCustomCoverPath(Objects.requireNonNull(binding.tieCoverUrl.getText()).toString());
            initCover();
        });
    }

    private void selectCover() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.bg_image_per)
                .onGranted((requestCode) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, ResultSelectCover);
                    return Unit.INSTANCE;
                })
                .request();
    }

    private void initCover() {
        if (!this.isFinishing() && book != null) {
            binding.ivCover.load(book.getCoverPath(), book.getName(), book.getAuthor());
        }
    }

    private void saveInfo() {
        if (book != null) {
            book.getBookInfoBean().setName(Objects.requireNonNull(binding.tieBookName.getText()).toString());
            book.getBookInfoBean().setAuthor(Objects.requireNonNull(binding.tieBookAuthor.getText()).toString());
            book.getBookInfoBean().setIntroduce(Objects.requireNonNull(binding.tieBookJj.getText()).toString());
            book.setCustomCoverPath(Objects.requireNonNull(binding.tieCoverUrl.getText()).toString());
            initCover();
            BookshelfHelp.saveBookToShelf(book);
            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, book);
            SoftInputUtil.hideIMM(getCurrentFocus());
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        moDialogHUD.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectCover:
                if (resultCode == RESULT_OK && null != data) {
                    binding.tieCoverUrl.setText(RealPathUtil.getPath(this, data.getData()));
                    book.setCustomCoverPath(Objects.requireNonNull(binding.tieCoverUrl.getText()).toString());
                    initCover();
                }
                break;
            case ResultEditCover:
                if (resultCode == RESULT_OK && null != data) {
                    String url = data.getStringExtra("url");
                    binding.tieCoverUrl.setText(url);
                    book.setCustomCoverPath(url);
                    initCover();
                }
                break;
        }
    }
}
