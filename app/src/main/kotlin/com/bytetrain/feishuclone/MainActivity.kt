package com.bytetrain.feishuclone

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bytetrain.feishuclone.features.mail.data.MockMailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.mapper.toUnifiedListItem
import com.bytetrain.feishuclone.features.mail.ui.createMailDetailScreen
import com.bytetrain.feishuclone.features.mail.ui.createMailListScreen
import com.bytetrain.feishuclone.features.message.data.MockMessageRepository
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
    private val messageRepository = MockMessageRepository()
    private val mailRepository = MockMailRepository()
    private val loadedMessages = mutableListOf<MessageItem>()
    private val loadedMails = mutableListOf<MailItem>()
    private var nextMessageCursor: String? = null
    private var nextMailCursor: String? = null
    private var hasMoreMessages: Boolean = true
    private var hasMoreMails: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "ByteTrain Feishu"
        setContentView(createRootView())
        renderSelectedRoute()
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
            messageRepository.loadPage(MESSAGE_PAGE_SIZE, null)
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
            messageRepository.loadPage(MESSAGE_PAGE_SIZE, nextMessageCursor)
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
            totalLabel = "Showing ${items.size} of 10000 mock conversations",
            hasMore = hasMoreMessages,
            onOpenDetail = { item ->
                selectedMessageItem = item
                renderMessageDetail(item)
            },
            onLoadMore = {
                loadNextMessagePage()
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
            totalLabel = "Showing ${items.size} of 10000 mock emails",
            hasMore = hasMoreMails,
            onOpenDetail = { item ->
                selectedMailItem = item
                renderMailDetail(item)
            },
            onLoadMore = {
                loadNextMailPage()
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
            mailRepository.loadPage(MAIL_PAGE_SIZE, null)
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
            mailRepository.loadPage(MAIL_PAGE_SIZE, nextMailCursor)
        }
        loadedMails += page.items
        nextMailCursor = page.nextCursor
        hasMoreMails = page.hasMore
    }

    companion object {
        private const val MESSAGE_PAGE_SIZE = 30
        private const val MAIL_PAGE_SIZE = 30
        private const val SELECTED_TAB_COLOR = 0xFF2F80ED.toInt()
        private const val UNSELECTED_TAB_COLOR = 0xFF8A94A6.toInt()
        private const val SELECTED_TAB_BACKGROUND_COLOR = 0xFFEAF2FF.toInt()
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
