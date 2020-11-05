package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.smoothScrollY
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentUserPreferencesBinding
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPreferencesFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "UserPreferencesFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val userPreferencesViewModel by viewModel { viewModelProvider.userPreferencesViewModel() }

    private var binding by viewBinding<FragmentUserPreferencesBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = FragmentUserPreferencesBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActivityViews()
        binding.layoutAddTags.setup(
            afterTagInput = userPreferencesViewModel::searchForTag,
            onTagAdded = { _, currentTags -> userPreferencesViewModel.saveDefaultTags(currentTags) },
            onTagRemoved = { tag, currentTags ->
                userPreferencesViewModel.saveDefaultTags(currentTags)
                userPreferencesViewModel.searchForTag(tag, currentTags)
            }
        )

        handleKeyboardVisibility()
        lifecycleScope.launch {
            appStateViewModel.userPreferencesContent.collect {
                setupAppearance(it.appearance)
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
        }
        lifecycleScope.launch {
            userPreferencesViewModel.appearanceChanged.collect { newAppearance ->
                when (newAppearance) {
                    Appearance.DarkTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Appearance.LightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
        lifecycleScope.launch {
            userPreferencesViewModel.suggestedTags.collect {
                binding.layoutAddTags.showSuggestedValuesAsTags(it, showRemoveIcon = false)
            }
        }
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

    private fun handleKeyboardVisibility() {
        binding.root.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

                ViewCompat.setPaddingRelative(
                    view,
                    initialPadding.start,
                    initialPadding.top,
                    initialPadding.end,
                    initialPadding.bottom + imeInsets.bottom,
                )

                with(binding.root) {
                    val lastChild = getChildAt(childCount - 1)
                    val bottom = lastChild.bottom + paddingBottom
                    val delta = height - scrollY - bottom

                    view.smoothScrollY(-delta)
                }
            } else {
                ViewCompat.setPaddingRelative(
                    view,
                    initialPadding.start,
                    initialPadding.top,
                    initialPadding.end,
                    initialPadding.bottom
                )
            }
        }
    }

    private fun setupAppearance(appearance: Appearance) {
        when (appearance) {
            Appearance.DarkTheme -> binding.buttonAppearanceDark.isChecked = true
            Appearance.LightTheme -> binding.buttonAppearanceLight.isChecked = true
            else -> binding.buttonAppearanceSystemDefault.isChecked = true
        }
        binding.buttonAppearanceDark.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.DarkTheme)
        }
        binding.buttonAppearanceLight.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.LightTheme)
        }
        binding.buttonAppearanceSystemDefault.setOnClickListener {
            userPreferencesViewModel.saveAppearance(Appearance.SystemDefault)
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
        binding.buttonBeforeSaving.setOnClickListener {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.BeforeSaving)
            binding.buttonBeforeSaving.isChecked = true
        }
        binding.buttonAfterSaving.setOnClickListener {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.AfterSaving)
            binding.buttonAfterSaving.isChecked = true
        }
        binding.buttonSkipEditing.setOnClickListener {
            userPreferencesViewModel.saveEditAfterSharing(EditAfterSharing.SkipEdit)
            binding.buttonSkipEditing.isChecked = true
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
        isChecked = initialValue
        setOnCheckedChangeListener { _, isChecked -> onCheckedChangeListener(isChecked) }
    }
}
