package com.jack.bookshelf.widget.dialog

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.jack.bookshelf.R
import com.jack.bookshelf.databinding.DialogLoginBinding
import com.jack.bookshelf.model.BookSourceManager
import com.jack.bookshelf.utils.*
import com.jack.bookshelf.utils.screen.ScreenUtils
import com.jack.bookshelf.utils.viewbindingdelegate.viewBinding
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.sdk27.listeners.onClick

/**
 * Source Login Dialog
 * Partly Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class SourceLoginDialog : DialogFragment() {

    companion object {
        fun start(fragmentManager: FragmentManager, sourceUrl: String) {
            SourceLoginDialog().apply {
                arguments = bundleOf(
                    Pair("sourceUrl", sourceUrl)
                )
            }.show(fragmentManager, "sourceLoginDialog")
        }
    }

    val binding by viewBinding(DialogLoginBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PaperDialogFragment)
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog?.window
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sourceUrl = arguments?.getString("sourceUrl")
        val source = BookSourceManager.getBookSourceByUrl(sourceUrl)
        source ?: let {
            dismiss()
            return
        }
        binding.tvTitleDialogLogin.text = getString(R.string.login_source, source.bookSourceName)
        val loginInfo = source.loginInfoMap
        val loginUi = GSON.fromJsonArray<RowUi>(source.loginUi)
        loginUi?.forEachIndexed { index, rowUi ->
            when (rowUi.type) {
                "text" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root, false)
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextInputLayout).hint = rowUi.name
                        it.findViewById<EditText>(R.id.editText).apply {
                            setText(loginInfo?.get(rowUi.name))
                        }
                    }
                "password" -> layoutInflater.inflate(R.layout.item_source_edit, binding.root, false)
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextInputLayout).hint = rowUi.name
                        it.findViewById<EditText>(R.id.editText).apply {
                            inputType =
                                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                            setText(loginInfo?.get(rowUi.name))
                        }
                    }
                "button" -> layoutInflater.inflate(
                    R.layout.item_source_login_dialog,
                    binding.root,
                    false
                )
                    .let {
                        binding.listView.addView(it)
                        it.id = index
                        (it as TextView).let { textView ->
                            textView.text = rowUi.name
                            textView.setPadding(ScreenUtils.dpToPx(16))
                        }
                        it.onClick {
                            if (rowUi.action.isAbsUrl()) {
                                context?.openUrl(rowUi.action!!)
                            }
                        }
                    }
            }
        }
        binding.ivCheckDialogLogin.setOnClickListener { v ->
            val loginData = hashMapOf<String, String?>()
            loginUi?.forEachIndexed { index, rowUi ->
                when (rowUi.type) {
                    "text", "password" -> {
                        val value = binding.listView.findViewById<TextInputLayout>(index)
                            .findViewById<EditText>(R.id.editText).text?.toString()
                        loginData[rowUi.name] = value
                    }

                }
            }
            source.putLoginInfo(loginData)
            Single.create<String> { emitter ->
                source.loginUrl?.let { loginUrl ->
                    emitter.onSuccess(source.evalJS(loginUrl).toString())
                } ?: let {
                    emitter.onError(Throwable(""))
                }
            }.compose(RxUtils::toSimpleSingle)
                .subscribe(object : SingleObserver<String> {

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: String) {
                        dismiss()
                    }

                    override fun onError(e: Throwable) {

                    }
                })
        }
    }

    data class RowUi(
        var name: String,
        var type: String,
        var action: String?
    )
}