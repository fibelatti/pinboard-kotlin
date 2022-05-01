package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.isGone
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.LayoutSettingToggleBinding

class SettingToggle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutSettingToggleBinding.inflate(LayoutInflater.from(context), this, true)
    private var onClickListener: (isChecked: Boolean) -> Unit = {}

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingToggle, defStyleAttr, 0)

        if (typedArray.hasValue(R.styleable.SettingToggle_settingTitle)) {
            val titleId = typedArray.getResourceId(R.styleable.SettingToggle_settingTitle, -1)

            if (titleId != -1) {
                binding.settingTitle.setText(titleId)
            } else {
                binding.settingTitle.text = typedArray.getText(R.styleable.SettingToggle_settingTitle)
            }
        } else {
            binding.settingTitle.isGone = true
        }

        if (typedArray.hasValue(R.styleable.SettingToggle_settingDescription)) {
            val titleId = typedArray.getResourceId(R.styleable.SettingToggle_settingDescription, -1)

            if (titleId != -1) {
                binding.settingDescription.setText(titleId)
            } else {
                binding.settingDescription.text = typedArray.getText(R.styleable.SettingToggle_settingDescription)
            }
        } else {
            binding.settingDescription.isGone = true
        }

        typedArray.recycle()

        setOnClickListener {
            binding.settingSwitch.isChecked = !binding.settingSwitch.isChecked
            onClickListener(binding.settingSwitch.isChecked)
        }
    }

    var isActive: Boolean
        get() = binding.settingSwitch.isChecked
        set(value) {
            binding.settingSwitch.isChecked = value
        }

    fun setOnChangedListener(body: (isChecked: Boolean) -> Unit) {
        onClickListener = body
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = true
}
