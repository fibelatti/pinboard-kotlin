package com.fibelatti.pinboard.core.extension

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.fibelatti.pinboard.R

fun FragmentManager.popTo(tag: String) {
    for (fragment in fragments.reversed()) {
        if (fragment.tag != tag) popBackStack() else break
    }
}

fun FragmentManager.slideUp(
    @IdRes containerId: Int,
    fragment: Fragment,
    tag: String,
    addToBackStack: Boolean = true,
    operation: FragmentTransaction.(Int, Fragment, String?) -> Unit = { _, _, _ ->
        add(containerId, fragment, tag)
    },
) {
    commit {
        val destination = fragment.apply {
            enterTransition = R.transition.slide_up
        }
        operation(containerId, destination, tag)

        if (addToBackStack) {
            addToBackStack(tag)
        }
    }
}
