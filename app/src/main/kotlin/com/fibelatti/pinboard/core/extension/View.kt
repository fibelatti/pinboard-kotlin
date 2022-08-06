package com.fibelatti.pinboard.core.extension

import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.fibelatti.core.extension.getContentView
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.LayoutFeedbackBannerBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun BottomAppBar.show() {
    animate()
        .translationY(0f)
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
    val banner = LayoutFeedbackBannerBinding.inflate(
        LayoutInflater.from(context),
        contentView,
        false
    ).apply {
        root.updateLayoutParams<FrameLayout.LayoutParams> {
            gravity = Gravity.CENTER_HORIZONTAL
            updateMargins(top = resources.getDimensionPixelSize(R.dimen.margin_xlarge))
        }
        root.alpha = 0F

        textViewFeedback.text = message
    }.root

    contentView.addView(banner)

    banner.animate()
        .alphaBy(1F)
        .setDuration(1_000)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            banner.animate()
                .alpha(0F)
                .setDuration(1_000)
                .setStartDelay(2_000)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { contentView.removeView(banner) }
                .start()
        }
        .start()
}
