package com.capstone.nik.mixology.ui.details

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkHeroImage
import com.capstone.nik.mixology.ui.components.DrinkRecipeBody
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showUpNavigation) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_up_navigation),
                        )
                    }
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.action_detail_share),
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                content()
            }
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
                } else {
                    Text(
                        text = cocktail.getmDrinkName().orEmpty(),
                        modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
