package com.bytetrain.feishuclone.shared.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bytetrain.feishuclone.shared.list.PagingUiState

typealias SharedListItemRenderer = (Context, Float, UnifiedListItem) -> View
typealias BadgeColorResolver = (String) -> BadgeColors

data class SharedPagedListConfig(
    val title: String,
    val totalLabel: String,
    val loadingText: String,
    val emptyText: String,
    val loadingMoreText: String,
    val loadMorePromptText: String,
    val endText: String,
    val titleTextColor: Int = 0xFF1F2933.toInt(),
    val itemRenderer: SharedListItemRenderer,
)

data class BadgeColors(
    val textColor: Int,
    val backgroundColor: Int,
)

data class BadgeRowStyle(
    val topPaddingDp: Int = 6,
    val bottomPaddingDp: Int = 0,
    val horizontalPaddingDp: Int = 6,
    val verticalPaddingDp: Int = 2,
    val cornerRadiusDp: Int = 8,
    val endMarginDp: Int = 6,
)

data class SharedDetailHeaderStyle(
    val titleTextColor: Int = 0xFF111827.toInt(),
    val actionTextColor: Int = 0xFF6B7280.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
)

fun createSharedPagedListScreen(
    context: Context,
    state: PagingUiState<UnifiedListItem>,
    config: SharedPagedListConfig,
    initialScrollY: Int,
    onOpenDetail: (UnifiedListItem, Int) -> Unit,
    onLoadMore: (Int) -> Unit,
    onRetryInitial: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    val scrollView = ScrollView(context).apply {
        isFillViewport = true
    }

    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(uiDp(density, 16), uiDp(density, 12), uiDp(density, 16), uiDp(density, 12))

        addView(createSharedListHeader(context, density, config))

        when (state) {
            PagingUiState.Loading -> addView(createSharedStateText(
                context = context,
                density = density,
                text = config.loadingText,
            ))
            PagingUiState.Empty -> addView(createSharedStateText(
                context = context,
                density = density,
                text = config.emptyText,
            ))
            is PagingUiState.Error -> addView(createSharedStateText(
                context = context,
                density = density,
                text = "${state.message}，点按重试",
                onClick = onRetryInitial,
            ))
            is PagingUiState.Content -> {
                addSharedItems(context, density, state.items, config, scrollView, onOpenDetail)
                addView(createSharedLoadMoreFooter(context, density, state, config))
            }
            is PagingUiState.LoadingMore -> {
                addSharedItems(context, density, state.items, config, scrollView, onOpenDetail)
                addView(createSharedLoadMoreFooter(context, density, state, config))
            }
            is PagingUiState.LoadMoreError -> {
                addSharedItems(context, density, state.items, config, scrollView, onOpenDetail)
                addView(createSharedLoadMoreFooter(context, density, state, config, onLoadMore))
            }
        }
    }

    scrollView.addView(content)
    scrollView.setOnScrollChangeListener { view, _, scrollY, _, _ ->
        val contentState = state as? PagingUiState.Content ?: return@setOnScrollChangeListener
        if (!contentState.hasMore) {
            return@setOnScrollChangeListener
        }

        val child = (view as ScrollView).getChildAt(0) ?: return@setOnScrollChangeListener
        val distanceToBottom = child.bottom - (view.height + scrollY)
        if (distanceToBottom <= uiDp(density, 96)) {
            onLoadMore(scrollY)
        }
    }
    scrollView.restoreScrollBeforeDraw(initialScrollY)

    return scrollView
}

fun createSharedLoadMoreFooter(
    context: Context,
    density: Float,
    state: PagingUiState<UnifiedListItem>,
    config: SharedPagedListConfig,
    onRetryLoadMore: ((Int) -> Unit)? = null,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(0, uiDp(density, 12), 0, uiDp(density, 8))

        addView(TextView(context).apply {
            text = when (state) {
                is PagingUiState.LoadingMore -> config.loadingMoreText
                is PagingUiState.LoadMoreError -> "${state.message}，点按重试"
                is PagingUiState.Content -> if (state.hasMore) config.loadMorePromptText else config.endText
                else -> ""
            }
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFF8A94A6.toInt())
            if (state is PagingUiState.LoadMoreError && onRetryLoadMore != null) {
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onRetryLoadMore(parentScrollY())
                }
            }
        })
    }

