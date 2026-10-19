package com.example.feature.learning.presentation.components

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/** Hosts only the browser-provided fullscreen view and returns it through its callback. */
internal class VideoWebChromeClient(private val context: Context) : WebChromeClient() {
    private var fullscreen: FrameLayout? = null
    private var exit: CustomViewCallback? = null
    private var back: OnBackPressedCallback? = null
    private var restoreBars: (() -> Unit)? = null

    private fun activity(): ComponentActivity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is ComponentActivity) return current
            current = current.baseContext
        }
        return current as? ComponentActivity
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        val host = activity()
        val decor = host?.window?.decorView as? ViewGroup
        if (fullscreen != null || host == null || decor == null || view.parent != null) {
            callback.onCustomViewHidden()
            return
        }
        val container = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
            addView(view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
        fullscreen = container
        exit = callback
        val controller = WindowInsetsControllerCompat(host.window, decor)
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(decor)
        val statusVisible = insets?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
        val navigationVisible = insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        val previousBehavior = controller.systemBarsBehavior
        restoreBars = {
            if (statusVisible) controller.show(WindowInsetsCompat.Type.statusBars())
            else controller.hide(WindowInsetsCompat.Type.statusBars())
            if (navigationVisible) controller.show(WindowInsetsCompat.Type.navigationBars())
            else controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = previousBehavior
        }
        // Keep the browser's fullscreen view in the same focused window. A separate
        // Dialog makes the WebView lose focus and YouTube pauses during the transition.
        decor.addView(container, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        back = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = onHideCustomView()
        }.also { host.onBackPressedDispatcher.addCallback(it) }
    }

    override fun onHideCustomView() {
        val container = fullscreen ?: return
        val callback = exit
        fullscreen = null
        exit = null
        back?.remove()
        back = null
        (container.parent as? ViewGroup)?.removeView(container)
        container.removeAllViews()
        restoreBars?.invoke()
        restoreBars = null
        callback?.onCustomViewHidden()
    }
}
