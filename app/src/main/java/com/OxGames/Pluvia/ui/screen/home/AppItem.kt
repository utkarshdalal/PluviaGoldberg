package com.OxGames.Pluvia.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.ui.util.ListItemImage

@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        headlineContent = { Text(text = appInfo.name) },
        leadingContent = {
            ListItemImage { appInfo.iconUrl }
        }
    )
}

@Preview
@Composable
private fun Preview_AppItem() {
    PluviaTheme {
        Surface {
            LazyColumn {
                items(List(5) { fakeAppInfo(it) }) {
                    AppItem(appInfo = it, onClick = {})
                }
            }
        }
    }
}