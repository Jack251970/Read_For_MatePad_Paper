package com.jack.bookshelf.view.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.basemvplib.BitIntentDataManager;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseFragment;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.databinding.FragmentBookListBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.presenter.BookDetailPresenter;
import com.jack.bookshelf.presenter.BookListPresenter;
import com.jack.bookshelf.presenter.ReadBookPresenter;
import com.jack.bookshelf.presenter.contract.BookListContract;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.activity.BookDetailActivity;
import com.jack.bookshelf.view.activity.ReadBookActivity;
import com.jack.bookshelf.view.adapter.BookShelfAdapter;
import com.jack.bookshelf.view.adapter.BookShelfGridAdapter;
import com.jack.bookshelf.view.adapter.BookShelfListAdapter;
import com.jack.bookshelf.view.adapter.base.OnItemClickListenerTwo;
import com.jack.bookshelf.view.dialog.PaperAlertDialog;
import com.jack.bookshelf.widget.viewpager.PaperViewPager;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

/**
 * BookList Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookListFragment extends MBaseFragment<BookListContract.Presenter>
        implements BookListContract.View {

    private CallbackValue callbackValue;
    private FragmentBookListBinding binding;
    private int bookPx;
    private boolean resumed = false;
    private boolean isRecreate;
    private int group;

    private BookShelfAdapter bookShelfAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            resumed = savedInstanceState.getBoolean("resumed");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected BookListContract.Presenter initInjector() {
        return new BookListPresenter();
    }

    @Override
    protected void initData() {
        callbackValue = (CallbackValue) getActivity();
        bookPx = preferences.getInt(getString(R.string.pk_bookshelf_px), 0);
        isRecreate = callbackValue != null && callbackValue.isRecreate();
    }

    @Override
    protected void bindView() {
        super.bindView();
        // 书架布局
        if (preferences.getInt("bookshelfLayout", 1) == 0) {
            binding.rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
            bookShelfAdapter = new BookShelfListAdapter(getActivity());
        } else {
            binding.rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), 4));
            bookShelfAdapter = new BookShelfGridAdapter(getActivity());
        }
        binding.rvBookshelf.setAdapter((RecyclerView.Adapter) bookShelfAdapter);
        binding.refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
    }

    @Override
    protected void firstRequest() {
        group = preferences.getInt("bookshelfGroup", 0);
        boolean needRefresh = preferences.getBoolean(getString(R.string.pk_auto_refresh), false)
                && !isRecreate && NetworkUtils.isNetWorkAvailable() && group != 2;
        mPresenter.queryBookShelf(needRefresh, group);
    }

    @Override
    protected void bindEvent() {
        // 书架刷新功能
        binding.refreshLayout.setOnRefreshListener(() -> {
            mPresenter.queryBookShelf(NetworkUtils.isNetWorkAvailable(), group);
            if (!NetworkUtils.isNetWorkAvailable()) {
                toast(R.string.network_connection_unavailable);
            }
            binding.refreshLayout.setRefreshing(false);
        });
        ItemTouchCallback itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setSwipeRefreshLayout(binding.refreshLayout);
        itemTouchCallback.setViewPager(callbackValue.getViewPager());
        // 载入书架排序规则
        if (bookPx == 2) {
            itemTouchCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
            itemTouchHelper.attachToRecyclerView(binding.rvBookshelf);
        } else {
            itemTouchCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
            itemTouchHelper.attachToRecyclerView(binding.rvBookshelf);
        }
        // 整理书架
        bookShelfAdapter.setItemClickListener(getAdapterListener());
        itemTouchCallback.setOnItemTouchCallbackListener(bookShelfAdapter.getItemTouchCallbackListener());
        binding.ivBack.setOnClickListener(v -> setArrange(false));
        binding.llDelete.setOnClickListener(v -> {
            if (bookShelfAdapter.getSelected().size() != 0) {
                PaperAlertDialog.builder(requireContext())
                        .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                        .setTitle(R.string.sure_del_book)
                        .setNegativeButton(R.string.cancel)
                        .setPositiveButton(R.string.delete)
                        .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                            @Override
                            public void forNegativeButton() {}

                            @Override
                            public void forPositiveButton() { delSelect();}
                        }).show(binding.getRoot());
            }});
        binding.llSelectAll.setOnClickListener(v -> bookShelfAdapter.selectAll());
    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                if (binding.toolBar.getVisibility() == View.VISIBLE) {
                    upSelectCount();
                    return;
                }
                BookShelfBean bookShelfBean = bookShelfAdapter.getBooks().get(index);
                Intent intent = new Intent(getContext(), ReadBookActivity.class);
                intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
                String key = String.valueOf(System.currentTimeMillis());
                String bookKey = "book" + key;
                intent.putExtra("bookKey", bookKey);
                BitIntentDataManager.getInstance().putData(bookKey, bookShelfBean.clone());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int index) {
                BookShelfBean bookShelfBean = bookShelfAdapter.getBooks().get(index);
                String key = String.valueOf(System.currentTimeMillis());
                BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("openFrom", BookDetailPresenter.FROM_BOOKSHELF);
                intent.putExtra("data_key", key);
                intent.putExtra("noteUrl", bookShelfBean.getNoteUrl());
                startActivity(intent);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumed) {
            resumed = false;
            stopBookShelfRefreshAnim();
        }
    }

    @Override
    public void onPause() {
        resumed = true;
        super.onPause();
    }

    private void stopBookShelfRefreshAnim() {
        if (bookShelfAdapter.getBooks() != null && bookShelfAdapter.getBooks().size() > 0) {
            for (BookShelfBean bookShelfBean : bookShelfAdapter.getBooks()) {
                if (bookShelfBean.isLoading()) {
                    bookShelfBean.setLoading(false);
                    refreshBook(bookShelfBean.getNoteUrl());
                }
            }
        }
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList, bookPx);
        if (bookShelfBeanList.size() == 0) {    // 书架中没有书籍
            binding.viewEmpty.tvEmpty.setText(R.string.bookshelf_empty);
            binding.viewEmpty.ivEmpty.setVisibility(View.VISIBLE);
            binding.viewEmpty.rlEmptyView.setVisibility(View.VISIBLE);
        } else {
            binding.viewEmpty.rlEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshBook(String noteUrl) {
        bookShelfAdapter.refreshBook(noteUrl);
    }

    @Override
    public void updateGroup(Integer group) {
        this.group = group;
    }

    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 设置整理书架模式
     */
    public void setArrange(boolean isArrange) {
        if (bookShelfAdapter != null) {
            bookShelfAdapter.setArrange(isArrange);
            if (isArrange) {
                binding.toolBar.setVisibility(View.VISIBLE);
                upSelectCount();
            } else {
                binding.toolBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 更新已选择项数与删除按钮启用状态
     */
    @SuppressLint("DefaultLocale")
    private void upSelectCount() {
        if (bookShelfAdapter.getSelected().size() == 0) {
            binding.tvSelectCount.setText(R.string.unselected);
            binding.tvDelete.setTextColor(getResources().getColor(R.color.mpp_button_unable));
            binding.ivDelete.setImageResource(R.drawable.ic_delete_unable);
        } else {
            binding.tvSelectCount.setText(getString(R.string.have_choose_items, bookShelfAdapter.getSelected().size()));
            binding.tvDelete.setTextColor(getResources().getColor(R.color.black));
            binding.ivDelete.setImageResource(R.drawable.ic_delete);
        }
    }

    private void delSelect() {
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            for (String noteUrl : bookShelfAdapter.getSelected()) {
                BookshelfHelp.removeFromBookShelf(BookshelfHelp.getBook(noteUrl));
            }
            bookShelfAdapter.getSelected().clear();
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        mPresenter.queryBookShelf(false, group);
                    }
                });
    }

    public interface CallbackValue {
        boolean isRecreate();

        int getGroup();

        PaperViewPager getViewPager();
    }
}
