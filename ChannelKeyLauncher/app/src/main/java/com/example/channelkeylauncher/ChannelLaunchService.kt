package com.example.channelkeylauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast

class ChannelLaunchService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val digitBuf = StringBuilder()
    private var overlay: OverlayBubble? = null
    private var lastResolvedChannel: Int? = null

    // 입력 확정 지연 (다자리 숫자)
    private val commitDelayMs = 800L
    private val commitRunnable = Runnable { commitDigits() }

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            flags = serviceInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        overlay = OverlayBubble(this, getSystemService(WindowManager::class.java))
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val inc = Prefs.getIncKey(this)
        val dec = Prefs.getDecKey(this)

        when (event.keyCode) {
            inc -> { stepChannel(+1); return true }
            dec -> { stepChannel(-1); return true }
        }

        val digit = keyCodeToDigit(event)
        if (digit != null) {
            digitBuf.append(digit)
            overlay?.show(digitBuf.toString())
            handler.removeCallbacks(commitRunnable)
            handler.postDelayed(commitRunnable, commitDelayMs)
            return true
        }

        return false
    }

    private fun keyCodeToDigit(ev: KeyEvent): Int? = when (ev.keyCode) {
        KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> 0
        KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> 1
        KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> 2
        KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> 3
        KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> 4
        KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> 5
        KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> 6
        KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> 7
        KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> 8
        KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> 9
        else -> null
    }

    private fun commitDigits() {
        val s = digitBuf.toString()
        digitBuf.clear()
        if (s.isEmpty()) return
        val ch = s.toIntOrNull() ?: return
        launchChannel(ch)
    }

    private fun launchChannel(channel: Int) {
        val map = Prefs.loadMap(this)
        val pkg = map[channel]
        overlay?.show(channel.toString())

        if (pkg == null) {
            Toast.makeText(this, "채널 " + channel + " 미할당", Toast.LENGTH_SHORT).show()
            lastResolvedChannel = null
            return
        }

        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Toast.makeText(this, "패키지 없음: " + pkg, Toast.LENGTH_SHORT).show()
            lastResolvedChannel = null
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        lastResolvedChannel = channel
    }

    private fun stepChannel(step: Int) {
        val map = Prefs.loadMap(this)
        if (map.isEmpty()) return
        val keys = map.keys.sorted()
        val current = lastResolvedChannel ?: keys.first()
        val idx = keys.indexOf(current).let { if (it < 0) 0 else it }
        val nextIdx = ((idx + step) % keys.size + keys.size) % keys.size
        val next = keys[nextIdx]
        launchChannel(next)
    }
}
