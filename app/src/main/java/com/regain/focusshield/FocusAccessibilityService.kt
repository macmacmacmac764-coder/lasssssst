package com.regain.focusshield

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {

    private val alwaysAllowedPackages = mutableSetOf(
        "com.regain.focusshield",
        "com.android.systemui"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Keep phone, SMS, camera and launcher apps usable during Focus Mode.
        addResolvedPackages(Intent(Intent.ACTION_DIAL))
        addResolvedPackages(Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
        })
        addResolvedPackages(Intent("android.media.action.IMAGE_CAPTURE"))
        addResolvedPackages(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        })
    }

    private fun addResolvedPackages(intent: Intent) {
        try {
            packageManager.queryIntentActivities(intent, 0).forEach { info ->
                alwaysAllowedPackages.add(info.activityInfo.packageName)
            }
        } catch (_: Exception) {
            // Device-specific package manager failures must never crash the service.
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (!Prefs.enabled(this)) return
            if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

            val pkg = event.packageName?.toString() ?: return
            if (pkg in alwaysAllowedPackages) return
            if (pkg in Prefs.allowed(this)) return
            if (pkg == packageName) return

            // Only block normal user-launchable applications.
            if (packageManager.getLaunchIntentForPackage(pkg) == null) return

            val intent = Intent(this, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        } catch (_: Exception) {
            // A third-party app or OEM-specific event must never crash ReGain.
        }
    }

    override fun onInterrupt() = Unit
}
