package com.fibelatti.pinboard.core.extension

import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultAnimationListener
import com.fibelatti.pinboard.databinding.LayoutFeedbackBannerBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun BottomAppBar.show() {
    animate().translationY(0f)
        .setDuration(resources.getInteger(R.integer.anim_time_default).toLong())
        .start()
}

fun FloatingActionButton.blink(onHidden: () -> Unit = {}) {
    hide(object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)
            onHidden()
            show()
        }
    })
}

fun View.smoothScrollY(scrollBy: Int) {
    ObjectAnimator.ofInt(this, "scrollY", scrollBy)
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .setDuration(resources.getInteger(R.integer.anim_time_long).toLong())
        .start()
}

@Suppress("MagicNumber")
fun View.showBanner(message: String) {
    val contentView = getContentView()
    val banner = LayoutFeedbackBannerBinding.inflate(LayoutInflater.from(context), contentView, false).apply {
        root.updateLayoutParams<FrameLayout.LayoutParams> {
            gravity = Gravity.CENTER_HORIZONTAL
            updateMargins(top = resources.getDimensionPixelSize(R.dimen.margin_xlarge))
        }

        textViewFeedback.text = message
    }.root

    contentView.addView(banner)

    val animTime = resources.getInteger(R.integer.anim_time_long).toLong()
    val disappearAnimation = AlphaAnimation(1F, 0F).apply {
        duration = animTime
        startOffset = animTime * 5
        interpolator = AccelerateInterpolator()
        setAnimationListener(object : DefaultAnimationListener() {
            override fun onAnimationEnd(animation: Animation?) {
                contentView.removeView(banner)
            }
        })
    }

    val appearAnimation = AlphaAnimation(0F, 1F).apply {
        duration = animTime
        interpolator = DecelerateInterpolator()
        setAnimationListener(object : DefaultAnimationListener() {
            override fun onAnimationEnd(animation: Animation?) {
                banner.startAnimation(disappearAnimation)
            }
        })
    }

    banner.startAnimation(appearAnimation)
}

private fun View.getContentView(): ViewGroup {
    var parent = parent as View
    while (parent.id != android.R.id.content) {
        parent = parent.parent as View
    }

    return parent as ViewGroup
}

/**
 * Shorthand function for running the given [block] when the [EditText] receives a [handledAction] or a keyboard
 * submit.
 */
inline fun EditText.onActionOrKeyboardSubmit(
    vararg handledAction: Int,
    crossinline block: EditText.() -> Unit,
) {
    val handledActions = handledAction.toList()

    setOnEditorActionListener { _, actionId, event ->
        val shouldHandle = actionId in handledActions ||
            event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

        return@setOnEditorActionListener if (shouldHandle) {
            block()
            true
        } else {
            false
        }
    }
}
