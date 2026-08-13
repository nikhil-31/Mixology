package com.capstone.nik.mixology.ui.details

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkHeroImage
import com.capstone.nik.mixology.ui.components.DrinkRecipeBody
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.MixologyText

@Composable
fun DrinkDetailsRoute(
    cocktail: Cocktail,
    showUpNavigation: Boolean,
    onBack: () -> Unit,
    viewModel: DrinkDetailsViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    wrapInScaffold: Boolean = true,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareLabel = stringResource(R.string.detail_share_via)

    LaunchedEffect(cocktail.getmDrinkId()) {
        viewModel.onIntent(DrinkDetailsIntent.Load(cocktail))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is DrinkDetailsEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
            is DrinkDetailsEffect.ShareRecipe ->
                context.startActivity(Intent.createChooser(effect.intent, shareLabel))
            DrinkDetailsEffect.NavigateBack -> onBack()
        }
    }

    val content: @Composable () -> Unit = {
        DrinkDetailsContent(
            state = state,
            onToggleSaved = { viewModel.onIntent(DrinkDetailsIntent.ToggleSaved) },
        )
    }

    if (wrapInScaffold) {
        DrinkDetailsScaffold(
            title = cocktail.getmDrinkName().orEmpty(),
            showUpNavigation = showUpNavigation,
            onBack = { viewModel.onIntent(DrinkDetailsIntent.Back) },
            onShare = { viewModel.onIntent(DrinkDetailsIntent.Share) },
            snackbarHostState = snackbarHostState,
            content = content,
        )
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailsScaffold(
    title: String,
    showUpNavigation: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = MixologyText) },
                navigationIcon = {
                    if (showUpNavigation) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_desc_up_navigation),
                                tint = MixologyText,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_detail_share),
                            tint = MixologyText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MixologyText,
                    actionIconContentColor = MixologyText,
                    navigationIconContentColor = MixologyText,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
    ) { padding ->
        Box(Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
fun DrinkDetailsContent(
    state: DrinkDetailsUiState,
    onToggleSaved: () -> Unit,
) {
    val cocktail = state.cocktail
    Box(modifier = Modifier.fillMaxSize()) {
        if (cocktail != null) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DrinkHeroImage(url = state.drink?.strDrinkThumb ?: cocktail.getmDrinkThumb())
                if (state.drink != null) {
                    DrinkRecipeBody(
                        name = state.drink.strDrink.orEmpty(),
                        alcoholic = state.drink.strAlcoholic,
                        instructions = state.drink.strInstructions,
                        ingredients = state.ingredients,
                        saved = state.saved,
                        onToggleSaved = onToggleSaved,
                    )
                }
            }
        }
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
