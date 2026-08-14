# ReGain — Free Focus Mode

This is a clean native Android implementation of the requested Focus Mode behavior.

## Included

- Focus Mode timer (30 minutes by default)
- Calls/phone UI, SMS/messaging, and camera allowed by default
- Select additional installed apps to allow during Focus Mode
- No subscriptions
- No Google Play Billing
- No premium lock
- Android Accessibility Service used to detect the foreground app

## Build on GitHub Actions

1. Upload the entire project preserving folders.
2. Open Actions.
3. Select **Build ReGain APK**.
4. Click **Run workflow**.
5. Download the artifact **ReGain-Free-Focus-APK**.

## First run on the phone

Open ReGain and tap **فعال‌سازی دسترسی Focus Mode**. Enable ReGain under Android Accessibility settings. Then choose additional allowed apps and start Focus Mode.

Important: Android manufacturer restrictions can affect Accessibility Services. The app does not attempt to bypass Android security or uninstall protections.

## Build sanity checks

This project is intentionally a standalone Android/Kotlin project. It does not use Capacitor, React, Vite, `capacitor.build.gradle`, `capacitor-android`, or `capacitor-cordova-android-plugins`.

The source files use normal Kotlin/Gradle casing (`import`, `class`, `private`, `override`, `implementation`, `plugins`, etc.). Do not change these keywords to uppercase.

The GitHub Actions workflow installs Gradle 8.2.1 and Java 17, then builds `:app:assembleDebug`.
