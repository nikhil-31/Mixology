package com.capstone.nik.mixology.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.ui.components.DrinkImage

@Composable
fun ShoppingRoute(
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShoppingScreen(
        state = state,
        onToggle = { viewModel.onIntent(ShoppingIntent.Toggle(it)) },
        onRemove = { viewModel.onIntent(ShoppingIntent.Remove(it)) },
        onClearChecked = { viewModel.onIntent(ShoppingIntent.ClearChecked) },
    )
}

@Composable
fun ShoppingScreen(
    state: ShoppingUiState,
    onToggle: (ShoppingItemEntity) -> Unit,
    onRemove: (ShoppingItemEntity) -> Unit,
    onClearChecked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.items.any { it.checked }) {
            TextButton(
                onClick = onClearChecked,
                modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp),
            ) {
                Text(stringResource(R.string.shopping_clear_checked))
            }
        }
        if (state.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.shopping_empty),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                state.sections.forEach { section ->
                    item(key = "section-${section.drinkId}") {
                        ShoppingSectionHeader(section)
                    }
                    items(section.items, key = { it.id }) { item ->
                        SwipeShoppingRow(
                            item = item,
                            measure = section.measure(item),
                            onToggle = onToggle,
                            onRemove = onRemove,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingSectionHeader(section: ShoppingSection) {
    val drink = section.drink ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DrinkImage(
            url = drink.thumb,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Text(
            text = drink.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeShoppingRow(
    item: ShoppingItemEntity,
    measure: String,
    onToggle: (ShoppingItemEntity) -> Unit,
    onRemove: (ShoppingItemEntity) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove(item)
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
    ) {
        ShoppingRow(item = item, measure = measure, onToggle = onToggle, onRemove = onRemove)
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItemEntity,
    measure: String,
    onToggle: (ShoppingItemEntity) -> Unit,
    onRemove: (ShoppingItemEntity) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(item) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggle(item) },
        )
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (item.checked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
        )
        if (measure.isNotEmpty()) {
            Text(
                text = measure,
                modifier = Modifier.padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
        IconButton(onClick = { onRemove(item) }) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.shopping_remove_item),
            )
        }
    }
}
