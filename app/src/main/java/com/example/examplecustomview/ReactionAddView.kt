package com.example.examplecustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class ReactionAddView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val plusText = "+"
    }

    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    private val textPaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        textSize = ReactionView.fontSize
        color = ReactionView.fontColor
        textWidth = measureText(plusText)
        textHeight = fontMetrics.bottom - fontMetrics.top
    }

    init {
        background = GradientDrawable().apply {
            cornerRadius = 8f.dp2px()
            setColor(ReactionView.unselColor)
        }
        isClickable = true
    }

    override fun onDraw(canvas: Canvas?) {
        // Рисуем текст
        canvas!!.drawText(
            plusText,
            width / 2f,
            ReactionView.naturalPadding - textPaint.fontMetricsInt.top,
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = textWidth + ReactionView.naturalPadding * 4
        val h = textHeight + ReactionView.naturalPadding * 2
        setMeasuredDimension(w.toInt(), h.toInt())
    }
}