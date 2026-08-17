package com.capstone.nik.mixology.ui.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkHeroImage
import com.capstone.nik.mixology.ui.components.DrinkRecipeBody
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.MixologyDetailsTitle
import com.capstone.nik.mixology.ui.theme.PosterBadge

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
            showUpNavigation = showUpNavigation,
            edgeToEdge = false,
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
    showUpNavigation: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
    edgeToEdge: Boolean = showUpNavigation,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier.then(
                    if (edgeToEdge) Modifier.navigationBarsPadding() else Modifier,
                ),
            ) {
                content()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (edgeToEdge) Modifier.statusBarsPadding() else Modifier)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showUpNavigation) {
                    HeroActionButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.content_desc_up_navigation),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    )
                }
                Box(Modifier.weight(1f))
                HeroActionButton(
                    onClick = onShare,
                    contentDescription = stringResource(R.string.action_detail_share),
                    imageVector = Icons.Default.Share,
                )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(356.dp),
                ) {
                    DrinkHeroImage(
                        url = state.drink?.strDrinkThumb ?: cocktail.getmDrinkThumb(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                    )
                }
                if (state.drink != null) {
                    DrinkRecipeBody(
                        name = state.drink.strDrink.orEmpty(),
                        alcoholic = state.drink.strAlcoholic,
                        glass = state.drink.strGlass,
                        category = state.drink.strCategory,
                        iba = state.drink.strIBA,
                        instructions = state.drink.strInstructions,
                        ingredients = state.ingredients,
                        saved = state.saved,
                        onToggleSaved = onToggleSaved,
                    )
                } else {
                    Text(
                        text = cocktail.getmDrinkName().orEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
                        style = MixologyDetailsTitle,
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

@Composable
private fun HeroActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    imageVector: ImageVector,
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PosterBadge),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = Color.White,
            )
        }
    }
}
