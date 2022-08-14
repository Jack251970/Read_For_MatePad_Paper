package com.jack.bookshelf.model.analyzeRule;

import static com.jack.bookshelf.constant.AppConstant.DEFAULT_USER_AGENT;

import android.content.SharedPreferences;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {
    private static final SharedPreferences preferences = MApplication.getConfigPreferences();

    public static Map<String, String> getDefaultHeader() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("User-Agent", getDefaultUserAgent());
        return headerMap;
    }

    public static String getDefaultUserAgent() {
        return preferences.getString(MApplication.getInstance().getString(R.string.pk_user_agent), DEFAULT_USER_AGENT);
    }
}
