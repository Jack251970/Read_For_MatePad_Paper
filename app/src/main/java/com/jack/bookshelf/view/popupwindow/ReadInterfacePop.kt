package com.jack.bookshelf.view.popupwindow

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import com.jack.bookshelf.R
import com.jack.bookshelf.databinding.PopReadInterfaceBinding
import com.jack.bookshelf.help.ReadBookControl
import com.jack.bookshelf.help.permission.Permissions
import com.jack.bookshelf.help.permission.PermissionsCompat
import com.jack.bookshelf.utils.*
import com.jack.bookshelf.utils.theme.ATH
import com.jack.bookshelf.view.activity.ReadBookActivity
import com.jack.bookshelf.view.activity.ReadStyleActivity
import com.jack.bookshelf.widget.font.FontSelector
import com.jack.bookshelf.widget.font.FontSelector.OnThisListener
import com.jack.bookshelf.widget.page.animation.PageAnimation
import timber.log.Timber

/**
 * Read Interface Pop Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class ReadInterfacePop : FrameLayout {
    private val binding = PopReadInterfaceBinding.inflate(
        LayoutInflater.from(
            context
        ), this, true
    )
    private var activity: ReadBookActivity? = null
    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(readBookActivity: ReadBookActivity, callback: Callback) {
        activity = readBookActivity
        this.callback = callback
        initData()
        bindEvent()
        initLight()
    }

    @SuppressLint("DefaultLocale")
    private fun initData() {
        setBg()
        updateBg(readBookControl.textDrawableIndex)
        binding.nbTextSize.text = String.format("%d", readBookControl.textSize)
    }

    fun show() {initLight()}

    fun initLight() {
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
        binding.scbFollowSys.setOnCheckedChangeListener { checkBox, isChecked ->
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
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.page_mode))
                .setSingleChoiceItems(
                    resources.getStringArray(R.array.indent),
                    readBookControl.indent
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.indent = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 繁简转换
        binding.llJFConvert.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.jf_convert))
                .setSingleChoiceItems(context.resources.getStringArray(R.array.convert_s), readBookControl.textConvert) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.textConvert = i
                    dialogInterface.dismiss()
                    callback?.refresh()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
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
        // 背景选择
        binding.civBgWhite.setOnClickListener {
            updateBg(0)
            callback!!.bgChange()
        }
        binding.civBgYellow.setOnClickListener {
            updateBg(1)
            callback!!.bgChange()
        }
        binding.civBgGreen.setOnClickListener {
            updateBg(2)
            callback!!.bgChange()
        }
        binding.civBgBlue.setOnClickListener {
            updateBg(3)
            callback!!.bgChange()
        }
        binding.civBgBlack.setOnClickListener {
            updateBg(4)
            callback!!.bgChange()
        }
        // 翻页动画
        binding.tvPageMode.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.page_mode))
                .setSingleChoiceItems(
                    PageAnimation.Mode.getAllPageMode(),
                    readBookControl.pageMode
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.pageMode = i
                    callback!!.upPageMode()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 正文标题
        binding.tvPageTitle.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.page_title))
                .setSingleChoiceItems(
                    arrayOf(
                        context.getString(R.string.show),
                        context.getString(R.string.hide)
                    ),
                    readBookControl.showTitle
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.showTitle = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 页眉
        binding.tvPageHeader.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.ad_page_header))
                .setSingleChoiceItems(
                    arrayOf(
                        context.getString(R.string.header_occasion_show),
                    ),
                    readBookControl.showTimeBattery
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.showTimeBattery = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 页脚
        binding.tvPageFooter.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.ad_page_footer))
                .setSingleChoiceItems(
                    arrayOf(
                        context.getString(R.string.show),
                        context.getString(R.string.hide),
                    ),
                    readBookControl.showFooter
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.showFooter = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 页脚分割线
        binding.tvPageCutOffLine.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.ad_page_cut_off_line))
                .setSingleChoiceItems(
                    arrayOf(
                        context.getString(R.string.show),
                        context.getString(R.string.hide),
                    ),
                    readBookControl.showLine
                ) { dialogInterface: DialogInterface, i: Int ->
                    readBookControl.showLine = i
                    callback!!.refresh()
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        // 自定义阅读样式
        binding.civBgWhite.setOnLongClickListener { customReadStyle(0) }
        binding.civBgYellow.setOnLongClickListener { customReadStyle(1) }
        binding.civBgGreen.setOnLongClickListener { customReadStyle(2) }
        binding.civBgBlue.setOnLongClickListener { customReadStyle(3) }
        binding.civBgBlack.setOnLongClickListener { customReadStyle(4) }
        binding.tvCustomReadingMode.setOnClickListener { customReadStyle(readBookControl.textDrawableIndex) }
        // 选择字体
        binding.flTextFont.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                activity!!.selectFontDir()
            } else {
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
                                    it.name.matches(FontSelector.fontRegex)
                                }
                            )
                        }.onFailure {
                            context.toastOnUi("获取文件出错\n${it.localizedMessage}")
                        }
                    }
                    .request()
            }
        }
        // 长按清除字体
        binding.flTextFont.setOnLongClickListener {
            clearFontPath()
            activity!!.toast(R.string.clear_font)
            true
        }
    }

    fun showFontSelector(uri: Uri) {
        kotlin.runCatching {
            val doc = DocumentFile.fromTreeUri(context, uri)
            DocumentUtils.listFiles(doc!!.uri) {
                it.name.matches(FontSelector.fontRegex)
            }.let {
                selectFont(it)
            }
        }.onFailure {
            context.toastOnUi("获取文件列表出错\n${it.localizedMessage}")
            Timber.e(it)
        }
    }

    private fun selectFont(docItems: List<FileDoc?>?) {
        FontSelector(context, readBookControl.fontPath)
            .setListener(object : OnThisListener {
                override fun setDefault() {
                    clearFontPath()
                }

                override fun setFontPath(fileDoc: FileDoc) {
                    setReadFonts(fileDoc)
                }
            })
            .create(docItems)
            .show()
    }

    // 自定义阅读样式
    private fun customReadStyle(index: Int): Boolean {
        val intent = Intent(activity, ReadStyleActivity::class.java)
        intent.putExtra("index", index)
        activity!!.startActivity(intent)
        return false
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

    fun setBg() {
        binding.tv0.setTextColor(readBookControl.getTextColor(0))
        binding.tv1.setTextColor(readBookControl.getTextColor(1))
        binding.tv2.setTextColor(readBookControl.getTextColor(2))
        binding.tv3.setTextColor(readBookControl.getTextColor(3))
        binding.tv4.setTextColor(readBookControl.getTextColor(4))
        binding.civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity, 100, 180))
        binding.civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity, 100, 180))
        binding.civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity, 100, 180))
        binding.civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity, 100, 180))
        binding.civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity, 100, 180))
    }

    private fun updateBg(index: Int) {
        binding.civBgWhite.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgYellow.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgGreen.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgBlack.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        binding.civBgBlue.borderColor = activity!!.getCompatColor(R.color.tv_text_default)
        when (index) {
            0 -> binding.civBgWhite.borderColor = Color.parseColor("#F3B63F")
            1 -> binding.civBgYellow.borderColor = Color.parseColor("#F3B63F")
            2 -> binding.civBgGreen.borderColor = Color.parseColor("#F3B63F")
            3 -> binding.civBgBlue.borderColor = Color.parseColor("#F3B63F")
            4 -> binding.civBgBlack.borderColor = Color.parseColor("#F3B63F")
        }
        readBookControl.textDrawableIndex = index
    }

    interface Callback {
        fun upPageMode()
        fun upTextSize()
        fun upMargin()
        fun bgChange()
        fun refresh()
    }
}