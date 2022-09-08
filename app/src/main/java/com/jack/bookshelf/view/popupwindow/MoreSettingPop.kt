package com.jack.bookshelf.view.popupwindow

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.jack.bookshelf.R
import com.jack.bookshelf.databinding.PopMoreSettingBinding
import com.jack.bookshelf.help.ReadBookControl
import com.jack.bookshelf.view.activity.ReadBookActivity

/**
 * Read More Setting Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class MoreSettingPop : FrameLayout {

    private val readBookControl = ReadBookControl.getInstance()
    private var callback: Callback? = null
    private val binding = PopMoreSettingBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context) {init(context)}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init(context)}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init(context)}

    private fun init(@Suppress("UNUSED_PARAMETER")context: Context) {
        binding.vwBg.setOnClickListener(null)
    }

    fun setListener(callback: Callback) {
        this.callback = callback
        initData()
        bindEvent()
    }

    private fun bindEvent() {
        setOnClickListener { this.visibility = View.GONE }
        binding.ivBackPopMoreSettingMenu.setOnClickListener{ callback?.back() }
        binding.tvScreenDirection.setOnClickListener {
            SelectMenu.builder(context)
                .setTitle(context.getString(R.string.screen_direction))
                .setBottomButton(context.getString(R.string.cancel))
                .setMenu(context.resources.getStringArray(R.array.screen_direction_list_title), readBookControl.screenDirection)
                .setListener(object : SelectMenu.OnItemClickListener {
                    override fun forBottomButton() {}

                    override fun forListItem(last: Int, i: Int) {
                        if (i != last) {
                            readBookControl.screenDirection = i
                            upScreenDirection(i)
                            callback!!.recreate()
                        }
                    }
                }).show(binding.root)
        }
        binding.tvKeepLightTime.setOnClickListener{
            SelectMenu.builder(context)
                .setTitle(context.getString(R.string.keep_light_time))
                .setBottomButton(context.getString(R.string.cancel))
                .setMenu(context.resources.getStringArray(R.array.screen_time_out), readBookControl.screenTimeOut)
                .setListener(object : SelectMenu.OnItemClickListener {
                    override fun forBottomButton() {}

                    override fun forListItem(last: Int, i: Int) {
                        if (i != last) {
                            readBookControl.screenTimeOut = i
                            upScreenTimeOut(i)
                            callback?.keepScreenOnChange(i)
                        }
                    }
                }).show(binding.root)
        }
        binding.swDisableReturnKey.setPreferenceKey("canKeyReturn", false)
            .setAddedListener { checked: Boolean -> readBookControl.setCanKeyReturn(checked) }
        binding.swClickOpenPage.setPreferenceKey("canClickTurn",true)
            .setAddedListener {
                checked: Boolean -> readBookControl.canClickTurn = checked
            }
            .setBindSwitch(binding.swClickAllNextPage);
        binding.swClickAllNextPage.setPreferenceKey("clickAllNext",false)
            .setAddedListener {
                checked: Boolean -> readBookControl.clickAllNext = checked
            }
        binding.swVolumeOpenPage.setPreferenceKey("canKeyTurn",false)
            .setAddedListener {
                checked: Boolean -> readBookControl.canVolumeKeyTurn = checked
            }
            .setBindSwitch(binding.swAloudVolumePage);
        binding.swAloudVolumePage.setPreferenceKey("readAloudCanKeyTurn",false)
            .setAddedListener {
                checked: Boolean -> readBookControl.aloudCanKeyTurn = checked
            }
        binding.swSelectText.setPreferenceKey("canSelectText",false)
            .setAddedListener {
                checked: Boolean -> readBookControl.isCanSelectText = checked
            }
        // 朗读语速调节
        /*binding.scbTtsFollowSys.isChecked = !binding.scbTtsFollowSys.isChecked
        binding.scbTtsFollowSys.setOnCheckedChangeListener { _, isChecked ->
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
        })*/
    }

    private fun initData() {
        upScreenDirection(readBookControl.screenDirection)
        upScreenTimeOut(readBookControl.screenTimeOut)
        // 朗读语速调节 默认跟随系统
        /*binding.scbTtsFollowSys.isChecked = readBookControl.isSpeechRateFollowSys
        binding.hpbTtsSpeechRate.isEnabled = !readBookControl.isSpeechRateFollowSys
        binding.hpbTtsSpeechRate.progress = readBookControl.speechRate - 5
        //CPM范围设置 每分钟阅读200字到2000字 默认500字/分钟
        binding.hpbClick.max = readBookControl.maxCPM - readBookControl.minCPM
        binding.hpbClick.progress = readBookControl.cpm
        binding.tvAutoPage.text = String.format("%sCPM", readBookControl.cpm)*/
    }

    private fun upScreenTimeOut(screenTimeOut: Int) {
        binding.tvKeepLightTimeNum.text = context.resources.getStringArray(R.array.screen_time_out)[screenTimeOut]
    }

    private fun upScreenDirection(screenDirection: Int) {
        val screenDirectionListTitle = context.resources.getStringArray(R.array.screen_direction_list_title)
        if (screenDirection >= screenDirectionListTitle.size) {
            binding.tvScreenDirectionCurrent.text = screenDirectionListTitle[0]
        } else {
            binding.tvScreenDirectionCurrent.text = screenDirectionListTitle[screenDirection]
        }
    }

    interface Callback {
        fun keepScreenOnChange(keepScreenOn: Int)
        fun recreate()
        fun changeSpeechRate(speechRate: Int)
        fun speechRateFollowSys()
        fun back()
    }
}