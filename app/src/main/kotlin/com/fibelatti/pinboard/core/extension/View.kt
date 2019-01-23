package com.fibelatti.pinboard.core.extension

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.fibelatti.pinboard.R
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar

@JvmOverloads
fun Context.toast(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

@JvmOverloads
fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration)
        .apply {
            val margin = context.resources.getDimensionPixelSize(R.dimen.margin_regular)
            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(margin, margin, margin, margin)
            }
            view.background = ContextCompat.getDrawable(context, R.drawable.background_snackbar)
        }
        .show()
}

fun BottomAppBar.show() {
    animate().translationY(0f)
        .setDuration(resources.getInteger(R.integer.anim_time_default).toLong())
        .start()
}
