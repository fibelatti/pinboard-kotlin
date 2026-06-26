package com.fibelatti.pinboard.features.licenses

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.BackArrow
import com.fibelatti.ui.foundation.copy
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.style.LibraryActionBadges
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryBadges
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OssLicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                OssLicensesScreen(
                    onBackNavClick = ::finish,
                )
            }
        }
    }
}

@Composable
private fun OssLicensesScreen(
    onBackNavClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = WindowInsets.safeDrawing.asPaddingValues(),
) {
    val localResources: Resources = LocalResources.current
    val libs: Libs? by produceLibraries {
        localResources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() }
    }

    LibrariesContainer(
        libraries = libs,
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = paddingValues.calculateTopPadding()),
        contentPadding = paddingValues.copy(top = 0.dp),
        badges = LibraryBadges(license = false),
        actionLabels = LibraryActionBadges(sponsorEnabled = false),
        variant = LibrariesVariant.Refined,
        header = {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxWidth(),
                ) {
                    LongClickIconButton(
                        painter = rememberVectorPainter(AppIcons.BackArrow),
                        description = stringResource(id = R.string.cd_navigate_back),
                        onClick = onBackNavClick,
                        iconTint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}
