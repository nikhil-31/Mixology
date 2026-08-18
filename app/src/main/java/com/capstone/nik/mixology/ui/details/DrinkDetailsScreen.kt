package com.capstone.nik.mixology.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkHeroImage
import com.capstone.nik.mixology.ui.components.DrinkHeroImageHeight
import com.capstone.nik.mixology.ui.components.DrinkHeroViewportHeight
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.DrinkRecipeBody
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.MixologyDetailsTitle
import com.capstone.nik.mixology.ui.theme.PosterBadge

@Composable
fun DrinkDetailsRoute(
    drink: Drink,
    showUpNavigation: Boolean,
    onBack: () -> Unit,
    viewModel: DrinkDetailsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    wrapInScaffold: Boolean = true,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareLabel = stringResource(R.string.detail_share_via)

    LaunchedEffect(drink.id) {
        viewModel.onIntent(DrinkDetailsIntent.Load(drink))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is DrinkDetailsEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
            is DrinkDetailsEffect.ShareRecipe ->
                context.startActivity(Intent.createChooser(effect.intent, shareLabel))
            is DrinkDetailsEffect.OpenUrl ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
            DrinkDetailsEffect.NavigateBack -> onBack()
        }
    }

    val content: @Composable () -> Unit = {
        DrinkDetailsContent(
            state = state,
            onToggleSaved = { viewModel.onIntent(DrinkDetailsIntent.ToggleSaved) },
            onNotesChanged = { viewModel.onIntent(DrinkDetailsIntent.UpdateNotes(it)) },
            onAddToShoppingList = { viewModel.onIntent(DrinkDetailsIntent.AddToShoppingList) },
            onOpenVideo = { viewModel.onIntent(DrinkDetailsIntent.OpenVideo(it)) },
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
    onNotesChanged: (String) -> Unit = {},
    onAddToShoppingList: () -> Unit = {},
    onOpenVideo: (String) -> Unit = {},
) {
    val drink = state.drink
    var fullscreenImage by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (drink != null) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DrinkHeroViewportHeight),
                ) {
                    DrinkHeroImage(
                        url = drink.thumb,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DrinkHeroImageHeight),
                        onClick = { fullscreenImage = true },
                    )
                }
                if (drink.hasRecipe) {
                    DrinkRecipeBody(
                        name = drink.name,
                        alcoholic = drink.alcoholic,
                        glass = drink.glass,
                        category = drink.category,
                        iba = drink.iba,
                        instructions = drink.instructions,
                        ingredients = drink.ingredients,
                        saved = state.saved,
                        onToggleSaved = onToggleSaved,
                        extraBottomContent = {
                            val video = drink.video
                            if (!video.isNullOrBlank()) {
                                TextButton(onClick = { onOpenVideo(video) }) {
                                    Text(stringResource(R.string.detail_watch_video))
                                }
                            }
                            if (state.saved) {
                                TextButton(onClick = onAddToShoppingList) {
                                    Text(stringResource(R.string.shopping_add_ingredients))
                                }
                                OutlinedTextField(
                                    value = drink.notes,
                                    onValueChange = onNotesChanged,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    label = { Text(stringResource(R.string.detail_notes)) },
                                    minLines = 2,
                                )
                            }
                        },
                    )
                } else {
                    Text(
                        text = drink.name,
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
    if (fullscreenImage && drink != null) {
        FullscreenDrinkImage(
            url = drink.thumb,
            onDismiss = { fullscreenImage = false },
        )
    }
}

@Composable
private fun FullscreenDrinkImage(
    url: String?,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            DrinkImage(
                url = url,
                contentScale = ContentScale.Fit,
                contentDescription = stringResource(R.string.content_desc_fullscreen_drink_image),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeroActionButton(
                    onClick = onDismiss,
                    contentDescription = stringResource(R.string.content_desc_up_navigation),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                )
            }
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
