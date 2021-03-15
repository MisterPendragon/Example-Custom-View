package com.example.examplecustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

internal class ReactionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        internal val naturalPadding = 8f.dp2px()
        internal val fontSize = 13f.sp2px()
        internal const val fontColor = Color.WHITE
        internal const val selColor = Color.DKGRAY
        internal const val unselColor = Color.GRAY

        fun generate(context: Context, emoji: String, count: Int): ReactionView {
            return ReactionView(context).apply {
                this.emoji = emoji
                this.count = count
            }
       }
    }

    private val content: String
        get() = "$emoji $count"
    private val textPaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
    }
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    private var background = GradientDrawable()

    var emoji: String = "😀"
        set(value) {
            if (field == value) return
            field = if (value.codePointCount(0, value.length) > 1) {
                convertUnicode2Emoji(value)
            } else {
                value
            }
            invalidateTextPaintAndMeasurements()
        }
    var count: Int = 1
        set(value) {
            if (field == value) return
            field = value
            invalidateTextPaintAndMeasurements()
        }
    private var stateSelected: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            invalidateBackground()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ReactionView,
            defStyleAttr, 0
        ).apply {

            try {
                emoji = getString(R.styleable.ReactionView_emoji) ?: emoji
            } finally {
                recycle()
            }
        }

        isClickable = true
        setOnClickListener { stateSelected = !stateSelected }
        invalidateTextPaintAndMeasurements()
        invalidateBackground()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            it.textSize = fontSize
            it.color = fontColor
            textWidth = it.measureText(content)
            textHeight = it.fontMetrics.bottom - it.fontMetrics.top
        }

    }

    private fun invalidateBackground() {
        background = GradientDrawable().apply {
            cornerRadius = 8f.dp2px()
            setColor(selColor.takeIf { stateSelected } ?: unselColor)
        }
        setBackground(background)
    }

    override fun onDraw(canvas: Canvas?) {
        // Рисуем текст
        canvas!!.drawText(
            content,
            width / 2f,
            naturalPadding - textPaint.fontMetricsInt.top,
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = textWidth + naturalPadding * 2
        val h = textHeight + naturalPadding * 2
        setMeasuredDimension(w.toInt(), h.toInt())
    }

    private fun convertUnicode2Emoji(unicodeText: String): String {
        if (!unicodeText.matches("(\\\\x[\\dA-Fa-f]{2})+".toRegex())) {
            throw IllegalArgumentException("Invalid unicode for emoji (\"$unicodeText\")")
        }

        val byteArray = ByteArray(unicodeText.length / 4)
        for (i in unicodeText.indices step 4) {
            byteArray[i / 4] = unicodeText.substring(i + 2, i + 4).toLong(16).toByte()
        }
        return String(byteArray)
    }

}