package com.jack.bookshelf.model;

import android.text.TextUtils;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.BaseModelImpl;
import com.jack.bookshelf.bean.ReplaceRuleBean;
import com.jack.bookshelf.dao.ReplaceRuleBeanDao;
import com.jack.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.jack.bookshelf.model.impl.IHttpGetApi;
import com.jack.bookshelf.utils.GsonUtils;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by GKF on 2018/2/12.
 * 替换规则管理
 */

public class ReplaceRuleManager {
    private static List<ReplaceRuleBean> replaceRuleBeansEnabled;

    public static List<ReplaceRuleBean> getEnabled() {
        if (replaceRuleBeansEnabled == null) {
            replaceRuleBeansEnabled = DbHelper.getDaoSession()
                    .getReplaceRuleBeanDao().queryBuilder()
                    .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                    .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                    .list();
        }
        return replaceRuleBeansEnabled;
    }

    /**
     * 合并广告话术规则
     */
    public static Single<Boolean> mergeAdRules(ReplaceRuleBean replaceRuleBean) {
        String rule = formatAdRule(replaceRuleBean.getRegex());

        int sn = replaceRuleBean.getSerialNumber();
        if (sn == 0) {
            sn = (int) (DbHelper.getDaoSession().getReplaceRuleBeanDao().queryBuilder().count() + 1);
            replaceRuleBean.setSerialNumber(sn);
        }

        List<ReplaceRuleBean> list = DbHelper.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .where(ReplaceRuleBeanDao.Properties.ReplaceSummary.eq(replaceRuleBean.getReplaceSummary()))
                .where(ReplaceRuleBeanDao.Properties.SerialNumber.notEq(sn))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
        if (list.size() < 1) {
            replaceRuleBean.setRegex(rule);
            return saveData(replaceRuleBean);
        } else {
            StringBuilder buffer = new StringBuilder(rule);
            for (ReplaceRuleBean li : list) {
                buffer.append('\n');
                buffer.append(li.getRegex());
            }
            replaceRuleBean.setRegex(formatAdRule(buffer.toString()));

            return Single.create((SingleOnSubscribe<Boolean>) emitter -> {

                DbHelper.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
                for (ReplaceRuleBean li : list) {
                    DbHelper.getDaoSession().getReplaceRuleBeanDao().delete(li);
                }
                refreshDataS();
                emitter.onSuccess(true);
            }).compose(RxUtils::toSimpleSingle);
        }
    }

    /**
     * 把输入的规则进行预处理（分段、排序、去重）。保存的是普通多行文本。
     */
    public static String formatAdRule(String rule) {
        if (rule == null)
            return "";
        String result = rule.trim();
        if (result.length() < 1)
            return "";

        String string = rule
//                用中文中的.视为。进行分段
                .replaceAll("(?<=([^a-zA-Z\\p{P}]{4,8}))\\.+(?![^a-zA-Z\\p{P}]{4,8})","\n")
//                用常见的适合分段的标点进行分段，句首句尾除外
//                .replaceAll("([^\\p{P}\n^])([…,，:：？。！?!~<>《》【】（）()]+)([^\\p{P}\n$])", "$1\n$3")
//                表达式无法解决句尾连续多个符号的问题
//                .replaceAll("[…,，:：？。！?!~<>《》【】（）()]+(?!\\s*\n|$)", "\n")
                .replaceAll("(?<![\\p{P}\n^])([…,，:：？。！?!~<>《》【】（）()]+)(?![\\p{P}\n$])", "\n")

                ;

        String[] lines = string.split("\n");
        List<String> list = new ArrayList<>();

        for (String s : lines) {
            s = s.trim();
            if (!list.contains(s)) {
                list.add(s);
            }
        }
        Collections.sort(list);
        StringBuilder buffer = new StringBuilder(rule.length() + 1);
        for (int i = 0; i < list.size(); i++) {
            buffer.append('\n');
            buffer.append(list.get(i));
        }
        return buffer.toString().trim();
    }

    public static Single<List<ReplaceRuleBean>> getAll() {
        return Single.create((SingleOnSubscribe<List<ReplaceRuleBean>>) emitter -> emitter.onSuccess(DbHelper.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list())).compose(RxUtils::toSimpleSingle);
    }

    public static Single<Boolean> saveData(ReplaceRuleBean replaceRuleBean) {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (replaceRuleBean.getSerialNumber() == 0) {
                replaceRuleBean.setSerialNumber((int) (DbHelper.getDaoSession().getReplaceRuleBeanDao().queryBuilder().count() + 1));
            }
            DbHelper.getDaoSession().getReplaceRuleBeanDao().insertOrReplace(replaceRuleBean);
            refreshDataS();
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static void delData(ReplaceRuleBean replaceRuleBean) {
        DbHelper.getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        refreshDataS();
    }

    public static void addDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            DbHelper.getDaoSession().getReplaceRuleBeanDao().insertOrReplaceInTx(replaceRuleBeans);
            refreshDataS();
        }
    }

    public static void delDataS(List<ReplaceRuleBean> replaceRuleBeans) {
        for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
            DbHelper.getDaoSession().getReplaceRuleBeanDao().delete(replaceRuleBean);
        }
        refreshDataS();
    }

    private static void refreshDataS() {
        replaceRuleBeansEnabled = DbHelper.getDaoSession()
                .getReplaceRuleBeanDao().queryBuilder()
                .where(ReplaceRuleBeanDao.Properties.Enable.eq(true))
                .orderAsc(ReplaceRuleBeanDao.Properties.SerialNumber)
                .list();
    }

    public static Observable<Boolean> importReplaceRule(String text) {
        if (TextUtils.isEmpty(text)) return null;
        text = text.trim();
        if (text.length() == 0) return null;
        if (StringUtils.isJsonType(text)) {
            return importReplaceRuleO(text)
                    .compose(RxUtils::toSimpleSingle);
        }
        if (NetworkUtils.isUrl(text)) {
            return BaseModelImpl.getInstance().getRetrofitString(StringUtils.getBaseUrl(text), "utf-8")
                    .create(IHttpGetApi.class)
                    .get(text, AnalyzeHeaders.getDefaultHeader())
                    .flatMap(rsp -> importReplaceRuleO(rsp.body()))
                    .compose(RxUtils::toSimpleSingle);
        }
        return Observable.error(new Exception(StringUtils.getString(R.string.not_json_or_url_format)));
    }

    private static Observable<Boolean> importReplaceRuleO(String json) {
        return Observable.create(e -> {
            try {
                List<ReplaceRuleBean> replaceRuleBeans = GsonUtils.parseJArray(json, ReplaceRuleBean.class);
                addDataS(replaceRuleBeans);
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }
}
