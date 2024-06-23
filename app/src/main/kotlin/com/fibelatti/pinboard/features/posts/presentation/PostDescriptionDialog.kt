package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.fibelatti.pinboard.core.android.composable.TextWithBlockquote
import com.fibelatti.ui.components.TextWithLinks
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

object PostDescriptionDialog {

    fun showPostDescriptionDialog(
        context: Context,
        appMode: AppMode,
        post: Post,
    ) {
        ComposeBottomSheetDialog(context) {
            BookmarkDescriptionScreen(
                appMode = appMode,
                title = post.displayTitle,
                url = post.url,
                description = post.displayDescription,
                notes = post.notes,
            )
        }.show()
    }
}

@Composable
private fun BookmarkDescriptionScreen(
    appMode: AppMode,
    title: String,
    url: String,
    description: String,
    notes: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )

        TextWithLinks(
            text = url,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            linkColor = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        TextWithBlockquote(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            textSize = 16.sp,
        )

        if (AppMode.LINKDING == appMode && !notes.isNullOrBlank()) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            TextWithBlockquote(
                text = notes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                textSize = 16.sp,
            )
        }
    }
}

@Composable
@ThemePreviews
private fun BookmarkDescriptionScreenPreview(
    @PreviewParameter(provider = LoremIpsum::class) description: String,
) {
    ExtendedTheme {
        BookmarkDescriptionScreen(
            appMode = AppMode.PINBOARD,
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            description = description,
            notes = description,
        )
    }
}
