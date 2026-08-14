package com.capstone.nik.mixology.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.nik.mixology.R

@Composable
fun MixologyDrawer(
    selectedRoute: String,
    onDestinationSelected: (DrawerDestination) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    ModalDrawerSheet(
        drawerContainerColor = colors.surface,
        modifier = Modifier.statusBarsPadding(),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(start = 16.dp, bottom = 16.dp),
            ) {
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.empty_profile),
                    contentDescription = stringResource(R.string.content_desc_logo_img),
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = colors.onSurface,
                    fontSize = 14.sp,
                )
            }
            drawerSections.forEach { section ->
                val titleRes = section.titleRes
                if (titleRes != null) {
                    Text(
                        text = stringResource(titleRes),
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurface,
                    )
                }
                section.items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(item.titleRes)) },
                        selected = selectedRoute == item.destination.route,
                        onClick = { onDestinationSelected(item.destination) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = colors.surfaceVariant,
                            unselectedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .testTag("drawer_${stringResource(item.titleRes)}"),
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
