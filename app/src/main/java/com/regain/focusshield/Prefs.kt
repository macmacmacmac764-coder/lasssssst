package com.regain.focusshield

import android.content.Context

object Prefs {
    private const val FILE = "regain_prefs"
    private const val FOCUS = "focus_enabled"
    private const val END = "focus_end"
    private const val APPS = "allowed_apps"

    fun enabled(c: Context) = c.getSharedPreferences(FILE, 0).getBoolean(FOCUS, false)
    fun end(c: Context) = c.getSharedPreferences(FILE, 0).getLong(END, 0L)

    fun setFocus(c: Context, enabled: Boolean, end: Long = 0L) {
        c.getSharedPreferences(FILE, 0).edit()
            .putBoolean(FOCUS, enabled)
            .putLong(END, end)
            .apply()
    }

    fun allowed(c: Context): Set<String> =
        c.getSharedPreferences(FILE, 0).getStringSet(APPS, emptySet()) ?: emptySet()

    fun setAllowed(c: Context, packages: Set<String>) {
        c.getSharedPreferences(FILE, 0).edit()
            .putStringSet(APPS, packages)
            .apply()
    }
}