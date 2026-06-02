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

fun createMailListScreen(
    context: Context,
    items: List<UnifiedListItem>,
    totalLabel: String,
    onOpenDetail: (UnifiedListItem) -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return ScrollView(context).apply {
        isFillViewport = true
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(density, 16), dp(density, 12), dp(density, 16), dp(density, 12))

            addView(createHeader(context, density, totalLabel))

            items.forEach { item ->
                addView(createMailCard(context, density, item, onOpenDetail))
            }
        })
    }
}

private fun createHeader(
    context: Context,
    density: Float,
    totalLabel: String,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(density, 12))

        addView(TextView(context).apply {
            text = "Mail"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
        })

        addView(TextView(context).apply {
            text = totalLabel
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
        })
    }

private fun createMailCard(
    context: Context,
    density: Float,
    item: UnifiedListItem,
    onOpenDetail: (UnifiedListItem) -> Unit,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(density, 14), dp(density, 12), dp(density, 14), dp(density, 12))
        isClickable = true
        isFocusable = true
        setOnClickListener { onOpenDetail(item) }
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(density, 8).toFloat()
            setColor(0xFFFFFFFF.toInt())
            setStroke(dp(density, 1), 0xFFE3E8EF.toInt())
        }

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(createAvatar(context, density, item), LinearLayout.LayoutParams(
                dp(density, 40),
                dp(density, 40),
            ))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(density, 10), 0, 0, 0)

                addView(TextView(context).apply {
                    text = item.title
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(0xFF172033.toInt())
                    maxLines = 1
                })

                addView(TextView(context).apply {
                    text = item.subtitle
                    textSize = 13f
                    setTextColor(0xFF697386.toInt())
                    maxLines = 2
                })
            }, LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f,
            ))

            addView(TextView(context).apply {
                text = item.timestampText
                textSize = 12f
                setTextColor(0xFF8A94A6.toInt())
                gravity = Gravity.END
            })
        })

        if (item.badges.isNotEmpty()) {
            addView(createBadgeRow(context, density, item.badges))
        }
    }.apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            bottomMargin = dp(density, 10)
        }
    }

private fun createAvatar(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): TextView =
    TextView(context).apply {
        text = item.avatar.label
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setTextColor(0xFFFFFFFF.toInt())
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(density, 8).toFloat()
            setColor(item.avatar.backgroundColor?.toColorIntOrNull() ?: 0xFF2D9CDB.toInt())
        }
        minWidth = dp(density, 40)
        minHeight = dp(density, 40)
    }

private fun createBadgeRow(
    context: Context,
    density: Float,
    badges: List<BadgeModel>,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, dp(density, 10), 0, 0)

        badges.forEach { badge ->
            addView(TextView(context).apply {
                text = badge.text
                textSize = 11f
                setTextColor(badge.tone.badgeTextColor())
                setPadding(dp(density, 7), dp(density, 3), dp(density, 7), dp(density, 3))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(density, 8).toFloat()
                    setColor(badge.tone.badgeBackgroundColor())
                }
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                rightMargin = dp(density, 6)
            })
        }
    }

private fun String.badgeBackgroundColor(): Int =
    when (this) {
        "unread" -> 0xFFFFE8E8.toInt()
        "attachment" -> 0xFFE8F1FF.toInt()
        "action" -> 0xFFEAF7ED.toInt()
        "reminder" -> 0xFFFFF0DB.toInt()
        "system" -> 0xFFE8F6FF.toInt()
        "collaboration" -> 0xFFEAF7ED.toInt()
        "report" -> 0xFFFFE8E8.toInt()
        "update" -> 0xFFF1F3F5.toInt()
        else -> 0xFFE5E7EB.toInt()
    }

private fun String.badgeTextColor(): Int =
    when (this) {
        "unread" -> 0xFFC92A2A.toInt()
        "attachment" -> 0xFF2563EB.toInt()
        "action" -> 0xFF1F7A3D.toInt()
        "reminder" -> 0xFF9A5B00.toInt()
        "system" -> 0xFF1B6B91.toInt()
        "collaboration" -> 0xFF1F7A3D.toInt()
        "report" -> 0xFFC92A2A.toInt()
        "update" -> 0xFF4B5563.toInt()
        else -> 0xFF374151.toInt()
    }

private fun String.toColorIntOrNull(): Int? =
    removePrefix("#").toLongOrNull(16)?.let { value ->
        (0xFF000000 or value).toInt()
    }

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
