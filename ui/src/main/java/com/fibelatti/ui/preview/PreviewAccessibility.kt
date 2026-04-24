package com.fibelatti.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Font Scale (130%)",
    group = "Accessibility",
    showBackground = true,
    fontScale = 1.3f,
)
public annotation class PreviewAccessibility
