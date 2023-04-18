package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.fibelatti.core.extension.doOnInitializeAccessibilityNodeInfo
import com.fibelatti.pinboard.databinding.LayoutTitleBinding

class TitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutTitleBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.textViewTitle.doOnInitializeAccessibilityNodeInfo { info ->
            info.isHeading = true
        }
        binding.textViewTitle.accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE
        binding.textViewSubtitle.accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE
        binding.buttonAction.accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE
    }

    fun setNavigation(@DrawableRes iconRes: Int, onClick: OnClickListener) {
        binding.buttonNavigateBack.apply {
            setImageDrawable(ContextCompat.getDrawable(context, iconRes))
            setOnClickListener(onClick)
            isVisible = true
        }
    }

    fun hideNavigation() {
        binding.buttonNavigateBack.setOnClickListener(null)
        binding.buttonNavigateBack.isVisible = false
    }

    fun setTitle(title: String) {
        if (title.isNotEmpty()) {
            binding.textViewTitle.text = title
            binding.textViewTitle.isVisible = true
        } else {
            hideTitle()
        }
    }

    fun hideTitle() {
        binding.textViewTitle.isVisible = false
    }

    fun setSubTitle(title: String) {
        if (title.isNotEmpty()) {
            binding.textViewSubtitle.text = title
            binding.textViewSubtitle.isVisible = true
        } else {
            hideSubTitle()
        }
    }

    fun hideSubTitle() {
        binding.textViewSubtitle.isVisible = false
    }

    fun setActionButton(label: String, onClick: OnClickListener) {
        binding.buttonAction.setOnClickListener(onClick)
        binding.buttonAction.text = label
        binding.buttonAction.isVisible = true
    }

    fun hideActionButton() {
        binding.buttonAction.setOnClickListener(null)
        binding.buttonAction.isVisible = false
    }
}
