package com.bytetrain.feishuclone

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.bytetrain.feishuclone.shared.navigation.AppRoutes

class MainActivity : Activity() {
    private lateinit var screenTitle: TextView
    private lateinit var screenDescription: TextView
    private lateinit var messageTab: Button
    private lateinit var mailTab: Button
    private var currentRoute: String = AppRoutes.MESSAGE_LIST

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

            addView(createContentArea(), LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ))

            addView(createBottomTabBar())
        }
    }

    private fun createContentArea(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            screenTitle = TextView(context).apply {
                textSize = 22f
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            addView(screenTitle, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ))

            screenDescription = TextView(context).apply {
                textSize = 14f
                gravity = Gravity.CENTER
            }
            addView(screenDescription, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ))
        }
    }

    private fun createBottomTabBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

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
        when (currentRoute) {
            AppRoutes.MESSAGE_LIST -> {
                screenTitle.text = "Messages"
                screenDescription.text = "Message list route: ${AppRoutes.MESSAGE_LIST}"
            }
            AppRoutes.MAIL_LIST -> {
                screenTitle.text = "Mail"
                screenDescription.text = "Mail list route: ${AppRoutes.MAIL_LIST}"
            }
        }

        messageTab.isSelected = currentRoute == AppRoutes.MESSAGE_LIST
        mailTab.isSelected = currentRoute == AppRoutes.MAIL_LIST
    }
}
