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

        addResolvedPackages(
            Intent(Intent.ACTION_DIAL)
        )

        addResolvedPackages(
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
            }
        )

        addResolvedPackages(
            Intent("android.media.action.IMAGE_CAPTURE")
        )

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
            // Ignore device-specific package manager errors.
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        try {

            if (event == null) {
                return
            }

            if (
                event.eventType !=
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            ) {
                return
            }

            /*
             * Prefs.enabled() also checks whether the saved
             * Focus end time has expired.
             */
            if (!Prefs.enabled(this)) {
                return
            }

            val endTime = Prefs.end(this)

            if (
                endTime > 0L &&
                System.currentTimeMillis() >= endTime
            ) {
                Prefs.setFocus(this, false)
                return
            }

            val pkg = event.packageName
                ?.toString()
                ?: return

            if (pkg == packageName) {
                return
            }

            if (pkg in alwaysAllowedPackages) {
                return
            }

            if (pkg in Prefs.allowed(this)) {
                return
            }

            /*
             * Ignore packages that are not normal launchable apps.
             */
            if (
                packageManager
                    .getLaunchIntentForPackage(pkg) == null
            ) {
                return
            }

            /*
             * Open the blocking screen on top of the
             * application that the user tried to open.
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
             * Never let a third-party app or OEM-specific
             * accessibility event crash the service.
             */
        }
    }

    override fun onInterrupt() {
        // Nothing to do.
    }
}
