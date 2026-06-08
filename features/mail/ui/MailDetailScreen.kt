package com.bytetrain.feishuclone.features.mail.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bytetrain.feishuclone.shared.ui.BadgeColors
import com.bytetrain.feishuclone.shared.ui.BadgeRowStyle
import com.bytetrain.feishuclone.shared.ui.SharedDetailHeaderStyle
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import com.bytetrain.feishuclone.shared.ui.createSharedBadgeRow
import com.bytetrain.feishuclone.shared.ui.createSharedDetailHeader
import com.bytetrain.feishuclone.shared.ui.uiDp

fun createMailDetailScreen(
    context: Context,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xFFF6F8FB.toInt())

        addView(createSharedDetailHeader(
            context = context,
            density = density,
            title = item.title,
            backContentDescription = "返回邮箱列表",
            onBack = onBack,
            style = SharedDetailHeaderStyle(titleTextColor = 0xFF172033.toInt()),
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))

        addView(ScrollView(context).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(uiDp(density, 16), uiDp(density, 16), uiDp(density, 16), uiDp(density, 20))

                addView(TextView(context).apply {
                    text = item.detail.title
                    textSize = 22f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(0xFF172033.toInt())
                    setPadding(0, 0, 0, uiDp(density, 12))
                })

                addView(createSenderRow(context, density, item))

                if (item.badges.isNotEmpty()) {
                    addView(createSharedBadgeRow(
                        context = context,
                        density = density,
                        badges = item.badges,
                        colorResolver = { BadgeColors(0xFF374151.toInt(), 0xFFEAF2FF.toInt()) },
                        style = BadgeRowStyle(
                            topPaddingDp = 0,
                            bottomPaddingDp = 14,
                            horizontalPaddingDp = 8,
                            verticalPaddingDp = 4,
                        ),
                    ))
                }

                addView(createBodyCard(context, density, item), LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ))
            })
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f,
        ))
    }
}

private fun createSenderRow(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 0, 0, uiDp(density, 14))

        addView(TextView(context).apply {
            text = item.avatar.label
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = roundedBackground(density, 0xFF2D9CDB.toInt(), 10)
        }, LinearLayout.LayoutParams(
            uiDp(density, 44),
            uiDp(density, 44),
        ).apply {
            rightMargin = uiDp(density, 10)
        })

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(context).apply {
                text = "ByteTrain Mail"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF172033.toInt())
            })

            addView(TextView(context).apply {
                text = item.timestampText
                textSize = 12f
                setTextColor(0xFF8A94A6.toInt())
            })
        }, LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f,
        ))
    }

private fun createBodyCard(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(uiDp(density, 14), uiDp(density, 14), uiDp(density, 14), uiDp(density, 14))
        background = roundedBackground(density, 0xFFFFFFFF.toInt(), 8)

        addView(TextView(context).apply {
            text = item.detail.body
            textSize = 15f
            setTextColor(0xFF374151.toInt())
            setPadding(0, 0, 0, uiDp(density, 12))
        })

        addView(TextView(context).apply {
            text = "请在移动端确认后继续处理，相关更新会继续同步到邮箱列表。"
            textSize = 15f
            setTextColor(0xFF374151.toInt())
        })
    }

private fun roundedBackground(density: Float, color: Int, radiusDp: Int): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = uiDp(density, radiusDp).toFloat()
        setColor(color)
    }
