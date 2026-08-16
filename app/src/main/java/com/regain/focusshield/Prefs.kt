package com.regain.focusshield

import android.content.Context

object Prefs {

    private const val FILE = "regain_prefs"

    private const val FOCUS = "focus_enabled"
    private const val END = "focus_end"
    private const val APPS = "allowed_apps"

    fun enabled(c: Context): Boolean {
        val prefs = c.getSharedPreferences(FILE, 0)
        val enabled = prefs.getBoolean(FOCUS, false)
        val end = prefs.getLong(END, 0L)

        if (!enabled) {
            return false
        }

        if (end > 0L && System.currentTimeMillis() >= end) {
            prefs.edit()
                .putBoolean(FOCUS, false)
                .putLong(END, 0L)
                .apply()

            return false
        }

        return true
    }

    fun end(c: Context): Long {
        return c.getSharedPreferences(FILE, 0)
            .getLong(END, 0L)
    }

    fun remaining(c: Context): Long {
        val endTime = end(c)

        if (endTime <= 0L) {
            return 0L
        }

        return (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun setFocus(
        c: Context,
        enabled: Boolean,
        end: Long = 0L
    ) {
        c.getSharedPreferences(FILE, 0)
            .edit()
            .putBoolean(FOCUS, enabled)
            .putLong(END, if (enabled) end else 0L)
            .apply()
    }

    fun allowed(c: Context): Set<String> {
        return c.getSharedPreferences(FILE, 0)
            .getStringSet(APPS, emptySet())
            ?.toSet()
            ?: emptySet()
    }

    fun setAllowed(
        c: Context,
        packages: Set<String>
    ) {
        c.getSharedPreferences(FILE, 0)
            .edit()
            .putStringSet(APPS, packages.toSet())
            .apply()
    }
}
