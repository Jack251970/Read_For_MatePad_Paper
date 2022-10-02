package com.jack.bookshelf.base;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.widget.viewpager.PaperViewPager;

import java.util.List;

/**
 * PaperViewPager Base Without TabLayout
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public abstract class BaseViewPagerActivity<T extends IPresenter> extends MBaseActivity<T> {
    // View
    protected PaperViewPager mVp;
    // Adapter
    protected FragmentPageAdapter fragmentPageAdapter;
    // Params
    protected List<Fragment> mFragmentList;
    protected List<String> mTitleList;
    // private List<ImageView> mIndicatorList;
    // private int lastChoose;

    // Abstract
    protected abstract List<Fragment> createFragments();
    protected abstract List<String> createTitles();
    // private abstract List<ImageView> createIndicators();

    @Override
    protected void bindView() {
        super.bindView();
        mVp = findViewById(R.id.view_pager);
        setUpViewPager();
    }

    private void setUpViewPager() {
        mFragmentList = createFragments();
        mTitleList = createTitles();
        // mIndicatorList = createIndicators();
        fragmentPageAdapter = new FragmentPageAdapter(getSupportFragmentManager());
        mVp.setAdapter(fragmentPageAdapter);
        mVp.setOffscreenPageLimit(3);
        //
    }

    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            mVp.setCurrentItem(item);
        }
        //
        //
    }

    public int getCurrentItem() { return mVp.getCurrentItem(); }

    public class FragmentPageAdapter extends FragmentPagerAdapter {

        FragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
    }
}