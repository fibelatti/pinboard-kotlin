package com.fibelatti.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Locale Preview - DE",
    group = "Localization",
    showBackground = true,
    locale = "de",
)
@Preview(
    name = "Locale Preview - FR",
    group = "Localization",
    showBackground = true,
    locale = "fr",
)
@Preview(
    name = "Locale Preview - ES",
    group = "Localization",
    showBackground = true,
    locale = "es",
)
public annotation class PreviewLocales
