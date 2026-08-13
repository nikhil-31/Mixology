package com.capstone.nik.mixology.ui.randomixer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.IngredientRow
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.MixologyRed
import com.capstone.nik.mixology.ui.theme.MixologyText
import kotlinx.coroutines.launch

private val SaveGreen = Color(0xFF4CAF50)

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

    RandomixerScreen(
        state = state,
        onSave = { viewModel.onIntent(RandomixerIntent.SwipeSave) },
        onDiscard = { viewModel.onIntent(RandomixerIntent.SwipeDiscard) },
    )
}

@Composable
fun RandomixerScreen(
    state: RandomixerUiState,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val drink = state.drink
        if (drink != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                SwipeableDrinkCard(
                    drink = drink,
                    ingredients = state.ingredients,
                    enabled = !state.loading,
                    onSwipedRight = onSave,
                    onSwipedLeft = onDiscard,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.randomixer_swipe_hint),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp),
                    fontSize = 13.sp,
                    color = MixologyText.copy(alpha = 0.7f),
                )
            }
        }
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun SwipeableDrinkCard(
    drink: Drink,
    ingredients: List<IngredientMeasure>,
    enabled: Boolean,
    onSwipedRight: () -> Unit,
    onSwipedLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drinkId = drink.idDrink.orEmpty()
    val offsetX = remember(drinkId) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var settled by remember(drinkId) { mutableStateOf(false) }
    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val threshold = screenWidthPx * 0.28f

    fun settle(toRight: Boolean) {
        if (settled) return
        settled = true
        scope.launch {
            val target = if (toRight) screenWidthPx * 1.4f else -screenWidthPx * 1.4f
            offsetX.animateTo(target, tween(220))
            if (toRight) onSwipedRight() else onSwipedLeft()
        }
    }

    val draggableState = rememberDraggableState { delta ->
        if (!enabled || settled) return@rememberDraggableState
        scope.launch { offsetX.snapTo(offsetX.value + delta) }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("randomixer_card")
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = (offsetX.value / 60f).coerceIn(-18f, 18f)
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled && !settled,
                    onDragStopped = { velocity ->
                        if (settled) return@draggable
                        val shouldSave = offsetX.value > threshold || velocity > 1200f
                        val shouldDiscard = offsetX.value < -threshold || velocity < -1200f
                        when {
                            shouldSave -> settle(toRight = true)
                            shouldDiscard -> settle(toRight = false)
                            else -> scope.launch {
                                offsetX.animateTo(0f, tween(200))
                            }
                        }
                    },
                ),
        ) {
            DrinkSwipeCardContent(drink = drink, ingredients = ingredients)
            val progress = (offsetX.value / threshold).coerceIn(-1f, 1f)
            if (progress > 0f) {
                SwipeStamp(
                    text = stringResource(R.string.randomixer_save),
                    color = SaveGreen,
                    alpha = progress,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(20.dp)
                        .graphicsLayer { rotationZ = -18f },
                )
            } else if (progress < 0f) {
                SwipeStamp(
                    text = stringResource(R.string.randomixer_nope),
                    color = MixologyRed,
                    alpha = -progress,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .graphicsLayer { rotationZ = 18f },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { if (enabled) settle(toRight = false) },
                containerColor = Color.White,
                contentColor = MixologyRed,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.content_desc_randomixer_discard),
                    modifier = Modifier.size(32.dp),
                )
            }
            FloatingActionButton(
                onClick = { if (enabled) settle(toRight = true) },
                containerColor = Color.White,
                contentColor = MixologyRed,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.content_desc_randomixer_save),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun DrinkSwipeCardContent(
    drink: Drink,
    ingredients: List<IngredientMeasure>,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DrinkImage(
                url = drink.strDrinkThumb,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
            ) {
                Text(
                    text = drink.strDrink.orEmpty(),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MixologyText,
                )
                if (!drink.strAlcoholic.isNullOrBlank()) {
                    Text(
                        text = drink.strAlcoholic,
                        modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                        color = MixologyRed,
                        fontSize = 17.sp,
                    )
                }
                Text(
                    text = stringResource(R.string.detail_screen_instructions),
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 6.dp),
                    fontSize = 17.sp,
                    color = MixologyText,
                )
                Text(
                    text = drink.strInstructions.orEmpty(),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    fontSize = 16.sp,
                    color = MixologyText,
                )
                if (ingredients.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.detail_screen_ingredients),
                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 6.dp),
                        fontSize = 17.sp,
                        color = MixologyText,
                    )
                    ingredients.forEach { item ->
                        IngredientRow(item)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SwipeStamp(
    text: String,
    color: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .border(3.dp, color.copy(alpha = alpha), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color.copy(alpha = alpha),
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
    )
}
