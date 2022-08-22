package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.TxtChapterRuleBean;
import com.jack.bookshelf.databinding.ActivityRecyclerVewBinding;
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
import com.jack.bookshelf.widget.filepicker.picker.FilePicker;
import com.jack.bookshelf.widget.modialog.TxtChapterRuleDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import kotlin.Unit;

/**
 * 目录正则管理界面
 * 目录正则是对本地导入的txt小说设置目录编号方式
 * Edited by Jack251970
 */

public class TxtChapterRuleActivity extends MBaseActivity<TxtChapterRuleContract.Presenter> implements TxtChapterRuleContract.View {
    private final int requestImport = 102;

    private ActivityRecyclerVewBinding binding;
    private TxtChapterRuleAdapter adapter;
    private boolean selectAll = true;

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
        binding = ActivityRecyclerVewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
        initRecyclerView();
        refresh();
    }

    // 设置顶部标题栏
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.chapter_regex);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_txt_chapter_rule_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // 菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_replace_rule:
                editChapterRule(null);
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import_txt_chapter_rule_default:
                importDefaultRule();
                break;
            case R.id.action_import_txt_chapter_rule_local:
                selectReplaceRuleFile();
                break;
            case R.id.action_del_all_txt_chapter_rule:
                mPresenter.delData(adapter.getData());
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 导入默认规则
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

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(binding.llContent, msg, length);
    }

    private void selectReplaceRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.READ_EXTERNAL_STORAGE, Permissions.WRITE_EXTERNAL_STORAGE)
                .rationale(R.string.need_storage_permission_to_backup_book_information)
                .onGranted((requestCode) -> {
                    FilePicker filePicker = new FilePicker(TxtChapterRuleActivity.this, FilePicker.FILE);
                    filePicker.setBackgroundColor(getResources().getColor(R.color.background));
                    filePicker.setTopBackgroundColor(getResources().getColor(R.color.background));
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
}
