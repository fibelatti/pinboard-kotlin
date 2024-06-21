@file:Suppress("PrivatePropertyName", "UNUSED")

package com.fibelatti.pinboard.features.filters.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import java.util.Random

private val LOREM_IPSUM_SOURCE: String = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales
laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu
elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat
risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante
nunc egestas quam, ultricies adipiscing velit enim at nunc. Aenean id diam
neque. Praesent ut lacus sed justo viverra fermentum et ut sem. Fusce
convallis gravida lacinia. Integer semper dolor ut elit sagittis lacinia.
Praesent sodales scelerisque eros at rhoncus. Duis posuere sapien vel ipsum
ornare interdum at eu quam. Vestibulum vel massa erat. Aenean quis sagittis
purus. Phasellus arcu purus, rutrum id consectetur non, bibendum at nibh.
""".trim()

class SavedFilterProvider(val size: Int) : PreviewParameterProvider<List<SavedFilter>> {

    constructor() : this(10)

    override val values: Sequence<List<SavedFilter>>
        get() = sequenceOf(List(size) { generateSavedFilter() })

    private val random = Random()

    private fun generateSavedFilter(): SavedFilter {
        val range = 1..3
        val value = range.random()
        return SavedFilter(
            searchTerm = LOREM_IPSUM_SOURCE.split(" ").take(value).joinToString(separator = " "),
            tags = LOREM_IPSUM_SOURCE.split(" ").take(value).map { Tag(name = it) },
        )
    }
}
