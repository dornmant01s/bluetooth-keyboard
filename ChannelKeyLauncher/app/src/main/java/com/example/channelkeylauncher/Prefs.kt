package com.example.channelkeylauncher

import android.content.Context
import org.json.JSONObject

object Prefs {
    private const val FILE = "prefs"
    private const val KEY_MAP = "channel_map"
    private const val KEY_INC = "inc_key"
    private const val KEY_DEC = "dec_key"

    fun loadMap(ctx: Context): MutableMap<Int, String> {
        val s = ctx.getSharedPreferences(FILE, 0).getString(KEY_MAP, "{}") ?: "{}"
        val json = JSONObject(s)
        val out = mutableMapOf<Int, String>()
        json.keys().forEach { k -> out[k.toInt()] = json.getString(k) }
        return out
    }
    fun saveMap(ctx: Context, map: Map<Int, String>) {
        val json = JSONObject()
        map.forEach { (k,v) -> json.put(k.toString(), v) }
        ctx.getSharedPreferences(FILE, 0).edit().putString(KEY_MAP, json.toString()).apply()
    }

    fun getIncKey(ctx: Context): Int = ctx.getSharedPreferences(FILE,0)
        .getInt(KEY_INC, android.view.KeyEvent.KEYCODE_PAGE_UP)
    fun getDecKey(ctx: Context): Int = ctx.getSharedPreferences(FILE,0)
        .getInt(KEY_DEC, android.view.KeyEvent.KEYCODE_PAGE_DOWN)
    fun setIncKey(ctx: Context, code: Int) { ctx.getSharedPreferences(FILE,0).edit().putInt(KEY_INC, code).apply() }
    fun setDecKey(ctx: Context, code: Int) { ctx.getSharedPreferences(FILE,0).edit().putInt(KEY_DEC, code).apply() }
}
