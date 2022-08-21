package com.jack.basemvplib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.basemvplib.impl.IView;
import com.monke.basemvplib.R;

/**
 * BaseActivity
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public abstract class BaseActivity<T extends IPresenter> extends AppCompatActivity implements IView {
    public final static String START_SHEAR_ELE = "start_with_share_ele";
    public static final int SUCCESS = 1;
    public static final int ERROR = -1;
    protected Bundle savedInstanceState;
    protected T mPresenter;
    protected boolean isRecreate;
    private Boolean startShareAnim = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if(getIntent()!=null){
            isRecreate = getIntent().getBooleanExtra("isRecreate", false);
            startShareAnim = getIntent().getBooleanExtra(START_SHEAR_ELE, false);
        }
        AppActivityManager.getInstance().add(this);
        initSDK();
        onCreateActivity();
        mPresenter = initInjector();
        attachView();
        initData();
        bindView();
        bindEvent();
        firstRequest();
    }

    /**
     * 首次逻辑操作
     */
    protected void firstRequest() {

    }

    /**
     * 事件触发绑定
     */
    protected void bindEvent() {

    }

    /**
     * 控件绑定
     */
    protected void bindView() {

    }

    /**
     * P层绑定V层
     */
    private void attachView() {
        if (null != mPresenter) {
            mPresenter.attachView(this);
        }
    }

    /**
     * P层解绑V层
     */
    private void detachView() {
        if (null != mPresenter) {
            mPresenter.detachView();
        }
    }

    /**
     * SDK初始化
     */
    protected void initSDK() {

    }

    /**
     * P层绑定   若无则返回null;
     */
    protected abstract T initInjector();

    /**
     * 布局载入  setContentView()
     */
    protected abstract void onCreateActivity();

    /**
     * 数据初始化
     */
    protected abstract void initData();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachView();
        AppActivityManager.getInstance().remove(this);
    }

    @Override
    public void recreate() {
        getIntent().putExtra("isRecreate", true);
        super.recreate();
    }

    public Context getContext(){
        return this;
    }

    public Boolean getStart_share_ele() {
        return startShareAnim;
    }

    /******************************************Toast***********************************************/

    public void toast(String msg) {
        toast(msg, Toast.LENGTH_SHORT, 0);
    }

    public void toast(String msg, int state) {
        toast(msg, Toast.LENGTH_LONG, state);
    }

    public void toast(int strId) {
        toast(getString(strId), Toast.LENGTH_SHORT, 0);
    }

    public void toast(int strId, int state) {
        toast(getString(strId), Toast.LENGTH_LONG, state);
    }

    @SuppressLint("InflateParams")
    public void toast(String msg, int length, int state) {
        Toast toast = new Toast(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_toast,null);
        ((TextView)view.findViewById(R.id.mpp_tv_toast)).setText(msg);
        toast.setView(view);
        toast.setDuration(length);
        toast.show();
    }
}