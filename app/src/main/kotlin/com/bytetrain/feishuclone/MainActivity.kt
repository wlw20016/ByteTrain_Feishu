package com.bytetrain.feishuclone

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
    private lateinit var messageTab: Button
    private lateinit var mailTab: Button
    private var currentRoute: String = AppRoutes.MESSAGE_LIST
    private var selectedMessageItem: UnifiedListItem? = null
    private val messageRepository = MockMessageRepository()
    private val loadedMessages = mutableListOf<MessageItem>()
    private var nextMessageCursor: String? = null
    private var hasMoreMessages: Boolean = true

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
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            messageTab = createTabButton("Messages", AppRoutes.MESSAGE_LIST)
            mailTab = createTabButton("Mail", AppRoutes.MAIL_LIST)

            addView(messageTab, LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f,
            ))
            addView(mailTab, LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f,
            ))
        }
    }

    private fun createTabButton(label: String, route: String): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { selectRoute(route) }
        }
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
                contentContainer.addView(createMailPlaceholder(), LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ))
            }
        }

        messageTab.isSelected = currentRoute == AppRoutes.MESSAGE_LIST
        mailTab.isSelected = currentRoute == AppRoutes.MAIL_LIST
    }

    private fun ensureInitialMessagesLoaded() {
        if (loadedMessages.isNotEmpty() || !hasMoreMessages) {
            return
        }

        loadNextMessagePage()
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

    private fun createMailPlaceholder(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 48, 0, 0)

            addView(TextView(context).apply {
                text = "Mail"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(context).apply {
                text = "Mail list route: ${AppRoutes.MAIL_LIST}"
                textSize = 14f
            })
        }

    companion object {
        private const val MESSAGE_PAGE_SIZE = 30
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
