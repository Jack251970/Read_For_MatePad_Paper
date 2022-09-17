package com.jack.bookshelf.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.dao.BookSourceBeanDao;
import com.jack.bookshelf.databinding.ActivityBookSourceBinding;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.help.permission.Permissions;
import com.jack.bookshelf.help.permission.PermissionsCompat;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.presenter.BookSourcePresenter;
import com.jack.bookshelf.presenter.contract.BookSourceContract;
import com.jack.bookshelf.service.ShareService;
import com.jack.bookshelf.utils.ACache;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.IOUtils;
import com.jack.bookshelf.utils.RealPathUtil;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.adapter.BookSourceAdapter;
import com.jack.bookshelf.widget.dialog.InputDialog;
import com.jack.bookshelf.widget.popupwindow.MoreSettingMenu;
import com.jack.bookshelf.widget.dialog.PaperAlertDialog;
import com.jack.bookshelf.widget.popupwindow.SelectMenu;
import com.jack.bookshelf.widget.filepicker.picker.FilePicker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.Unit;

/**
 * Book Source Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookSourceActivity extends MBaseActivity<BookSourceContract.Presenter>
        implements BookSourceContract.View {
    private final int IMPORT_SOURCE = 102;
    private ActivityBookSourceBinding binding;
    private ItemTouchCallback itemTouchCallback;
    private boolean selectAll = true;
    private BookSourceAdapter adapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean isSearch;
    private final SharedPreferences preferences = MApplication.getConfigPreferences();

    private MoreSettingMenu moreSettingMenu;

    public static void startThis(@NonNull Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, BookSourceActivity.class), requestCode);
    }

    @Override
    protected BookSourceContract.Presenter initInjector() {
        return new BookSourcePresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityBookSourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initData() {}

    @Override
    protected void bindView() {
        super.bindView();
        // 初始化一级菜单
        initMenu();
        // 初始化搜索框
        initSearchView();
        // 初始化RecyclerView
        initRecyclerView();
        // 返回
        binding.ivBackBookSource.setOnClickListener(v -> finish());
        // 快速搜索
        binding.ivQuickSearchBookSource.setOnClickListener(v -> {
            List<String> groupList = BookSourceManager.getGroupList();
            groupList.add(0,getString(R.string.enabled_book_source));
            SelectMenu.builder(getContext())
                    .setTitle(getString(R.string.quick_search))
                    .setBottomButton(getString(R.string.cancel))
                    .setMenu(groupList)
                    .setListener(new SelectMenu.OnItemClickListener() {
                        @Override
                        public void forBottomButton() {}

                        @Override
                        public void forListItem(int lastChoose, int position) {
                            if (position == 0) {
                                binding.searchView.setQuery("enabled", false);
                            } else {
                                binding.searchView.setQuery(groupList.get(position), true);
                            }
                        }
                    }).show(binding.getRoot());
        });
        // 全选
        binding.ivSelectAllBookSource.setOnClickListener(v -> selectAllDataS());
        // 书源排序
        binding.ivUpGroupBookSource.setOnClickListener(v ->
                SelectMenu.builder(this)
                    .setTitle(getString(R.string.book_source_sequence))
                    .setBottomButton(getString(R.string.cancel))
                    .setMenu(getResources().getStringArray(R.array.book_source_sequence), getSort())
                    .setListener(new SelectMenu.OnItemClickListener() {
                        @Override
                        public void forBottomButton() {}

                        @Override
                        public void forListItem(int lastChoose, int position) {
                            if (lastChoose != position) {
                                switch (position) {
                                    case 0:
                                        upSourceSort(0);
                                        break;
                                    case 1:
                                        upSourceSort(1);
                                        break;
                                    case 2:
                                        upSourceSort(2);
                                        break;
                                }
                            }
                        }
                    }).show(binding.getRoot()));
        // 更多选项
        binding.ivMoreSettingsBookSource.setOnClickListener(v -> {
            if (!moreSettingMenu.isShowing()) {
                moreSettingMenu.show(binding.getRoot(), binding.ivMoreSettingsBookSource);
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        refreshBookSource();
    }

    /**
     * 初始化一级菜单
     */
    private void initMenu() {
        moreSettingMenu = MoreSettingMenu.builder(this)
                .setMenu(R.array.more_setting_menu_book_source, R.array.icon_more_setting_menu_book_source)
                .setOnclick(position -> {
                    switch (position) {
                        case 0:
                            addBookSource();
                            break;
                        case 1:
                            selectBookSourceFile();
                            break;
                        case 2:
                            importBookSourceOnLine();
                            break;
                        case 3:
                            importBookSourceDefault();
                            break;
                        case 4:
                            revertSelection();
                            break;
                        case 5:
                            deleteSelectDialog();
                            break;
                        case 6:
                            mPresenter.checkBookSource(adapter.getSelectDataList());
                            break;
                        case 7:
                            ShareService.startThis(this, adapter.getSelectDataList());
                            break;
                    }
                });
    }

    /**
     * 初始化搜索框
     */
    private void initSearchView() {
        mSearchAutoComplete = binding.searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setTextSize(16);
        ImageView closeButton = binding.searchView.findViewById(R.id.search_close_btn);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setImageResource(R.drawable.ic_close);
        binding.searchView.setQueryHint(getString(R.string.search_book_source));
        binding.searchView.onActionViewExpanded();
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSearch = !TextUtils.isEmpty(newText);
                refreshBookSource();
                return false;
            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        adapter = new BookSourceAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
        setDragEnable(getSort());
    }

    private void setDragEnable(int sort) {
        if (itemTouchCallback == null) { return; }
        adapter.setSort(sort);
        itemTouchCallback.setDragEnable(sort == 0);
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            if (!bookSourceBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        AsyncTask.execute(() -> DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(adapter.getDataList()));
        setResult(RESULT_OK);
    }

    private void revertSelection() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!bookSourceBean.getEnable());
        }
        adapter.notifyDataSetChanged();
        saveDate(adapter.getDataList());
        setResult(RESULT_OK);
    }

    public void upSearchView(int size) {
        binding.searchView.setQueryHint(getString(R.string.search_book_source_num, size));
    }

    @Override
    public void refreshBookSource() {
        if (isSearch) {
            List<BookSourceBean> sourceBeanList;
            if (binding.searchView.getQuery().toString().equals("enabled")) {
                sourceBeanList = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                        .where(BookSourceBeanDao.Properties.Enable.eq(1))
                        .orderRaw(BookSourceManager.getBookSourceSort())
                        .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                        .list();
            } else {
                String term = "%" + binding.searchView.getQuery() + "%";
                sourceBeanList = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                        .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                                BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                                BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                        .orderRaw(BookSourceManager.getBookSourceSort())
                        .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                        .list();
            }
            adapter.resetDataS(sourceBeanList);
        } else {
            adapter.resetDataS(BookSourceManager.getAllBookSource());
        }
    }

    public void delBookSource(BookSourceBean bookSource) {
        mPresenter.delData(bookSource);
        setResult(RESULT_OK);
    }

    public void saveDate(BookSourceBean date) {
        mPresenter.saveData(date);
        setResult(RESULT_OK);
    }

    public void saveDate(List<BookSourceBean> date) {
        mPresenter.saveData(date);
        setResult(RESULT_OK);
    }

    /**
     * 更新书源排序规则
     */
    private void upSourceSort(int sort) {
        preferences.edit().putInt("SourceSort", sort).apply();
        setDragEnable(sort);
        refreshBookSource();
    }

    /**
     * 读取书源排序规则
     */
    public int getSort() {
        return preferences.getInt("SourceSort", 0);
    }

    /**
     * 添加本地书源
     */
    private void addBookSource() {
        Intent intent = new Intent(this, SourceEditActivity.class);
        startActivityForResult(intent, SourceEditActivity.EDIT_SOURCE);
    }

    /**
     * 删除所选书源
     */
    private void deleteSelectDialog() {
        PaperAlertDialog.builder(this)
                .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                .setTitle(R.string.del_delete_book_source)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.delete)
                .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                    @Override
                    public void forNegativeButton() {}

                    @Override
                    public void forPositiveButton() { mPresenter.delData(adapter.getSelectDataList());}
                }).show(binding.getRoot());
    }

    /**
     * 导入本地书源
     */
    private void selectBookSourceFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.please_grant_storage_permission)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(BookSourceActivity.this, FilePicker.FILE);
                    filePicker.setBackgroundColor(Color.WHITE);
                    filePicker.setTopBackgroundColor(Color.WHITE);
                    filePicker.setAllowExtensions(getResources().getStringArray(R.array.text_suffix));
                    filePicker.setOnFilePickListener(s -> mPresenter.importBookSourceLocal(s));
                    filePicker.show();
                    filePicker.getSubmitButton().setText(R.string.sys_file_picker);
                    filePicker.getSubmitButton().setOnClickListener(view -> {
                        filePicker.dismiss();
                        selectFileSys();
                    });
                    return Unit.INSTANCE;
                })
                .request();
    }

    /**
     * 导入网络书源
     */
    private void importBookSourceOnLine() {
        String cu = ACache.get(this).getAsString("sourceUrl");
        String[] cacheUrls = cu == null ? new String[]{} : cu.split(";");
        List<String> urlList = new ArrayList<>(Arrays.asList(cacheUrls));
        InputDialog.builder(this)
                .setDefaultValue("")
                .setTitle(getString(R.string.input_book_source_url))
                .setShowDel(true)
                .setAdapterValues(urlList)
                .setCallback(new InputDialog.Callback() {
                    @Override
                    public void setInputText(String inputText) {
                        inputText = StringUtils.trim(inputText);
                        if (!urlList.contains(inputText)) {
                            urlList.add(0, inputText);
                            ACache.get(BookSourceActivity.this).put("sourceUrl", TextUtils.join(";", urlList));
                        }
                        mPresenter.importBookSource(inputText);
                    }

                    @Override
                    public void delete(String value) {
                        urlList.remove(value);
                        ACache.get(BookSourceActivity.this).put("sourceUrl", TextUtils.join(";", urlList));
                    }
                }).show();
    }

    /**
     * 导入默认书源
     */
    private void importBookSourceDefault() {
        String json = null;
        try {
            InputStream inputStream = MApplication.getInstance().getAssets()
                    .open("defaultData/bookSource.json");
            json = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<BookSourceBean> sourceDefaultList = GsonUtils.parseJArray(json, BookSourceBean.class);
        if (sourceDefaultList != null) {
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(sourceDefaultList);
            preferences.edit()
                    .putBoolean("importDefaultBookSource", true)
                    .apply();
            toast(R.string.import_success);
            refreshBookSource();
        }
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/*", "application/json"});
        intent.setType("*/*");//设置类型
        startActivityForResult(intent, IMPORT_SOURCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SourceEditActivity.EDIT_SOURCE:
                    refreshBookSource();
                    setResult(RESULT_OK);
                    break;
                case IMPORT_SOURCE:
                    if (data != null && data.getData() != null) {
                        mPresenter.importBookSourceLocal(RealPathUtil.getPath(this, data.getData()));
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearch) {
                try {
                    // 如果搜索框中有文字，则会先清空文字
                    mSearchAutoComplete.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(binding.llContent, msg, length);
    }

    @Override
    public void showSnackBar(String msg, int length) {
        super.showSnackBar(binding.llContent, msg, length);
    }
}
