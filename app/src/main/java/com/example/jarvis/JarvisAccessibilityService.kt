package com.example.jarvis

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.view.accessibility.AccessibilityEvent

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        var instance: JarvisAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun openAppByName(spokenName: String) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val normalizedSpoken = spokenName.trim().lowercase()

        val match = apps.firstOrNull { app: ApplicationInfo ->
            val label = pm.getApplicationLabel(app).toString().lowercase()
            label.contains(normalizedSpoken) || normalizedSpoken.contains(label)
        }

        match?.let {
            pm.getLaunchIntentForPackage(it.packageName)?.let { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
}
