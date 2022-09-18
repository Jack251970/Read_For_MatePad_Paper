package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.hwangjr.rxbus.RxBus;
import com.jack.basemvplib.BitIntentDataManager;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.bean.SearchHistoryBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivitySearchBookBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.presenter.BookDetailPresenter;
import com.jack.bookshelf.presenter.SearchBookPresenter;
import com.jack.bookshelf.presenter.contract.SearchBookContract;
import com.jack.bookshelf.utils.SoftInputUtil;
import com.jack.bookshelf.view.adapter.SearchBookAdapter;
import com.jack.bookshelf.view.adapter.SearchBookshelfAdapter;
import com.jack.bookshelf.widget.dialog.PaperAlertDialog;
import com.jack.bookshelf.widget.menu.SelectMenu;
import com.jack.bookshelf.widget.recycler.refresh.OnLoadMoreListener;

import java.util.List;
import java.util.Objects;

/**
 * Search Book Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SearchBookActivity extends MBaseActivity<SearchBookContract.Presenter>
        implements SearchBookContract.View, SearchBookshelfAdapter.CallBack {

    private ActivitySearchBookBinding binding;
    private View refreshErrorView;
    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean showHistory;
    private String searchKey;
    private SearchBookshelfAdapter searchBookshelfAdapter;

    public static void startByKey(Context context, String searchKey) {
        Intent intent = new Intent(context, SearchBookActivity.class);
        intent.putExtra("searchKey", searchKey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected SearchBookContract.Presenter initInjector() {
        return new SearchBookPresenter();
    }

    @Override
    protected void onCreateActivity() {
        binding = ActivitySearchBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        searchBookAdapter = new SearchBookAdapter(this);
        searchBookshelfAdapter = new SearchBookshelfAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        // 返回
        binding.ivBackSearchBook.setOnClickListener(v -> {
            SoftInputUtil.hideIMM(getCurrentFocus());
            finish();
        });
        // 初始化搜索框
        initSearchView();
        // 更多选项
        binding.ivMoreSettingsSearchBook.setOnClickListener(v -> {
            List<String> groupList = BookSourceManager.getEnableGroupList();
            int last_choose = 0;
            groupList.add(0,getString(R.string.all_source));
            if (MApplication.SEARCH_GROUP != null) {
                for (int i = 0; i < groupList.size(); i++) {
                    if (groupList.get(i).equals(MApplication.SEARCH_GROUP)) {
                        last_choose = i;
                        break;
                    }
                }
            }
            SelectMenu.builder(getContext())
                    .setTitle(getString(R.string.book_source_search_range))
                    .setBottomButton(getString(R.string.cancel))
                    .setMenu(groupList,last_choose)
                    .setListener(new SelectMenu.OnItemClickListener() {
                        @Override
                        public void forBottomButton() {}

                        @Override
                        public void forListItem(int lastChoose, int position) {
                            if (lastChoose != position) {
                                if (position == 0) {
                                    MApplication.SEARCH_GROUP = null;
                                } else {
                                    MApplication.SEARCH_GROUP = groupList.get(position);
                                }
                                mPresenter.initSearchEngineS(MApplication.SEARCH_GROUP);
                            }
                        }
                    }).show(binding.getRoot());
        });
        // 主界面无事件
        binding.llSearchHistory.setOnClickListener(null);
        binding.rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));
        // 未搜索到相关书籍
        binding.rfRvSearchBooks.setNoDataAndRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_refresh_no_data, null),
                refreshErrorView);
        refreshErrorView = LayoutInflater.from(this).inflate(R.layout.view_refresh_error, null);
        // 重新刷新
        refreshErrorView.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> toSearch());
        // 搜索结果书籍点击
        searchBookAdapter.setItemClickListener((view, position) -> {
            String dataKey = String.valueOf(System.currentTimeMillis());
            Intent intent = new Intent(SearchBookActivity.this, BookDetailActivity.class);
            intent.putExtra("openFrom", BookDetailPresenter.FROM_SEARCH);
            intent.putExtra("data_key", dataKey);
            BitIntentDataManager.getInstance().putData(dataKey, searchBookAdapter.getItemData(position));
            startActivity(intent);
        });
        // 停止刷新
        binding.fabSearchStop.setOnClickListener(view -> {
            binding.fabSearchStop.setVisibility(View.INVISIBLE);
            mPresenter.stopSearch();
        });
        // 书架建议
        binding.rvBookshelf.setLayoutManager(new FlexboxLayoutManager(this));
        binding.rvBookshelf.setAdapter(searchBookshelfAdapter);
    }

    @Override
    public void onBackPressed() {super.onBackPressed();}

    /**
     * 初始化搜索栏
     */
    private void initSearchView() {
        mSearchAutoComplete = binding.searchView.findViewById(R.id.search_src_text);
        binding.searchView.setQueryHint(getString(R.string.search_book_name_or_author));
        // 获取到TextView的控件
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mSearchAutoComplete.setPadding(15, 0, 0, 0);
        binding.searchView.onActionViewExpanded();
        LinearLayout editFrame = binding.searchView.findViewById(R.id.search_edit_frame);
        ImageView closeButton = binding.searchView.findViewById(R.id.search_close_btn);
        ImageView goButton = binding.searchView.findViewById(R.id.search_go_btn);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editFrame.getLayoutParams();
        params.setMargins(20, 0, 10, 0);
        editFrame.setLayoutParams(params);
        closeButton.setScaleX(0.9f);
        closeButton.setScaleY(0.9f);
        closeButton.setPadding(0, 0, 0, 0);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setImageResource(R.drawable.ic_close);
        goButton.setScaleX(0.8f);
        goButton.setScaleY(0.8f);
        goButton.setPadding(0, 0, 0, 0);
        goButton.setBackgroundColor(Color.TRANSPARENT);
        goButton.setImageResource(R.drawable.ic_search);
        binding.searchView.setSubmitButtonEnabled(true);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query))
                    return false;
                searchKey = query.trim();
                if (!searchKey.toLowerCase().startsWith("set:")) {
                    toSearch();
                    binding.searchView.clearFocus();
                } else {
                    parseSecretCode(searchKey);
                    finish();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    List<BookInfoBean> beans = BookshelfHelp.searchBookInfo(newText);
                    searchBookshelfAdapter.setItems(beans);
                    if (beans.size() > 0) {
                        binding.tvBookshelf.setVisibility(View.VISIBLE);
                        binding.rvBookshelf.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvBookshelf.setVisibility(View.GONE);
                        binding.rvBookshelf.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvBookshelf.setVisibility(View.GONE);
                    binding.rvBookshelf.setVisibility(View.GONE);
                }
                if (!Objects.requireNonNull(newText).toLowerCase().startsWith("set")) {
                    mPresenter.querySearchHistory(newText);
                } else {
                    showHideSetting();
                }
                return false;
            }
        });
        binding.searchView.setOnQueryTextFocusChangeListener((view, b) -> {
            showHistory = b;
            if (!b && binding.searchView.getQuery().toString().trim().equals("")) {
                finish();
            }
            if (showHistory) {
                binding.fabSearchStop.setVisibility(View.INVISIBLE);
                mPresenter.stopSearch();
            }
            openOrCloseHistory(showHistory);
        });
    }

    @Override
    protected void bindEvent() {
        // 搜索历史的清除按钮
        binding.ivSearchHistoryClean.setOnClickListener(v -> {
            PaperAlertDialog.builder(this)
                    .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                    .setTitle(R.string.delete_all_history)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.delete)
                    .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                        @Override
                        public void forNegativeButton() {}

                        @Override
                        public void forPositiveButton() { mPresenter.cleanSearchHistory();;}
                    }).show(binding.getRoot());
        });
        // 搜索历史其他按钮
        binding.rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                binding.fabSearchStop.setVisibility(View.VISIBLE);
                mPresenter.toSearchBooks(null, false);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                binding.fabSearchStop.setVisibility(View.VISIBLE);
                mPresenter.toSearchBooks(null, true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        Intent intent = this.getIntent();
        searchBook(intent.getStringExtra("searchKey"));
    }

    @Override
    public void onPause() {
        super.onPause();
        showHistory = binding.llSearchHistory.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        openOrCloseHistory(showHistory);
    }

    @Override
    public void searchBook(String searchKey) {
        if (!TextUtils.isEmpty(searchKey)) {
            binding.searchView.setQuery(searchKey, true);
            showHistory = false;
        } else {
            showHistory = true;
            mPresenter.querySearchHistory("");
        }
        openOrCloseHistory(showHistory);
    }

    private void showHideSetting() {
        binding.tflSearchHistory.removeAllViews();
        TextView tagView;
        String[] hideSettings = {"show_nav_shelves", "fade_tts", "use_regex_in_new_rule", "blur_sim_back", "async_draw", "disable_scroll_click_turn"};

        for (String text : hideSettings) {
            tagView = (TextView) getLayoutInflater().inflate(R.layout.item_search_history, binding.tflSearchHistory, false);
            tagView.setTag(text);
            tagView.setText(text);
            tagView.setOnClickListener(view -> {
                String key = "set:" + view.getTag();
                binding.searchView.setQuery(key, false);
            });
            binding.tflSearchHistory.addView(tagView);
        }
    }

    private void parseSecretCode(String code) {
        code = code.toLowerCase().replaceAll("^\\s*set:", "").trim();
        String[] param = code.split("\\s+");
        String msg = null;
        boolean enable = param.length == 1 || !param[1].equals("false");
        switch (param[0]) {
            case "show_nav_shelves":
                MApplication.getConfigPreferences().edit().putBoolean("showNavShelves", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用侧边栏书架！";
                RxBus.get().post(RxBusTag.RECREATE, true);
                break;
            case "fade_tts":
                MApplication.getConfigPreferences().edit().putBoolean("fadeTTS", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用朗读时淡入淡出！";
                break;
            case "use_regex_in_new_rule":
                MApplication.getConfigPreferences().edit().putBoolean("useRegexInNewRule", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用新建替换规则时默认使用正则表达式！";
                break;
            case "blur_sim_back":
                MApplication.getConfigPreferences().edit().putBoolean("blurSimBack", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用仿真翻页背景虚化！";
                break;
            case "async_draw":
                MApplication.getConfigPreferences().edit().putBoolean("asyncDraw", enable).apply();
                msg = "已" + (enable ? "启" : "禁") + "用异步加载！";
                break;
            case "disable_scroll_click_turn":
                MApplication.getConfigPreferences().edit().putBoolean("disableScrollClickTurn", enable).apply();
                msg = "已" + (enable ? "禁" : "启") + "用滚动模式点击翻页！";
                break;
        }
        if (msg == null) {
            toast("无法识别设置密码: " + code, Toast.LENGTH_SHORT);
        } else {
            toast(msg, Toast.LENGTH_SHORT);
        }
    }

    /**
     * 开始搜索
     */
    private void toSearch() {
        if (!TextUtils.isEmpty(searchKey)) {
            mPresenter.insertSearchHistory();
            //执行搜索请求
            new Handler().postDelayed(() -> {
                mPresenter.initPage();
                binding.rfRvSearchBooks.startRefresh();
                binding.fabSearchStop.setVisibility(View.VISIBLE);
                mPresenter.toSearchBooks(searchKey, false);
            }, 0);
        }
    }

    /**
     * 开启或关闭搜索历史
     */
    private void openOrCloseHistory(Boolean open) {
        if (open) {
            if (binding.llSearchHistory.getVisibility() != View.VISIBLE) {
                binding.llSearchHistory.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.llSearchHistory.getVisibility() == View.VISIBLE) {
                binding.llSearchHistory.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 加入新的搜索历史
     */
    private void addNewHistories(List<SearchHistoryBean> historyBeans) {
        binding.tflSearchHistory.removeAllViews();
        if (historyBeans != null) {
            TextView tagView;
            for (SearchHistoryBean searchHistoryBean : historyBeans) {
                tagView = (TextView) getLayoutInflater().inflate(R.layout.item_search_history, binding.tflSearchHistory, false);
                tagView.setTag(searchHistoryBean);
                tagView.setText(searchHistoryBean.getContent());
                tagView.setOnClickListener(view -> {
                    SearchHistoryBean historyBean = (SearchHistoryBean) view.getTag();
                    List<BookInfoBean> beans = BookshelfHelp.searchBookInfo(historyBean.getContent());
                    binding.searchView.setQuery(historyBean.getContent(), beans.isEmpty());
                });
                binding.tflSearchHistory.addView(tagView);
            }
        }
    }

    /**
     * 搜索历史插入或者修改成功
     */
    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        mPresenter.querySearchHistory(searchKey);
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> data) {
        addNewHistories(data);
        if (binding.tflSearchHistory.getChildCount() > 0) {
            binding.ivSearchHistoryClean.setVisibility(View.VISIBLE);
        } else {
            binding.ivSearchHistoryClean.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void refreshSearchBook() {
        searchBookAdapter.upData(SearchBookAdapter.DataAction.CLEAR, null);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        binding.fabSearchStop.setVisibility(View.INVISIBLE);
        binding.rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        binding.fabSearchStop.setVisibility(View.INVISIBLE);
        binding.rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void searchBookError(Throwable throwable) {
        if (searchBookAdapter.getICount() == 0) {
            ((TextView) refreshErrorView.findViewById(R.id.tv_error_msg)).setText(throwable.getMessage());
            binding.rfRvSearchBooks.refreshError();
        } else {
            binding.rfRvSearchBooks.loadMoreError();
        }
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books, mSearchAutoComplete.getText().toString().trim());
    }

    @Override
    protected void onDestroy() {
        mPresenter.stopSearch();
        super.onDestroy();
    }

    @Override
    public EditText getEdtContent() {
        return mSearchAutoComplete;
    }

    @Override
    public SearchBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void finish() { super.finish(); }

    @Override
    public void openBookInfo(BookInfoBean bookInfoBean) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("noteUrl", bookInfoBean.getNoteUrl());
        startActivity(intent);
    }
}
