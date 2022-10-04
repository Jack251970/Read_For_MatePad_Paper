package com.jack.bookshelf.view.popupwindow

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jack.bookshelf.base.adapter.ItemViewHolder
import com.jack.bookshelf.base.adapter.RecyclerAdapter
import com.jack.bookshelf.databinding.ItemPopupKeyboardToolBinding
import com.jack.bookshelf.databinding.PopKeyboardToolBinding
import org.jetbrains.anko.sdk27.listeners.onClick

/**
 * Popup Help Keyboard
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class KeyboardToolPop(
        context: Context,
        private val chars: List<String>,
        val callBack: CallBack?
) : PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {

    private val binding = PopKeyboardToolBinding.inflate(LayoutInflater.from(context))

    init {
        isTouchable = true
        isOutsideTouchable = false
        isFocusable = false
        inputMethodMode = INPUT_METHOD_NEEDED // 解决遮盖输入法
        contentView = binding.root
        initRecyclerView()
    }

    private fun initRecyclerView() = with(contentView) {
        val adapter = Adapter(context)
        binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
        adapter.setItems(chars)
    }

    inner class Adapter(context: Context) : RecyclerAdapter<String, ItemPopupKeyboardToolBinding>(context) {

        override fun getViewBinding(parent: ViewGroup): ItemPopupKeyboardToolBinding {
            return ItemPopupKeyboardToolBinding.inflate(inflater, parent, false)
        }

        override fun convert(holder: ItemViewHolder, binding: ItemPopupKeyboardToolBinding, item: String, payloads: MutableList<Any>) {
            with(binding) {
                textView.text = item
                root.onClick { callBack?.sendText(item) }
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemPopupKeyboardToolBinding) {}
    }

    interface CallBack {
        fun sendText(text: String)
    }
}
