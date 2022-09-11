package com.jack.bookshelf.view.activity;

import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jack.basemvplib.BitIntentDataManager;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.constant.BookType;
import com.jack.bookshelf.databinding.ActivitySourceEditBinding;
import com.jack.bookshelf.presenter.SourceEditPresenter;
import com.jack.bookshelf.presenter.contract.SourceEditContract;
import com.jack.bookshelf.service.ShareService;
import com.jack.bookshelf.utils.SoftInputUtil;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.adapter.SourceEditAdapter;
import com.jack.bookshelf.view.dialog.SourceLoginDialog;
import com.jack.bookshelf.view.popupwindow.KeyboardToolPop;
import com.jack.bookshelf.view.popupwindow.MoreSettingMenu;
import com.jack.bookshelf.view.popupwindow.PaperAlertDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Book Source Edit Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View, KeyboardToolPop.CallBack {
    public final static int EDIT_SOURCE = 1101;

    private ActivitySourceEditBinding binding;
    private SourceEditAdapter adapter;
    private final List<SourceEdit> sourceEditList = new ArrayList<>();
    private final List<SourceEdit> findEditList = new ArrayList<>();
    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;
    private PopupWindow mSoftKeyboardTool;
    private boolean mIsSoftKeyBoardShowing = false;
    private boolean showFind;
    private final String[] keyHelp = {"@", "&", "|", "%", "/", ":", "[", "]", "(", ")", "{", "}", "<", ">", "\\", "$", "#", "!", ".",
            "href", "src", "textNodes", "xpath", "json", "css", "id", "class", "tag"};

    private MoreSettingMenu moreSettingMenu;

    public static void startThis(Object object, BookSourceBean sourceBean) {
        String key = String.valueOf(System.currentTimeMillis());
        BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            Intent intent = new Intent(activity, SourceEditActivity.class);
            intent.putExtra("data_key", key);
            activity.startActivityForResult(intent, EDIT_SOURCE);
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            Intent intent = new Intent(fragment.getContext(), SourceEditActivity.class);
            intent.putExtra("data_key", key);
            fragment.startActivityForResult(intent, EDIT_SOURCE);
        } else if (object instanceof Context) {
            Context context = (Context) object;
            Intent intent = new Intent(context, SourceEditActivity.class);
            intent.putExtra("data_key", key);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    protected SourceEditContract.Presenter initInjector() {
        return new SourceEditPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            serialNumber = savedInstanceState.getInt("serialNumber");
            enable = savedInstanceState.getBoolean("enable");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putInt("serialNumber", serialNumber);
        outState.putBoolean("enable", enable);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivitySourceEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        String key = this.getIntent().getStringExtra("data_key");
        if (title == null) {
            if (isEmpty(key)) {
                title = getString(R.string.add_book_source);
                bookSourceBean = new BookSourceBean();
            } else {
                title = getString(R.string.edit_book_source);
                bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
                serialNumber = bookSourceBean.getSerialNumber();
                enable = bookSourceBean.getEnable();
            }
        }
        binding.tvTitleBookSource.setText(title);
    }

    @Override
    protected void bindView() {
        initMenu();
        // 输入法帮助字符
        mSoftKeyboardTool = new KeyboardToolPop(this, Arrays.asList(keyHelp), this);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
        adapter = new SourceEditAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.reSetData(sourceEditList);
        setText(bookSourceBean);
    }

    /**
     * 初始化一级菜单
     */
    private void initMenu() {
        moreSettingMenu = MoreSettingMenu.builder(this)
                .setMenu(R.array.more_setting_menu_source_edit, R.array.icon_more_setting_menu_source_edit)
                .setOnclick(position -> {
                    switch (position) {
                        case 0:
                            BookSourceBean bookSourceBean = getBookSource(true);
                            if (!isEmpty(bookSourceBean.getLoginUrl())) {
                                if (isEmpty(bookSourceBean.getLoginUi())) {
                                    SourceLoginActivity.startThis(this, getBookSource(true));
                                } else {
                                    SourceLoginDialog.Companion.start(
                                            getSupportFragmentManager(),
                                            bookSourceBean.getBookSourceUrl()
                                    );
                                }
                            } else {
                                toast(R.string.source_no_login);
                            }
                            break;
                        case 1:
                            PaperAlertDialog.builder(this)
                                    .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                                    .setTitle(R.string.if_copy_find_content)
                                    .setNegativeButton(R.string.no)
                                    .setPositiveButton(R.string.yes)
                                    .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                                        @Override
                                        public void forNegativeButton() { mPresenter.copySource(getBookSourceStr(false)); }

                                        @Override
                                        public void forPositiveButton() { mPresenter.copySource(getBookSourceStr(true)); }
                                    }).show(binding.getRoot());
                            break;
                        case 2:
                            mPresenter.pasteSource();
                            break;
                        case 3:
                            shareText(getBookSourceStr(true));
                            break;
                        case 4:
                            ShareService.startThis(this, Collections.singletonList(getBookSource(true)));
                            break;
                        case 5:
                            // 规则说明
                            openRuleSummary();
                            break;
                    }
                });
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        // 切换按钮
        binding.tvEditFind.setOnClickListener(v -> {
            binding.recyclerView.clearFocus();
            if (showFind) {
                adapter.reSetData(sourceEditList);
                binding.tvEditFind.setText(R.string.edit_find);
            } else {
                adapter.reSetData(findEditList);
                binding.tvEditFind.setText(R.string.back);
            }
            showFind = !showFind;
            binding.recyclerView.scrollToPosition(0);
        });
        // 返回
        binding.ivBackSourceEdit.setOnClickListener(v -> {
            SoftInputUtil.hideIMM(getCurrentFocus());
            if (!back()) { finish(); }
        });
        // 保存
        binding.ivSaveSourceEdit.setOnClickListener(v -> {
            if (canSaveBookSource()) {
                mPresenter.saveSource(getBookSource(true), bookSourceBean)
                        .subscribe(new MyObserver<>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                bookSourceBean = getBookSource(true);
                                toast(R.string.save_success);
                                setResult(RESULT_OK);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                toast(e.getLocalizedMessage());
                            }
                        });
            }
        });
        // 调试
        binding.ivDebugSourceEdit.setOnClickListener(v -> {
            if (canSaveBookSource()) {
                mPresenter.saveSource(getBookSource(true), bookSourceBean)
                        .subscribe(new MyObserver<>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                bookSourceBean = getBookSource(true);
                                setResult(RESULT_OK);
                                SourceDebugActivity.startThis(SourceEditActivity.this, getBookSource(true).getBookSourceUrl());
                            }

                            @Override
                            public void onError(Throwable e) {
                                toast(e.getLocalizedMessage());
                            }
                        });
            }
        });
        // 更多菜单
        binding.ivMoreSettingsSourceEdit.setOnClickListener(v -> {
            if (!moreSettingMenu.isShowing()) {
                moreSettingMenu.show(binding.getRoot(), binding.ivMoreSettingsSourceEdit);
            }
        });
    }

    /**
     * 判断是否可以保存书源
     */
    private boolean canSaveBookSource() {
        SoftInputUtil.hideIMM(binding.recyclerView);
        binding.recyclerView.clearFocus();
        BookSourceBean bookSourceBean = getBookSource(true);
        if (isEmpty(bookSourceBean.getBookSourceName()) || isEmpty(bookSourceBean.getBookSourceUrl())) {
            toast(R.string.non_null_source_name_url, Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    @Override
    public String getBookSourceStr(boolean hasFind) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        return gson.toJson(getBookSource(hasFind));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setText(BookSourceBean bookSourceBean) {
        sourceEditList.clear();
        findEditList.clear();
        adapter.notifyDataSetChanged();
        sourceEditList.add(new SourceEdit("bookSourceUrl", bookSourceBean.getBookSourceUrl(), R.string.book_source_url));
        sourceEditList.add(new SourceEdit("bookSourceName", bookSourceBean.getBookSourceName(), R.string.book_source_name));
        sourceEditList.add(new SourceEdit("bookSourceGroup", bookSourceBean.getBookSourceGroup(), R.string.book_source_group));
        sourceEditList.add(new SourceEdit("loginUrl", bookSourceBean.getLoginUrl(), R.string.book_source_login_url));
        sourceEditList.add(new SourceEdit("loginUi", bookSourceBean.getLoginUi(), R.string.login_ui));
        sourceEditList.add(new SourceEdit("loginCheckJs", bookSourceBean.getLoginCheckJs(), R.string.login_check_js));
        //搜索
        sourceEditList.add(new SourceEdit("ruleSearchUrl", bookSourceBean.getRuleSearchUrl(), R.string.rule_search_url));
        sourceEditList.add(new SourceEdit("ruleSearchList", bookSourceBean.getRuleSearchList(), R.string.rule_search_list));
        sourceEditList.add(new SourceEdit("ruleSearchName", bookSourceBean.getRuleSearchName(), R.string.rule_search_name));
        sourceEditList.add(new SourceEdit("ruleSearchAuthor", bookSourceBean.getRuleSearchAuthor(), R.string.rule_search_author));
        sourceEditList.add(new SourceEdit("ruleSearchKind", bookSourceBean.getRuleSearchKind(), R.string.rule_search_kind));
        sourceEditList.add(new SourceEdit("ruleSearchLastChapter", bookSourceBean.getRuleSearchLastChapter(), R.string.rule_search_last_chapter));
        sourceEditList.add(new SourceEdit("ruleSearchIntroduce", bookSourceBean.getRuleSearchIntroduce(), R.string.rule_search_introduce));
        sourceEditList.add(new SourceEdit("ruleSearchCoverUrl", bookSourceBean.getRuleSearchCoverUrl(), R.string.rule_search_cover_url));
        sourceEditList.add(new SourceEdit("ruleSearchNoteUrl", bookSourceBean.getRuleSearchNoteUrl(), R.string.rule_search_note_url));
        //详情页
        sourceEditList.add(new SourceEdit("ruleBookUrlPattern", bookSourceBean.getRuleBookUrlPattern(), R.string.book_url_pattern));
        sourceEditList.add(new SourceEdit("ruleBookInfoInit", bookSourceBean.getRuleBookInfoInit(), R.string.rule_book_info_init));
        sourceEditList.add(new SourceEdit("ruleBookName", bookSourceBean.getRuleBookName(), R.string.rule_book_name));
        sourceEditList.add(new SourceEdit("ruleBookAuthor", bookSourceBean.getRuleBookAuthor(), R.string.rule_book_author));
        sourceEditList.add(new SourceEdit("ruleCoverUrl", bookSourceBean.getRuleCoverUrl(), R.string.rule_cover_url));
        sourceEditList.add(new SourceEdit("ruleIntroduce", bookSourceBean.getRuleIntroduce(), R.string.rule_introduce));
        sourceEditList.add(new SourceEdit("ruleBookKind", bookSourceBean.getRuleBookKind(), R.string.rule_book_kind));
        sourceEditList.add(new SourceEdit("ruleBookLastChapter", bookSourceBean.getRuleBookLastChapter(), R.string.rule_book_last_chapter));
        sourceEditList.add(new SourceEdit("ruleChapterUrl", bookSourceBean.getRuleChapterUrl(), R.string.rule_chapter_list_url));
        //目录页
        sourceEditList.add(new SourceEdit("ruleChapterUrlNext", bookSourceBean.getRuleChapterUrlNext(), R.string.rule_chapter_list_url_next));
        sourceEditList.add(new SourceEdit("ruleChapterList", bookSourceBean.getRuleChapterList(), R.string.rule_chapter_list));
        sourceEditList.add(new SourceEdit("ruleChapterName", bookSourceBean.getRuleChapterName(), R.string.rule_chapter_name));
        sourceEditList.add(new SourceEdit("ruleContentUrl", bookSourceBean.getRuleContentUrl(), R.string.rule_content_url));
        sourceEditList.add(new SourceEdit("ruleChapterVip", bookSourceBean.getRuleChapterVip(), R.string.rule_vip));
        sourceEditList.add(new SourceEdit("ruleChapterPay", bookSourceBean.getRuleChapterPay(), R.string.rule_pay));
        //正文页
        sourceEditList.add(new SourceEdit("ruleContentUrlNext", bookSourceBean.getRuleContentUrlNext(), R.string.rule_content_url_next));
        sourceEditList.add(new SourceEdit("ruleBookContent", bookSourceBean.getRuleBookContent(), R.string.rule_book_content));
        sourceEditList.add(new SourceEdit("ruleBookContentReplace", bookSourceBean.getRuleBookContentReplace(), R.string.rule_book_content_replace));
        sourceEditList.add(new SourceEdit("httpUserAgent", bookSourceBean.getHttpUserAgent(), R.string.source_user_agent));
        //发现
        findEditList.add(new SourceEdit("ruleFindUrl", bookSourceBean.getRuleFindUrl(), R.string.rule_find_url));
        findEditList.add(new SourceEdit("ruleFindList", bookSourceBean.getRuleFindList(), R.string.rule_find_list));
        findEditList.add(new SourceEdit("ruleFindName", bookSourceBean.getRuleFindName(), R.string.rule_find_name));
        findEditList.add(new SourceEdit("ruleFindAuthor", bookSourceBean.getRuleFindAuthor(), R.string.rule_find_author));
        findEditList.add(new SourceEdit("ruleFindKind", bookSourceBean.getRuleFindKind(), R.string.rule_find_kind));
        findEditList.add(new SourceEdit("ruleFindIntroduce", bookSourceBean.getRuleFindIntroduce(), R.string.rule_find_introduce));
        findEditList.add(new SourceEdit("ruleFindLastChapter", bookSourceBean.getRuleFindLastChapter(), R.string.rule_find_last_chapter));
        findEditList.add(new SourceEdit("ruleFindCoverUrl", bookSourceBean.getRuleFindCoverUrl(), R.string.rule_find_cover_url));
        findEditList.add(new SourceEdit("ruleFindNoteUrl", bookSourceBean.getRuleFindNoteUrl(), R.string.rule_find_note_url));
        // 是否未发现页面
        if (showFind) {
            adapter.reSetData(findEditList);
        } else {
            adapter.reSetData(sourceEditList);
        }
        binding.cbIsAudio.setChecked(Objects.equals(bookSourceBean.getBookSourceType(), BookType.AUDIO));
        binding.cbIsEnable.setChecked(bookSourceBean.getEnable());
    }

    private BookSourceBean getBookSource(boolean hasFind) {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        for (SourceEdit sourceEdit : sourceEditList) {
            switch (sourceEdit.getKey()) {
                case "bookSourceUrl":
                    bookSourceBeanN.setBookSourceUrl(sourceEdit.value);
                    break;
                case "bookSourceName":
                    bookSourceBeanN.setBookSourceName(sourceEdit.value);
                    break;
                case "bookSourceGroup":
                    bookSourceBeanN.setBookSourceGroup(sourceEdit.value);
                    break;
                case "loginUrl":
                    bookSourceBeanN.setLoginUrl(sourceEdit.value);
                    break;
                case "loginUi":
                    bookSourceBeanN.setLoginUi(sourceEdit.value);
                    break;
                case "loginCheckJs":
                    bookSourceBeanN.setLoginCheckJs(sourceEdit.value);
                    break;
                case "ruleSearchUrl":
                    bookSourceBeanN.setRuleSearchUrl(sourceEdit.value);
                    break;
                case "ruleSearchList":
                    bookSourceBeanN.setRuleSearchList(sourceEdit.value);
                    break;
                case "ruleSearchName":
                    bookSourceBeanN.setRuleSearchName(sourceEdit.value);
                    break;
                case "ruleSearchAuthor":
                    bookSourceBeanN.setRuleSearchAuthor(sourceEdit.value);
                    break;
                case "ruleSearchKind":
                    bookSourceBeanN.setRuleSearchKind(sourceEdit.value);
                    break;
                case "ruleSearchIntroduce":
                    bookSourceBeanN.setRuleSearchIntroduce(sourceEdit.value);
                    break;
                case "ruleSearchLastChapter":
                    bookSourceBeanN.setRuleSearchLastChapter(sourceEdit.value);
                    break;
                case "ruleSearchCoverUrl":
                    bookSourceBeanN.setRuleSearchCoverUrl(sourceEdit.value);
                    break;
                case "ruleSearchNoteUrl":
                    bookSourceBeanN.setRuleSearchNoteUrl(sourceEdit.value);
                    break;
                case "ruleBookUrlPattern":
                    bookSourceBeanN.setRuleBookUrlPattern(sourceEdit.value);
                    break;
                case "ruleBookInfoInit":
                    bookSourceBeanN.setRuleBookInfoInit(sourceEdit.value);
                    break;
                case "ruleBookName":
                    bookSourceBeanN.setRuleBookName(sourceEdit.value);
                    break;
                case "ruleBookAuthor":
                    bookSourceBeanN.setRuleBookAuthor(sourceEdit.value);
                    break;
                case "ruleCoverUrl":
                    bookSourceBeanN.setRuleCoverUrl(sourceEdit.value);
                    break;
                case "ruleIntroduce":
                    bookSourceBeanN.setRuleIntroduce(sourceEdit.value);
                    break;
                case "ruleBookKind":
                    bookSourceBeanN.setRuleBookKind(sourceEdit.value);
                    break;
                case "ruleBookLastChapter":
                    bookSourceBeanN.setRuleBookLastChapter(sourceEdit.value);
                    break;
                case "ruleChapterUrl":
                    bookSourceBeanN.setRuleChapterUrl(sourceEdit.value);
                    break;
                case "ruleChapterUrlNext":
                    bookSourceBeanN.setRuleChapterUrlNext(sourceEdit.value);
                    break;
                case "ruleChapterList":
                    bookSourceBeanN.setRuleChapterList(sourceEdit.value);
                    break;
                case "ruleChapterName":
                    bookSourceBeanN.setRuleChapterName(sourceEdit.value);
                    break;
                case "ruleVip":
                    bookSourceBeanN.setRuleChapterVip(sourceEdit.value);
                    break;
                case "rulePay":
                    bookSourceBeanN.setRuleChapterPay(sourceEdit.value);
                    break;
                case "ruleContentUrl":
                    bookSourceBeanN.setRuleContentUrl(sourceEdit.value);
                    break;
                case "ruleContentUrlNext":
                    bookSourceBeanN.setRuleContentUrlNext(sourceEdit.value);
                    break;
                case "ruleBookContent":
                    bookSourceBeanN.setRuleBookContent(sourceEdit.value);
                    break;
                case "ruleBookContentReplace":
                    bookSourceBeanN.setRuleBookContentReplace(sourceEdit.value);
                    break;
                case "httpUserAgent":
                    bookSourceBeanN.setHttpUserAgent(sourceEdit.value);
                    break;
            }
        }
        if (hasFind) {
            for (SourceEdit sourceEdit : findEditList) {
                switch (sourceEdit.getKey()) {
                    case "ruleFindUrl":
                        bookSourceBeanN.setRuleFindUrl(sourceEdit.value);
                        break;
                    case "ruleFindList":
                        bookSourceBeanN.setRuleFindList(sourceEdit.value);
                        break;
                    case "ruleFindName":
                        bookSourceBeanN.setRuleFindName(sourceEdit.value);
                        break;
                    case "ruleFindAuthor":
                        bookSourceBeanN.setRuleFindAuthor(sourceEdit.value);
                        break;
                    case "ruleFindKind":
                        bookSourceBeanN.setRuleFindKind(sourceEdit.value);
                        break;
                    case "ruleFindIntroduce":
                        bookSourceBeanN.setRuleFindIntroduce(sourceEdit.value);
                        break;
                    case "ruleFindLastChapter":
                        bookSourceBeanN.setRuleFindLastChapter(sourceEdit.value);
                        break;
                    case "ruleFindCoverUrl":
                        bookSourceBeanN.setRuleFindCoverUrl(sourceEdit.value);
                        break;
                    case "ruleFindNoteUrl":
                        bookSourceBeanN.setRuleFindNoteUrl(sourceEdit.value);
                        break;
                }
            }
        }
        bookSourceBeanN.setSerialNumber(serialNumber);
        bookSourceBeanN.setEnable(binding.cbIsEnable.isChecked());
        bookSourceBeanN.setBookSourceType(binding.cbIsAudio.isChecked() ? BookType.AUDIO : null);
        return bookSourceBeanN;
    }

    private void openRuleSummary() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.source_rule_url)));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open, Toast.LENGTH_LONG);
        }
    }

    private void shareText(String text) {
        try {
            Intent textIntent = new Intent(Intent.ACTION_SEND);
            textIntent.setType("text/plain");
            textIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(textIntent, "Source Share"));
        } catch (Exception e) {
            toast(R.string.can_not_share, Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (back()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSoftKeyboardTool != null) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private boolean back() {
        if (bookSourceBean == null) {
            bookSourceBean = new BookSourceBean();
        }
        if (!getBookSource(true).equals(bookSourceBean)) {
            PaperAlertDialog.builder(this)
                    .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                    .setTitle(R.string.exit_no_save)
                    .setNegativeButton(R.string.no)
                    .setPositiveButton(R.string.yes)
                    .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                        @Override
                        public void forNegativeButton() { finish(); }

                        @Override
                        public void forPositiveButton() {}
                    }).show(binding.getRoot());
            return true;
        }
        return false;
    }

    @Override
    public void sendText(@NotNull String txt) {
        if (isEmpty(txt)) return;
        View view = getWindow().getDecorView().findFocus();
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            Editable edit = editText.getEditableText(); //获取EditText的文字
            if (start < 0 || start >= edit.length()) {
                edit.append(txt);
            } else {
                edit.replace(start, end, txt);  //光标所在位置插入文字
            }
        }
    }

    private void showKeyboardTopPopupWindow() {
        if (isFinishing()) return;
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            return;
        }
        if (mSoftKeyboardTool != null & !this.isFinishing()) {
            mSoftKeyboardTool.showAtLocation(binding.llContent, Gravity.BOTTOM, 0, 0);
        }
    }

    private void closePopupWindow() {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前页面窗口的显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = SoftInputUtil.getScreenHeight(SourceEditActivity.this);
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean preShowing = mIsSoftKeyBoardShowing;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsSoftKeyBoardShowing = true; // 超过屏幕五分之一则表示弹出了输入法
                binding.recyclerView.setPadding(0, 0, 0, 100);
                showKeyboardTopPopupWindow();
            } else {
                mIsSoftKeyBoardShowing = false;
                binding.recyclerView.setPadding(0, 0, 0, 0);
                if (preShowing) {
                    closePopupWindow();
                }
            }
        }
    }

    public static class SourceEdit {
        private String key;
        private String value;
        private final int hint;

        SourceEdit(String key, String value, int hint) {
            this.key = key;
            this.value = value;
            this.hint = hint;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getHint() {
            return hint;
        }
    }
}
