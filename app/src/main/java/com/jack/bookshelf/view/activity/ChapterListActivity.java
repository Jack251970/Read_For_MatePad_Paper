package com.jack.bookshelf.view.activity;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BitIntentDataManager;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.BaseViewPagerActivity;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.databinding.ActivityChapterListBinding;
import com.jack.bookshelf.help.ReadBookControl;
import com.jack.bookshelf.view.fragment.BookmarkFragment;
import com.jack.bookshelf.view.fragment.ChapterListFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Chapter List Page & Bookmark List Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ChapterListActivity extends BaseViewPagerActivity<IPresenter> {

    private ActivityChapterListBinding binding;
    SearchView.SearchAutoComplete mSearchAutoComplete;
    private final ReadBookControl readBookControl = ReadBookControl.getInstance();
    private BookShelfBean bookShelf;
    private List<BookChapterBean> chapterBeanList;

    public static void startThis(Activity activity, BookShelfBean bookShelf, List<BookChapterBean> chapterBeanList) {
        Intent intent = new Intent(activity, ChapterListActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        String bookKey = "book" + key;
        intent.putExtra("bookKey", bookKey);
        BitIntentDataManager.getInstance().putData(bookKey, bookShelf.clone());
        String chapterListKey = "chapterList" + key;
        intent.putExtra("chapterListKey", chapterListKey);
        BitIntentDataManager.getInstance().putData(chapterListKey, chapterBeanList);
        activity.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrientation(readBookControl.getScreenDirection());
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bookShelf != null) {
            String key = String.valueOf(System.currentTimeMillis());
            String bookKey = "book" + key;
            getIntent().putExtra("bookKey", bookKey);
            BitIntentDataManager.getInstance().putData(bookKey, bookShelf.clone());
            String chapterListKey = "chapterList" + key;
            getIntent().putExtra("chapterListKey", chapterListKey);
            BitIntentDataManager.getInstance().putData(chapterListKey, chapterBeanList);
        }
    }

    @Override
    protected void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreateActivity() {
        binding = ActivityChapterListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void bindView() {
        super.bindView();
        initSearchView();
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.tvBookTitleChapterList.setText(bookShelf.getBookInfoBean().getName());
        binding.ivChapterListIndicator.setVisibility(VISIBLE);
        binding.tvChapterList.setOnClickListener(v -> {
            setCurrentItem(0);
            binding.ivChapterListIndicator.setVisibility(VISIBLE);
            binding.ivBookmarkIndicator.setVisibility(INVISIBLE);
        });
        binding.tvBookmark.setOnClickListener(v -> {
            setCurrentItem(1);
            binding.ivChapterListIndicator.setVisibility(INVISIBLE);
            binding.ivBookmarkIndicator.setVisibility(VISIBLE);
        });
        binding.ivSearch.setOnClickListener(v -> {
            binding.tvBookTitleChapterList.setVisibility(GONE);
            binding.ivSearch.setVisibility(GONE);
            binding.searchView.setVisibility(VISIBLE);
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
        closeButton.setOnClickListener(v -> searchViewCollapsed());
        binding.searchView.onActionViewExpanded();
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (getCurrentItem() == 1) {
                    ((BookmarkFragment) mFragmentList.get(1)).startSearch(newText);
                } else {
                    ((ChapterListFragment) mFragmentList.get(0)).startSearch(newText);
                }
                return false;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initData() {
        String bookKey = getIntent().getStringExtra("bookKey");
        bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(bookKey);
        String chapterListKey = getIntent().getStringExtra("chapterListKey");
        chapterBeanList = (List<BookChapterBean>) BitIntentDataManager.getInstance().getData(chapterListKey);
    }

    @Override
    protected List<Fragment> createTabFragments() {
        ChapterListFragment chapterListFragment = new ChapterListFragment(bookShelf,chapterBeanList);
        BookmarkFragment bookmarkFragment = new BookmarkFragment(bookShelf);
        return Arrays.asList(chapterListFragment, bookmarkFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(getString(R.string.chapter_list), getString(R.string.bookmark));
    }

    @Override
    public void onBackPressed() {
        if (binding.searchView.getVisibility() == VISIBLE) {
            searchViewCollapsed();
            return;
        }
        finish();
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    public List<BookChapterBean> getChapterBeanList() {
        return chapterBeanList;
    }

    public void searchViewCollapsed() {
        mSearchAutoComplete.setText("");
        binding.tvBookTitleChapterList.setVisibility(VISIBLE);
        binding.ivSearch.setVisibility(VISIBLE);
        binding.searchView.setVisibility(GONE);
    }
}