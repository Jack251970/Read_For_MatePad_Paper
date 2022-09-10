package com.jack.bookshelf.view.activity

import android.annotation.SuppressLint
import com.jack.basemvplib.BitIntentDataManager
import com.jack.basemvplib.impl.IPresenter
import com.jack.bookshelf.base.MBaseActivity
import com.jack.bookshelf.databinding.ActivityWebViewBinding
import com.jack.bookshelf.utils.theme.ThemeStore

/**
 * WebView Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class WebViewActivity : MBaseActivity<IPresenter>() {

    val binding by lazy {
        ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun initInjector(): IPresenter? {
        return null
    }

    override fun onCreateActivity() {
        window.decorView.setBackgroundColor(ThemeStore.backgroundColor(this))
        setContentView(binding.root)
        binding.ivBackWebView.setOnClickListener{ finish() }
        binding.tvTitleWebView.text = intent.getStringExtra("title")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initData() {
        val settings = binding.webView.settings
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.defaultTextEncodingName = "UTF-8"
        settings.javaScriptEnabled = true
        val url = intent.getStringExtra("url")
        val header = BitIntentDataManager.getInstance().getData(url) as? Map<String, String>
        url?.let {
            if (header == null) {
                binding.webView.loadUrl(url)
            } else {
                binding.webView.loadUrl(url, header)
            }
        }
    }
}