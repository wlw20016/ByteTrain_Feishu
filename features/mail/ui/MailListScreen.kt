package com.bytetrain.feishuclone.features.mail.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bytetrain.feishuclone.shared.list.PagingUiState
import com.bytetrain.feishuclone.shared.ui.BadgeColors
import com.bytetrain.feishuclone.shared.ui.BadgeRowStyle
import com.bytetrain.feishuclone.shared.ui.SharedPagedListConfig
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import com.bytetrain.feishuclone.shared.ui.createSharedBadgeRow
import com.bytetrain.feishuclone.shared.ui.createSharedPagedListScreen
import com.bytetrain.feishuclone.shared.ui.toColorIntOrNull
import com.bytetrain.feishuclone.shared.ui.uiDp

fun createMailListScreen(
    context: Context,
    state: PagingUiState<UnifiedListItem>,
    totalLabel: String,
    initialScrollY: Int,
    onOpenDetail: (UnifiedListItem, Int) -> Unit,
    onLoadMore: (Int) -> Unit,
    onRetryInitial: () -> Unit,
): View =
    createSharedPagedListScreen(
        context = context,
        state = state,
        config = SharedPagedListConfig(
            title = "邮箱",
            totalLabel = totalLabel,
            loadingText = "正在加载邮件...",
            emptyText = "暂无邮件",
            loadingMoreText = "正在加载更多邮件...",
            loadMorePromptText = "上滑加载更多",
            endText = "没有更多邮件",
            titleTextColor = 0xFF172033.toInt(),
            itemRenderer = ::createMailCard,
        ),
        initialScrollY = initialScrollY,
        onOpenDetail = onOpenDetail,
        onLoadMore = onLoadMore,
        onRetryInitial = onRetryInitial,
    )

private fun createMailCard(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(uiDp(density, 14), uiDp(density, 12), uiDp(density, 14), uiDp(density, 12))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = uiDp(density, 8).toFloat()
            setColor(0xFFFFFFFF.toInt())
            setStroke(uiDp(density, 1), 0xFFE3E8EF.toInt())
        }

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(createAvatar(context, density, item), LinearLayout.LayoutParams(
                uiDp(density, 40),
                uiDp(density, 40),
            ))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(uiDp(density, 10), 0, 0, 0)

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
            addView(createSharedBadgeRow(
                context = context,
                density = density,
                badges = item.badges,
                colorResolver = ::mailBadgeColors,
                style = BadgeRowStyle(topPaddingDp = 10, horizontalPaddingDp = 7, verticalPaddingDp = 3),
            ))
        }
    }.apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            bottomMargin = uiDp(density, 10)
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
            cornerRadius = uiDp(density, 8).toFloat()
            setColor(item.avatar.backgroundColor?.toColorIntOrNull() ?: 0xFF2D9CDB.toInt())
        }
        minWidth = uiDp(density, 40)
        minHeight = uiDp(density, 40)
    }

private fun mailBadgeColors(tone: String): BadgeColors =
    BadgeColors(
        textColor = when (tone) {
            "unread" -> 0xFFC92A2A.toInt()
            "attachment" -> 0xFF2563EB.toInt()
            "action" -> 0xFF1F7A3D.toInt()
            "reminder" -> 0xFF9A5B00.toInt()
            "system" -> 0xFF1B6B91.toInt()
            "collaboration" -> 0xFF1F7A3D.toInt()
            "report" -> 0xFFC92A2A.toInt()
            "update" -> 0xFF4B5563.toInt()
            else -> 0xFF374151.toInt()
        },
        backgroundColor = when (tone) {
            "unread" -> 0xFFFFE8E8.toInt()
            "attachment" -> 0xFFE8F1FF.toInt()
            "action" -> 0xFFEAF7ED.toInt()
            "reminder" -> 0xFFFFF0DB.toInt()
            "system" -> 0xFFE8F6FF.toInt()
            "collaboration" -> 0xFFEAF7ED.toInt()
            "report" -> 0xFFFFE8E8.toInt()
            "update" -> 0xFFF1F3F5.toInt()
            else -> 0xFFE5E7EB.toInt()
        },
    )
