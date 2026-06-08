package com.bytetrain.feishuclone.features.message.ui

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

fun createMessageListScreen(
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
            title = "消息",
            totalLabel = totalLabel,
            loadingText = "正在加载消息...",
            emptyText = "暂无消息",
            loadingMoreText = "正在加载更多消息...",
            loadMorePromptText = "上滑加载更多",
            endText = "没有更多消息",
            titleTextColor = 0xFF1F2933.toInt(),
            itemRenderer = ::createMessageRow,
        ),
        initialScrollY = initialScrollY,
        onOpenDetail = onOpenDetail,
        onLoadMore = onLoadMore,
        onRetryInitial = onRetryInitial,
    )

private fun createMessageRow(
    context: Context,
    density: Float,
    item: UnifiedListItem,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, uiDp(density, 10), 0, uiDp(density, 10))

        addView(createAvatar(context, density, item), LinearLayout.LayoutParams(
            uiDp(density, 44),
            uiDp(density, 44),
        ))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(uiDp(density, 12), 0, 0, 0)

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
                addView(createSharedBadgeRow(
                    context = context,
                    density = density,
                    badges = item.badges,
                    colorResolver = ::messageBadgeColors,
                    style = BadgeRowStyle(topPaddingDp = 6, horizontalPaddingDp = 6, verticalPaddingDp = 2),
                ))
            }
        }, LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f,
        ))
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
        minWidth = uiDp(density, 44)
        minHeight = uiDp(density, 44)
    }

private fun messageBadgeColors(tone: String): BadgeColors =
    BadgeColors(
        textColor = when (tone) {
            "unread" -> 0xFFC92A2A.toInt()
            "pinned" -> 0xFF2563EB.toInt()
            "muted" -> 0xFF6B7280.toInt()
            "bot" -> 0xFF7E22CE.toInt()
            else -> 0xFF374151.toInt()
        },
        backgroundColor = when (tone) {
            "unread" -> 0xFFFFE8E8.toInt()
            "pinned" -> 0xFFE8F1FF.toInt()
            "muted" -> 0xFFF1F3F5.toInt()
            "bot" -> 0xFFF2E9FF.toInt()
            else -> 0xFFE5E7EB.toInt()
        },
    )
