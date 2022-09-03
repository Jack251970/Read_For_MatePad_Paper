package com.jack.bookshelf.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.TxtChapterRuleBean;
import com.jack.bookshelf.databinding.ActivityWelcomeBinding;
import com.jack.bookshelf.presenter.ReadBookPresenter;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Welcome Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class WelcomeActivity extends AppCompatActivity {
    private final SharedPreferences preferences = MApplication.getConfigPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        onCreateActivity();
        initData();
    }

    private void onCreateActivity() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AsyncTask.execute(DbHelper::getDaoSession);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (preferences.getBoolean(getString(R.string.pk_default_read), false)) {
                startReadActivity();
            } else {
                startBookshelfActivity();
            }
            finish();
        }).start();
    }

    private void startBookshelfActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void startReadActivity() {
        startActivity(new Intent(this,ReadBookActivity.class).putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP));
    }

    private void initData() {
        if (!preferences.getBoolean("importDefaultBookSource", false)) {
            String json = null;
            try {
                InputStream inputStream = MApplication.getInstance().getAssets()
                        .open("defaultData/bookSource.json");
                json = IOUtils.toString(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<BookSourceBean> sourceDefaultList = GsonUtils.parseJArray(json, BookSourceBean.class);
            if (sourceDefaultList != null) {
                DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(sourceDefaultList);
            }
            preferences.edit()
                    .putBoolean("importDefaultBookSource", sourceDefaultList != null)
                    .apply();
        }
        if (!preferences.getBoolean("importDefaultTxtRule", false)) {
            String json = null;
            try {
                InputStream inputStream = MApplication.getInstance().getAssets().open("defaultData/txtChapterRule.json");
                json = IOUtils.toString(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<TxtChapterRuleBean> ruleDefaultList = GsonUtils.parseJArray(json, TxtChapterRuleBean.class);
            if (ruleDefaultList != null) {
                DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(ruleDefaultList);
            }
            preferences.edit()
                    .putBoolean("importDefaultTxtRule", ruleDefaultList != null)
                    .apply();
        }
    }
}