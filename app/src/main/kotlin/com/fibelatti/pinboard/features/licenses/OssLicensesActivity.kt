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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.ui.foundation.copy
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.chipColors
import com.mikepenz.aboutlibraries.ui.compose.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
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
    val libs: Libs? by rememberLibraries {
        localResources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() }
    }

    LibrariesContainer(
        libraries = libs,
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = paddingValues.calculateTopPadding()),
        contentPadding = paddingValues.copy(top = 0.dp),
        showLicenseBadges = false,
        colors = LibraryDefaults.libraryColors(
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            versionChipColors = LibraryDefaults.chipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            licenseChipColors = LibraryDefaults.chipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            dialogConfirmButtonColor = MaterialTheme.colorScheme.primary,
        ),
        padding = LibraryDefaults.libraryPadding(
            versionPadding = LibraryDefaults.chipPadding(
                containerPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            ),
            licenseDialogContentPadding = 16.dp,
        ),
        header = {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxWidth(),
                ) {
                    LongClickIconButton(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        description = stringResource(id = R.string.cd_navigate_back),
                        onClick = onBackNavClick,
                        iconTint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}
