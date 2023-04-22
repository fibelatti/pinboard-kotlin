package com.fibelatti.pinboard.core.android.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.fibelatti.pinboard.R
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun EmptyListContent(
    icon: Painter = painterResource(id = R.drawable.ic_pin),
    title: String = stringResource(id = R.string.posts_empty_title),
    description: String = stringResource(id = R.string.posts_empty_description),
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, end = 24.dp),
    ) {
        val (clBoard, clIcon, clTitle, clDescription) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(clBoard) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .height(200.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp),
                ),
        )

        Image(
            painter = icon,
            contentDescription = "",
            modifier = Modifier.constrainAs(clIcon) {
                linkTo(clBoard.start, clBoard.end, bias = 0.95F)
                linkTo(clBoard.top, clBoard.bottom, bias = 0.9F)
            },
        )

        Text(
            text = title,
            modifier = Modifier.constrainAs(clTitle) {
                start.linkTo(parent.start, margin = 32.dp)
                top.linkTo(clBoard.bottom, margin = 8.dp)
                end.linkTo(parent.end, margin = 32.dp)
            },
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = description,
            modifier = Modifier.constrainAs(clDescription) {
                start.linkTo(parent.start, margin = 32.dp)
                top.linkTo(clTitle.bottom, margin = 8.dp)
                end.linkTo(parent.end, margin = 32.dp)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
@ThemePreviews
private fun EmptyListContentPreview() {
    ExtendedTheme {
        EmptyListContent()
    }
}
