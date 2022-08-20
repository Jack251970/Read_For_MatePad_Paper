package com.jack.bookshelf.base;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.widget.viewpager.ViewPager;
import java.util.List;

/**
 * ViewPager Base Without TabLayout
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public abstract class BaseViewPagerActivity<T extends IPresenter> extends MBaseActivity<T> {

    protected ViewPager mVp;
    protected TabFragmentPageAdapter tabFragmentPageAdapter;
    protected List<Fragment> mFragmentList;
    // private List<ImageView> mIndicatorList;
    // private int lastChoose;

    protected abstract List<Fragment> createTabFragments();
    // private abstract List<ImageView> createTabIndicators();

    @Override
    protected void bindView() {
        super.bindView();
        mVp = findViewById(R.id.tab_vp);
        setUpViewPager();
    }

    private void setUpViewPager() {
        mFragmentList = createTabFragments();
        // mIndicatorList = createTabIndicators();
        tabFragmentPageAdapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        mVp.setAdapter(tabFragmentPageAdapter);
        mVp.setOffscreenPageLimit(3);
        //
    }

    public void setCurrentItem(int item) {
        if (mVp.getCurrentItem() != item) {
            mVp.setCurrentItem(item);
        }
        //
        //
    }

    public class TabFragmentPageAdapter extends FragmentPagerAdapter {

        TabFragmentPageAdapter(FragmentManager fm) {
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
    }
}
