package com.example.channelkeylauncher

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView

class OverlayBubble(private val ctx: Context, private val wm: WindowManager) {
    private var view = LayoutInflater.from(ctx).inflate(R.layout.overlay_number, null)
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 100 }

    private val handler = Handler(Looper.getMainLooper())
    private var shown = false

    fun show(text: String, ms: Long = 700) {
        (view.findViewById<TextView>(R.id.numberText)).text = text
        if (!shown) {
            wm.addView(view, params); shown = true
        }
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ hide() }, ms)
    }
    fun hide() {
        if (shown) {
            wm.removeView(view); shown = false
        }
    }
}
