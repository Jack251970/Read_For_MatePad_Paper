package com.jack.bookshelf.constant;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.os.Environment;

import com.google.gson.reflect.TypeToken;
import com.jack.bookshelf.BuildConfig;
import com.jack.bookshelf.help.FileHelp;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class AppConstant {
    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";

    public static final long TIME_OUT = BuildConfig.DEBUG ? 600 : 180;

    // Book Date Convert Format
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    // Download Path
    public static final File APK_DOWNLOAD_File = Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE);

    // BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileHelp.getFilesPath() + File.separator + "book_cache" + File.separator;

    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {}.getType();

    public static final String DEFAULT_WEB_DAV_URL = "https://dav.jianguoyun.com/dav/";

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36";

    public static final Pattern JS_PATTERN = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);
    public static final Pattern EXP_PATTERN = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}");

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");
}