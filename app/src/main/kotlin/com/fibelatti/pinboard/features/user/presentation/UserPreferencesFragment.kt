package com.fibelatti.pinboard.features.user.presentation

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.smoothScrollY
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentUserPreferencesBinding
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private var binding by viewBinding<FragmentUserPreferencesBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentUserPreferencesBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActivityViews()
        setupViews()
        handleKeyboardVisibility()
        setupViewModels()
    }

    override fun onDestroy() {
        mainActivity?.updateViews { bottomAppBar, _ ->
            bottomAppBar.hideKeyboard()
        }
        activity?.supportFragmentManager?.setFragmentResult(TAG, bundleOf())
        super.onDestroy()
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.user_preferences_title)
            hideSubTitle()
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

    private fun setupViews() {
        binding.layoutPeriodicSync.isVisible = mainVariant
        binding.layoutPrivateDefault.isVisible = mainVariant

        binding.checkboxDynamicColors.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        binding.layoutAddTags.setup(
            afterTagInput = userPreferencesViewModel::searchForTag,
            onTagAdded = { _, currentTags -> userPreferencesViewModel.saveDefaultTags(currentTags) },
            onTagRemoved = { tag, currentTags ->
                userPreferencesViewModel.saveDefaultTags(currentTags)
                userPreferencesViewModel.searchForTag(tag, currentTags)
            }
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
                setupPeriodicSync(it.periodicSync)
                setupAppearance(it.appearance, it.applyDynamicColors)
                setupPreferredDateFormat(it.preferredDateFormat)
                setupPreferredDetailsView(it.preferredDetailsView)

                binding.checkboxAutoFillDescription.setValueAndChangeListener(
                    it.autoFillDescription,
                    userPreferencesViewModel::saveAutoFillDescription
                )
                binding.checkboxShowDescriptionInLists.setValueAndChangeListener(
                    it.showDescriptionInLists,
                    userPreferencesViewModel::saveShowDescriptionInLists
                )

                setupEditAfterSharing(it.editAfterSharing)

                binding.checkboxPrivateDefault.setValueAndChangeListener(
                    it.defaultPrivate,
                    userPreferencesViewModel::saveDefaultPrivate
                )
                binding.checkboxReadLaterDefault.setValueAndChangeListener(
                    it.defaultReadLater,
                    userPreferencesViewModel::saveDefaultReadLater
                )

                binding.layoutAddTags.showTags(it.defaultTags)
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)

        userPreferencesViewModel.appearanceChanged
            .onEach { newAppearance ->
                when (newAppearance) {
                    Appearance.DarkTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Appearance.LightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)

        userPreferencesViewModel.suggestedTags
            .onEach {
                binding.layoutAddTags.showSuggestedValuesAsTags(it, showRemoveIcon = false)
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    private fun setupPeriodicSync(periodicSync: PeriodicSync) {
        when (periodicSync) {
            PeriodicSync.Off -> binding.buttonPeriodicSyncOff.isChecked = true
            PeriodicSync.Every6Hours -> binding.buttonPeriodicSync6Hours.isChecked = true
            PeriodicSync.Every12Hours -> binding.buttonPeriodicSync12Hours.isChecked = true
            PeriodicSync.Every24Hours -> binding.buttonPeriodicSync24Hours.isChecked = true
        }
        binding.buttonPeriodicSyncOff.selectOnClick {
            userPreferencesViewModel.savePeriodicSync(PeriodicSync.Off)
        }
        binding.buttonPeriodicSync6Hours.selectOnClick {
            userPreferencesViewModel.savePeriodicSync(PeriodicSync.Every6Hours)
        }
        binding.buttonPeriodicSync12Hours.selectOnClick {
            userPreferencesViewModel.savePeriodicSync(PeriodicSync.Every12Hours)
        }
        binding.buttonPeriodicSync24Hours.selectOnClick {
            userPreferencesViewModel.savePeriodicSync(PeriodicSync.Every24Hours)
        }
    }

    private fun setupAppearance(appearance: Appearance, applyDynamicColors: Boolean) {
        when (appearance) {
            Appearance.DarkTheme -> binding.buttonAppearanceDark.isChecked = true
            Appearance.LightTheme -> binding.buttonAppearanceLight.isChecked = true
            else -> binding.buttonAppearanceSystemDefault.isChecked = true
        }
        binding.buttonAppearanceDark.selectOnClick {
            userPreferencesViewModel.saveAppearance(Appearance.DarkTheme)
        }
        binding.buttonAppearanceLight.selectOnClick {
            userPreferencesViewModel.saveAppearance(Appearance.LightTheme)
        }
        binding.buttonAppearanceSystemDefault.selectOnClick {
            userPreferencesViewModel.saveAppearance(Appearance.SystemDefault)
        }

        binding.checkboxDynamicColors.isSaveEnabled = false // To prevent a recreate loop
        binding.checkboxDynamicColors.setValueAndChangeListener(applyDynamicColors) {
            userPreferencesViewModel.saveApplyDynamicColors(it)
            ActivityCompat.recreate(requireActivity())
        }
    }

    private fun setupPreferredDateFormat(preferredDateFormat: PreferredDateFormat) {
        when (preferredDateFormat) {
            PreferredDateFormat.DayMonthYearWithTime -> binding.buttonDateFormatDayFirst.isChecked = true
            PreferredDateFormat.MonthDayYearWithTime -> binding.buttonDateFormatMonthFirst.isChecked = true
            PreferredDateFormat.YearMonthDayWithTime -> binding.buttonDateFormatYearFirst.isChecked = true
        }
        binding.buttonDateFormatDayFirst.selectOnClick {
            userPreferencesViewModel.savePreferredDateFormat(PreferredDateFormat.DayMonthYearWithTime)
        }
        binding.buttonDateFormatMonthFirst.selectOnClick {
            userPreferencesViewModel.savePreferredDateFormat(PreferredDateFormat.MonthDayYearWithTime)
        }
        binding.buttonDateFormatYearFirst.selectOnClick {
            userPreferencesViewModel.savePreferredDateFormat(PreferredDateFormat.YearMonthDayWithTime)
        }
    }

    private fun setupPreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        val markAsReadOnOpen: Boolean = when (preferredDetailsView) {
            is PreferredDetailsView.InAppBrowser -> {
                inAppSelected(preferredDetailsView.markAsReadOnOpen)
                preferredDetailsView.markAsReadOnOpen
            }
            is PreferredDetailsView.ExternalBrowser -> {
                externalSelected(preferredDetailsView.markAsReadOnOpen)
                preferredDetailsView.markAsReadOnOpen
            }
            PreferredDetailsView.Edit -> {
                editSelected()
                false
            }
        }

        binding.buttonPreferredDetailsViewInApp.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(
                PreferredDetailsView.InAppBrowser(binding.checkboxMarkAsReadOnOpen.isChecked)
            )
            inAppSelected(markAsReadOnOpen)
        }
        binding.buttonPreferredDetailsViewExternal.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(
                PreferredDetailsView.ExternalBrowser(binding.checkboxMarkAsReadOnOpen.isChecked)
            )
            externalSelected(markAsReadOnOpen)
        }
        binding.buttonPreferredDetailsViewEdit.setOnClickListener {
            userPreferencesViewModel.savePreferredDetailsView(PreferredDetailsView.Edit)
            editSelected()
        }
    }

    private fun setupEditAfterSharing(editAfterSharing: EditAfterSharing) {
        when (editAfterSharing) {
            EditAfterSharing.BeforeSaving -> binding.buttonBeforeSaving.isChecked = true
            EditAfterSharing.AfterSaving -> binding.buttonAfterSaving.isChecked = true
            EditAfterSharing.SkipEdit -> binding.buttonSkipEditing.isChecked = true
        }
        binding.buttonBeforeSaving.selectOnClick {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.BeforeSaving)
        }
        binding.buttonAfterSaving.selectOnClick {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.AfterSaving)
        }
        binding.buttonSkipEditing.selectOnClick {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.SkipEdit)
        }
    }

    private fun inAppSelected(markAsReadOnOpen: Boolean) {
        binding.buttonPreferredDetailsViewInApp.isChecked = true
        binding.textViewPreferredDetailsViewCaveat.setText(
            R.string.user_preferences_preferred_details_in_app_browser_caveat
        )
        binding.checkboxMarkAsReadOnOpen.setValueAndChangeListener(
            markAsReadOnOpen,
            userPreferencesViewModel::saveMarkAsReadOnOpen
        )
        binding.checkboxMarkAsReadOnOpen.visible()
        binding.checkboxMarkAsReadOnOpenCaveat.visible()
    }

    private fun externalSelected(markAsReadOnOpen: Boolean) {
        binding.buttonPreferredDetailsViewExternal.isChecked = true
        binding.textViewPreferredDetailsViewCaveat.setText(
            R.string.user_preferences_preferred_details_external_browser_caveat
        )
        binding.checkboxMarkAsReadOnOpen.setValueAndChangeListener(
            markAsReadOnOpen,
            userPreferencesViewModel::saveMarkAsReadOnOpen
        )
        binding.checkboxMarkAsReadOnOpen.visible()
        binding.checkboxMarkAsReadOnOpenCaveat.visible()
    }

    private fun editSelected() {
        binding.buttonPreferredDetailsViewEdit.isChecked = true
        binding.textViewPreferredDetailsViewCaveat.setText(
            R.string.user_preferences_preferred_details_post_details_caveat
        )
        binding.checkboxMarkAsReadOnOpen.gone()
        binding.checkboxMarkAsReadOnOpenCaveat.gone()
    }

    private fun CheckBox.setValueAndChangeListener(
        initialValue: Boolean,
        onCheckedChangeListener: (Boolean) -> Unit,
    ) {
        setOnCheckedChangeListener(null)
        isChecked = initialValue
        setOnCheckedChangeListener { _, isChecked -> onCheckedChangeListener(isChecked) }
    }

    private fun MaterialButton.selectOnClick(
        onClickListener: () -> Unit,
    ) {
        setOnClickListener {
            onClickListener()
            isChecked = true
        }
    }
}
