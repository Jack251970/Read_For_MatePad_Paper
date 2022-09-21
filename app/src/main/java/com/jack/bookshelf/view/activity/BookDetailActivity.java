package com.jack.bookshelf.view.activity;

import static com.jack.bookshelf.presenter.BookDetailPresenter.FROM_BOOKSHELF;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.AppActivityManager;
import com.jack.basemvplib.BitIntentDataManager;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivityBookDetailBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.presenter.BookDetailPresenter;
import com.jack.bookshelf.presenter.ReadBookPresenter;
import com.jack.bookshelf.presenter.contract.BookDetailContract;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.widget.dialog.modialog.ChangeSourceDialog;
import com.jack.bookshelf.widget.dialog.modialog.MoDialogHUD;
import com.jack.bookshelf.widget.menu.MoreSettingMenuBookDetail;
import com.jack.bookshelf.widget.menu.SelectMenu;

/**
 * Book Detail Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookDetailActivity extends MBaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {

    private ActivityBookDetailBinding binding;
    private MoDialogHUD moDialogHUD;
    private String author;
    private BookShelfBean bookShelfBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    protected BookDetailContract.Presenter initInjector() {
        return new BookDetailPresenter();
    }

    @Override
    protected void onCreateActivity() {
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        mPresenter.initData(getIntent());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String dataKey = String.valueOf(System.currentTimeMillis());
        getIntent().putExtra("openFrom", FROM_BOOKSHELF);
        getIntent().putExtra("data_key", dataKey);
        BitIntentDataManager.getInstance().putData(dataKey, mPresenter.getBookShelf());
    }

    @Override
    protected void bindView() {
        // 弹窗
        moDialogHUD = new MoDialogHUD(this);
        // 内容简介
        binding.tvIntro.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            // 启用书籍信息编辑
            binding.ivEditBook.setVisibility(View.VISIBLE);
            updateView();
        } else {
            if (mPresenter.getSearchBook() == null) return;
            // 禁用书籍信息编辑
            binding.ivEditBook.setVisibility(View.GONE);
            SearchBookBean searchBookBean = mPresenter.getSearchBook();
            // 加载封面
            upImageView(searchBookBean.getCoverUrl(), searchBookBean.getName(), searchBookBean.getAuthor());
            // 显示书名
            binding.tvName.setText(searchBookBean.getName());
            // 显示作者
            author = searchBookBean.getAuthor();
            binding.tvAuthor.setText(TextUtils.isEmpty(author) ? getString(R.string.unknown) : author);
            // 显示来源
            String origin = TextUtils.isEmpty(searchBookBean.getOrigin()) ? getString(R.string.unknown) : searchBookBean.getOrigin();
            binding.tvOrigin.setText(origin);
            // 显示最新阅读进度
            binding.tvChapter.setText(searchBookBean.getLastChapter());
            // 显示简介
            binding.tvIntro.setText(StringUtils.formatHtml2Intor(searchBookBean.getIntroduce()));
            // 更新按钮文字
            updateButtonText(R.string.add_to_shelf, R.string.start_read);
            binding.tvRead.setOnClickListener(v -> mPresenter.addToBookShelf());
            // 显示加载信息
            binding.tvIntro.setVisibility(View.INVISIBLE);
            binding.tvLoading.setVisibility(View.VISIBLE);
            binding.tvLoading.setText(R.string.is_loading);
            binding.tvLoading.setOnClickListener(null);
        }
    }

    /**
     * 从书架中打开
     */
    @Override
    public void updateView() {
        bookShelfBean = mPresenter.getBookShelf();
        BookInfoBean bookInfoBean;
        if (null != bookShelfBean) {
            // 在线书籍显示更多选项菜单
            if (BookShelfBean.LOCAL_TAG.equals(bookShelfBean.getTag())) {
                binding.ivMenu.setVisibility(View.GONE);
            } else {
                binding.ivMenu.setVisibility(View.VISIBLE);
            }
            bookInfoBean = bookShelfBean.getBookInfoBean();
            binding.tvName.setText(bookInfoBean.getName());
            author = bookInfoBean.getAuthor();
            binding.tvAuthor.setText(TextUtils.isEmpty(author) ? getString(R.string.unknown) : author);
            // 更新分组信息
            binding.tvGroups.setText(getResources().getStringArray(R.array.book_groups)[bookShelfBean.getGroup()]);
            if (mPresenter.getInBookShelf()) { // 书籍已经在书架内
                binding.tvChapter.setText(bookShelfBean.getDurChapterName()); // last
                // 更新按钮文字
                updateButtonText(R.string.remove_from_bookshelf, R.string.continue_read);
                binding.tvShelf.setOnClickListener(v -> mPresenter.removeFromBookShelf());
            } else {    // 书籍不在书架内
                if (!TextUtils.isEmpty(bookShelfBean.getLastChapterName())) {
                    binding.tvChapter.setText(bookShelfBean.getLastChapterName()); // last
                }
                // 更新按钮文字
                updateButtonText(R.string.add_to_shelf, R.string.start_read);
                binding.tvShelf.setOnClickListener(v -> mPresenter.addToBookShelf());
            }
            binding.tvIntro.setText(StringUtils.formatHtml2Intor(bookInfoBean.getIntroduce()));
            if (binding.tvIntro.getVisibility() != View.VISIBLE) {
                binding.tvIntro.setVisibility(View.VISIBLE);
            }
            String origin = bookInfoBean.getOrigin();
            if (!TextUtils.isEmpty(origin)) {
                binding.ivWeb.setVisibility(View.VISIBLE);
                binding.tvOrigin.setText(origin);
            } else {
                binding.ivWeb.setVisibility(View.INVISIBLE);
                binding.tvOrigin.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(bookShelfBean.getCustomCoverPath())) {
                upImageView(bookShelfBean.getCustomCoverPath(), bookInfoBean.getName(), bookInfoBean.getAuthor());
            } else {
                upImageView(bookInfoBean.getCoverUrl(), bookInfoBean.getName(), bookInfoBean.getAuthor());
            }
            if (bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                binding.tvChangeOrigin.setVisibility(View.INVISIBLE);
            } else {
                binding.tvChangeOrigin.setVisibility(View.VISIBLE);
            }
            upChapterSizeTv();
        }
        binding.tvLoading.setVisibility(View.GONE);
        binding.tvLoading.setOnClickListener(null);
    }

    /**
     * 显示更多选项菜单
     */
    private void showMoreSetting() {
        MoreSettingMenuBookDetail moreSettingMenuBookDetail = new MoreSettingMenuBookDetail(this
                , mPresenter.getBookShelf().getAllowUpdate()
                , new MoreSettingMenuBookDetail.OnItemClickListener() {
            @Override
            public void refreshBook() { refresh(); }

            @Override
            public void manageUpdate() {
                mPresenter.getBookShelf().setAllowUpdate(!mPresenter.getBookShelf().getAllowUpdate());
                mPresenter.addToBookShelf();
            }

            @Override
            public void editBookSource() {
                BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(mPresenter.getBookShelf().getTag());
                if (sourceBean != null) {
                    SourceEditActivity.startThis(BookDetailActivity.this, sourceBean);
                }
            }

            @Override
            public void copyBookUrl() {
                ClipboardManager clipboard = (ClipboardManager) BookDetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, mPresenter.getBookShelf().getNoteUrl());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clipData);
                    toast(R.string.copy_complete);
                }
            }
        });
        moreSettingMenuBookDetail.show(binding.getRoot(), binding.ivMenu);
    }

    /**
     * 加载书籍失败
     */
    @Override
    public void getBookShelfError() {
        binding.tvLoading.setVisibility(View.VISIBLE);
        binding.tvLoading.setText(R.string.load_error_retry);
        binding.tvLoading.setOnClickListener(v -> {
            binding.tvLoading.setText(R.string.is_loading);
            binding.tvLoading.setOnClickListener(null);
            mPresenter.getBookShelfInfo();
        });
    }

    /**
     * 更新底部按钮文字
     */
    private void updateButtonText(int strId1, int strId2) {
        binding.tvShelf.setText(strId1);
        binding.tvRead.setText(strId2);
    }

    /**
     * 更新封面
     */
    private void upImageView(String path, String name, String author) {
        binding.ivCover.load(path, name, author);
    }

    /**
     * 显示正在加载中...
     */
    private void refresh() {
        binding.tvLoading.setVisibility(View.VISIBLE);
        binding.tvLoading.setText(R.string.is_loading);
        binding.tvLoading.setOnClickListener(null);
        mPresenter.getBookShelf().getBookInfoBean().setBookInfoHtml(null);
        mPresenter.getBookShelf().getBookInfoBean().setChapterListHtml(null);
        mPresenter.getBookShelfInfo();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void bindEvent() {
        // ToolBar点击事件
        binding.ivBack.setOnClickListener(v -> finish());
        binding.ivEditBook.setOnClickListener(v -> {
            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            BookInfoEditActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
            }
        });
        binding.ivMenu.setOnClickListener(v -> showMoreSetting());
        // 作者名字点击事件
        binding.tvName.setOnClickListener(v -> {
            if (bookShelfBean == null) return;
            if (TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getName())) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, bookShelfBean.getBookInfoBean().getName());
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, bookShelfBean.getBookInfoBean().getName());
            }
            finish();
        });
        binding.tvToc.setOnClickListener(v -> ChapterListActivity.startThis(this, mPresenter.getBookShelf(), mPresenter.getChapterList()));
        binding.tvChangeOrigin.setOnClickListener(view ->
                ChangeSourceDialog.builder(BookDetailActivity.this, mPresenter.getBookShelf())
                        .setCallback(searchBookBean -> {
                            binding.tvOrigin.setText(searchBookBean.getOrigin());
                            binding.tvLoading.setVisibility(View.VISIBLE);
                            binding.tvLoading.setText(R.string.is_loading);
                            binding.tvLoading.setOnClickListener(null);
                            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                                mPresenter.changeBookSource(searchBookBean);
                            } else {
                                mPresenter.initBookFormSearch(searchBookBean);
                                mPresenter.getBookShelfInfo();
                            }
                        }).show());
        binding.tvRead.setOnClickListener(v -> readBook());
        // 封面点击事件
        binding.ivCover.setOnClickListener(null);
        // 作者点击事件
        binding.tvAuthor.setOnClickListener(view -> {
            if (TextUtils.isEmpty(author)) return;
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, author);
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, author);
            }
            finish();
        });
        // 分组点击事件
        binding.tvManageGroups.setOnClickListener(v ->
                SelectMenu.builder(this)
                .setTitle(getString(R.string.manage_groups))
                .setBottomButton(getString(R.string.cancel))
                .setMenu(getResources().getStringArray(R.array.book_groups),
                        bookShelfBean.getGroup())
                .setListener(new SelectMenu.OnItemClickListener() {
                    @Override
                    public void forBottomButton() {}

                    @Override
                    public void forListItem(int lastChoose, int position) {
                        if (position != lastChoose) {
                            mPresenter.getBookShelf().setGroup(position);
                        }
                        if (mPresenter.getInBookShelf()) {
                            mPresenter.addToBookShelf();
                        }
                    }
                }).show(binding.getRoot()));
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (mPresenter.getOpenFrom() == BookDetailPresenter.FROM_SEARCH) {
            // 网络请求
            mPresenter.getBookShelfInfo();
        }
    }

    @Override
    public void readBook() {
        if (!mPresenter.getInBookShelf()) {
            BookshelfHelp.saveBookToShelf(mPresenter.getBookShelf());
            if (mPresenter.getChapterList() != null)
                DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(mPresenter.getChapterList());
        }
        Intent intent = new Intent(BookDetailActivity.this, ReadBookActivity.class);
        intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
        intent.putExtra("inBookshelf", mPresenter.getInBookShelf());
        String key = String.valueOf(System.currentTimeMillis());
        String bookKey = "book" + key;
        intent.putExtra("bookKey", bookKey);
        BitIntentDataManager.getInstance().putData(bookKey, mPresenter.getBookShelf().clone());
        startActivity(intent);
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void upChapterSizeTv() {
        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            bookShelfBean.getChapterListSize();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onDestroy() {
        moDialogHUD.dismiss();
        super.onDestroy();
    }
}