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
import com.jack.bookshelf.utils.*
import com.jack.bookshelf.view.activity.ReadBookActivity
import com.jack.bookshelf.widget.font.FontSelectorDialog
import com.jack.bookshelf.widget.font.FontSelectorDialog.OnThisListener
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

    constructor(context: Context) : super(context) {init()}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init()}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init()}

    private fun init() {binding.vwBg.setOnClickListener(null)}

    fun setListener(readBookActivity: ReadBookActivity, mainView: View, callback: Callback) {
        activity = readBookActivity
        this.mainView = mainView
        this.callback = callback
        initData()
        bindEvent()
        initLight()
    }

    private fun initData() {binding.nbTextSize.text = String.format("%d", readBookControl.textSize)}

    fun show() {initLight()}

    private fun initLight() {
        binding.hpbLight.progress = readBookControl.light
        binding.scbFollowSys.isChecked = readBookControl.lightFollowSys
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

    /**
     * 控件事件
     */
    @SuppressLint("DefaultLocale")
    private fun bindEvent() {
        // 亮度调节
        binding.scbFollowSys.isChecked = !binding.scbFollowSys.isChecked
        binding.scbFollowSys.setOnCheckedChangeListener { _, isChecked ->
            readBookControl.lightFollowSys = isChecked
            if (isChecked) {
                // 跟随系统
                binding.hpbLight.isEnabled = false
                setScreenBrightness()
            } else {
                // 不跟随系统
                binding.hpbLight.isEnabled = true
                setScreenBrightness(readBookControl.light)
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
                .setTitle(context.getString(R.string.jf_convert))
                .setBottomButton(context.getString(R.string.cancel))
                .setMenu(resources.getStringArray(R.array.convert_s), readBookControl.textConvert)
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
            PermissionsCompat.Builder(activity!!)
                .addPermissions(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
                .rationale(R.string.need_storage_permission_to_backup_book_information)
                .onGranted {
                    kotlin.runCatching {
                        selectFont(
                            DocumentUtils.listFiles(FileUtils.getSdCardPath() + "/Fonts") {
                                it.name.matches(FontSelectorDialog.fontRegex)
                            }
                        )
                    }.onFailure {
                        context.toastOnUi("获取文件出错\n${it.localizedMessage}")
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
                selectFont(it)
            }
        }.onFailure {
            context.toastOnUi("获取文件列表出错\n${it.localizedMessage}")
            Timber.e(it)
        }
    }

    private fun selectFont(docItems: List<FileDoc?>?) {
        FontSelectorDialog(context)
            .setFile(readBookControl.fontPath, docItems)
            .setListener(object : OnThisListener {
                override fun setDefault() {
                    clearFontPath()
                }

                override fun setFontPath(fileDoc: FileDoc) {
                    setReadFonts(fileDoc)
                }
            })
            .show(mainView)
    }

    // 设置字体
    fun setReadFonts(fileDoc: FileDoc) {
        if (fileDoc.isContentScheme) {
            val file = FileUtils.createFileIfNotExist(context.externalFiles, "Fonts", fileDoc.name)
            file.writeBytes(fileDoc.uri.readBytes(context))
            readBookControl.setReadBookFont(file.absolutePath)
        } else {
            readBookControl.setReadBookFont(fileDoc.uri.toString())
        }
        callback!!.refresh()
    }

    // 清除字体
    fun clearFontPath() {
        readBookControl.setReadBookFont(null)
        callback!!.refresh()
    }

    interface Callback {
        fun upTextSize()
        fun upMargin()
        fun refresh()
    }
}