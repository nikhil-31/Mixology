package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.theme.PosterScrimCenter
import com.capstone.nik.mixology.ui.theme.PosterScrimStart

val DrinkCardRailWidth = 140.dp
val DrinkCardRailHeight = 200.dp
const val SavedDrinkCardAspectRatio = 3f / 4f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrinkCard(
    item: Drink,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    posterHeight: Dp? = null,
    posterAspectRatio: Float = 1f,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { menuOpen = true },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (posterHeight != null) Modifier.height(posterHeight)
                    else Modifier.aspectRatio(posterAspectRatio),
                ),
        ) {
            DrinkImage(
                url = item.thumb,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, PosterScrimCenter, PosterScrimStart),
                        ),
                    ),
            )
            Text(
                text = item.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
            )
            FavoriteButton(
                saved = item.saved,
                onClick = onToggleSaved,
                overlay = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            )
        }
    }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            if (item.saved) {
                                R.string.content_desc_unsave_cocktail
                            } else {
                                R.string.content_desc_save_cocktail
                            },
                        ),
                    )
                },
                onClick = {
                    menuOpen = false
                    onToggleSaved()
                },
            )
        }
    }
}
