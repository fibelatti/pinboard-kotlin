package com.fibelatti.pinboard.features.user.presentation

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.customview.SettingToggle
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.smoothScrollY
import com.fibelatti.pinboard.databinding.FragmentUserPreferencesBinding
import com.fibelatti.pinboard.features.BottomBarHost.Companion.bottomBarHost
import com.fibelatti.pinboard.features.TitleLayoutHost.Companion.titleLayoutHost
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserPreferencesFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "UserPreferencesFragment"
    }

    private val userPreferencesViewModel: UserPreferencesViewModel by viewModels()

    private val binding by viewBinding(FragmentUserPreferencesBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentUserPreferencesBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActivityViews()
        setupViews()
        handleKeyboardVisibility()
        setupViewModels()
    }

    override fun onDestroy() {
        bottomBarHost.update { bottomAppBar, _ ->
            bottomAppBar.hideKeyboard()
        }
        activity?.supportFragmentManager?.setFragmentResult(TAG, bundleOf())
        super.onDestroy()
    }

    private fun setupActivityViews() {
        titleLayoutHost.update {
            setTitle(R.string.user_preferences_title)
            hideSubTitle()
            setNavigateUp { navigateBack() }
        }
        bottomBarHost.update { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                isVisible = false
            }
            fab.hide()
        }
    }

    private fun setupViews() {
        binding.groupBackgroundSync.isVisible = mainVariant
        binding.togglePrivateDefault.isVisible = mainVariant

        binding.toggleDynamicColors.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        binding.layoutAddTags.setup(
            onTextChanged = userPreferencesViewModel::searchForTag,
            onTagAdded = userPreferencesViewModel::saveDefaultTags,
            onTagRemoved = userPreferencesViewModel::saveDefaultTags,
        )
    }

    private fun handleKeyboardVisibility() {
        binding.root.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

                view.updatePadding(bottom = initialPadding.bottom + imeInsets.bottom)

                with(binding.root) {
                    val lastChild = getChildAt(childCount - 1)
                    val bottom = lastChild.bottom + paddingBottom
                    val delta = height - scrollY - bottom

                    view.smoothScrollY(-delta)
                }
            } else {
                view.updatePadding(bottom = initialPadding.bottom)
            }
        }
    }

    private fun setupViewModels() {
        userPreferencesViewModel.currentPreferences
            .onEach {
                binding.toggleAutoUpdate.setActiveAndOnChangedListener(
                    it.autoUpdate,
                    userPreferencesViewModel::saveAutoUpdate
                )

                setupPeriodicSync(it.periodicSync)
                setupAppearance(it.appearance, it.applyDynamicColors)
                setupPreferredDateFormat(it.preferredDateFormat)
                setupPreferredDetailsView(it.preferredDetailsView)

                binding.toggleAutoFillDescription.setActiveAndOnChangedListener(
                    it.autoFillDescription,
                    userPreferencesViewModel::saveAutoFillDescription
                )
                binding.toggleShowDescriptionInLists.setActiveAndOnChangedListener(
                    it.showDescriptionInLists,
                    userPreferencesViewModel::saveShowDescriptionInLists
                )

                setupEditAfterSharing(it.editAfterSharing)

                binding.togglePrivateDefault.setActiveAndOnChangedListener(
                    it.defaultPrivate,
                    userPreferencesViewModel::saveDefaultPrivate
                )
                binding.toggleReadLaterDefault.setActiveAndOnChangedListener(
                    it.defaultReadLater,
                    userPreferencesViewModel::saveDefaultReadLater
                )

                binding.layoutAddTags.showTags(it.defaultTags)

                userPreferencesViewModel.searchForTag(tag = "", currentTags = it.defaultTags)
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        userPreferencesViewModel.appearanceChanged
            .onEach { newAppearance ->
                when (newAppearance) {
                    Appearance.DarkTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Appearance.LightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        userPreferencesViewModel.suggestedTags
            .onEach { binding.layoutAddTags.showSuggestedValuesAsTags(it, showRemoveIcon = false) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupPeriodicSync(periodicSync: PeriodicSync) {
        binding.buttonPeriodicSync.setupSelectionButton(
            currentSelection = periodicSync,
            buttonText = { option: PeriodicSync ->
                when (option) {
                    PeriodicSync.Off -> R.string.user_preferences_periodic_sync_off
                    PeriodicSync.Every6Hours -> R.string.user_preferences_periodic_sync_6_hours
                    PeriodicSync.Every12Hours -> R.string.user_preferences_periodic_sync_12_hours
                    PeriodicSync.Every24Hours -> R.string.user_preferences_periodic_sync_24_hours
                }
            },
            title = R.string.user_preferences_periodic_sync,
            options = {
                listOf(
                    PeriodicSync.Off,
                    PeriodicSync.Every6Hours,
                    PeriodicSync.Every12Hours,
                    PeriodicSync.Every24Hours,
                )
            },
            onOptionSelected = userPreferencesViewModel::savePeriodicSync,
        )
    }

    @Suppress("MagicNumber")
    private fun setupAppearance(appearance: Appearance, applyDynamicColors: Boolean) {
        binding.buttonAppearance.setupSelectionButton(
            currentSelection = appearance,
            buttonText = { option: Appearance ->
                when (option) {
                    Appearance.DarkTheme -> R.string.user_preferences_appearance_dark
                    Appearance.LightTheme -> R.string.user_preferences_appearance_light
                    Appearance.SystemDefault -> R.string.user_preferences_appearance_system_default
                }
            },
            title = R.string.user_preferences_appearance,
            options = {
                listOf(
                    Appearance.DarkTheme,
                    Appearance.LightTheme,
                    Appearance.SystemDefault,
                )
            },
            onOptionSelected = userPreferencesViewModel::saveAppearance,
        )

        binding.toggleDynamicColors.setActiveAndOnChangedListener(applyDynamicColors) {
            userPreferencesViewModel.saveApplyDynamicColors(it)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(300L) // Wait until the switch is done animating
                ActivityCompat.recreate(requireActivity())
            }
        }
    }

    private fun setupPreferredDateFormat(preferredDateFormat: PreferredDateFormat) {
        binding.buttonPreferredDateFormat.setupSelectionButton(
            currentSelection = preferredDateFormat,
            buttonText = { option: PreferredDateFormat ->
                when (option) {
                    PreferredDateFormat.DayMonthYearWithTime -> R.string.user_preferences_date_format_day_first
                    PreferredDateFormat.MonthDayYearWithTime -> R.string.user_preferences_date_format_month_first
                    PreferredDateFormat.ShortYearMonthDayWithTime ->
                        R.string.user_preferences_date_format_short_year_first
                    PreferredDateFormat.YearMonthDayWithTime -> R.string.user_preferences_date_format_year_first
                }
            },
            title = R.string.user_preferences_date_format,
            options = {
                listOf(
                    PreferredDateFormat.DayMonthYearWithTime,
                    PreferredDateFormat.MonthDayYearWithTime,
                    PreferredDateFormat.ShortYearMonthDayWithTime,
                    PreferredDateFormat.YearMonthDayWithTime,
                )
            },
            onOptionSelected = userPreferencesViewModel::savePreferredDateFormat,
        )
    }

    private fun setupPreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        binding.buttonPreferredDetailsView.setupSelectionButton(
            currentSelection = preferredDetailsView,
            buttonText = { option: PreferredDetailsView ->
                when (option) {
                    is PreferredDetailsView.InAppBrowser -> R.string.user_preferences_preferred_details_in_app_browser
                    is PreferredDetailsView.ExternalBrowser ->
                        R.string.user_preferences_preferred_details_external_browser
                    is PreferredDetailsView.Edit -> R.string.user_preferences_preferred_details_post_details
                }
            },
            title = R.string.user_preferences_preferred_details_view,
            options = {
                listOf(
                    PreferredDetailsView.InAppBrowser(binding.toggleMarkAsReadOnOpen.isActive),
                    PreferredDetailsView.ExternalBrowser(binding.toggleMarkAsReadOnOpen.isActive),
                    PreferredDetailsView.Edit,
                )
            },
            onOptionSelected = { option: PreferredDetailsView ->
                when (option) {
                    is PreferredDetailsView.InAppBrowser -> {
                        userPreferencesViewModel.savePreferredDetailsView(option)
                        binding.toggleMarkAsReadOnOpen.isVisible = true
                    }
                    is PreferredDetailsView.ExternalBrowser -> {
                        userPreferencesViewModel.savePreferredDetailsView(option)
                        binding.toggleMarkAsReadOnOpen.isVisible = true
                    }
                    is PreferredDetailsView.Edit -> {
                        userPreferencesViewModel.savePreferredDetailsView(option)
                        binding.toggleMarkAsReadOnOpen.isVisible = false
                    }
                }
            }
        )

        binding.toggleMarkAsReadOnOpen.isVisible = preferredDetailsView != PreferredDetailsView.Edit
        binding.toggleMarkAsReadOnOpen.setActiveAndOnChangedListener(
            initialValue = when (preferredDetailsView) {
                is PreferredDetailsView.InAppBrowser -> preferredDetailsView.markAsReadOnOpen
                is PreferredDetailsView.ExternalBrowser -> preferredDetailsView.markAsReadOnOpen
                is PreferredDetailsView.Edit -> false
            },
            onChangedListener = userPreferencesViewModel::saveMarkAsReadOnOpen,
        )
    }

    private fun setupEditAfterSharing(editAfterSharing: EditAfterSharing) {
        binding.buttonEditAfterSharing.setupSelectionButton(
            currentSelection = editAfterSharing,
            buttonText = { option: EditAfterSharing ->
                when (option) {
                    is EditAfterSharing.BeforeSaving -> R.string.user_preferences_edit_after_sharing_before_saving
                    is EditAfterSharing.AfterSaving -> R.string.user_preferences_edit_after_sharing_after_saving
                    is EditAfterSharing.SkipEdit -> R.string.user_preferences_edit_after_sharing_skip
                }
            },
            title = R.string.user_preferences_edit_after_sharing_title,
            options = {
                listOf(
                    EditAfterSharing.BeforeSaving,
                    EditAfterSharing.AfterSaving,
                    EditAfterSharing.SkipEdit,
                )
            },
            onOptionSelected = userPreferencesViewModel::saveEditAfterSharing,
        )
    }

    private fun SettingToggle.setActiveAndOnChangedListener(
        initialValue: Boolean,
        onChangedListener: (Boolean) -> Unit,
    ) {
        isActive = initialValue
        setOnChangedListener(onChangedListener)
    }

    private fun <T> MaterialButton.setupSelectionButton(
        currentSelection: T,
        buttonText: (T) -> Int,
        @StringRes title: Int,
        options: () -> List<T>,
        onOptionSelected: (T) -> Unit,
    ) = apply {
        setText(buttonText(currentSelection))
        setOnClickListener {
            SelectionDialog.show(
                context = requireContext(),
                title = getString(title),
                options = options(),
                optionName = { option -> getString(buttonText(option)) },
                onOptionSelected = onOptionSelected,
            )
        }
    }
}
