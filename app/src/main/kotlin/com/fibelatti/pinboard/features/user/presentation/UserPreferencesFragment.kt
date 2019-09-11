package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.DarkTheme
import com.fibelatti.pinboard.core.android.LightTheme
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.mainActivity
import kotlinx.android.synthetic.main.fragment_user_preferences.*
import javax.inject.Inject

class UserPreferencesFragment @Inject constructor() : BaseFragment(R.layout.fragment_user_preferences) {

    companion object {
        @JvmStatic
        val TAG: String = "UserPreferencesFragment"
    }

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(this) }
    private val userPreferencesViewModel by lazy { viewModelFactory.get<UserPreferencesViewModel>(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()

        viewLifecycleOwner.observe(appStateViewModel.userPreferencesContent) {
            setupAppearance(it.appearance)
            setupAutoFillDescription(it.autoFillDescription)
            setupShowDescriptionInLists(it.showDescriptionInLists)
            setupShowDescriptionInDetails(it.showDescriptionInDetails)
            setupEditAfterSharing(it.editAfterSharing)
            setupPrivateDefault(it.defaultPrivate)
            setupReadLaterDefault(it.defaultReadLater)
        }

        viewLifecycleOwner.observeEvent(userPreferencesViewModel.appearanceChanged) {
            activity?.recreate()
        }
    }

    private fun setupAppearance(appearance: Appearance) {
        when (appearance) {
            DarkTheme -> buttonAppearanceDark.isChecked = true
            LightTheme -> buttonAppearanceLight.isChecked = true
        }
        buttonAppearanceDark.setOnClickListener {
            userPreferencesViewModel.saveAppearance(DarkTheme)
        }
        buttonAppearanceLight.setOnClickListener {
            userPreferencesViewModel.saveAppearance(LightTheme)
        }
    }

    private fun setupAutoFillDescription(value: Boolean) {
        checkboxAutoFillDescription.isChecked = value
        checkboxAutoFillDescription.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveAutoFillDescription(isChecked)
        }
    }

    private fun setupShowDescriptionInLists(value: Boolean) {
        checkboxShowDescriptionInLists.isChecked = value
        checkboxShowDescriptionInLists.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveShowDescriptionInLists(isChecked)
        }
    }

    private fun setupShowDescriptionInDetails(value: Boolean) {
        checkboxShowDescriptionInDetails.isChecked = value
        checkboxShowDescriptionInDetails.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveShowDescriptionInDetails(isChecked)
        }
    }

    private fun setupEditAfterSharing(value: Boolean) {
        checkboxEditAfterSharing.isChecked = value
        checkboxEditAfterSharing.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveEditAfterSharing(isChecked)
        }
    }

    private fun setupPrivateDefault(value: Boolean) {
        checkboxPrivateDefault.isChecked = value
        checkboxPrivateDefault.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveDefaultPrivate(isChecked)
        }
    }

    private fun setupReadLaterDefault(value: Boolean) {
        checkboxReadLaterDefault.isChecked = value
        checkboxReadLaterDefault.setOnCheckedChangeListener { _, isChecked ->
            userPreferencesViewModel.saveDefaultReadLater(isChecked)
        }
    }

    private fun setupLayout() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.user_preferences_title)
            setNavigateUp { navigateBack() }
        }
        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                gone()
            }
            fab.hide()
        }
    }
}
