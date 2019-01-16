package com.fibelatti.pinboard.core.android

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

inline fun AppCompatActivity.inSupportFragmentManagerTransaction(
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