fun createSharedBadgeRow(
    context: Context,
    density: Float,
    badges: List<BadgeModel>,
    colorResolver: BadgeColorResolver,
    style: BadgeRowStyle = BadgeRowStyle(),
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, uiDp(density, style.topPaddingDp), 0, uiDp(density, style.bottomPaddingDp))

        badges.forEach { badge ->
            val colors = colorResolver(badge.tone)
            addView(TextView(context).apply {
                text = badge.text
                textSize = 11f
                setTextColor(colors.textColor)
                setPadding(
                    uiDp(density, style.horizontalPaddingDp),
                    uiDp(density, style.verticalPaddingDp),
                    uiDp(density, style.horizontalPaddingDp),
                    uiDp(density, style.verticalPaddingDp),
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = uiDp(density, style.cornerRadiusDp).toFloat()
                    setColor(colors.backgroundColor)
                }
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                rightMargin = uiDp(density, style.endMarginDp)
            })
        }
    }

fun createSharedDetailHeader(
    context: Context,
    density: Float,
    title: String,
    backContentDescription: String,
    onBack: () -> Unit,
    style: SharedDetailHeaderStyle = SharedDetailHeaderStyle(),
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(uiDp(density, 14), uiDp(density, 10), uiDp(density, 14), uiDp(density, 10))
        setBackgroundColor(style.backgroundColor)

        addView(TextView(context).apply {
            text = "<"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(style.titleTextColor)
            isClickable = true
            isFocusable = true
            contentDescription = backContentDescription
            setOnClickListener { onBack() }
        }, LinearLayout.LayoutParams(
            uiDp(density, 36),
            uiDp(density, 36),
        ).apply {
            rightMargin = uiDp(density, 6)
        })

        addView(TextView(context).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(style.titleTextColor)
            maxLines = 1
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        addView(TextView(context).apply {
            text = "..."
            textSize = 20f
            gravity = Gravity.END
            setTextColor(style.actionTextColor)
        })
    }

fun ScrollView.restoreScrollBeforeDraw(initialScrollY: Int) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnPreDrawListener(this)
            }
            scrollTo(0, initialScrollY)
            return true
        }
    })
}

fun View.parentScrollY(): Int {
    var current = parent
    while (current is View) {
        if (current is ScrollView) {
            return current.scrollY
        }
        current = current.parent
    }
    return 0
}

fun String.toColorIntOrNull(): Int? =
    removePrefix("#").toLongOrNull(16)?.let { value ->
        (0xFF000000 or value).toInt()
    }

fun uiDp(density: Float, value: Int): Int =
    (value * density).toInt()

private fun LinearLayout.addSharedItems(
    context: Context,
    density: Float,
    items: List<UnifiedListItem>,
    config: SharedPagedListConfig,
    scrollView: ScrollView,
    onOpenDetail: (UnifiedListItem, Int) -> Unit,
) {
    items.forEach { item ->
        addView(config.itemRenderer(context, density, item).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener {
                onOpenDetail(item, scrollView.scrollY)
            }
        })
    }
}

private fun createSharedStateText(
    context: Context,
    density: Float,
    text: String,
    onClick: (() -> Unit)? = null,
): View =
    TextView(context).apply {
        this.text = text
        textSize = 14f
        gravity = Gravity.CENTER
        setTextColor(0xFF697386.toInt())
        setPadding(0, uiDp(density, 40), 0, uiDp(density, 40))
        if (onClick != null) {
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

private fun createSharedListHeader(
    context: Context,
    density: Float,
    config: SharedPagedListConfig,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, uiDp(density, 12))

        addView(TextView(context).apply {
            text = config.title
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(config.titleTextColor)
        })

        addView(TextView(context).apply {
            text = config.totalLabel
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
        })
    }
