package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.model.IngredientMeasure

@Composable
fun IngredientRow(
    item: IngredientMeasure,
    modifier: Modifier = Modifier,
    /** Marks an ingredient the user already has in My Bar. */
    inBar: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Owned ingredients get a tinted band so they stand out at a glance.
            .then(if (inBar) Modifier.background(colors.primary.copy(alpha = 0.14f)) else Modifier)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IngredientImage(url = item.imageUrl, size = 40.dp)
        Text(
            text = item.ingredient,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (inBar) FontWeight.SemiBold else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colors.onSurface,
        )
        Text(
            text = item.measure,
            style = MaterialTheme.typography.bodyMedium,
            color = if (inBar) colors.onSurface else colors.onSurfaceVariant,
        )
        if (inBar) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.detail_in_your_bar),
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
