package com.fibelatti.ui.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Phone",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_9,
)
@Preview(
    name = "Phone — Landscape",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
)
@Preview(
    name = "Phone — Small",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    device = Devices.NEXUS_5,
)
@Preview(
    name = "Foldable — Unfolded",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    device = Devices.FOLDABLE,
)
@Preview(
    name = "Tablet — Portrait",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    // Devices.PIXEL_TABLET has no named portrait constant
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
)
@Preview(
    name = "Tablet — Landscape",
    group = "Device",
    showSystemUi = true,
    showBackground = true,
    device = Devices.TABLET,
)
public annotation class PreviewDevices
