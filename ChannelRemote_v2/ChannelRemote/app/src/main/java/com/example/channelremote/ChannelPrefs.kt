
package com.example.channelremote

import android.content.Context
import android.content.Intent
import org.json.JSONArray

class ChannelPrefs(private val context: Context) {
    private val sp = context.getSharedPreferences("channels", Context.MODE_PRIVATE)

    private fun keyFor(channel: Int) = "pkg_$channel"

    fun getPackageForChannel(channel: Int): String? =
        sp.getString(keyFor(channel), null)

    fun setPackageForChannel(channel: Int, pkg: String) {
        sp.edit().putString(keyFor(channel), pkg).apply()
        addToSet(channel)
    }

    fun clearChannel(channel: Int) {
        sp.edit().remove(keyFor(channel)).apply()
        removeFromSet(channel)
    }

    fun allAssignedChannels(): List<Int> {
        val json = sp.getString("channels_set", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Int>()
            for (i in 0 until arr.length()) list.add(arr.getInt(i))
            list.filter { getPackageForChannel(it) != null }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun addToSet(channel: Int) {
        val cur = JSONArray(sp.getString("channels_set", "[]") ?: "[]")
        val set = mutableSetOf<Int>()
        for (i in 0 until cur.length()) set.add(cur.getInt(i))
        set.add(channel)
        val out = JSONArray()
        set.forEach { out.put(it) }
        sp.edit().putString("channels_set", out.toString()).apply()
    }

    private fun removeFromSet(channel: Int) {
        val cur = JSONArray(sp.getString("channels_set", "[]") ?: "[]")
        val set = mutableSetOf<Int>()
        for (i in 0 until cur.length()) set.add(cur.getInt(i))
        set.remove(channel)
        val out = JSONArray()
        set.forEach { out.put(it) }
        sp.edit().putString("channels_set", out.toString()).apply()
    }

    fun launchChannel(channel: Int): Boolean {
        val pkg = getPackageForChannel(channel) ?: return false
        val pm = context.packageManager
        val launch = pm.getLaunchIntentForPackage(pkg) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        sp.edit().putInt("current_channel", channel).apply()
        return true
    }

    fun currentChannel(): Int = sp.getInt("current_channel", -1)

    fun stepChannel(delta: Int): Int? {
        val assigned = allAssignedChannels().sorted()
        if (assigned.isEmpty()) return null
        val cur = currentChannel()
        val idx = if (assigned.contains(cur)) assigned.indexOf(cur) else -1
        val nextIdx = if (idx == -1) 0 else (idx + delta + assigned.size) % assigned.size
        val nextCh = assigned[nextIdx]
        launchChannel(nextCh)
        return nextCh
    }
}
