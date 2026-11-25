package com.capstone.nik.mixology.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.ada.AdaPlushieFilled
import com.capstone.nik.mixology.ui.ada.AdaPlushieOutlined

data class BottomNavItem(
    val titleRes: Int,
    val destination: DrawerDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        titleRes = R.string.nav_item_home,
        // Recipes is the home screen; the old home lives in Settings as Discover.
        destination = DrawerDestination.RecipesHome,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        titleRes = R.string.nav_item_my_bar,
        destination = DrawerDestination.Bar,
        selectedIcon = Icons.Filled.LocalBar,
        unselectedIcon = Icons.Outlined.LocalBar,
    ),
    BottomNavItem(
        titleRes = R.string.nav_bottom_shopping,
        destination = DrawerDestination.Shopping,
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
    ),
    BottomNavItem(
        titleRes = R.string.nav_bottom_ada,
        destination = DrawerDestination.Ada,
        selectedIcon = AdaPlushieFilled,
        unselectedIcon = AdaPlushieOutlined,
    ),
)

fun DrawerDestination.isSelectedBottomNav(item: BottomNavItem): Boolean {
    return when (val tab = item.destination) {
        DrawerDestination.Hot -> this is DrawerDestination.Hot
        DrawerDestination.Bar -> this is DrawerDestination.Bar
        DrawerDestination.Ada -> this is DrawerDestination.Ada
        DrawerDestination.RecipesHome -> this is DrawerDestination.RecipesHome
        DrawerDestination.Shopping -> this is DrawerDestination.Shopping
        DrawerDestination.Catalog,
        DrawerDestination.Settings,
        DrawerDestination.Recipes,
        -> false
        is DrawerDestination.Filter -> this is DrawerDestination.Filter && filter == tab.filter
    }
}

fun DrawerDestination.isBottomNavTab(): Boolean = when (this) {
    DrawerDestination.Catalog,
    DrawerDestination.Recipes,
    DrawerDestination.Settings,
    -> false
    else -> bottomNavItems.any { isSelectedBottomNav(it) }
}

/** Badges above this read "99+", so the badge stays a dot-sized pill. */
private const val BADGE_MAX = 99

@Composable
fun MixologyBottomBar(
    currentDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit,
    shoppingLeft: Int = 0,
    onReselect: (DrawerDestination) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    Column {
        // A hairline so the bar doesn't melt into the page, most of all in dark mode.
        HorizontalDivider(thickness = 0.5.dp, color = colors.outlineVariant.copy(alpha = 0.6f))
        NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination.isSelectedBottomNav(item)
                val label = stringResource(item.titleRes)
                val badge = if (item.destination == DrawerDestination.Shopping) shoppingLeft else 0
                val badgeDescription = pluralStringResource(R.plurals.nav_bottom_badge, badge, badge)
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (selected) {
                            onReselect(item.destination)
                        } else {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDestinationSelected(item.destination)
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (badge > 0) {
                                    Badge(
                                        containerColor = colors.primary,
                                        contentColor = colors.onPrimary,
                                        modifier = Modifier
                                            .testTag("bottom_badge_$label")
                                            .clearAndSetSemantics { contentDescription = badgeDescription },
                                    ) {
                                        Text(if (badge > BADGE_MAX) "$BADGE_MAX+" else badge.toString())
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primary,
                        selectedTextColor = colors.primary,
                        // A tint of the brand colour reads as "you are here" more clearly than grey.
                        indicatorColor = colors.primary.copy(alpha = 0.16f),
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                    ),
                    modifier = Modifier.testTag("bottom_$label"),
                )
            }
        }
    }
}
