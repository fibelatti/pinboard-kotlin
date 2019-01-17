package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction

inline fun FragmentActivity.inTransaction(
    allowStateLoss: Boolean = false,
    block: FragmentTransaction.() -> Unit
) {
    with(supportFragmentManager) {
        beginTransaction().apply {
            block(this)

            if (!isStateSaved) {
                commit()
            } else if (allowStateLoss) {
                commitAllowingStateLoss()
            }
        }
    }
}

inline fun Fragment.inTransaction(
    allowStateLoss: Boolean = false,
    block: FragmentTransaction.() -> Unit
) {
    activity?.inTransaction(allowStateLoss, block)
}
