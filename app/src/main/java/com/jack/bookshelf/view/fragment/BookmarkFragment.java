package com.jack.bookshelf.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.base.MBaseFragment;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookmarkBean;
import com.jack.bookshelf.bean.OpenChapterBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.FragmentBookmarkListBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.view.activity.ChapterListActivity;
import com.jack.bookshelf.view.adapter.BookmarkAdapter;
import com.jack.bookshelf.widget.modialog.BookmarkDialog;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

/**
 * Bookmark Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookmarkFragment extends MBaseFragment<IPresenter> {

    private FragmentBookmarkListBinding binding;
    private final BookShelfBean bookShelf;
    private List<BookmarkBean> bookmarkBeanList;
    private BookmarkAdapter adapter;

    public BookmarkFragment(BookShelfBean bookShelf) {
        super();
        this.bookShelf = bookShelf;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBookmarkListBinding.inflate(inflater, container, false);
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
    protected void bindView() {
        super.bindView();
        adapter = new BookmarkAdapter(bookShelf, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page) {
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
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean) {
                /*if (getFatherView() != null) {
                    getFatherView().searchViewCollapsed();
                }*/
                if (getFatherActivity() != null) {
                    getFatherActivity().searchViewCollapsed();
                }
                showBookmark(bookmarkBean);
            }
        });
        binding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvList.setAdapter(adapter);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (bookShelf != null) {
                bookmarkBeanList = BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName());
                emitter.onSuccess(true);
            } else {
                emitter.onSuccess(false);
            }
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean) {
                            adapter.setAllBookmark(bookmarkBeanList);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        RxBus.get().unregister(this);
    }

    public void startSearch(String key) {
        adapter.search(key);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showBookmark(BookmarkBean bookmarkBean) {
        BookmarkDialog.builder(getContext(), bookmarkBean, false)
                .setPositiveButton(new BookmarkDialog.Callback() {

                    @Override
                    public void saveBookmark(BookmarkBean bookmarkBean) {
                        DbHelper.getDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void delBookmark(BookmarkBean bookmarkBean) {
                        DbHelper.getDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
                        bookmarkBeanList = BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName());
                        adapter.setAllBookmark(bookmarkBeanList);
                    }

                    @Override
                    public void openChapter(int chapterIndex, int pageIndex) {
                        RxBus.get().post(RxBusTag.OPEN_BOOK_MARK, bookmarkBean);
                        /*if (getFatherView() != null) {
                            getFatherView().finish();
                        }*/
                        if (getFatherActivity() != null) {
                            getFatherActivity().finish();
                        }
                    }
                }).show();
    }

    /*private ReadChapterBookmarkPop getFatherView() {
        return (ReadChapterBookmarkPop) getView();
    }*/

    private ChapterListActivity getFatherActivity() {
        return (ChapterListActivity) getActivity();
    }
}