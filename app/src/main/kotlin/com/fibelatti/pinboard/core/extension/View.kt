package com.fibelatti.pinboard.core.extension

import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.fibelatti.core.extension.getContentView
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
