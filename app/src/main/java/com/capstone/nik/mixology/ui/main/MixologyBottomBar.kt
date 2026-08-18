package com.capstone.nik.mixology.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter

data class BottomNavItem(
    val titleRes: Int,
    val destination: DrawerDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        titleRes = R.string.nav_item_home,
        destination = DrawerDestination.Hot,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        titleRes = R.string.nav_bottom_catalog,
        destination = DrawerDestination.Catalog,
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
    ),
    BottomNavItem(
        titleRes = R.string.nav_item_my_bar,
        destination = DrawerDestination.Bar,
        selectedIcon = Icons.Filled.LocalBar,
        unselectedIcon = Icons.Outlined.LocalBar,
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
        DrawerDestination.Catalog -> this is DrawerDestination.Catalog
        DrawerDestination.Bar -> this is DrawerDestination.Bar
        DrawerDestination.Shopping -> false
        DrawerDestination.Settings -> false
        is DrawerDestination.Filter -> this is DrawerDestination.Filter && filter == tab.filter
    }
}

fun DrawerDestination.isBottomNavTab(): Boolean = bottomNavItems.any { isSelectedBottomNav(it) }

@Composable
fun MixologyBottomBar(
    currentDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface) {
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
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.surfaceVariant,
                    unselectedIconColor = colors.onSurface,
                    unselectedTextColor = colors.onSurface,
                ),
                modifier = Modifier.testTag("bottom_$label"),
            )
        }
    }
}
