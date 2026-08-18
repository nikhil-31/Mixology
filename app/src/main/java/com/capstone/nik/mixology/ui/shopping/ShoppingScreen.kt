package com.capstone.nik.mixology.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.ShoppingItemEntity

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
        if (state.items.isEmpty()) {
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
                items(state.items, key = { it.id }) { item ->
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
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (item.checked) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                        )
                        IconButton(onClick = { onRemove(item) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.shopping_remove_item),
                            )
                        }
                    }
                }
            }
        }
    }
}
