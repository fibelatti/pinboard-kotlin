package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.TextWithLinks
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

object PostDescriptionDialog {

    fun showPostDescriptionDialog(
        context: Context,
        post: Post,
    ) {
        ComposeBottomSheetDialog(context) {
            BookmarkDescriptionScreen(
                title = post.title,
                url = post.url,
                description = post.description,
            )
        }.show()
    }
}

@Composable
private fun BookmarkDescriptionScreen(
    title: String,
    url: String,
    description: String,
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

        Divider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        TextWithLinks(
            text = description,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            linkColor = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
@ThemePreviews
private fun BookmarkDescriptionScreenPreview(
    @PreviewParameter(provider = LoremIpsum::class) description: String,
) {
    ExtendedTheme {
        BookmarkDescriptionScreen(
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            description = description,
        )
    }
}
