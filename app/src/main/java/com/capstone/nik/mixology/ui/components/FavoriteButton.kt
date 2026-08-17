package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.theme.PosterBadge

@Composable
fun FavoriteButton(
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 30,
    overlay: Boolean = false,
) {
    val icon = painterResource(
        if (saved) R.drawable.ic_fav_filled else R.drawable.ic_fav_unfilled_black,
    )
    val contentDescription = stringResource(R.string.content_desc_Add_Delete_button)
    val tint = when {
        saved -> MaterialTheme.colorScheme.primary
        overlay -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    if (overlay) {
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PosterBadge)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = tint,
            )
        }
    } else {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = modifier
                .size(size.dp)
                .clickable(onClick = onClick)
                .padding(2.dp),
            tint = tint,
        )
    }
}
