package com.jack.bookshelf.base.adapter

import android.animation.Animator
import android.view.View

/**
 * adapter item 动画
 */
interface BaseAnimation {

    fun getAnimators(view: View): Array<Animator>
}
