package com.OxGames.Pluvia.ui.screen.library.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.ui.util.ListItemImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppItem(
    modifier: Modifier = Modifier,
    appInfo: LibraryItem,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        headlineContent = { Text(text = appInfo.name) },
        supportingContent = {
            Column {
                val tags = mutableListOf<String>()
                if (appInfo.isShared) tags.add("Family Shared")
                if (tags.isNotEmpty()) {
                    Text(
                        text = tags.joinToString(" • "), 
                        fontStyle = FontStyle.Italic, 
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Determine badge text and color based on status and timestamp
                val badgeText: String?
                val badgeColor: Color?

                when (appInfo.isDrmFree) {
                    true -> {
                        badgeText = "DRM-Free"
                        badgeColor = Color(0xFF4CAF50) // Green
                    }
                    false -> {
                        badgeText = "DRM Protected"
                        badgeColor = Color(0xFFF44336) // Red
                    }
                    null -> {
                        if (appInfo.drmCheckTimestamp == null) {
                            badgeText = "Checking DRM..."
                            badgeColor = Color.Gray
                        } else {
                            badgeText = "Unknown DRM"
                            badgeColor = Color.Gray
                        }
                    }
                }

                // Display badge if text is determined
                if (badgeText != null && badgeColor != null) {
                    Badge(containerColor = badgeColor) {
                         Text(badgeText, modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        },
        leadingContent = {
            ListItemImage { appInfo.clientIconUrl }
        },
    )
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_AppItem() {
    PluviaTheme {
        Surface {
            LazyColumn {
                items(
                    items = List(5) { idx ->
                        val item = fakeAppInfo(idx)
                        LibraryItem(
                            index = idx,
                            appId = item.id,
                            name = item.name,
                            iconHash = item.iconHash,
                            isShared = idx % 2 == 0,
                        )
                    },
                    itemContent = {
                        AppItem(appInfo = it, onClick = {})
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    },
                )
            }
        }
    }
}
