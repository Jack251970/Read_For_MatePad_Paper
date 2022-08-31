package com.jack.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.ReplaceRuleBean;

/**
 * Replace Rule Input Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ReplaceRuleDialog extends BaseDialog {
    private AppCompatEditText tieReplaceSummary;
    private AppCompatEditText tieReplaceRule;
    private AppCompatEditText tieReplaceTo;
    private AppCompatEditText tieUseTo;
    private CheckBox cbUseRegex;
    private TextView tvOk;

    private ReplaceRuleBean replaceRuleBean;
    private final BookShelfBean bookShelfBean;

    private int ReplaceUIMode = 1; // 替换规则编辑UI的模式  1 默认  2 广告话术  3 添加广告话术
    public static int DefaultUI = 1, AdUI = 2, AddAdUI = 3;
    private String str_summary = "";

    public static ReplaceRuleDialog builder(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean, int replaceUIMode) {
        return new ReplaceRuleDialog(context, replaceRuleBean, bookShelfBean, replaceUIMode);
    }

    public static ReplaceRuleDialog builder(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        return new ReplaceRuleDialog(context, replaceRuleBean, bookShelfBean);
    }

    @SuppressLint("InflateParams")
    private ReplaceRuleDialog(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean) {
        super(context, R.style.PaperAlertDialogTheme);
        this.replaceRuleBean = replaceRuleBean;
        this.bookShelfBean = bookShelfBean;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_replace_rule, null);
        bindView(view);
        setContentView(view);
    }

    @SuppressLint("InflateParams")
    private ReplaceRuleDialog(Context context, ReplaceRuleBean replaceRuleBean, BookShelfBean bookShelfBean, int replaceUIMod) {
        super(context, R.style.PaperAlertDialogTheme);
        this.replaceRuleBean = replaceRuleBean;
        this.bookShelfBean = bookShelfBean;
        this.ReplaceUIMode = replaceUIMod;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_replace_rule, null);
        bindView(view);
        setContentView(view);
    }

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tieReplaceRule = view.findViewById(R.id.tie_replace_rule);
        tieReplaceSummary = view.findViewById(R.id.tie_replace_summary);
        tieReplaceTo = view.findViewById(R.id.tie_replace_to);
        tieUseTo = view.findViewById(R.id.tie_use_to);
        cbUseRegex = view.findViewById(R.id.cb_use_regex);
        tvOk = view.findViewById(R.id.tv_ok);
        TextView replace_ad_intro = view.findViewById(R.id.replace_ad_intro);
        TextView tvTitle = view.findViewById(R.id.title);
        View til_replace_to = view.findViewById(R.id.til_replace_to);
        if (replaceRuleBean != null) {
            tieReplaceSummary.setText(replaceRuleBean.getReplaceSummary());
            tieReplaceTo.setText(replaceRuleBean.getReplacement());
            tieReplaceRule.setText(replaceRuleBean.getRegex());
            tieUseTo.setText(replaceRuleBean.getUseTo());
            cbUseRegex.setChecked(replaceRuleBean.getIsRegex());
            // 初始化广告话术规则的UI
            if (ReplaceUIMode == DefaultUI) {
                if (replaceRuleBean.getReplaceSummary().matches("^" + view.getContext().getString(R.string.replace_ad) + ".*"))
                    ReplaceUIMode = AdUI;
            }
            if (ReplaceUIMode > DefaultUI) {
                til_replace_to.setVisibility(View.GONE);
                cbUseRegex.setVisibility(View.GONE);
                replace_ad_intro.setVisibility(View.VISIBLE);
                tieReplaceSummary.setInputType(EditorInfo.TYPE_NULL);
                tieReplaceRule.setMaxLines(8);

                if (ReplaceUIMode == AdUI) {
                    tvTitle.setText(view.getContext().getString(R.string.replace_ad_title));
                } else {
                    tvTitle.setText(view.getContext().getString(R.string.replace_add_ad_title));
                }
                str_summary = view.getContext().getString(R.string.replace_ad);
                TextWatcher mTextWatcher = new TextWatcher() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String str=s.toString().trim();
                        if (str.replaceAll("[\\s,]", "").length() > 0)
                            tieReplaceSummary.setText(str_summary + "-" + str);
                        else
                            tieReplaceSummary.setText(str_summary);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}
                };
                tieUseTo.addTextChangedListener(mTextWatcher);

            }
        } else {
            replaceRuleBean = new ReplaceRuleBean();
            replaceRuleBean.setEnable(true);
            cbUseRegex.setChecked(MApplication.getConfigPreferences().getBoolean("useRegexInNewRule", false));
            if (bookShelfBean != null) {
                tieUseTo.setText(String.format("%s,%s", bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getTag()));
            }
        }

    }

    public ReplaceRuleDialog setPositiveButton(Callback callback) {
        tvOk.setOnClickListener(v -> {
            replaceRuleBean.setReplaceSummary(getEditableText(tieReplaceSummary.getText()));
            replaceRuleBean.setRegex(getEditableText(tieReplaceRule.getText()));
            replaceRuleBean.setIsRegex(cbUseRegex.isChecked());
            replaceRuleBean.setReplacement(getEditableText(tieReplaceTo.getText()));
            replaceRuleBean.setUseTo(getEditableText(tieUseTo.getText()));
            callback.onPositiveButton(replaceRuleBean);
            dismiss();
        });
        return this;
    }

    private String getEditableText(Editable editable) {
        if (editable == null) {
            return "";
        }
        return editable.toString();
    }

    public interface Callback {
        void onPositiveButton(ReplaceRuleBean replaceRuleBean);
    }
}
