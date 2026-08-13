package com.capstone.nik.mixology.ui.randomixer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.ui.components.DrinkHeroImage
import com.capstone.nik.mixology.ui.components.DrinkRecipeBody
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomixerRoute(
    snackbarHostState: SnackbarHostState,
    viewModel: RandomixerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is RandomixerEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
        }
    }

    PullToRefreshBox(
        isRefreshing = state.loading && state.drink != null,
        onRefresh = { viewModel.onIntent(RandomixerIntent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val drink = state.drink
            if (drink != null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DrinkHeroImage(url = drink.strDrinkThumb)
                    DrinkRecipeBody(
                        name = drink.strDrink.orEmpty(),
                        alcoholic = drink.strAlcoholic,
                        instructions = drink.strInstructions,
                        ingredients = state.ingredients,
                        saved = state.saved,
                        onToggleSaved = { viewModel.onIntent(RandomixerIntent.ToggleSaved) },
                    )
                }
            }
            if (state.loading && drink == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
