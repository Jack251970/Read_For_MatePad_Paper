package com.jack.bookshelf.base.adapter

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 * Created by Invincible on 2017/12/15.
 */
@Suppress("unused")
class ItemAnimation private constructor() {

    var itemAnimEnabled = false
    var itemAnimFirstOnly = true
    var itemAnimation: BaseAnimation? = null
    var itemAnimInterpolator: Interpolator = LinearInterpolator()
    var itemAnimDuration: Long = 0L
    var itemAnimStartPosition: Int = -1

    fun animation(animation: BaseAnimation? = null) = apply {
        if (animation != null) {
            itemAnimation = animation
        }
    }

    companion object {
        fun create() = ItemAnimation()
    }
}