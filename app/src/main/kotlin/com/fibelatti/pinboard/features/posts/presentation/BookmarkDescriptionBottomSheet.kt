package com.fibelatti.pinboard.features.posts.presentation

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
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.composable.TextWithBlockquote
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.AppBottomSheet
import com.fibelatti.ui.components.AppSheetState
import com.fibelatti.ui.components.TextWithLinks
import com.fibelatti.ui.components.bottomSheetData
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun BookmarkDescriptionBottomSheet(
    sheetState: AppSheetState,
    appMode: AppMode,
) {
    val post: Post = sheetState.bottomSheetData() ?: return

    AppBottomSheet(
        sheetState = sheetState,
    ) {
        BookmarkDescriptionScreen(
            appMode = appMode,
            title = post.displayTitle,
            url = post.url,
            description = post.displayDescription,
            notes = post.notes,
        )
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
            textSize = MaterialTheme.typography.bodyLarge.fontSize,
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
                textSize = MaterialTheme.typography.bodyLarge.fontSize,
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
