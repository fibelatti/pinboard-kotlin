package com.fibelatti.pinboard.core.extension

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

fun View.showBanner(@StringRes messageRes: Int) {
    showBanner(message = resources.getString(messageRes))
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

private fun View.getContentView(): ViewGroup {
    var parent = parent as View
    while (parent.id != android.R.id.content) {
        parent = parent.parent as View
    }

    return parent as ViewGroup
}

@Composable
private fun Banner(
    message: String,
) {
    Surface(
        modifier = Modifier
            .padding(all = 24.dp)
            .safeDrawingPadding(),
        shape = MaterialTheme.shapes.small,
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
