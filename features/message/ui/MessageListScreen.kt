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
import com.bytetrain.feishuclone.shared.ui.BadgeModel
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem

fun createMessageListScreen(
    context: Context,
    items: List<UnifiedListItem>,
    totalLabel: String,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    initialScrollY: Int,
    onOpenDetail: (UnifiedListItem, Int) -> Unit,
    onLoadMore: (Int) -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    val scrollView = ScrollView(context).apply {
        isFillViewport = true
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(density, 16), dp(density, 12), dp(density, 16), dp(density, 12))

            addView(createHeader(context, density, totalLabel))

            items.forEach { item ->
                addView(createMessageRow(context, density, item, onOpenDetail))
            }

            addView(createLoadMoreFooter(context, density, hasMore, isLoadingMore))
        })

        setOnScrollChangeListener { view, _, scrollY, _, _ ->
            if (!hasMore || isLoadingMore) {
                return@setOnScrollChangeListener
            }

            val child = (view as ScrollView).getChildAt(0) ?: return@setOnScrollChangeListener
            val distanceToBottom = child.bottom - (view.height + scrollY)
            if (distanceToBottom <= dp(density, 96)) {
                onLoadMore(scrollY)
            }
        }
    }

    scrollView.post {
        scrollView.scrollTo(0, initialScrollY)
    }

    return scrollView
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
            text = "Messages"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF1F2933.toInt())
        })

        addView(TextView(context).apply {
            text = totalLabel
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
        })
    }

private fun createMessageRow(
    context: Context,
    density: Float,
    item: UnifiedListItem,
    onOpenDetail: (UnifiedListItem, Int) -> Unit,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(density, 10), 0, dp(density, 10))
        isClickable = true
        isFocusable = true
        setOnClickListener {
            val scrollY = parentScrollY()
            onOpenDetail(item, scrollY)
        }

        addView(createAvatar(context, density, item), LinearLayout.LayoutParams(
            dp(density, 44),
            dp(density, 44),
        ))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(density, 12), 0, 0, 0)

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(TextView(context).apply {
                    text = item.title
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(0xFF1F2933.toInt())
                    maxLines = 1
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

                addView(TextView(context).apply {
                    text = item.timestampText
                    textSize = 12f
                    setTextColor(0xFF8A94A6.toInt())
                    gravity = Gravity.END
                })
            })

            addView(TextView(context).apply {
                text = item.subtitle
                textSize = 13f
                setTextColor(0xFF697386.toInt())
                maxLines = 1
            })

            if (item.badges.isNotEmpty()) {
                addView(createBadgeRow(context, density, item.badges))
            }
        }, LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f,
        ))
    }

private fun View.parentScrollY(): Int {
    var current = parent
    while (current is View) {
        if (current is ScrollView) {
            return current.scrollY
        }
        current = current.parent
    }
    return 0
}

private fun createAvatar(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): TextView =
    TextView(context).apply {
        text = item.avatar.label
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setTextColor(0xFFFFFFFF.toInt())
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(item.avatar.backgroundColor?.toColorIntOrNull() ?: 0xFF2F80ED.toInt())
        }
        minWidth = dp(density, 44)
        minHeight = dp(density, 44)
    }

private fun createBadgeRow(
    context: Context,
    density: Float,
    badges: List<BadgeModel>,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, dp(density, 6), 0, 0)

        badges.forEach { badge ->
            addView(TextView(context).apply {
                text = badge.text
                textSize = 11f
                setTextColor(badge.tone.badgeTextColor())
                setPadding(dp(density, 6), dp(density, 2), dp(density, 6), dp(density, 2))
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

private fun createLoadMoreFooter(
    context: Context,
    density: Float,
    hasMore: Boolean,
    isLoadingMore: Boolean,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(0, dp(density, 12), 0, dp(density, 8))

        addView(TextView(context).apply {
            text = when {
                isLoadingMore -> "Loading more messages..."
                hasMore -> "Pull up to load more"
                else -> "No more messages"
            }
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFF8A94A6.toInt())
        })
    }

private fun String.badgeBackgroundColor(): Int =
    when (this) {
        "unread" -> 0xFFFFE8E8.toInt()
        "pinned" -> 0xFFE8F1FF.toInt()
        "muted" -> 0xFFF1F3F5.toInt()
        "bot" -> 0xFFF2E9FF.toInt()
        else -> 0xFFE5E7EB.toInt()
    }

private fun String.badgeTextColor(): Int =
    when (this) {
        "unread" -> 0xFFC92A2A.toInt()
        "pinned" -> 0xFF2563EB.toInt()
        "muted" -> 0xFF6B7280.toInt()
        "bot" -> 0xFF7E22CE.toInt()
        else -> 0xFF374151.toInt()
    }

private fun String.toColorIntOrNull(): Int? =
    removePrefix("#").toLongOrNull(16)?.let { value ->
        (0xFF000000 or value).toInt()
    }

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
