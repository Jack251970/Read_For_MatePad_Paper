package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.TxtChapterRuleBean;
import com.jack.bookshelf.databinding.ActivityTxtChapterRuleBinding;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.help.permission.Permissions;
import com.jack.bookshelf.help.permission.PermissionsCompat;
import com.jack.bookshelf.model.TxtChapterRuleManager;
import com.jack.bookshelf.presenter.TxtChapterRulePresenter;
import com.jack.bookshelf.presenter.contract.TxtChapterRuleContract;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.IOUtils;
import com.jack.bookshelf.utils.RealPathUtil;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.adapter.TxtChapterRuleAdapter;
import com.jack.bookshelf.view.dialog.PaperAlertDialog;
import com.jack.bookshelf.view.popupmenu.MoreSettingMenu;
import com.jack.bookshelf.widget.filepicker.picker.FilePicker;
import com.jack.bookshelf.widget.modialog.TxtChapterRuleDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import kotlin.Unit;

/**
 * Text Chapter Rule Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class TxtChapterRuleActivity extends MBaseActivity<TxtChapterRuleContract.Presenter>
        implements TxtChapterRuleContract.View {
    private final int requestImport = 102;

    private ActivityTxtChapterRuleBinding binding;
    private TxtChapterRuleAdapter adapter;
    private boolean selectAll = true;

    private MoreSettingMenu moreSettingMenu;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, TxtChapterRuleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected TxtChapterRuleContract.Presenter initInjector() {
        return new TxtChapterRulePresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityTxtChapterRuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        initRecyclerView(); // must in front of refresh()!!!
        refresh();
    }

    @Override
    protected void bindView() {
        // 初始化一级菜单
        initMenu();
        // 返回
        binding.ivBackTxtChapterRule.setOnClickListener(v -> finish());
        // 新增目录正则
        binding.ivAddTxtChapterRule.setOnClickListener(v -> editChapterRule(null));
        // 全选目录正则
        binding.ivSelectAllTxtChapterRule.setOnClickListener(v -> selectAllDataS());
        // 更多选项
        binding.ivMoreSettingsTxtChapterRule.setOnClickListener(v -> {
            if (!moreSettingMenu.isShowing()) {
                moreSettingMenu.show(binding.getRoot(), binding.ivMoreSettingsTxtChapterRule);
            }
        });
    }

    /**
     * 初始化一级菜单
     */
    private void initMenu() {
        moreSettingMenu = MoreSettingMenu.builder(this)
                .setMenu(getResources().getStringArray(R.array.more_setting_menu_txt_chapter_rule))
                .setOnclick(position -> {
                    switch (position) {
                        case 0:
                            selectTxtChapterRuleFile();
                            break;
                        case 1:
                            importDefaultRule();
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
        adapter = new TxtChapterRuleAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    /**
     * 新增目录正则
     */
    public void editChapterRule(TxtChapterRuleBean txtChapterRuleBean) {
        TxtChapterRuleDialog.builder(this, txtChapterRuleBean)
                .setPositiveButton(txtChapterRuleBean1 -> {
                    if (txtChapterRuleBean != null) {
                        TxtChapterRuleManager.del(txtChapterRuleBean);
                    }
                    TxtChapterRuleManager.save(txtChapterRuleBean1);
                    refresh();
                })
                .show();
    }

    /**
     * 导入本地正则
     */
    private void selectTxtChapterRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.need_storage_permission_to_backup_book_information)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(TxtChapterRuleActivity.this, FilePicker.FILE);
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
     * 导入默认正则
     */
    private void importDefaultRule() {
        String json = null;
        try {
            InputStream inputStream = MApplication.getInstance().getAssets().open("defaultData/txtChapterRule.json");
            json = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<TxtChapterRuleBean> ruleDefaultList = GsonUtils.parseJArray(json, TxtChapterRuleBean.class);
        if (ruleDefaultList != null) {
            DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(ruleDefaultList);
            toast(R.string.import_success);
            refresh();
            preferences.edit()
                    .putBoolean("importDefaultBookSource", true)
                    .apply();
        }
    }

    /**
     * 删除所有正则
     */
    private void deleteAllDialog() {
        PaperAlertDialog.builder(this)
                .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                .setTitle(R.string.del_delete_all_txt_chapter_rule)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.delete)
                .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                    @Override
                    public void forNegativeButton() {}

                    @Override
                    public void forPositiveButton() { mPresenter.delData(adapter.getData());}
                }).show(binding.getRoot());
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (TxtChapterRuleBean ruleBean : adapter.getData()) {
            if (ruleBean.getEnable() == null || !ruleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectAllDataS() {
        for (TxtChapterRuleBean ruleBean : adapter.getData()) {
            ruleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        TxtChapterRuleManager.save(adapter.getData());
    }

    public void delData(TxtChapterRuleBean ruleBean) {
        mPresenter.delData(ruleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getData());
    }

    @Override
    public void refresh() {
        adapter.resetDataS(TxtChapterRuleManager.getAll());
    }

    private void selectFileSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");//设置类型
        startActivityForResult(intent, requestImport);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestImport) {
            if (data != null) {
                mPresenter.importDataSLocal(RealPathUtil.getPath(this, data.getData()));
            }
        }
    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(binding.llContent, msg, length);
    }
}
