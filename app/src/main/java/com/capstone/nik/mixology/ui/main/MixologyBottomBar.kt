package com.capstone.nik.mixology.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.theme.MixologyGray
import com.capstone.nik.mixology.ui.theme.MixologyRed
import com.capstone.nik.mixology.ui.theme.MixologyText

data class BottomNavItem(
    val titleRes: Int,
    val destination: DrawerDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        titleRes = R.string.nav_bottom_home,
        destination = DrawerDestination.Filter(DrinkFilter.ALCOHOLIC),
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        titleRes = R.string.nav_item_hot,
        destination = DrawerDestination.Hot,
        selectedIcon = Icons.Filled.Whatshot,
        unselectedIcon = Icons.Outlined.Whatshot,
    ),
    BottomNavItem(
        titleRes = R.string.nav_bottom_saved,
        destination = DrawerDestination.Filter(DrinkFilter.SAVED),
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
    ),
    BottomNavItem(
        titleRes = R.string.nav_item_randomixer,
        destination = DrawerDestination.Randomixer,
        selectedIcon = Icons.Filled.Shuffle,
        unselectedIcon = Icons.Outlined.Shuffle,
    ),
)

fun DrawerDestination.isSelectedBottomNav(item: BottomNavItem): Boolean {
    return when (val tab = item.destination) {
        DrawerDestination.Randomixer -> this is DrawerDestination.Randomixer
        DrawerDestination.Hot -> this is DrawerDestination.Hot
        is DrawerDestination.Filter -> this is DrawerDestination.Filter && filter == tab.filter
    }
}

@Composable
fun MixologyBottomBar(
    currentDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit,
) {
    NavigationBar(containerColor = Color.White) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination.isSelectedBottomNav(item)
            val label = stringResource(item.titleRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(item.destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MixologyRed,
                    selectedTextColor = MixologyRed,
                    indicatorColor = MixologyGray,
                    unselectedIconColor = MixologyText,
                    unselectedTextColor = MixologyText,
                ),
                modifier = Modifier.testTag("bottom_$label"),
            )
        }
    }
}
