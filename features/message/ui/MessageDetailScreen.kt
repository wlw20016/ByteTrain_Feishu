package com.bytetrain.feishuclone.features.message.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem

fun createMessageDetailScreen(
    context: Context,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xFFF6F7F9.toInt())

        addView(createConversationHeader(context, density, item, onBack), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))

        addView(ScrollView(context).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(density, 14), dp(density, 12), dp(density, 14), dp(density, 12))

                addView(createOutgoingBubble(context, density, item.detail.body))
                addView(createIncomingBubble(context, density, "收到，我看一下。"))
                addView(createIncomingBubble(context, density, "这个点我们晚点同步确认。"))
                addView(createTimelineText(context, density, item.timestampText))
                addView(createOutgoingBubble(context, density, "好的，我先按这个方向推进。"))
            })
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f,
        ))

        addView(createComposerBar(context, density), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))
    }
}

private fun createConversationHeader(
    context: Context,
    density: Float,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(density, 14), dp(density, 10), dp(density, 14), dp(density, 10))
        setBackgroundColor(0xFFFFFFFF.toInt())

        addView(TextView(context).apply {
            text = "<"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFF111827.toInt())
            isClickable = true
            isFocusable = true
            contentDescription = "返回消息列表"
            setOnClickListener { onBack() }
        }, LinearLayout.LayoutParams(
            dp(density, 36),
            dp(density, 36),
        ).apply {
            rightMargin = dp(density, 6)
        })

        addView(TextView(context).apply {
            text = item.title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF111827.toInt())
            maxLines = 1
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        addView(TextView(context).apply {
            text = "..."
            textSize = 20f
            gravity = Gravity.END
            setTextColor(0xFF6B7280.toInt())
        })
    }

private fun createOutgoingBubble(
    context: Context,
    density: Float,
    text: String,
): View =
    LinearLayout(context).apply {
        gravity = Gravity.END
        setPadding(dp(density, 56), dp(density, 6), 0, dp(density, 6))

        addView(TextView(context).apply {
            this.text = text
            textSize = 15f
            setTextColor(0xFF102A12.toInt())
            setPadding(dp(density, 12), dp(density, 9), dp(density, 12), dp(density, 9))
            background = bubbleBackground(density, 0xFF92F29A.toInt())
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))
    }

private fun createIncomingBubble(
    context: Context,
    density: Float,
    text: String,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.START
        setPadding(0, dp(density, 6), dp(density, 56), dp(density, 6))

        addView(TextView(context).apply {
            this.text = text.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = bubbleBackground(density, 0xFF2F80ED.toInt())
        }, LinearLayout.LayoutParams(
            dp(density, 32),
            dp(density, 32),
        ).apply {
            rightMargin = dp(density, 8)
        })

        addView(TextView(context).apply {
            this.text = text
            textSize = 15f
            setTextColor(0xFF1F2933.toInt())
            setPadding(dp(density, 12), dp(density, 9), dp(density, 12), dp(density, 9))
            background = bubbleBackground(density, 0xFFFFFFFF.toInt())
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))
    }

private fun createTimelineText(
    context: Context,
    density: Float,
    timestampText: String,
): View =
    TextView(context).apply {
        text = timestampText
        textSize = 12f
        gravity = Gravity.CENTER
        setTextColor(0xFF9AA3AF.toInt())
        setPadding(0, dp(density, 12), 0, dp(density, 8))
    }

private fun createComposerBar(
    context: Context,
    density: Float,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(density, 10), dp(density, 8), dp(density, 10), dp(density, 8))
        setBackgroundColor(0xFFFFFFFF.toInt())

        addView(TextView(context).apply {
            text = "+"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFF6B7280.toInt())
        }, LinearLayout.LayoutParams(
            dp(density, 36),
            dp(density, 36),
        ))

        addView(TextView(context).apply {
            text = "输入消息"
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(0xFF9AA3AF.toInt())
            setPadding(dp(density, 12), 0, dp(density, 12), 0)
            background = bubbleBackground(density, 0xFFF1F3F5.toInt())
        }, LinearLayout.LayoutParams(
            0,
            dp(density, 36),
            1f,
        ))

        addView(TextView(context).apply {
            text = "发送"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(0xFF9AA3AF.toInt())
        }, LinearLayout.LayoutParams(
            dp(density, 52),
            dp(density, 36),
        ))
    }

private fun bubbleBackground(density: Float, color: Int): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(density, 8).toFloat()
        setColor(color)
    }

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
