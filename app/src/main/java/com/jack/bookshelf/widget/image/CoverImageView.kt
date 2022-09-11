package com.jack.bookshelf.widget.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jack.bookshelf.R
import com.jack.bookshelf.help.glide.ImageLoader

/**
 * Cover Image View
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class CoverImageView : androidx.appcompat.widget.AppCompatImageView {
    internal var width: Float = 0.toFloat()
    internal var height: Float = 0.toFloat()
    private var nameHeight = 0f
    private var authorHeight = 0f
    private val namePaint = TextPaint()
    private val authorPaint = TextPaint()
    private var name: String? = null
    private var author: String? = null
    private var loadFailed = false
    private val coverRadius = 10f
    private val paint = Paint()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        namePaint.typeface = Typeface.DEFAULT_BOLD
        namePaint.isAntiAlias = true
        namePaint.textAlign = Paint.Align.CENTER
        namePaint.textSkewX = -0.2f
        authorPaint.typeface = Typeface.DEFAULT
        authorPaint.isAntiAlias = true
        authorPaint.textAlign = Paint.Align.CENTER
        authorPaint.textSkewX = -0.1f
        paint.color = Color.BLACK
        paint.isAntiAlias = true    // 抗锯齿
        paint.isDither = true   // 图形抖动处理
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 50f  // 线宽
        paint.strokeCap = Paint.Cap.ROUND   // 端点圆角
        paint.strokeJoin = Paint.Join.ROUND // 圆角连接
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = measuredWidth * 7 / 5
        super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        width = getWidth().toFloat()
        height = getHeight().toFloat()
        namePaint.textSize = width / 6
        namePaint.strokeWidth = namePaint.textSize / 10
        authorPaint.textSize = width / 9
        authorPaint.strokeWidth = authorPaint.textSize / 10
        nameHeight = height / 2
        authorHeight = nameHeight + authorPaint.fontSpacing
    }

    override fun onDraw(canvas: Canvas) {
        if (width >= 10 && height > 10) {
            @SuppressLint("DrawAllocation")
            val path = Path()   // 创建圆角矩形
            path.moveTo(coverRadius, 0f)
            path.lineTo(width - 10, 0f)
            path.quadTo(width, 0f, width, coverRadius)
            path.lineTo(width, height - 10)
            path.quadTo(width, height, width - 10, height)
            path.lineTo(coverRadius, height)
            path.quadTo(0f, height, 0f, height - 10)
            path.lineTo(0f, coverRadius)
            path.quadTo(0f, 0f, coverRadius, 0f)
            // 裁剪圆角矩形的封面
            canvas.clipPath(path)
        }
        super.onDraw(canvas)
        // 绘制边框
        val rectOut = RectF(0f, 0f, width, height)
        val rectIn = RectF(3f, 3f, width - 3, height - 3)
        canvas.drawDoubleRoundRect(rectOut, coverRadius, coverRadius,
            rectIn, coverRadius, coverRadius, paint)
        if (!loadFailed) return
        name?.let {
            namePaint.color = Color.WHITE
            namePaint.style = Paint.Style.STROKE
            canvas.drawText(it, width / 2, nameHeight, namePaint)
            namePaint.color = Color.BLACK
            namePaint.style = Paint.Style.FILL
            canvas.drawText(it, width / 2, nameHeight, namePaint)
        }
        author?.let {
            authorPaint.color = Color.WHITE
            authorPaint.style = Paint.Style.STROKE
            canvas.drawText(it, width / 2, authorHeight, authorPaint)
            authorPaint.color = Color.BLACK
            authorPaint.style = Paint.Style.FILL
            canvas.drawText(it, width / 2, authorHeight, authorPaint)
        }
    }

    private fun setText(name: String?, author: String?) {
        this.name =
                when {
                    name == null -> null
                    name.length > 5 -> name.substring(0, 4) + "…"
                    else -> name
                }
        this.author =
                when {
                    author == null -> null
                    author.length > 8 -> author.substring(0, 7) + "…"
                    else -> author
                }
    }

    fun load(path: String?, name: String?, author: String?) {
        setText(name, author)
        ImageLoader.load(context, path)     //Glide自动识别http://和file://
                .placeholder(R.drawable.image_cover_default)
                .error(R.drawable.image_cover_default)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                    ): Boolean {
                        e?.printStackTrace()
                        loadFailed = true
                        return false
                    }

                    override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                    ): Boolean {
                        loadFailed = false
                        return false
                    }

                })
                .centerCrop()
                .into(this)
    }
}