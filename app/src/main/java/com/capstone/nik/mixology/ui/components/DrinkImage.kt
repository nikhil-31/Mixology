package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.capstone.nik.mixology.R

@Composable
fun DrinkImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    errorRes: Int = R.drawable.empty_glass,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url?.takeIf { it.isNotBlank() && it != "null" })
            .error(errorRes)
            .fallback(errorRes)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.content_desc_drink_image),
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
fun CircularDrinkImage(
    url: String?,
    size: Dp,
    errorRes: Int = R.drawable.empty_glass,
    contentDescription: String? = stringResource(R.string.content_desc_drink_image),
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url?.takeIf { it.isNotBlank() && it != "null" })
            .error(errorRes)
            .fallback(errorRes)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
    )
}

@Composable
fun IngredientImage(
    url: String,
    size: Dp,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .error(R.drawable.error_ingredient_image)
            .fallback(R.drawable.error_ingredient_image)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.content_desc_ingredients_image),
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(size),
    )
}
