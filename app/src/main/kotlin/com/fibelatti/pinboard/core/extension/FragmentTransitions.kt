package com.fibelatti.pinboard.core.extension

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.fibelatti.pinboard.R

fun FragmentActivity.popTo(tag: String) {
    for (fragment in supportFragmentManager.fragments.reversed()) {
        if (fragment.tag != tag) {
            supportFragmentManager.popBackStack()
        } else {
            break
        }
    }
}

fun FragmentActivity.slideFromTheRight(
    @IdRes containerId: Int,
    fragment: Fragment,
    tag: String,
    addToBackStack: Boolean = true,
) {
    supportFragmentManager.commit {
        setCustomAnimations(
            R.anim.slide_right_in,
            R.anim.slide_left_out,
            R.anim.slide_left_in,
            R.anim.slide_right_out,
        )
        add(containerId, fragment, tag)

        if (addToBackStack) {
            addToBackStack(tag)
        }
    }
}

fun FragmentActivity.slideUp(
    @IdRes containerId: Int,
    fragment: Fragment,
    tag: String,
    addToBackStack: Boolean = true,
) {
    supportFragmentManager.commit {
        setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
        add(containerId, fragment, tag)

        if (addToBackStack) {
            addToBackStack(tag)
        }
    }
}
