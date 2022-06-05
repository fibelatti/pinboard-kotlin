package com.fibelatti.core.extension

import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.fragment.app.FragmentManager

fun FragmentManager.setupForAccessibility() {
    addOnBackStackChangedListener {
        val lastIndex = fragments.size - 1
        fragments.forEachIndexed { index, fragment ->
            val view = fragment.view ?: return@forEachIndexed

            view.importantForAccessibility = if (index == lastIndex) {
                View.IMPORTANT_FOR_ACCESSIBILITY_YES
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
        }
    }
}

fun View.doOnInitializeAccessibilityNodeInfo(block: (info: AccessibilityNodeInfoCompat) -> Unit) {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            block(info)
        }
    })
}
