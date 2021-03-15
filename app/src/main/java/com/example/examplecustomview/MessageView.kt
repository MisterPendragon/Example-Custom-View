package com.example.examplecustomview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

class MessageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private val roundRadius = 16f.dp2px()
        private val avatarSize = 45f.dp2px()
        private val padding = 8f.dp2px()
        private val usernameFontSize = ReactionView.fontSize * 9 / 10
        private val messageFontSize = ReactionView.fontSize
        private const val usernameFontColor = Color.GREEN
        private const val messageFontColor = ReactionView.fontColor
        private const val chatBgColor = ReactionView.unselColor

    }

    private var roundedAvatarDrawable: RoundedBitmapDrawable? = null
    private val avatarBoundary = RectF(0f, 0f, avatarSize, avatarSize)

    private val usernamePaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.LEFT
        color = usernameFontColor
        textSize = usernameFontSize
    }
    private val messagePaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.LEFT
        color = messageFontColor
        textSize = messageFontSize
    }
    private var usernameWidth: Float = 0f
    private var usernameHeight: Float = 0f
    private val messageLines = arrayListOf<String>()
    private var messageWidth: Float = 0f
    private var messageHeight: Float = 0f

    private val chatPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        color = chatBgColor
    }
    private val chatWidth
        get() = maxOf(usernameWidth, messageWidth) + padding * 2
    private val chatHeight
        get() = messageHeight + usernameHeight + padding * 2
    private val chatBoundary = RectF()
        get() = field.apply {
            set(avatarSize + padding, 0f, avatarSize + padding + chatWidth, chatHeight)
        }

    private val reactionLayout = ReactionLayout(context).apply {
        setPadding((avatarSize + padding).toInt(), 0, 0, 0)
    }


    private var username: String = "username"
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }
    private var message: String = "message"
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MessageView,
            defStyleAttr, 0
        ).apply {

            try {
                username = getString(R.styleable.MessageView_username) ?: username
                message = getString(R.styleable.MessageView_message) ?: message
            } finally {
                recycle()
            }
        }

        isClickable = false

        // draw avatar & message by onDraw()
        setWillNotDraw(false)

        // добавим reactionLayout
        addView(reactionLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }
    fun addReaction(emoji: String, count: Int) {
        reactionLayout.addReactionView(ReactionView.generate(context, emoji, count))
    }

    fun setOnAddReactionClickedListener(listener: () -> Unit) {
        (reactionLayout.getChildAt(reactionLayout.childCount - 1) as ReactionAddView).setOnClickListener { listener() }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val reactionLayoutTop = chatHeight.toInt()
        reactionLayout.layout(0, reactionLayoutTop,
            reactionLayout.measuredWidth,
            reactionLayoutTop + reactionLayout.measuredHeight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        measureUsername()
        measureMessage()

        val reactionHeight = reactionLayout.measuredHeight
        val maxHeight = maxOf(avatarSize, chatHeight + reactionHeight)

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSizeAndState(maxHeight.toInt(), heightMeasureSpec, 0)
        )
    }

    override fun onDraw(canvas: Canvas) {
        // аватарка
        roundedAvatarDrawable?.let {
            it.setBounds(avatarBoundary.left.toInt(), avatarBoundary.top.toInt(), avatarBoundary.right.toInt(), avatarBoundary.bottom.toInt())
            it.draw(canvas)
        } ?: canvas.drawRoundRect(avatarBoundary, avatarSize, avatarSize, chatPaint)

        // Бэкграунд чата
        canvas.drawRoundRect(chatBoundary, roundRadius, roundRadius, chatPaint)

        // имя пользователя
        canvas.drawText(username, avatarSize + padding * 2, padding - usernamePaint.fontMetrics.top, usernamePaint)

        // сообщение
        val lineHeight = with(messagePaint.fontMetrics) { bottom - top }
        val lineOffset = messagePaint.fontMetrics.top
        messageLines.forEachIndexed { index, line ->
            canvas.drawText(line, avatarSize + padding * 2, padding + usernameHeight + lineHeight * index - lineOffset, messagePaint)
        }
    }

    private fun measureUsername() {
        usernameWidth = usernamePaint.measureText(username)
        usernameHeight = with(usernamePaint.fontMetrics) { bottom - top }
    }

    private fun measureMessage() {
        val availableWidth = measuredWidth - (avatarSize + padding * 3)
        if (availableWidth < 0) return


        messageLines.clear()
        messageLines.add("")
        var curWidth = 0f
        var cursor = 0
        while (cursor < message.length) {
            val boundary = "\\b".toRegex().find(message, cursor + 1) ?: break
            val rangeStr = message.substring(cursor, boundary.range.last + 1)
            val rangeWidth = messagePaint.measureText(rangeStr)
            if (curWidth + rangeWidth <= availableWidth) {
                if (messageLines.last().isNotEmpty() || rangeStr.isNotEmpty()) {
                    messageLines[messageLines.lastIndex] += rangeStr
                }
                cursor = boundary.range.last + 1
                curWidth += rangeWidth
            } else if (messageLines.last().isEmpty()) {
                for (i in cursor + 1..boundary.range.last + 1) {
                    if (messagePaint.measureText(message.substring(cursor, i)) > availableWidth) {
                        messageLines[messageLines.lastIndex] = message.substring(cursor, i - 1)
                        cursor = i - 1
                        break
                    }
                }
            } else {
                messageLines[messageLines.lastIndex] = messageLines.last().trim()
                curWidth = 0f
                messageLines.add("")
                if (rangeStr.isBlank()) {
                    cursor = boundary.range.last + 1
                }
            }
        }

        messageWidth = messageLines.maxOf { messagePaint.measureText(it) }
        messageHeight = with(messagePaint.fontMetrics) { bottom - top } * messageLines.size
    }
}