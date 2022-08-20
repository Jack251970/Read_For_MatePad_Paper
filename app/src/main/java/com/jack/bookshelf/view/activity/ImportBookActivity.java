package com.jack.bookshelf.view.activity;

import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;

import com.jack.bookshelf.base.BaseViewPagerActivity;
import androidx.fragment.app.Fragment;

import com.jack.bookshelf.R;
import com.jack.bookshelf.databinding.ActivityImportBookBinding;
import com.jack.bookshelf.presenter.ImportBookPresenter;
import com.jack.bookshelf.presenter.contract.ImportBookContract;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.fragment.BaseFileFragment;
import com.jack.bookshelf.view.fragment.FileCategoryFragment;
import com.jack.bookshelf.view.fragment.LocalBookFragment;
import com.jack.bookshelf.widget.viewpager.ViewPager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Import Local Book
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ImportBookActivity extends BaseViewPagerActivity<ImportBookContract.Presenter> implements ImportBookContract.View {

    private ActivityImportBookBinding binding;
    private LocalBookFragment mLocalFragment;
    private FileCategoryFragment mCategoryFragment;
    private BaseFileFragment mCurFragment;
    private boolean ifSelectAll = false;

    private final BaseFileFragment.OnFileCheckedListener mListener = new BaseFileFragment.OnFileCheckedListener() {
        @Override
        public void onItemCheckedChange(boolean isChecked) {
            changeMenuStatus();
        }

        @Override
        public void onCategoryChanged() {
            // 状态归零
            mCurFragment.setCheckedAll(false);
            // 改变菜单
            changeMenuStatus();
        }
    };

    @Override
    protected ImportBookContract.Presenter initInjector() {
        return new ImportBookPresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityImportBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 初始化TabIndicator
        binding.ivLocalBookIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {}

    @Override
    protected void bindView() {
        super.bindView();
    }

    @Override
    protected List<Fragment> createTabFragments() {
        mCategoryFragment = new FileCategoryFragment(); // 本机目录
        mLocalFragment = new LocalBookFragment();   // 智能导入
        return Arrays.asList(mCategoryFragment, mLocalFragment);
    }

    @Override
    protected void bindEvent() {
        // 返回事件
        binding.ivBack.setOnClickListener(v -> finish());
        // 界面改变事件
        binding.tvLocalBook.setOnClickListener(v -> {
            setCurrentItem(0);
            binding.ivLocalBookIndicator.setVisibility(View.VISIBLE);
            binding.ivSmartImportIndicator.setVisibility(View.INVISIBLE);
        });
        binding.tvSmartImport.setOnClickListener(v -> {
            setCurrentItem(1);
            binding.ivLocalBookIndicator.setVisibility(View.INVISIBLE);
            binding.ivSmartImportIndicator.setVisibility(View.VISIBLE);
        });
        // 界面改变事件监听
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mCurFragment = (BaseFileFragment) mFragmentList.get(position);
                changeMenuStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        // 加入书架事件
        binding.fileSystemBtnAddBook.setOnClickListener(
                (v) -> {
                    // 获取选中的文件
                    List<File> files = mCurFragment.getCheckedFiles();
                    // 转换成CollBook,并存储
                    mPresenter.importBooks(files);
                }
        );
        binding.fileSystemCbSelectedAll.setOnClickListener(
                (view) -> {
                    // 设置全选状态
                    ifSelectAll = !ifSelectAll;
                    mCurFragment.setCheckedAll(ifSelectAll);
                    // 改变菜单状态
                    changeMenuStatus();
                }
        );
        mCategoryFragment.setOnFileCheckedListener(mListener);
        mLocalFragment.setOnFileCheckedListener(mListener);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        mCurFragment = (BaseFileFragment) mFragmentList.get(0);
    }

    /**
     * 改变底部选择栏的状态
     */
    private void changeMenuStatus() {
        // 加入书架按钮状态设置,全选状态刷新
        if (mCurFragment.getCheckedCount() == 0) {  // 选中数量为0
            // 加入书籍按钮禁用
            changeAddToBookshelfStatus(false);
            // 全选状态刷新
            if (ifSelectAll) {
                mCurFragment.setChecked(false);
                ifSelectAll = mCurFragment.isCheckedAll();
            }
        } else {    // 选中数量不为0
            // 加入书籍按钮启用
            changeAddToBookshelfStatus(true);
            // 如果选中的全部的数据，则设置为全选
            if (mCurFragment.getCheckedCount() == mCurFragment.getCheckableCount()) {
                mCurFragment.setChecked(true);
                ifSelectAll = mCurFragment.isCheckedAll();
            } else if (mCurFragment.isCheckedAll()) { // 如果曾经是全选则替换
                mCurFragment.setChecked(false);
                ifSelectAll = mCurFragment.isCheckedAll();
            }
        }
        // 全选按钮状态设置
        changeCheckedAllStatus();
    }

    /**
     * 改变加入书架按钮的状态
     */
    private void changeAddToBookshelfStatus(boolean ifEnable) {
        binding.fileSystemBtnAddBook.setEnabled(ifEnable);
        binding.fileSystemBtnAddBook.setClickable(ifEnable);
        if (ifEnable) {
            binding.ivFileSystemBtnAddBook.setImageResource(R.drawable.ic_add_to_bookshelf);
            binding.tvFileSystemBtnAddBook.setTextColor(getColor(R.color.black));
        } else {
            binding.ivFileSystemBtnAddBook.setImageResource(R.drawable.ic_add_to_bookshelf_unable);
            binding.tvFileSystemBtnAddBook.setTextColor(getColor(R.color.text_light));
        }
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        // 获取可选择的文件数量
        int count = mCurFragment.getCheckableCount();
        // 设置是否能够全选
        if (count > 0) {
            binding.fileSystemCbSelectedAll.setClickable(true);
            binding.fileSystemCbSelectedAll.setEnabled(true);
            binding.ivFileSystemCbSelectedAll.setImageResource(R.drawable.mpp_ic_select_all);
            binding.tvFileSystemCbSelectedAll.setTextColor(getColor(R.color.black));
        } else {
            binding.fileSystemCbSelectedAll.setClickable(false);
            binding.fileSystemCbSelectedAll.setEnabled(false);
            binding.ivFileSystemCbSelectedAll.setImageResource(R.drawable.ic_select_all_unable);
            binding.tvFileSystemCbSelectedAll.setTextColor(getColor(R.color.text_light));
        }
    }

    @Override
    public void addSuccess() {
        // 设置HashMap为false
        mCurFragment.setCheckedAll(false);
        // 改变菜单状态
        changeMenuStatus();
    }

    @Override
    public void addError(String msg) {}
}
