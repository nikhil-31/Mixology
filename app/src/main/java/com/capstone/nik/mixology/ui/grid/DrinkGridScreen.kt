package com.capstone.nik.mixology.ui.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.SavedDrinkCardAspectRatio
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun DrinkGridRoute(
    filter: DrinkFilter,
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Drink) -> Unit,
    viewModel: DrinkGridViewModel = hiltViewModel(key = filter.name),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(filter) {
        viewModel.onIntent(DrinkGridIntent.Bind(filter))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is DrinkGridEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            is DrinkGridEffect.ShowMessageRes -> snackbarHostState.showSnackbar(context.getString(effect.resId))
            is DrinkGridEffect.OpenDrink -> onDrinkClick(effect.drink)
        }
    }

    DrinkGridScreen(
        filter = filter,
        drinks = state.drinks,
        onDrinkClick = { viewModel.onIntent(DrinkGridIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(DrinkGridIntent.ToggleSaved(it)) },
    )
}

@Composable
fun DrinkGridScreen(
    filter: DrinkFilter,
    drinks: List<Drink>,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    val empty = filter.showEmptySaved && drinks.isEmpty()
    Box(modifier = Modifier.fillMaxSize()) {
        if (empty) {
            Text(
                text = stringResource(R.string.empty_string_add_a_drink),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 50.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(drinks, key = { it.id }) { item ->
                    DrinkCard(
                        item = item,
                        onClick = { onDrinkClick(item) },
                        onToggleSaved = { onToggleSaved(item) },
                        posterAspectRatio = if (filter.showEmptySaved) {
                            SavedDrinkCardAspectRatio
                        } else {
                            1f
                        },
                    )
                }
            }
        }
    }
}
