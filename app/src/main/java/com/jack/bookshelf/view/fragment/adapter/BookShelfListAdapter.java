package com.jack.bookshelf.view.fragment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.adapter.base.OnItemClickListenerTwo;
import com.jack.bookshelf.widget.BadgeView;
import com.jack.bookshelf.widget.RotateLoading;
import com.jack.bookshelf.widget.image.CoverImageView;
import com.jack.bookshelf.widget.views.ATECheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * List Bookshelf Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("NotifyDataSetChanged")
public class BookShelfListAdapter extends RecyclerView.Adapter<BookShelfListAdapter.MyViewHolder> implements BookShelfAdapter {
    private boolean isArrange;
    private final Activity activity;
    private List<BookShelfBean> books;
    private OnItemClickListenerTwo itemClickListener;
    private int bookshelfPx;
    private final HashSet<String> selectList = new HashSet<>();

    private final ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {}

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            return true;
        }
    };


    public BookShelfListAdapter(Activity activity) {
        this.activity = activity;
        books = new ArrayList<>();
    }


    @Override
    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
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
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        final BookShelfBean bookShelfBean = books.get(index);
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
            holder.vwSelect.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        if (!activity.isFinishing()) {
            holder.ivCover.load(bookShelfBean.getCoverPath(), bookShelfBean.getName(), bookShelfBean.getAuthor());
        }
        holder.tvName.setText(bookInfoBean.getName());
        holder.tvAuthor.setText(bookInfoBean.getAuthor());
        holder.tvRead.setText(bookShelfBean.getDurChapterName());
        holder.tvLast.setText(bookShelfBean.getLastChapterName());
        holder.ivCover.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        holder.ivCover.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(v, index);
            }
            return true;
        });
        holder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        if (bookshelfPx == 2) { // 书籍手动排序
            holder.ivCover.setOnClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(view, index);
                }
            });
        } else {
            holder.flContent.setOnLongClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(view, index);
                }
                return true;
            });
        }
        if ((bookshelfPx == 2) && bookShelfBean.getSerialNumber() != index) {
            bookShelfBean.setSerialNumber(index);
            AsyncTask.execute(() -> DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean));
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
        ConstraintLayout flContent;
        CoverImageView ivCover;
        BadgeView bvProgress;
        TextView tvName;
        TextView tvAuthor;
        TextView tvRead;
        TextView tvLast;
        RotateLoading rotateLoading;
        View vwSelect;
        ImageView ivBack;
        ImageView ivEditBook;
        ATECheckBox checkBox;
        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.cv_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            bvProgress = itemView.findViewById(R.id.bv_progress);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            rotateLoading.setLoadingColor(ThemeStore.accentColor(itemView.getContext()));
            vwSelect = itemView.findViewById(R.id.vw_select);
            ivBack = itemView.findViewById(R.id.iv_back);
            ivEditBook = itemView.findViewById(R.id.iv_edit_book);
            checkBox = itemView.findViewById(R.id.checkbox_book);
        }
    }
}