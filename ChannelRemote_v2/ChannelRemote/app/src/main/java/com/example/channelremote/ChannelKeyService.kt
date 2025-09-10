
package com.example.channelremote

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class ChannelKeyService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var overlayTextView: TextView? = null
    private var overlayContainer: FrameLayout? = null
    private var wm: WindowManager? = null

    private val digitBuffer = StringBuilder()
    private val commitDelayMs = 700L

    private val commitRunnable = Runnable {
        commitBuffer()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val prefs = ChannelPrefs(this)

        // Number keys (top row)
        if (event.keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
            val ch = event.keyCode - KeyEvent.KEYCODE_0
            appendDigit(ch)
            return true
        }
        // Number pad keys
        if (event.keyCode in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9) {
            val ch = event.keyCode - KeyEvent.KEYCODE_NUMPAD_0
            appendDigit(ch)
            return true
        }

        // Enter to commit immediately
        if (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            commitBuffer()
            return true
        }
        // Clear buffer (Back or Escape or Del)
        if (event.keyCode == KeyEvent.KEYCODE_DEL || event.keyCode == KeyEvent.KEYCODE_ESCAPE || event.keyCode == KeyEvent.KEYCODE_BACK) {
            clearBuffer()
            return true
        }

        // Channel up/down defaults
        when (event.keyCode) {
            KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_NUMPAD_ADD, KeyEvent.KEYCODE_F12 -> {
                val next = prefs.stepChannel(+1)
                next?.let { showOverlay("채널 $it") }
                return true
            }
            KeyEvent.KEYCODE_PAGE_DOWN, KeyEvent.KEYCODE_NUMPAD_SUBTRACT, KeyEvent.KEYCODE_F11 -> {
                val next = prefs.stepChannel(-1)
                next?.let { showOverlay("채널 $it") }
                return true
            }
        }

        return false
    }

    private fun appendDigit(d: Int) {
        digitBuffer.append(d)
        showOverlay(digitBuffer.toString())
        handler.removeCallbacks(commitRunnable)
        handler.postDelayed(commitRunnable, commitDelayMs)
    }

    private fun commitBuffer() {
        handler.removeCallbacks(commitRunnable)
        val txt = digitBuffer.toString()
        if (txt.isEmpty()) {
            removeOverlaySoon()
            return
        }
        val channel = try { txt.toInt() } catch (_: Exception) { -1 }
        digitBuffer.clear()

        if (channel >= 0) {
            val prefs = ChannelPrefs(this)
            val ok = prefs.launchChannel(channel)
            if (ok) showOverlay("채널 $channel")
            else showOverlay("채널 $channel (미지정)")
        }
        removeOverlaySoon()
    }

    private fun clearBuffer() {
        digitBuffer.clear()
        removeOverlay()
    }

    private fun ensureOverlay() {
        if (overlayContainer != null) return
        val inflater = LayoutInflater.from(this)
        val container = inflater.inflate(R.layout.overlay_label, null) as FrameLayout
        overlayTextView = container.findViewById(R.id.tvOverlay)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 150
        wm?.addView(container, params)
        overlayContainer = container
    }

    private fun showOverlay(text: String) {
        ensureOverlay()
        overlayTextView?.text = text
    }

    private fun removeOverlaySoon() {
        handler.postDelayed({ removeOverlay() }, 900)
    }

    private fun removeOverlay() {
        try {
            overlayContainer?.let { wm?.removeView(it) }
        } catch (_: Exception) {
        } finally {
            overlayContainer = null
            overlayTextView = null
        }
    }
}
