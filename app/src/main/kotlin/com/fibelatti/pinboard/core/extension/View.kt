package com.fibelatti.pinboard.core.extension

import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.fibelatti.core.extension.getContentView
import com.fibelatti.pinboard.R
import com.fibelatti.ui.foundation.topSystemBarsPaddingCompat
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun BottomAppBar.show() {
    animate()
        .translationY(0f)
        .setDuration(resources.getInteger(R.integer.anim_time_default).toLong())
        .start()
}

fun FloatingActionButton.blink(onHidden: () -> Unit = {}) {
    hide(
        object : FloatingActionButton.OnVisibilityChangedListener() {
            override fun onHidden(fab: FloatingActionButton?) {
                super.onHidden(fab)
                onHidden()
                show()
            }
        },
    )
}

fun View.smoothScrollY(scrollBy: Int) {
    ObjectAnimator.ofInt(this, "scrollY", scrollBy)
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .setDuration(resources.getInteger(R.integer.anim_time_long).toLong())
        .start()
}

fun View.showBanner(message: String) {
    val banner = ComposeView(context).apply {
        alpha = 0F
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setThemedContent {
            Banner(message = message)
        }
    }

    val contentView = getContentView()
    contentView.addView(
        banner,
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER_HORIZONTAL,
        ),
    )

    banner.animate()
        .alphaBy(1F)
        .setDuration(500)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            banner.animate()
                .alpha(0F)
                .setDuration(500)
                .setStartDelay(1_500)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { contentView.removeView(banner) }
                .start()
        }
        .start()
}

@Composable
private fun Banner(
    message: String,
) {
    Surface(
        modifier = Modifier
            .padding(all = 24.dp)
            .topSystemBarsPaddingCompat(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 8.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(all = 16.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
@ThemePreviews
private fun BannerPreview() {
    ExtendedTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Banner(message = "Sample message")
        }
    }
}
