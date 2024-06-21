package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.android.extension.hideKeyboard
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TagsFragment : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "TagsFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModel()
    private val mainViewModel: MainViewModel by activityViewModel()
    private val tagsViewModel: TagsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            TagListScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                tagsViewModel = tagsViewModel,
                onBackPressed = { navigateBack() },
                onError = ::handleError,
                onTagLongClicked = ::showTagQuickActions,
            )
        }
    }

    override fun onDestroyView() {
        requireView().hideKeyboard()
        super.onDestroyView()
    }

    private fun showTagQuickActions(tag: Tag) {
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = TagQuickActions.allOptions(tag),
            optionName = { getString(it.title) },
            optionIcon = TagQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is TagQuickActions.Rename -> {
                        RenameTagDialog.show(
                            context = requireContext(),
                            tag = option.tag,
                            onRename = tagsViewModel::renameTag,
                        )
                    }
                }
            },
        )
    }
}

private sealed class TagQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val tag: Tag

    data class Rename(
        override val tag: Tag,
    ) : TagQuickActions(
        title = R.string.quick_actions_rename,
        icon = R.drawable.ic_edit,
    )

    companion object {

        fun allOptions(
            tag: Tag,
        ): List<TagQuickActions> = listOf(
            Rename(tag),
        )
    }
}
