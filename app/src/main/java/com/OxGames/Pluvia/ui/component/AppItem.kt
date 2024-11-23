package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.component.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun AppItem(appInfo: AppInfo, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = appInfo.name) },
        leadingContent = {
            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                model = appInfo.iconUrl,
                error = if (LocalInspectionMode.current) {
                    painterResource(R.drawable.ic_launcher_background)
                } else null, // Should have a fallback icon
                contentDescription = "App icon",
            )
        }
    )
}

@Preview
@Composable
private fun Preview_AppItem() {
    PluviaTheme {
        Surface {
            LazyColumn {
                items(List(5) { fakeAppInfo() }) {
                    AppItem(appInfo = it, onClick = {})
                }
            }
        }
    }
}