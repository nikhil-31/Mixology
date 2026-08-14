package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R

@Composable
fun FavoriteButton(
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 30,
) {
    Icon(
        painter = painterResource(
            if (saved) R.drawable.ic_fav_filled else R.drawable.ic_fav_unfilled_black,
        ),
        contentDescription = stringResource(R.string.content_desc_Add_Delete_button),
        modifier = modifier
            .size(size.dp)
            .clickable(onClick = onClick)
            .padding(2.dp),
        tint = if (saved) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
}
