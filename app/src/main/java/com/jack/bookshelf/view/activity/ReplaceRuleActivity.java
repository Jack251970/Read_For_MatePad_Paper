package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BitIntentDataManager;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.ReplaceRuleBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivityReplaceRuleBinding;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.help.permission.Permissions;
import com.jack.bookshelf.help.permission.PermissionsCompat;
import com.jack.bookshelf.model.ReplaceRuleManager;
import com.jack.bookshelf.presenter.ReplaceRulePresenter;
import com.jack.bookshelf.presenter.contract.ReplaceRuleContract;
import com.jack.bookshelf.utils.ACache;
import com.jack.bookshelf.utils.RealPathUtil;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.view.adapter.ReplaceRuleAdapter;
import com.jack.bookshelf.widget.dialog.InputDialog;
import com.jack.bookshelf.widget.dialog.PaperAlertDialog;
import com.jack.bookshelf.widget.dialog.modialog.MoDialogHUD;
import com.jack.bookshelf.widget.dialog.modialog.ReplaceRuleDialog;
import com.jack.bookshelf.widget.filepicker.picker.FilePicker;
import com.jack.bookshelf.widget.menu.MoreSettingMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.Unit;

/**
 * Replace Rule Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ReplaceRuleActivity extends MBaseActivity<ReplaceRuleContract.Presenter>
        implements ReplaceRuleContract.View {
    private final int IMPORT_SOURCE = 102;

    private ActivityReplaceRuleBinding binding;
    private BookShelfBean bookShelfBean;
    private MoDialogHUD moDialogHUD;
    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

    private MoreSettingMenu moreSettingMenu;

    public static void startThis(Context context, BookShelfBean shelfBean) {
        String key = String.valueOf(System.currentTimeMillis());
        Intent intent = new Intent(context, ReplaceRuleActivity.class);
        BitIntentDataManager.getInstance().putData(key, shelfBean);
        intent.putExtra("data_key", key);
        context.startActivity(intent);
    }

    @Override
    protected ReplaceRuleContract.Presenter initInjector() {
        return new ReplaceRulePresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        binding = ActivityReplaceRuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        String dataKey = getIntent().getStringExtra("data_key");
        if (!TextUtils.isEmpty(dataKey)) {
            bookShelfBean = (BookShelfBean) BitIntentDataManager.getInstance().getData(dataKey);
        }
        initRecyclerView();
        refresh();
    }

    @Override
    protected void bindView() {
        moDialogHUD = new MoDialogHUD(this);
        // 初始化一级菜单
        initMenu();
        // 返回
        binding.ivBack.setOnClickListener(v -> finish());
        // 添加替换规则
        binding.ivAddReplaceRule.setOnClickListener(v -> editReplaceRule(null));
        // 全选替换规则
        binding.ivSelectAllReplaceRule.setOnClickListener(v -> selectAllDataS());
        // 更多选项菜单
        binding.ivMoreSettingsReplaceRule.setOnClickListener(v -> {
            if (!moreSettingMenu.isShowing()) {
                moreSettingMenu.show(binding.getRoot(), binding.ivMoreSettingsReplaceRule);
            }
        });
    }

    /**
     * 初始化一级菜单
     */
    private void initMenu() {
        moreSettingMenu = MoreSettingMenu.builder(this)
                .setMenu(R.array.more_setting_menu_replace_rule, R.array.icon_more_setting_menu_replace_rule)
                .setOnclick(position -> {
                    switch (position) {
                        case 0:
                            selectReplaceRuleFile();
                            break;
                        case 1:
                            importOnlineReplaceRule();
                            break;
                        case 2:
                            deleteAllDialog();
                            break;
                    }
                });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        adapter = new ReplaceRuleAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    /**
     * 添加替换规则
     */
    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        ReplaceRuleDialog.builder(this, replaceRuleBean, bookShelfBean)
                .setPositiveButton(replaceRuleBean1 ->
                        ReplaceRuleManager.saveData(replaceRuleBean1)
                                .subscribe(new MySingleObserver<>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        refresh();
                                    }
                                })).show();
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (ReplaceRuleBean replaceRuleBean : adapter.getData()) {
            if (replaceRuleBean.getEnable() == null || !replaceRuleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectAllDataS() {
        for (ReplaceRuleBean replaceRuleBean : adapter.getData()) {
            replaceRuleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        ReplaceRuleManager.addDataS(adapter.getData());
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        mPresenter.delData(replaceRuleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getData());
    }

    /**
     * 删除所有规则
     */
    private void deleteAllDialog() {
        PaperAlertDialog.builder(this)
                .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                .setTitle(R.string.del_delete_all_replace_rule)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.delete)
                .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                    @Override
                    public void forNegativeButton() {}

                    @Override
                    public void forPositiveButton() { mPresenter.delData(adapter.getData());}
                }).show(binding.getRoot());
    }

    /**
     * 导入本地规则
     */
    private void selectReplaceRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.need_storage_permission_to_backup_book_information)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(ReplaceRuleActivity.this, FilePicker.FILE);
                    filePicker.setBackgroundColor(Color.WHITE);
                    filePicker.setTopBackgroundColor(Color.WHITE);
                    filePicker.setItemHeight(30);
                    filePicker.setAllowExtensions(getResources().getStringArray(R.array.text_suffix));
                    filePicker.setOnFilePickListener(s -> mPresenter.importDataSLocal(s));
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
     * 导入网络规则
     */
    private void importOnlineReplaceRule() {
        String cacheUrl = ACache.get(this).getAsString("replaceUrl");
        String[] cacheUrls = cacheUrl == null ? new String[]{} : cacheUrl.split(";");
        List<String> urlList = new ArrayList<>(Arrays.asList(cacheUrls));
        InputDialog.builder(this)
                .setTitle(getString(R.string.input_replace_url))
                .setDefaultValue(cacheUrl)
                .setAdapterValues(urlList)
                .setCallback(new InputDialog.Callback() {
                    @Override
                    public void setInputText(String inputText) {
                        inputText = StringUtils.trim(inputText);
                        if (!urlList.contains(inputText)) {
                            urlList.add(0, inputText);
                            ACache.get(ReplaceRuleActivity.this).put("replaceUrl", TextUtils.join(";", urlList));
                        }
                        mPresenter.importDataS(inputText);
                    }

                    @Override
                    public void delete(String value) {

                    }
                }).show();
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/*", "application/json"});
        intent.setType("*/*");//设置类型
        startActivityForResult(intent, IMPORT_SOURCE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_SOURCE) {
            if (data != null) {
                mPresenter.importDataSLocal(RealPathUtil.getPath(this, data.getData()));
            }
        }
    }

    @Override
    public void refresh() {
        ReplaceRuleManager.getAll()
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(List<ReplaceRuleBean> replaceRuleBeans) {
                        adapter.resetDataS(replaceRuleBeans);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        super.onDestroy();
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(binding.clContent, msg, length);
    }
}
