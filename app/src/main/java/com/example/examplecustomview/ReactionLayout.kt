package com.example.examplecustomview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.postDelayed

class ReactionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    companion object {
        private val GAP = 8f.dp2px().toInt()
    }

    init {
        addReactionView(ReactionAddView(context))
    }

    fun addReactionView(view: View) {
        addView(view, childCount - 1)
        postDelayed(1) { requestLayout() }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l - paddingLeft - paddingRight
        var curX = 0
        var curY = GAP
        children.forEach { child ->
            if (child.visibility != GONE) {
                if (curX > 0 && curX + child.measuredWidth > w) {
                    curX = 0
                    curY += child.measuredHeight + GAP
                }

                val childRight = curX + child.measuredWidth + paddingLeft
                val childBottom = curY + child.measuredHeight
                child.layout(curX + paddingLeft, curY, childRight, childBottom)

                curX += child.measuredWidth + GAP
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        // Find bottom-most child
        var maxHeight: Int = with(children.last()) { y.toInt() + height }

        // Account for padding too
        maxHeight += paddingTop + paddingBottom

        // Check against minimum height and width
        maxHeight = maxHeight.coerceAtLeast(suggestedMinimumHeight)

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSizeAndState(maxHeight, heightMeasureSpec, 0)
        )
    }
}