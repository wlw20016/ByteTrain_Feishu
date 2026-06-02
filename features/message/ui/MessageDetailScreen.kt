package com.bytetrain.feishuclone.features.message.ui

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bytetrain.feishuclone.shared.ui.DetailMeta
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem

fun createMessageDetailScreen(
    context: Context,
    item: UnifiedListItem,
    onBack: () -> Unit,
): View {
    val density = context.resources.displayMetrics.density

    return ScrollView(context).apply {
        isFillViewport = true
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(density, 16), dp(density, 12), dp(density, 16), dp(density, 12))

            addView(Button(context).apply {
                text = "Back to messages"
                setOnClickListener { onBack() }
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ))

            addView(TextView(context).apply {
                text = item.detail.title
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF1F2933.toInt())
                setPadding(0, dp(density, 18), 0, dp(density, 8))
            })

            addView(TextView(context).apply {
                text = item.detail.body
                textSize = 15f
                setTextColor(0xFF4B5563.toInt())
                setPadding(0, 0, 0, dp(density, 16))
            })

            item.detail.metas.forEach { meta ->
                addView(createMetaRow(context, density, meta))
            }
        })
    }
}

private fun createMetaRow(
    context: Context,
    density: Float,
    meta: DetailMeta,
): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(density, 8), 0, dp(density, 8))

        addView(TextView(context).apply {
            text = meta.label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF6B7280.toInt())
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        addView(TextView(context).apply {
            text = meta.value
            textSize = 13f
            gravity = Gravity.END
            setTextColor(0xFF1F2933.toInt())
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f))
    }

private fun dp(density: Float, value: Int): Int =
    (value * density).toInt()
