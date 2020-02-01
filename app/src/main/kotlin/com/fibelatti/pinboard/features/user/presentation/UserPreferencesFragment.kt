package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import kotlinx.android.synthetic.main.fragment_user_preferences.*
import javax.inject.Inject

class UserPreferencesFragment @Inject constructor() : BaseFragment(R.layout.fragment_user_preferences) {

    companion object {
        @JvmStatic
        val TAG: String = "UserPreferencesFragment"
    }

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(requireActivity()) }
    private val userPreferencesViewModel by lazy { viewModelFactory.get<UserPreferencesViewModel>(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.observe(appStateViewModel.userPreferencesContent) {
            setupActivityViews()
            setupAppearance(it.appearance)
            setupPreferredDetailsView(it.preferredDetailsView)
            setupAutoFillDescription(it.autoFillDescription)
            setupShowDescriptionInLists(it.showDescriptionInLists)
            setupShowDescriptionInDetails(it.showDescriptionInDetails)
            setupEditAfterSharing(it.editAfterSharing)
            setupPrivateDefault(it.defaultPrivate)
            setupReadLaterDefault(it.defaultReadLater)
        }

        viewLifecycleOwner.observeEvent(userPreferencesViewModel.appearanceChanged) { newAppearance ->
            when (newAppearance) {
                Appearance.DarkTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Appearance.LightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setupActivityViews() {
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

    private fun setupAppearance(appearance: Appearance) {
        when (appearance) {
            Appearance.DarkTheme -> buttonAppearanceDark.isChecked = true
            Appearance.LightTheme -> buttonAppearanceLight.isChecked = true
            else -> buttonAppearanceSystemDefault.isChecked = true
        }
        buttonAppearanceDark.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.DarkTheme)
        }
        buttonAppearanceLight.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.LightTheme)
        }
        buttonAppearanceSystemDefault.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.SystemDefault)
        }
    }

    private fun setupPreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        when (preferredDetailsView) {
            PreferredDetailsView.InAppBrowser -> {
                buttonPreferredDetailsViewInApp.isChecked = true
                textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_in_app_browser_caveat)
            }
            PreferredDetailsView.ExternalBrowser -> {
                buttonPreferredDetailsViewExternal.isChecked = true
                textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_external_browser_caveat)
            }
            PreferredDetailsView.Edit -> {
                buttonPreferredDetailsViewEdit.isChecked = true
                textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_post_details_caveat)
            }
        }

        buttonPreferredDetailsViewInApp.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(PreferredDetailsView.InAppBrowser)
            textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_in_app_browser_caveat)
        }
        buttonPreferredDetailsViewExternal.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(PreferredDetailsView.ExternalBrowser)
            textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_external_browser_caveat)
        }
        buttonPreferredDetailsViewEdit.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(PreferredDetailsView.Edit)
            textViewPreferredDetailsViewCaveat.setText(R.string.user_preferences_preferred_details_post_details_caveat)
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
}
