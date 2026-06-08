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
import com.bytetrain.feishuclone.shared.ui.SharedDetailHeaderStyle
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import com.bytetrain.feishuclone.shared.ui.createSharedDetailHeader
import com.bytetrain.feishuclone.shared.ui.uiDp

fun createMessageDetailScreen(
    context: Context,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xFFF6F7F9.toInt())

        addView(createSharedDetailHeader(
            context = context,
            density = density,
            title = item.title,
            backContentDescription = "返回消息列表",
            onBack = onBack,
            style = SharedDetailHeaderStyle(titleTextColor = 0xFF111827.toInt()),
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))

        addView(ScrollView(context).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(uiDp(density, 14), uiDp(density, 12), uiDp(density, 14), uiDp(density, 12))

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

private fun createOutgoingBubble(
    context: Context,
    density: Float,
    text: String,
): View =
    LinearLayout(context).apply {
        gravity = Gravity.END
        setPadding(uiDp(density, 56), uiDp(density, 6), 0, uiDp(density, 6))

        addView(TextView(context).apply {
            this.text = text
            textSize = 15f
            setTextColor(0xFF102A12.toInt())
            setPadding(uiDp(density, 12), uiDp(density, 9), uiDp(density, 12), uiDp(density, 9))
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
        setPadding(0, uiDp(density, 6), uiDp(density, 56), uiDp(density, 6))

        addView(TextView(context).apply {
            this.text = text.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = bubbleBackground(density, 0xFF2F80ED.toInt())
        }, LinearLayout.LayoutParams(
            uiDp(density, 32),
            uiDp(density, 32),
        ).apply {
            rightMargin = uiDp(density, 8)
        })

        addView(TextView(context).apply {
            this.text = text
            textSize = 15f
            setTextColor(0xFF1F2933.toInt())
            setPadding(uiDp(density, 12), uiDp(density, 9), uiDp(density, 12), uiDp(density, 9))
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
        setPadding(0, uiDp(density, 12), 0, uiDp(density, 8))
    }

private fun createComposerBar(
    context: Context,
    density: Float,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(uiDp(density, 10), uiDp(density, 8), uiDp(density, 10), uiDp(density, 8))
        setBackgroundColor(0xFFFFFFFF.toInt())

        addView(TextView(context).apply {
            text = "+"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFF6B7280.toInt())
        }, LinearLayout.LayoutParams(
            uiDp(density, 36),
            uiDp(density, 36),
        ))

        addView(TextView(context).apply {
            text = "输入消息"
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(0xFF9AA3AF.toInt())
            setPadding(uiDp(density, 12), 0, uiDp(density, 12), 0)
            background = bubbleBackground(density, 0xFFF1F3F5.toInt())
        }, LinearLayout.LayoutParams(
            0,
            uiDp(density, 36),
            1f,
        ))

        addView(TextView(context).apply {
            text = "发送"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(0xFF9AA3AF.toInt())
        }, LinearLayout.LayoutParams(
            uiDp(density, 52),
            uiDp(density, 36),
        ))
    }

private fun bubbleBackground(density: Float, color: Int): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = uiDp(density, 8).toFloat()
        setColor(color)
    }
