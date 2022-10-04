package com.jack.bookshelf.widget.recycler.refresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.databinding.ViewRefreshRecyclerViewBinding;

import java.util.Objects;

/**
 * Refresh Recycle View
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class RefreshRecyclerView extends FrameLayout {
    private final ViewRefreshRecyclerViewBinding binding = ViewRefreshRecyclerViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    private View noDataView;
    private View refreshErrorView;
    private BaseRefreshListener baseRefreshListener;
    private OnLoadMoreListener loadMoreListener;

    private final OnTouchListener refreshTouchListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (baseRefreshListener != null && ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getIsRequesting() == 0) {
                        if (binding.tvLoading.getVisibility() != VISIBLE) {
                            binding.tvLoading.setVisibility(VISIBLE);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (baseRefreshListener != null) {
                        if (((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getIsRequesting() == 0) {
                            if (baseRefreshListener != null) {
                                // 带有进度的，执行刷新响应
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, true);
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                            } else {
                                // 不带进度的
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(false, false);
                                ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, true);
                                baseRefreshListener.startRefresh();
                                if (noDataView != null) {
                                    noDataView.setVisibility(GONE);
                                }
                                if (refreshErrorView != null) {
                                    refreshErrorView.setVisibility(GONE);
                                }
                                binding.tvLoading.setVisibility(VISIBLE);
                            }
                        }
                    }
                    break;
            }
            return false;
        }
    };

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bindEvent();
    }

    public void setBaseRefreshListener(BaseRefreshListener baseRefreshListener) {
        this.baseRefreshListener = baseRefreshListener;
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindEvent() {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((RefreshRecyclerViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).canLoadMore()
                        && recyclerView.getAdapter().getItemCount() - 1 == ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findLastVisibleItemPosition()) {
                    if (!((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getLoadMoreError()) {
                        if (null != loadMoreListener) {
                            ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(2, false);
                            loadMoreListener.startLoadMore();
                        }
                    }
                }
            }
        });
        binding.recyclerView.setOnTouchListener(refreshTouchListener);
    }

    public RecyclerView getRecyclerView() {
        return binding.recyclerView;
    }

    public void refreshError() {
        binding.tvLoading.setVisibility(INVISIBLE);
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, true);
        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(VISIBLE);
        }
    }

    public void startRefresh() {
        if (baseRefreshListener != null) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsAll(false, false);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsRequesting(1, false);
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(1, true);
            binding.tvLoading.setVisibility(VISIBLE);
            if (noDataView != null) {
                noDataView.setVisibility(GONE);
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishRefresh(Boolean needNotify) {
        finishRefresh(((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getICount() == 0, needNotify);
    }

    public void finishRefresh(Boolean isAll, Boolean needNotify) {
        if (isAll) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, false);
            binding.tvLoading.setVisibility(INVISIBLE);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(true, needNotify);
        } else {
            binding.tvLoading.setVisibility(INVISIBLE);
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, needNotify);
        }

        if (isAll) {
            if (noDataView != null) {
                binding.recyclerView.post(() -> {
                    if (((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).getICount() == 0) {
                        noDataView.setVisibility(VISIBLE);
                    } else {
                        noDataView.setVisibility(GONE);
                    }
                });
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishLoadMore(Boolean isAll, Boolean needNotification) {
        if (isAll) {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, false);
            ((RefreshRecyclerViewAdapter) binding.recyclerView.getAdapter()).setIsAll(true, needNotification);
        } else {
            ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setIsRequesting(0, needNotification);
        }
        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(GONE);
        }
    }

    public void setRefreshRecyclerViewAdapter(RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setRefreshRecyclerViewAdapter(View headerView, RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        binding.flContent.addView(headerView, 0);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setItemTouchHelperCallback(ItemTouchHelper.Callback callback) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    public void loadMoreError() {
        binding.tvLoading.setVisibility(INVISIBLE);
        ((RefreshRecyclerViewAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).setLoadMoreError(true, true);
    }

    public void setNoDataAndRefreshErrorView(View noData, View refreshError) {
        if (noData != null) {
            noDataView = noData;
            noDataView.setVisibility(GONE);
            addView(noDataView, getChildCount() - 1);
        }
        if (refreshError != null) {
            refreshErrorView = refreshError;
            addView(refreshErrorView, 2);
            refreshErrorView.setVisibility(GONE);
        }
    }
}