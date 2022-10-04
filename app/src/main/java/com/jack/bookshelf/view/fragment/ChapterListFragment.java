package com.jack.bookshelf.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseFragment;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookContentBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.OpenChapterBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.FragmentChapterListBinding;
import com.jack.bookshelf.view.activity.CatalogActivity;
import com.jack.bookshelf.view.fragment.adapter.ChapterListAdapter;

import java.util.List;

/**
 * Chapter List Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ChapterListFragment extends MBaseFragment<IPresenter> {

    private FragmentChapterListBinding binding;
    private ChapterListAdapter chapterListAdapter;
    private LinearLayoutManager layoutManager;

    private final BookShelfBean bookShelf;
    private final List<BookChapterBean> chapterBeanList;
    private boolean isChapterReverse;

    public ChapterListFragment(BookShelfBean bookShelf, List<BookChapterBean> chapterList) {
        super();
        this.bookShelf = bookShelf;
        this.chapterBeanList = chapterList;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentChapterListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void initData() {
        super.initData();
        isChapterReverse = preferences.getBoolean("isChapterReverse", false);
    }

    @Override
    protected void bindView() {
        super.bindView();
        binding.rvList.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, isChapterReverse));
        binding.rvList.setItemAnimator(null);
        chapterListAdapter = new ChapterListAdapter(bookShelf, chapterBeanList, (index, page) -> {
            if (index != bookShelf.getDurChapter()) {
                RxBus.get().post(RxBusTag.SKIP_TO_CHAPTER, new OpenChapterBean(index, page));
            }
            /*if (getFatherView() != null) {
                getFatherView().searchViewCollapsed();
                getFatherView().finish();
            }*/
            if (getFatherActivity() != null) {
                getFatherActivity().searchViewCollapsed();
                getFatherActivity().finish();
            }
        });
        if (bookShelf != null) {
            binding.rvList.setAdapter(chapterListAdapter);
            updateIndex(bookShelf.getDurChapter());
            updateChapterInfo();
        }
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        binding.tvCurrentChapterInfo.setOnClickListener(view -> layoutManager.scrollToPositionWithOffset(bookShelf.getDurChapter(), 0));
        binding.ivChapterTop.setOnClickListener(v -> binding.rvList.scrollToPosition(0));
        binding.ivChapterBottom.setOnClickListener(v -> {
            if (chapterListAdapter.getItemCount() > 0) {
                binding.rvList.scrollToPosition(chapterListAdapter.getItemCount() - 1);
            }
        });
    }

    public void startSearch(String key) {
        chapterListAdapter.search(key);
    }

    private void updateIndex(int durChapter) {
        chapterListAdapter.setIndex(durChapter);
        layoutManager.scrollToPositionWithOffset(durChapter, 0);
    }

    private void updateChapterInfo() {
        if (bookShelf != null) {
            if (chapterListAdapter.getItemCount() == 0) {
                binding.tvCurrentChapterInfo.setText(bookShelf.getDurChapterName());
            } else {
                binding.tvCurrentChapterInfo.setText(requireContext().getString(R.string.chapter_info_format,
                        bookShelf.getDurChapterName(), bookShelf.getDurChapter() + 1, bookShelf.getChapterListSize()));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHAPTER_CHANGE)})
    public void chapterChange(BookContentBean bookContentBean) {
        if (bookShelf != null && bookShelf.getNoteUrl().equals(bookContentBean.getNoteUrl())) {
            chapterListAdapter.upChapter(bookContentBean.getDurChapterIndex());
        }
    }

    /*private ReadChapterBookmarkPop getFatherView() {
        return (ReadChapterBookmarkPop) getView();
    }*/

    private CatalogActivity getFatherActivity() {
        return (CatalogActivity) getActivity();
    }
}