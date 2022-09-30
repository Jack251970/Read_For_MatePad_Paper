package com.jack.bookshelf.view.popupwindow

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.documentfile.provider.DocumentFile
import com.jack.bookshelf.R
import com.jack.bookshelf.databinding.PopReadInterfaceBinding
import com.jack.bookshelf.help.ReadBookControl
import com.jack.bookshelf.help.permission.Permissions
import com.jack.bookshelf.help.permission.PermissionsCompat
import com.jack.bookshelf.utils.DocumentUtils
import com.jack.bookshelf.utils.toastOnUi
import com.jack.bookshelf.view.activity.ReadBookActivity
import com.jack.bookshelf.widget.dialog.FontSelectorDialog
import com.jack.bookshelf.widget.dialog.FontSelectorDialog.OnThisListener
import com.jack.bookshelf.widget.menu.SelectMenu
import timber.log.Timber

/**
 * Read Interface Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class ReadInterfacePop : FrameLayout {
    private val binding = PopReadInterfaceBinding.inflate(LayoutInflater.from(context), this, true)
    private var activity: ReadBookActivity? = null
    private val readBookControl = ReadBookControl.getInstance()
    private var mainView: View? = null
    private var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(readBookActivity: ReadBookActivity, mainView: View, callback: Callback) {
        activity = readBookActivity
        this.mainView = mainView
        this.callback = callback
        initData()
        bindEvent()
        initLight()
    }

    private fun initData() {
        binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
    }

    private fun initLight() {
        binding.hpbLight.progress = readBookControl.light
        if (!readBookControl.lightFollowSys) {
            setScreenBrightness(readBookControl.light)
        }
    }

    private fun setScreenBrightness() {
        val params = activity!!.window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity!!.window.attributes = params
    }

    @Suppress("NAME_SHADOWING")
    fun setScreenBrightness(value: Int) {
        var value = value
        if (value < 1) value = 1
        val params = activity!!.window.attributes
        params.screenBrightness = value * 1.0f / 32f
        activity!!.window.attributes = params
    }

    @SuppressLint("DefaultLocale")
    private fun bindEvent() {
        // 亮度调节
        binding.scbFollowSys.setPreferenceKey("lightFollowSys", true)
            .setAddedListener { checked: Boolean ->
                run {
                    binding.hpbLight.isEnabled = !checked
                    if (checked) {
                        setScreenBrightness()
                    } else {
                        setScreenBrightness(readBookControl.light)
                    }
                }
            }
        binding.hpbLight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (!readBookControl.lightFollowSys) {
                    readBookControl.light = i
                    setScreenBrightness(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        // 字号减
        binding.nbTextSizeDec.setOnClickListener {
            var fontSize = readBookControl.textSize - 1
            if (fontSize < 10) fontSize = 10
            readBookControl.textSize = fontSize
            binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
            callback!!.upTextSize()
        }
        // 字号加
        binding.nbTextSizeAdd.setOnClickListener {
            var fontSize = readBookControl.textSize + 1
            if (fontSize > 40) fontSize = 40
            readBookControl.textSize = fontSize
            binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
            callback!!.upTextSize()
        }
        // 粗/细字体切换
        binding.flTextBold.setOnClickListener {
            readBookControl.textBold = !readBookControl.textBold
            callback!!.upTextSize()
        }
        // 缩进
        binding.flIndent.setOnClickListener {
            SelectMenu.builder(context)
                .setTitle(context.getString(R.string.indent))
                .setBottomButton(context.getString(R.string.cancel))
                .setMenu(resources.getStringArray(R.array.indent), readBookControl.indent)
                .setListener(object : SelectMenu.OnItemClickListener {
                    override fun forBottomButton() {}
                    override fun forListItem(lastChoose: Int, position: Int) {
                        if (position != lastChoose) {
                            readBookControl.indent = position
                            callback!!.refresh()
                        }
                    }
                }).show(mainView)
        }
        // 繁简转换
        binding.llJFConvert.setOnClickListener {
            SelectMenu.builder(context)
                .setTitle(context.getString(R.string.chinese_convert))
                .setBottomButton(context.getString(R.string.cancel))
                .setMenu(resources.getStringArray(R.array.chinese_convert), readBookControl.textConvert)
                .setListener(object : SelectMenu.OnItemClickListener {
                    override fun forBottomButton() {}
                    override fun forListItem(lastChoose: Int, position: Int) {
                        if (position != lastChoose) {
                            readBookControl.textConvert = position
                            callback!!.refresh()
                        }
                    }
                }).show(mainView)
        }
        // 行距单倍
        binding.tvRowDef0.setOnClickListener {
            readBookControl.lineMultiplier = 0.6f
            readBookControl.paragraphSize = 1.5f
            callback!!.upTextSize()
        }
        // 行距双倍
        binding.tvRowDef1.setOnClickListener {
            readBookControl.lineMultiplier = 1.2f
            readBookControl.paragraphSize = 1.8f
            callback!!.upTextSize()
        }
        // 行距三倍
        binding.tvRowDef2.setOnClickListener {
            readBookControl.lineMultiplier = 1.8f
            readBookControl.paragraphSize = 2.0f
            callback!!.upTextSize()
        }
        // 行距默认
        binding.tvRowDef.setOnClickListener {
            readBookControl.lineMultiplier = 1.0f
            readBookControl.paragraphSize = 1.8f
            callback!!.upTextSize()
        }
        // 间距设置选项
        binding.tvSpace.setOnClickListener { activity!!.readAdjustMarginIn() }
        // 选择字体
        binding.flTextFont.setOnClickListener {
            PermissionsCompat.Builder(activity!!, mainView!!)
                .addPermissions(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
                .rationale(R.string.need_storage_permission_to_backup_book_information)
                .onGranted {
                    kotlin.runCatching {
                        selectFont()
                    }.onFailure {
                        context.toastOnUi(context.getString(R.string.get_file_list_error,it.localizedMessage))
                    }
                }
                .request()
        }
    }

    fun showFontSelector(uri: Uri) {
        kotlin.runCatching {
            val doc = DocumentFile.fromTreeUri(context, uri)
            DocumentUtils.listFiles(doc!!.uri) {
                it.name.matches(FontSelectorDialog.fontRegex)
            }.let {
                selectFont()
            }
        }.onFailure {
            context.toastOnUi(context.getString(R.string.get_file_list_error,it.localizedMessage))
            Timber.e(it)
        }
    }

    private fun selectFont() {
        FontSelectorDialog(context)
            .setListener(object : OnThisListener {
                override fun forMenuItem(item: Int) {
                    readBookControl.fontItem = item
                    callback!!.refresh()
                }

                override fun forBottomButton() {
                    activity!!.selectFontDir()
                }
            })
            .show(mainView)
    }

    interface Callback {
        fun upTextSize()
        fun upMargin()
        fun refresh()
    }
}