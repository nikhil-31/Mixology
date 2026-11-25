package com.capstone.nik.mixology.ui.tour

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.ada.AdaPlushieFilled
import kotlinx.coroutines.launch

/** One page of the welcome tour. [icon] is null for the page that shows the app's logo. */
internal enum class WelcomeTourPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val icon: ImageVector?,
) {
    Welcome(R.string.tour_welcome_title, R.string.tour_welcome_body, null),
    // The tab pages are titled with the tabs' own names, so they can't drift apart.
    Home(R.string.nav_item_home, R.string.tour_home_body, Icons.Filled.Home),
    MyBar(R.string.nav_item_my_bar, R.string.tour_bar_body, Icons.Filled.LocalBar),
    Shopping(R.string.nav_bottom_shopping, R.string.tour_shopping_body, Icons.Filled.ShoppingCart),
    Ada(R.string.nav_bottom_ada, R.string.tour_ada_body, AdaPlushieFilled),
}

/**
 * The first-run tour: a page for the app and one for each tab. [onDone] is called by Skip and by
 * Get started; the caller decides what "done" means (marking the tour as seen).
 */
@Composable
fun WelcomeTourScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = WelcomeTourPage.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val lastPage = pagerState.currentPage == pages.lastIndex
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .testTag("tour_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Keeps its height on the last page, where there is nothing left to skip.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!lastPage) {
                TextButton(onClick = onDone, modifier = Modifier.testTag("tour_skip")) {
                    Text(stringResource(R.string.tour_skip))
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("tour_pager"),
        ) { index ->
            WelcomeTourPageContent(pages[index])
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 12.dp)
                .heightIn(min = 8.dp)
                .clearAndSetSemantics {},
        ) {
            repeat(pages.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            },
                        ),
                )
            }
        }
        Button(
            onClick = {
                if (lastPage) {
                    onDone()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .testTag(if (lastPage) "tour_done" else "tour_next"),
        ) {
            Text(stringResource(if (lastPage) R.string.tour_done else R.string.tour_next))
        }
    }
}

@Composable
private fun WelcomeTourPageContent(page: WelcomeTourPage) {
    // Centered when it fits; scrolls on a short screen or at a large font size.
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            if (page.icon == null) {
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(30.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { heading() }
                    .testTag("tour_title"),
            )
            Text(
                text = stringResource(page.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
