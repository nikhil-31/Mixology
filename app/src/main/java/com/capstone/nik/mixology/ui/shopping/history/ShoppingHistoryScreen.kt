package com.capstone.nik.mixology.ui.shopping.history

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.ShoppingTrip
import com.capstone.nik.mixology.data.ShoppingTripItem
import java.util.Date

@Composable
fun ShoppingHistoryRoute(
    onBack: () -> Unit,
    onOpenTrip: (Long) -> Unit,
    viewModel: ShoppingHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShoppingHistoryScreen(
        state = state,
        onBack = onBack,
        onOpenTrip = onOpenTrip,
        onDelete = { viewModel.onIntent(ShoppingHistoryIntent.Delete(it)) },
        onRefresh = { viewModel.onIntent(ShoppingHistoryIntent.Refresh) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingHistoryScreen(
    state: ShoppingHistoryUiState,
    onBack: () -> Unit,
    onOpenTrip: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRefresh: () -> Unit,
    nowMillis: Long = System.currentTimeMillis(),
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTitle(stringResource(R.string.shopping_history), onBack, "shopping_history_back") },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            val groups = groupTripsByDay(state.trips, nowMillis)
            if (state.loaded && groups.isEmpty()) {
                HistoryEmpty()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("shopping_history_list"),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    groups.forEach { group ->
                        item(key = "day-${group.trips.first().id}") { DayHeader(group.day) }
                        items(group.trips, key = { it.id }) { trip ->
                            SwipeToDelete(onDelete = { onDelete(trip.id) }) {
                                TripRow(trip = trip, onClick = { onOpenTrip(trip.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingTripRoute(
    id: Long,
    onBack: () -> Unit,
    viewModel: ShoppingHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShoppingTripScreen(trip = state.trip(id), loaded = state.loaded, onBack = onBack)
}

@Composable
fun ShoppingTripScreen(trip: ShoppingTrip?, loaded: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val title = trip?.let {
        val date = Date(it.at)
        "${DateFormat.getMediumDateFormat(context).format(date)}, ${DateFormat.getTimeFormat(context).format(date)}"
    }.orEmpty()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTitle(title, onBack, "shopping_trip_back") },
    ) { padding ->
        if (trip == null) {
            if (loaded) {
                Text(
                    text = stringResource(R.string.history_trip_missing),
                    modifier = Modifier.padding(padding).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }
        val (added, bought) = trip.items.partition { it.addedToBar }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("shopping_trip"),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "summary") {
                Text(
                    text = tripSummary(trip),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (added.isNotEmpty()) {
                item(key = "added") { SectionTitle(stringResource(R.string.history_added_to_bar)) }
                items(added.sortedBy { it.name.lowercase() }, key = { "a-${it.drinkId}-${it.name}" }) { TripItemRow(it) }
            }
            if (bought.isNotEmpty()) {
                item(key = "bought") { SectionTitle(stringResource(R.string.history_bought)) }
                items(bought.sortedBy { it.name.lowercase() }, key = { "b-${it.drinkId}-${it.name}" }) { TripItemRow(it) }
            }
        }
    }
}

@Composable
private fun tripSummary(trip: ShoppingTrip): String {
    val items = pluralStringResource(R.plurals.history_trip_items, trip.items.size, trip.items.size)
    return if (trip.addedCount > 0) {
        "$items · " + pluralStringResource(R.plurals.history_trip_added, trip.addedCount, trip.addedCount)
    } else {
        items
    }
}

@Composable
private fun BackTitle(title: String, onBack: () -> Unit, tag: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.testTag(tag)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_up_navigation),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DayHeader(day: TripDay) {
    val context = LocalContext.current
    val text = when (day) {
        TripDay.Today -> stringResource(R.string.history_today)
        TripDay.Yesterday -> stringResource(R.string.history_yesterday)
        is TripDay.On -> DateFormat.getLongDateFormat(context).format(Date(day.startMillis))
    }
    SectionTitle(text)
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun TripRow(trip: ShoppingTrip, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("trip_${trip.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = DateFormat.getTimeFormat(context).format(Date(trip.at)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = tripSummary(trip),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trip.items.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}

@Composable
private fun TripItemRow(item: ShoppingTripItem) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (item.addedToBar) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primary)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            item.drinkName?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        item.measure?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) {
        val label = stringResource(R.string.history_delete)
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .semantics { customActions = listOf(CustomAccessibilityAction(label) { onDelete(); true }) },
        ) { content() }
    }
}

@Composable
private fun HistoryEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
            .testTag("shopping_history_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.padding(top = 96.dp))
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.history_empty_title),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.history_empty),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
