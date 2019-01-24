package com.fibelatti.pinboard.core.android

import androidx.transition.Transition

object DefaultTransitionListener : Transition.TransitionListener {

    override fun onTransitionEnd(transition: Transition) {}

    override fun onTransitionResume(transition: Transition) {}

    override fun onTransitionPause(transition: Transition) {}

    override fun onTransitionCancel(transition: Transition) {}

    override fun onTransitionStart(transition: Transition) {}
}
