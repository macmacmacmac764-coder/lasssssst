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

        // Phone / Dialer
        addResolvedPackages(
            Intent(Intent.ACTION_DIAL)
        )

        // SMS
        addResolvedPackages(
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
            }
        )

        // Camera
        addResolvedPackages(
            Intent("android.media.action.IMAGE_CAPTURE")
        )

        // Home / Launcher
        addResolvedPackages(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
        )
    }

    private fun addResolvedPackages(intent: Intent) {
        try {
            packageManager
                .queryIntentActivities(intent, 0)
                .forEach { info ->
                    alwaysAllowedPackages.add(
                        info.activityInfo.packageName
                    )
                }
        } catch (_: Exception) {
            // Never crash the accessibility service.
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        try {

            if (event == null) {
                return
            }

            /*
             * We only care when the foreground window changes.
             */
            if (
                event.eventType !=
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            ) {
                return
            }

            /*
             * Prefs.enabled() also checks the saved
             * Focus end time.
             */
            if (!Prefs.enabled(this)) {
                return
            }

            /*
             * Safety check for expired Focus.
             */
            val endTime = Prefs.end(this)

            if (
                endTime > 0L &&
                System.currentTimeMillis() >= endTime
            ) {
                Prefs.setFocus(this, false)
                return
            }

            val pkg =
                event.packageName?.toString()
                    ?: return

            /*
             * Never block ReGain itself.
             */
            if (pkg == packageName) {
                return
            }

            /*
             * System UI, Launcher, Dialer, SMS and Camera
             * are always allowed.
             */
            if (pkg in alwaysAllowedPackages) {
                return
            }

            /*
             * Apps explicitly selected by the user
             * are allowed during Focus.
             */
            if (pkg in Prefs.allowed(this)) {
                return
            }

            /*
             * Ignore packages that are not normal
             * launchable applications.
             */
            if (
                packageManager
                    .getLaunchIntentForPackage(pkg) == null
            ) {
                return
            }

            /*
             * Bring the ReGain blocking screen to the front.
             */
            val blockIntent = Intent(
                this,
                BlockActivity::class.java
            ).apply {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }

            startActivity(blockIntent)

        } catch (_: Exception) {
            /*
             * An OEM-specific accessibility event
             * must never crash ReGain.
             */
        }
    }

    override fun onInterrupt() {
        // Nothing to do.
    }
}
