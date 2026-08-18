package com.regain.focusshield

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {

    private val alwaysAllowedPackages = mutableSetOf(
        "com.regain.focusshield",
        "com.android.systemui"
    )

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L

    companion object {
        private const val BLOCK_COOLDOWN = 700L
    }

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
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        try {

            if (event == null) {
                return
            }

            val type = event.eventType

            if (
                type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type != AccessibilityEvent.TYPE_WINDOWS_CHANGED
            ) {
                return
            }

            /*
             * Focus must still be active.
             * Prefs.enabled() also checks the saved end time.
             */
            if (!Prefs.enabled(this)) {
                lastBlockedPackage = null
                return
            }

            val endTime = Prefs.end(this)

            if (
                endTime > 0L &&
                System.currentTimeMillis() >= endTime
            ) {
                Prefs.setFocus(this, false)
                lastBlockedPackage = null
                return
            }

            val pkg =
                event.packageName?.toString()
                    ?: return

            /*
             * ReGain itself is always allowed.
             */
            if (pkg == packageName) {
                return
            }

            /*
             * System UI / phone / SMS / camera / launcher.
             */
            if (pkg in alwaysAllowedPackages) {
                return
            }

            /*
             * User-selected allowed apps.
             */
            if (pkg in Prefs.allowed(this)) {
                return
            }

            /*
             * Only deal with normal launchable applications.
             */
            if (
                packageManager
                    .getLaunchIntentForPackage(pkg) == null
            ) {
                return
            }

            /*
             * Prevent an event storm.
             */
            val now = SystemClock.uptimeMillis()

            if (
                pkg == lastBlockedPackage &&
                now - lastBlockTime < BLOCK_COOLDOWN
            ) {
                return
            }

            lastBlockedPackage = pkg
            lastBlockTime = now

            blockApplication(pkg)

        } catch (_: Exception) {
            /*
             * Never allow a bad third-party event
             * to crash the accessibility service.
             */
        }
    }

    private fun blockApplication(
        blockedPackage: String
    ) {

        try {

            /*
             * STEP 1
             *
             * Actually remove the blocked app from the
             * foreground.
             *
             * This is the important difference from the
             * previous version.
             */
            performGlobalAction(
                GLOBAL_ACTION_HOME
            )

        } catch (_: Exception) {
        }

        try {

            /*
             * STEP 2
             *
             * Show ReGain's blocking screen.
             */
            val intent = Intent(
                this,
                BlockActivity::class.java
            ).apply {

                putExtra(
                    "blocked_package",
                    blockedPackage
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }

            startActivity(intent)

        } catch (_: Exception) {
        }
    }

    override fun onInterrupt() {
        // Nothing to do.
    }
}
