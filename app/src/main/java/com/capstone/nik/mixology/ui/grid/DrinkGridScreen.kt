package com.capstone.nik.mixology.ui.grid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.FavoriteButton
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.MixologyText

@Composable
fun DrinkGridRoute(
    filter: DrinkFilter,
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Cocktail) -> Unit,
    viewModel: DrinkGridViewModel = viewModel(key = filter.name),
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
            is DrinkGridEffect.OpenDrink -> onDrinkClick(effect.cocktail)
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
    drinks: List<DrinkListItem>,
    onDrinkClick: (Cocktail) -> Unit,
    onToggleSaved: (DrinkListItem) -> Unit,
) {
    val empty = filter.showEmptySaved && drinks.isEmpty()
    Box(modifier = Modifier.fillMaxSize()) {
        if (empty) {
            Text(
                text = stringResource(R.string.empty_string_add_a_drink),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                color = MixologyText,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(3.dp, 3.dp, 3.dp, 50.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(drinks, key = { it.id }) { item ->
                    DrinkCard(
                        item = item,
                        onClick = { onDrinkClick(item.toCocktail()) },
                        onToggleSaved = { onToggleSaved(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrinkCard(
    item: DrinkListItem,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(1.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
    ) {
        Column {
            DrinkImage(
                url = item.thumb,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MixologyText,
                )
                FavoriteButton(saved = item.saved, onClick = onToggleSaved)
            }
        }
    }
}
