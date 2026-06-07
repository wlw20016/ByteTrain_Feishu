package com.bytetrain.feishuclone

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.mapper.toUnifiedListItem
import com.bytetrain.feishuclone.features.mail.ui.createMailDetailScreen
import com.bytetrain.feishuclone.features.mail.ui.createMailListScreen
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.features.message.mapper.toUnifiedListItem
import com.bytetrain.feishuclone.features.message.ui.createMessageDetailScreen
import com.bytetrain.feishuclone.features.message.ui.createMessageListScreen
import com.bytetrain.feishuclone.shared.navigation.AppRoutes
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class MainActivity : Activity() {
    private lateinit var contentContainer: LinearLayout
    private lateinit var messageTab: LinearLayout
    private lateinit var mailTab: LinearLayout
    private var currentRoute: String = AppRoutes.MESSAGE_LIST
    private var selectedMessageItem: UnifiedListItem? = null
    private var selectedMailItem: UnifiedListItem? = null
    private val repositoryProvider = AppRepositoryProvider()
    private val messageRepository = repositoryProvider.createMessageRepository()
    private val mailRepository = repositoryProvider.createMailRepository()
    private val loadedMessages = mutableListOf<MessageItem>()
    private val loadedMails = mutableListOf<MailItem>()
    private var nextMessageCursor: String? = null
    private var nextMailCursor: String? = null
    private var hasMoreMessages: Boolean = true
    private var hasMoreMails: Boolean = true
    private var messageListScrollY: Int = 0
    private var mailListScrollY: Int = 0
    private var isLoadingMoreMessages: Boolean = false
    private var isLoadingMoreMails: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "ByteTrain Feishu"
        setContentView(createRootView())
        registerSystemBackCallback()
        renderSelectedRoute()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (!handleBackNavigation()) {
            super.onBackPressed()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            return handleBackNavigation() || super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    private fun registerSystemBackCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                OnBackInvokedCallback {
                    if (!handleBackNavigation()) {
                        finish()
                    }
                },
            )
        }
    }

    private fun handleBackNavigation(): Boolean {
        return when {
            currentRoute == AppRoutes.MESSAGE_LIST && selectedMessageItem != null -> {
                Log.d(TAG, "Back navigation: message detail -> message list")
                selectedMessageItem = null
                renderMessageList()
                true
            }
            currentRoute == AppRoutes.MAIL_LIST && selectedMailItem != null -> {
                Log.d(TAG, "Back navigation: mail detail -> mail list")
                selectedMailItem = null
                renderMailList()
                true
            }
            else -> false
        }
    }

    private fun createRootView(): LinearLayout {
        val density = resources.displayMetrics.density
        val horizontalPadding = (24 * density).toInt()
        val topPadding = (32 * density).toInt()
        val bottomPadding = (16 * density).toInt()

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            contentContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }
            addView(contentContainer, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ))

            addView(createBottomTabBar())
        }
    }

    private fun createBottomTabBar(): LinearLayout {
        val density = resources.displayMetrics.density

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(density, 6), 0, 0)

            messageTab = createNavigationTab("消息", AppRoutes.MESSAGE_LIST, R.drawable.ic_messages_24)
            mailTab = createNavigationTab("邮箱", AppRoutes.MAIL_LIST, R.drawable.ic_mail_24)

            addView(messageTab, LinearLayout.LayoutParams(
                0,
                dp(density, 64),
                1f,
            ))
            addView(mailTab, LinearLayout.LayoutParams(
                0,
                dp(density, 64),
                1f,
            ))
        }
    }

    private fun createNavigationTab(label: String, route: String, iconResId: Int): LinearLayout {
        val density = resources.displayMetrics.density

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = label
            setPadding(dp(density, 8), dp(density, 4), dp(density, 8), dp(density, 4))

            addView(ImageView(context).apply {
                setImageResource(iconResId)
                setColorFilter(UNSELECTED_TAB_COLOR)
            }, LinearLayout.LayoutParams(
                dp(density, 28),
                dp(density, 28),
            ))

            addView(TextView(context).apply {
                text = label
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(UNSELECTED_TAB_COLOR)
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(density, 3)
            })

            setOnClickListener { selectRoute(route) }
        }
    }

    private fun applyTabSelection(tab: LinearLayout, selected: Boolean) {
        val color = if (selected) SELECTED_TAB_COLOR else UNSELECTED_TAB_COLOR
        tab.isSelected = selected
        tab.background = if (selected) {
            GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(resources.displayMetrics.density, 12).toFloat()
                setColor(SELECTED_TAB_BACKGROUND_COLOR)
            }
        } else {
            null
        }

        (tab.getChildAt(0) as? ImageView)?.setColorFilter(color)
        (tab.getChildAt(1) as? TextView)?.setTextColor(color)
    }

    private fun selectRoute(route: String) {
        if (currentRoute == route) {
            return
        }

        currentRoute = route
        renderSelectedRoute()
    }

    private fun renderSelectedRoute() {
        contentContainer.removeAllViews()

        when (currentRoute) {
            AppRoutes.MESSAGE_LIST -> {
                ensureInitialMessagesLoaded()
                selectedMessageItem?.let { selected ->
                    renderMessageDetail(selected)
                } ?: renderMessageList()
            }
            AppRoutes.MAIL_LIST -> {
                ensureInitialMailsLoaded()
                selectedMailItem?.let { selected ->
                    renderMailDetail(selected)
                } ?: renderMailList()
            }
        }

        applyTabSelection(messageTab, currentRoute == AppRoutes.MESSAGE_LIST)
        applyTabSelection(mailTab, currentRoute == AppRoutes.MAIL_LIST)
    }

    private fun ensureInitialMessagesLoaded() {
        if (loadedMessages.isNotEmpty() || !hasMoreMessages) {
            return
        }

        loadInitialMessagePage()
    }

    private fun loadInitialMessagePage() {
        val page = runSuspendBlocking {
            messageRepository.loadPage(messagePageSize(), null)
        }
        loadedMessages += page.items
        nextMessageCursor = page.nextCursor
        hasMoreMessages = page.hasMore
    }

    private fun loadNextMessagePage() {
        if (!hasMoreMessages) {
            return
        }

        val page = runSuspendBlocking {
            messageRepository.loadPage(messagePageSize(), nextMessageCursor)
        }
        loadedMessages += page.items
        nextMessageCursor = page.nextCursor
        hasMoreMessages = page.hasMore
    }

    private fun renderMessageList() {
        val items = loadedMessages.map { it.toUnifiedListItem() }
        contentContainer.removeAllViews()
        contentContainer.addView(createMessageListScreen(
            context = this,
            items = items,
            totalLabel = "Showing ${items.size} of 10000 conversations",
            hasMore = hasMoreMessages,
            isLoadingMore = isLoadingMoreMessages,
            initialScrollY = messageListScrollY,
            onOpenDetail = { item, scrollY ->
                messageListScrollY = scrollY
                selectedMessageItem = item
                renderMessageDetail(item)
            },
            onLoadMore = { scrollY ->
                if (isLoadingMoreMessages) {
                    return@createMessageListScreen
                }
                messageListScrollY = scrollY
                isLoadingMoreMessages = true
                loadNextMessagePage()
                isLoadingMoreMessages = false
                renderMessageList()
            },
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
    }

    private fun renderMessageDetail(item: UnifiedListItem) {
        contentContainer.removeAllViews()
        contentContainer.addView(createMessageDetailScreen(
            context = this,
            item = item,
            onBack = {
                selectedMessageItem = null
                renderMessageList()
            },
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
    }

    private fun renderMailList() {
        val items = loadedMails.map { it.toUnifiedListItem() }
        contentContainer.removeAllViews()
        contentContainer.addView(createMailListScreen(
            context = this,
            items = items,
            totalLabel = "Showing ${items.size} of 10000 emails",
            hasMore = hasMoreMails,
            isLoadingMore = isLoadingMoreMails,
            initialScrollY = mailListScrollY,
            onOpenDetail = { item, scrollY ->
                mailListScrollY = scrollY
                selectedMailItem = item
                renderMailDetail(item)
            },
            onLoadMore = { scrollY ->
                if (isLoadingMoreMails) {
                    return@createMailListScreen
                }
                mailListScrollY = scrollY
                isLoadingMoreMails = true
                loadNextMailPage()
                isLoadingMoreMails = false
                renderMailList()
            },
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
    }

    private fun renderMailDetail(item: UnifiedListItem) {
        contentContainer.removeAllViews()
        contentContainer.addView(createMailDetailScreen(
            context = this,
            item = item,
            onBack = {
                selectedMailItem = null
                renderMailList()
            },
        ), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
    }

    private fun ensureInitialMailsLoaded() {
        if (loadedMails.isNotEmpty() || !hasMoreMails) {
            return
        }

        loadInitialMailPage()
    }

    private fun loadInitialMailPage() {
        val page = runSuspendBlocking {
            mailRepository.loadPage(mailPageSize(), null)
        }
        loadedMails += page.items
        nextMailCursor = page.nextCursor
        hasMoreMails = page.hasMore
    }

    private fun loadNextMailPage() {
        if (!hasMoreMails) {
            return
        }

        val page = runSuspendBlocking {
            mailRepository.loadPage(mailPageSize(), nextMailCursor)
        }
        loadedMails += page.items
        nextMailCursor = page.nextCursor
        hasMoreMails = page.hasMore
    }

    private fun messagePageSize(): Int =
        screenVisiblePageSize(MESSAGE_ROW_HEIGHT_DP)

    private fun mailPageSize(): Int =
        screenVisiblePageSize(MAIL_CARD_HEIGHT_DP)

    private fun screenVisiblePageSize(estimatedItemHeightDp: Int): Int {
        val density = resources.displayMetrics.density
        val estimatedItemHeightPixels = dp(density, estimatedItemHeightDp).coerceAtLeast(1)
        val availableHeightPixels = if (::contentContainer.isInitialized && contentContainer.height > 0) {
            contentContainer.height
        } else {
            resources.displayMetrics.heightPixels - dp(density, FALLBACK_VERTICAL_CHROME_DP)
        }.coerceAtLeast(estimatedItemHeightPixels)

        val visibleItemCount = kotlin.math.ceil(
            availableHeightPixels.toDouble() / estimatedItemHeightPixels.toDouble(),
        ).toInt()

        return (visibleItemCount + PAGE_PRELOAD_ITEMS).coerceIn(
            MIN_VISIBLE_PAGE_SIZE,
            MAX_REPOSITORY_PAGE_SIZE,
        )
    }

    companion object {
        private const val MESSAGE_ROW_HEIGHT_DP = 64
        private const val MAIL_CARD_HEIGHT_DP = 92
        private const val PAGE_PRELOAD_ITEMS = 2
        private const val MIN_VISIBLE_PAGE_SIZE = 1
        private const val MAX_REPOSITORY_PAGE_SIZE = 200
        private const val FALLBACK_VERTICAL_CHROME_DP = 112
        private const val SELECTED_TAB_COLOR = 0xFF2F80ED.toInt()
        private const val UNSELECTED_TAB_COLOR = 0xFF8A94A6.toInt()
        private const val SELECTED_TAB_BACKGROUND_COLOR = 0xFFEAF2FF.toInt()
        private const val TAG = "ByteTrainMainActivity"
    }
}

private fun <T> runSuspendBlocking(block: suspend () -> T): T {
    var value: T? = null
    var error: Throwable? = null
    val latch = CountDownLatch(1)

    block.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.fold(
                onSuccess = { value = it },
                onFailure = { error = it },
            )
            latch.countDown()
        }
    })

    latch.await()
    error?.let { throw it }
    return value ?: error("Suspend block completed without a value")
}

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
