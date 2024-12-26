package com.OxGames.Pluvia.ui.component.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val Icons.Filled.VR: ImageVector
    get() {
        if (vr != null) {
            return vr!!
        }
        vr = ImageVector.Builder(
            name = "VR",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 36.0F,
            viewportHeight = 36.0F,
        ).materialPath {
            moveTo(11.45F, 26.5F)
            horizontalLineTo(7.625F)
            lineTo(1.0F, 9.0F)
            horizontalLineTo(5.025F)
            lineTo(9.625F, 22.325F)
            lineTo(14.1F, 9.0F)
            horizontalLineTo(18.125F)
            lineTo(11.45F, 26.5F)
            close()
        }.materialPath {
            moveTo(34.552F, 26.5F)
            horizontalLineTo(30.477F)
            lineTo(26.952F, 20.6F)
            horizontalLineTo(26.527F)
            horizontalLineTo(23.927F)
            verticalLineTo(26.5F)
            horizontalLineTo(20.252F)
            verticalLineTo(9.0F)
            horizontalLineTo(26.802F)
            curveTo(29.202F, 9.0F, 30.9686F, 9.48333F, 32.102F, 10.45F)
            curveTo(33.2353F, 11.4F, 33.802F, 12.7333F, 33.802F, 14.45F)
            curveTo(33.802F, 15.8F, 33.502F, 16.925F, 32.902F, 17.825F)
            curveTo(32.3186F, 18.725F, 31.4936F, 19.4083F, 30.427F, 19.875F)
            lineTo(34.552F, 26.5F)

            moveTo(23.927F, 12.125F)
            verticalLineTo(17.45F)
            horizontalLineTo(26.802F)
            curveTo(27.7686F, 17.45F, 28.5186F, 17.2083F, 29.052F, 16.725F)
            curveTo(29.602F, 16.225F, 29.877F, 15.5417F, 29.877F, 14.675F)
            curveTo(29.877F, 13.825F, 29.6103F, 13.1917F, 29.077F, 12.775F)
            curveTo(28.5603F, 12.3417F, 27.727F, 12.125F, 26.577F, 12.125F)
            horizontalLineTo(23.927F)
            close()
        }.build()
        return vr!!
    }
private var vr: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconVRPreview() {
    Image(modifier = Modifier.size(64.dp), imageVector = Icons.Filled.VR, contentDescription = null)
}
