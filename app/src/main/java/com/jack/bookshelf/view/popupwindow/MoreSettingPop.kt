package com.jack.bookshelf.view.popupwindow

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import com.jack.bookshelf.R
import com.jack.bookshelf.databinding.PopMoreSettingBinding
import com.jack.bookshelf.help.ReadBookControl
import com.jack.bookshelf.utils.theme.ATH
import com.jack.bookshelf.widget.modialog.PageKeyDialog
import org.jetbrains.anko.sdk27.listeners.onClick

/**
 * 阅读界面->更多设置界面
 * Copyright (c) 2017. 章钦豪. All rights reserved.
 * Edited by Jack Ye
 */

class MoreSettingPop : FrameLayout {

    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null
    private val binding = PopMoreSettingBinding.inflate(LayoutInflater.from(context),
        this, true)

    constructor(context: Context) : super(context) {init(context)}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init(context)}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init(context)}

    private fun init(context: Context) {binding.vwBg.setOnClickListener(null)}

    fun setListener(callback: Callback) {
        this.callback = callback
        initData()
        bindEvent()
    }

    private fun bindEvent() {
        setOnClickListener { this.visibility = View.GONE }
        //朗读语速调节
        binding.llTtsSpeechRate.setOnClickListener { v ->
            binding.scbTtsFollowSys.setChecked(
                !binding.scbTtsFollowSys.isChecked,
                true
            )
        }
        binding.scbTtsFollowSys.setOnCheckedChangeListener { checkBox, isChecked ->
            if (isChecked) {
                //跟随系统
                binding.hpbTtsSpeechRate.isEnabled = false
                readBookControl.isSpeechRateFollowSys = true
                callback?.speechRateFollowSys()
            } else {
                //不跟随系统
                binding.hpbTtsSpeechRate.isEnabled = true
                readBookControl.isSpeechRateFollowSys = false
                if (callback != null) {
                    callback!!.changeSpeechRate(readBookControl.speechRate)
                }
            }
        }
        binding.hpbTtsSpeechRate.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                readBookControl.speechRate = seekBar.progress + 5
                callback?.changeSpeechRate(readBookControl.speechRate)
            }
        })
        // 自动翻页阅读速度(CPM)设置
        binding.hpbClick.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                binding.tvAutoPage.text = String.format("%sCPM", i + readBookControl.minCPM)
                readBookControl.cpm = i + readBookControl.minCPM
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.sbHideStatusBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideStatusBar = isChecked
                callback?.recreate()
            }
        }
        binding.sbToLh.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.toLh = isChecked
                callback?.recreate()
            }
        }
        binding.sbHideNavigationBar.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.hideNavigationBar = isChecked
                initData()
                callback?.recreate()
            }
        }
        binding.swVolumeNextPage.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.canKeyTurn = b
                upView()
            }
        }
        // 朗读时音量键翻页
        binding.swReadAloudKey.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.aloudCanKeyTurn = b
            }
        }
        // 禁用返回键
        binding.swDisableReturnKey.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                readBookControl.canKeyReturn = b
            }
        }
        binding.sbClick.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.canClickTurn = isChecked
                upView()
            }
        }
        binding.sbClickAllNext.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.clickAllNext = isChecked
            }
        }
        binding.llScreenTimeOut.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.keep_light))
                    .setSingleChoiceItems(
                            context.resources.getStringArray(R.array.screen_time_out),
                            readBookControl.screenTimeOut
                    ) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.screenTimeOut = i
                        upScreenTimeOut(i)
                        callback?.keepScreenOnChange(i)
                        dialogInterface.dismiss()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.llScreenDirection.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.screen_direction))
                    .setSingleChoiceItems(context.resources.getStringArray(R.array.screen_direction_list_title), readBookControl.screenDirection) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.screenDirection = i
                        upScreenDirection(i)
                        dialogInterface.dismiss()
                        callback?.recreate()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.llNavigationBarColor.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.re_navigation_bar_color))
                    .setSingleChoiceItems(context.resources.getStringArray(R.array.NavBarColors), readBookControl.navBarColor) { dialogInterface: DialogInterface, i: Int ->
                        readBookControl.navBarColor = i
                        upNavBarColor(i)
                        dialogInterface.dismiss()
                        callback?.recreate()
                    }
                    .create()
            dialog.show()
            ATH.setAlertDialogTint(dialog)
        }
        binding.sbSelectText.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed) {
                readBookControl.isCanSelectText = isChecked
            }
        }
        binding.llClickKeyCode.onClick {
            PageKeyDialog(context).show()
        }
    }

    private fun initData() {
        upScreenDirection(readBookControl.screenDirection)
        upScreenTimeOut(readBookControl.screenTimeOut)
        upNavBarColor(readBookControl.navBarColor)
        // 朗读语速调节 默认跟随系统
        binding.scbTtsFollowSys.isChecked = readBookControl.isSpeechRateFollowSys
        binding.hpbTtsSpeechRate.isEnabled = !readBookControl.isSpeechRateFollowSys
        binding.hpbTtsSpeechRate.progress = readBookControl.speechRate - 5
        //CPM范围设置 每分钟阅读200字到2000字 默认500字/分钟
        binding.hpbClick.max = readBookControl.maxCPM - readBookControl.minCPM
        binding.hpbClick.progress = readBookControl.cpm
        binding.tvAutoPage.text = String.format("%sCPM", readBookControl.cpm)
        // 禁用返回键
        binding.swDisableReturnKey.isChecked = readBookControl.canKeyReturn
        // 音量键翻页
        binding.swVolumeNextPage.isChecked = readBookControl.canKeyTurn
        // 点击翻页
        binding.sbClick.isChecked = readBookControl.canClickTurn
        binding.sbHideStatusBar.isChecked = readBookControl.hideStatusBar
        binding.sbToLh.isChecked = readBookControl.toLh
        binding.sbHideNavigationBar.isChecked = readBookControl.hideNavigationBar
        upView()
    }

    private fun upView() {
        // 朗读时音量键翻页,canKeyTurn是另一个处理函数,不是错误!
        binding.swReadAloudKey.isEnabled = readBookControl.canKeyTurn
        // 点击总是翻下一页,canClickTurn是另一个处理函数,不是错误!
        binding.sbClickAllNext.isEnabled = readBookControl.canClickTurn
        // 状态栏、导航栏设置
        if (readBookControl.hideNavigationBar) {
            binding.llNavigationBarColor.isEnabled = false
            binding.reNavBarColorVal.isEnabled = false
        } else {
            binding.llNavigationBarColor.isEnabled = true
            binding.reNavBarColorVal.isEnabled = true
        }
    }

    private fun upScreenTimeOut(screenTimeOut: Int) {
        binding.tvScreenTimeOut.text = context.resources.getStringArray(R.array.screen_time_out)[screenTimeOut]
    }

    private fun upScreenDirection(screenDirection: Int) {
        val screenDirectionListTitle = context.resources.getStringArray(R.array.screen_direction_list_title)
        if (screenDirection >= screenDirectionListTitle.size) {
            binding.tvScreenDirection.text = screenDirectionListTitle[0]
        } else {
            binding.tvScreenDirection.text = screenDirectionListTitle[screenDirection]
        }
    }

    private fun upNavBarColor(nColor: Int) {
        binding.reNavBarColorVal.text = context.resources.getStringArray(R.array.NavBarColors)[nColor]
    }

    interface Callback {
        fun upBar()
        fun keepScreenOnChange(keepScreenOn: Int)
        fun recreate()
        fun refreshPage()
        // 朗读语速调节
        fun changeSpeechRate(speechRate: Int)
        fun speechRateFollowSys()
    }
}