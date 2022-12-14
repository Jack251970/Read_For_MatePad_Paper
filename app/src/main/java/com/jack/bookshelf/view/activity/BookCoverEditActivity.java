package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.dao.SearchBookBeanDao;
import com.jack.bookshelf.databinding.ActivityBookCoverEditBinding;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.model.SearchBookModel;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

/**
 * Book Cover Edit Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BookCoverEditActivity extends MBaseActivity<IPresenter> {
    private ActivityBookCoverEditBinding binding;
    private SearchBookModel searchBookModel;
    private String name;
    private String author;
    private Boolean isLoading = true;
    private final List<String> urls = new ArrayList<>();
    private final List<String> origins = new ArrayList<>();

    @Override
    protected void onCreateActivity() {
        binding = ActivityBookCoverEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        binding.ivBack.setOnClickListener(v -> finish());
        name = getIntent().getStringExtra("name");
        author = getIntent().getStringExtra("author");
        ChangeCoverAdapter changeCoverAdapter = new ChangeCoverAdapter();
        binding.rfRvChangeCover.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rfRvChangeCover.setAdapter(changeCoverAdapter);
        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                binding.swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void refreshFinish(Boolean value) {
                binding.swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                if (value) {
                    isLoading = false;
                }
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                if (!value.isEmpty()) {
                    SearchBookBean bookBean = value.get(0);
                    if (bookBean.getName().equals(name)
                            && bookBean.getCoverUrl() != null
                            && !urls.contains(bookBean.getCoverUrl())) {
                        urls.add(bookBean.getCoverUrl());
                        origins.add(bookBean.getOrigin());
                        changeCoverAdapter.notifyItemChanged(urls.size() - 1);
                    }
                }
            }

            @Override
            public void searchBookError(Throwable throwable) {
                binding.swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
        binding.swipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isLoading) {
                isLoading = true;
                long time = System.currentTimeMillis();
                searchBookModel.setSearchTime(time);
                searchBookModel.search(name, time, new ArrayList<>(), false);
            }
        });
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(name), SearchBookBeanDao.Properties.Author.eq(author), SearchBookBeanDao.Properties.CoverUrl.isNotNull())
                    .build().list();
            for (SearchBookBean searchBook : searchBookBeans) {
                BookSourceBean bean = BookSourceManager.getBookSourceByUrl(searchBook.getTag());
                if (bean != null) {
                    String url = searchBook.getCoverUrl();
                    if (url != null && !urls.contains(url)) {
                        urls.add(url);
                        origins.add(searchBook.getOrigin());
                    }
                }
            }
            e.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (urls.isEmpty()) {
                            binding.swipeRefreshLayout.setRefreshing(true);
                            long time = System.currentTimeMillis();
                            searchBookModel.setSearchTime(time);
                            searchBookModel.search(name, time, new ArrayList<>(), false);
                        } else {
                            changeCoverAdapter.notifyDataSetChanged();
                            isLoading = false;
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchBookModel.onDestroy();
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    public class ChangeCoverAdapter extends RefreshRecyclerViewAdapter {
        ChangeCoverAdapter() {
            super(false);
        }

        @Override
        public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_cover, parent, false));
        }

        @Override
        public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.bind(urls.get(position), origins.get(position), holder);
        }

        @Override
        public int getIViewType(int position) {
            return 0;
        }

        @Override
        public int getICount() {
            return urls.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvSourceName;

            MyViewHolder(View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvSourceName = itemView.findViewById(R.id.tv_source_name);
            }

            public void bind(String url, String origin, RecyclerView.ViewHolder holder) {
                tvSourceName.setText(origin);
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .error(R.drawable.image_cover_default)
                        .into(ivCover);
                ivCover.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        }
    }
}
