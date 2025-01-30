package com.OxGames.Pluvia.ui.screen.friends

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.materialkolor.ktx.isLight

@Composable
fun ProfileButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val isLight = MaterialTheme.colorScheme.background.isLight()
    Card(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    lineHeight = 14.sp,
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_ProfileButton() {
    PluviaTheme {
        Surface {
            ProfileButton(
                icon = Icons.Default.Home,
                text = "Button Button",
                onClick = { },
            )
        }
    }
}
