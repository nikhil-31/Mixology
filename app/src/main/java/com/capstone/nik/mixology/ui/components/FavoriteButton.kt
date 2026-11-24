package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.SaveConfetti
import com.capstone.nik.mixology.ui.theme.PosterBadge

@Composable
fun FavoriteButton(
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 30,
    overlay: Boolean = false,
) {
    val view = LocalView.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    val icon = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    val contentDescription = stringResource(
        if (saved) R.string.content_desc_unsave_cocktail else R.string.content_desc_save_cocktail,
    )
    val tint = if (overlay) {
        Color.White
    } else if (saved) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    fun handleClick() {
        if (!saved) {
            SaveConfetti.burstAt(view, origin.x, origin.y)
        }
        onClick()
    }

    val positionModifier = Modifier.onGloballyPositioned { coordinates ->
        val pos = coordinates.positionInWindow()
        origin = Offset(
            pos.x + coordinates.size.width / 2f,
            pos.y + coordinates.size.height / 2f,
        )
    }

    if (overlay) {
        // The badge stays 32dp; the box around it is the 48dp a finger needs.
        Box(
            modifier = modifier
                .then(positionModifier)
                .size(OverlayTouchSize)
                .clip(CircleShape)
                .toggleable(value = saved, role = Role.Checkbox, onValueChange = { handleClick() }),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(OverlayBadgeSize)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PosterBadge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(20.dp),
                    tint = tint,
                )
            }
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = modifier
                .then(positionModifier)
                .size(size.dp)
                .toggleable(value = saved, role = Role.Checkbox, onValueChange = { handleClick() })
                .padding(2.dp),
            tint = tint,
        )
    }
}

private val OverlayTouchSize = 48.dp
private val OverlayBadgeSize = 32.dp

/** How far the overlay badge sits inside its touch area, for callers lining the badge up with an edge. */
val FavoriteOverlayInset = (OverlayTouchSize - OverlayBadgeSize) / 2
