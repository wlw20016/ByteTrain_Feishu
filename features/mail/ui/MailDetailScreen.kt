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
import com.bytetrain.feishuclone.shared.ui.BadgeModel
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem

fun createMailDetailScreen(
    context: Context,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xFFF6F8FB.toInt())

        addView(createHeader(context, density, item, onBack), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))

        addView(ScrollView(context).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(density, 16), dp(density, 16), dp(density, 16), dp(density, 20))

                addView(TextView(context).apply {
                    text = item.detail.title
                    textSize = 22f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(0xFF172033.toInt())
                    setPadding(0, 0, 0, dp(density, 12))
                })

                addView(createSenderRow(context, density, item))

                if (item.badges.isNotEmpty()) {
                    addView(createBadgeRow(context, density, item.badges))
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

private fun createHeader(
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
            setTextColor(0xFF172033.toInt())
            isClickable = true
            isFocusable = true
            contentDescription = "返回邮箱列表"
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
            setTextColor(0xFF172033.toInt())
            maxLines = 1
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        addView(TextView(context).apply {
            text = "..."
            textSize = 20f
            gravity = Gravity.END
            setTextColor(0xFF6B7280.toInt())
        })
    }

private fun createSenderRow(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 0, 0, dp(density, 14))

        addView(TextView(context).apply {
            text = item.avatar.label
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = roundedBackground(density, 0xFF2D9CDB.toInt(), 10)
        }, LinearLayout.LayoutParams(
            dp(density, 44),
            dp(density, 44),
        ).apply {
            rightMargin = dp(density, 10)
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

private fun createBadgeRow(
    context: Context,
    density: Float,
    badges: List<BadgeModel>,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, 0, 0, dp(density, 14))

        badges.forEach { badge ->
            addView(TextView(context).apply {
                text = badge.text
                textSize = 11f
                setTextColor(0xFF374151.toInt())
                setPadding(dp(density, 8), dp(density, 4), dp(density, 8), dp(density, 4))
                background = roundedBackground(density, 0xFFEAF2FF.toInt(), 8)
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                rightMargin = dp(density, 6)
            })
        }
    }

private fun createBodyCard(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(density, 14), dp(density, 14), dp(density, 14), dp(density, 14))
        background = roundedBackground(density, 0xFFFFFFFF.toInt(), 8)

        addView(TextView(context).apply {
            text = item.detail.body
            textSize = 15f
            setTextColor(0xFF374151.toInt())
            setPadding(0, 0, 0, dp(density, 12))
        })

        addView(TextView(context).apply {
            text = "请在移动端确认后续处理，相关更新会继续同步到邮箱列表。"
            textSize = 15f
            setTextColor(0xFF374151.toInt())
        })
    }

private fun roundedBackground(density: Float, color: Int, radiusDp: Int): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(density, radiusDp).toFloat()
        setColor(color)
    }

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
