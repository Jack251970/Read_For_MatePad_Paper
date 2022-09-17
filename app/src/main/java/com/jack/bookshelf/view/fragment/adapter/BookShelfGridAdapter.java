package com.jack.bookshelf.view.fragment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.view.adapter.base.OnItemClickListenerTwo;
import com.jack.bookshelf.widget.RotateLoading;
import com.jack.bookshelf.widget.imageview.CoverImageView;
import com.jack.bookshelf.widget.onoff.checkbox.RectCheckBox;
import com.jack.bookshelf.widget.text.BadgeTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Grid Bookshelf Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("NotifyDataSetChanged")
public class BookShelfGridAdapter extends RecyclerView.Adapter<BookShelfGridAdapter.MyViewHolder> implements BookShelfAdapter {
    private boolean isArrange;
    private List<BookShelfBean> books;
    private OnItemClickListenerTwo itemClickListener;
    private int bookshelfPx;
    private final Activity activity;
    private final HashSet<String> selectList = new HashSet<>();

    private final ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {}

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            BookShelfBean shelfBean = books.get(srcPosition);
            books.remove(srcPosition);
            books.add(targetPosition, shelfBean);
            notifyItemMoved(srcPosition, targetPosition);
            int start = srcPosition;
            int end = targetPosition;
            if (start > end) {
                start = targetPosition;
                end = srcPosition;
            }
            notifyItemRangeChanged(start, end - start + 1);
            return true;
        }
    };

    public BookShelfGridAdapter(Activity activity) {
        this.activity = activity;
        books = new ArrayList<>();
    }

    @Override
    public void setArrange(boolean isArrange) {
        selectList.clear();
        this.isArrange = isArrange;
        notifyDataSetChanged();
    }

    @Override
    public void selectAll() {
        if (selectList.size() == books.size()) {
            selectList.clear();
        } else {
            for (BookShelfBean bean : books) {
                selectList.add(bean.getNoteUrl());
            }
        }
        notifyDataSetChanged();
        itemClickListener.onClick(null, 0);
    }

    @Override
    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    @Override
    public void refreshBook(String noteUrl) {
        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).getNoteUrl(), noteUrl)) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        BookShelfBean bookShelfBean = books.get(index);
        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
        if (isArrange) {
            // 启用选择按钮与选择背景
            holder.vwSelect.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
            // 刷新选择状态
            holder.checkBox.setChecked(selectList.contains(bookShelfBean.getNoteUrl()));
            // 设置选择监听
            holder.vwSelect.setOnClickListener(v -> {
                if (selectList.contains(bookShelfBean.getNoteUrl())) {
                    selectList.remove(bookShelfBean.getNoteUrl());
                    holder.checkBox.setChecked(false);
                } else {
                    selectList.add(bookShelfBean.getNoteUrl());
                    holder.checkBox.setChecked(true);
                }
                itemClickListener.onClick(v, index);
            });
        } else {
            // 启用选择按钮与选择背景
            holder.vwSelect.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        holder.tvName.setText(!StringUtils.isTrimEmpty(bookInfoBean.getName()) ? bookInfoBean.getName() : activity.getString(R.string.unknown));
        holder.tvName.setBackgroundColor(Color.WHITE);
        if (!activity.isFinishing()) {
            holder.ivCover.load(bookShelfBean.getCoverPath(), bookShelfBean.getName(), bookShelfBean.getAuthor());
        }
        holder.ivCover.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        holder.tvName.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(view, index);
            }
        });
        if (bookshelfPx != 2) {
            holder.ivCover.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, index);
                }
                return true;
            });
        } else if (bookShelfBean.getSerialNumber() != index) {
            bookShelfBean.setSerialNumber(index);
            new Thread(() -> DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean)).start();
        }
        if (bookShelfBean.isLoading()) {
            holder.bvProgress.setVisibility(View.INVISIBLE);
            holder.rotateLoading.setVisibility(View.VISIBLE);
            holder.rotateLoading.start();
        } else {
            holder.bvProgress.setBadgeProgress(BookshelfHelp.getReadProgress(bookShelfBean));
            holder.bvProgress.setBackground();
            holder.rotateLoading.setVisibility(View.INVISIBLE);
            holder.rotateLoading.stop();
        }
    }

    @Override
    public void setItemClickListener(OnItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public synchronized void replaceAll(List<BookShelfBean> newDataS, int bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        selectList.clear();
        if (null != newDataS && newDataS.size() > 0) {
            BookshelfHelp.order(newDataS, bookshelfPx);
            books = newDataS;
        } else {
            books.clear();
        }
        notifyDataSetChanged();
        if (isArrange) {
            itemClickListener.onClick(null, 0);
        }
    }

    @Override
    public List<BookShelfBean> getBooks() {
        return books;
    }

    @Override
    public HashSet<String> getSelected() {
        return selectList;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CoverImageView ivCover;
        TextView tvName;
        BadgeTextView bvProgress;
        RotateLoading rotateLoading;
        View vwSelect;
        RectCheckBox checkBox;
        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            bvProgress = itemView.findViewById(R.id.bv_progress);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            rotateLoading.setLoadingColor(Color.BLACK);
            vwSelect = itemView.findViewById(R.id.vw_select);
            checkBox = itemView.findViewById(R.id.checkbox_book);
        }
    }
}
