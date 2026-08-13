package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.theme.MixologyRed
import com.capstone.nik.mixology.ui.theme.MixologyText

@Composable
fun DrinkRecipeBody(
    name: String,
    alcoholic: String?,
    instructions: String?,
    ingredients: List<IngredientMeasure>,
    saved: Boolean,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
    extraBottomContent: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontSize = 22.sp,
                color = MixologyText,
            )
            FavoriteButton(saved = saved, onClick = onToggleSaved, size = 40)
        }
        if (!alcoholic.isNullOrBlank()) {
            Text(
                text = alcoholic,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                color = MixologyRed,
                fontSize = 17.sp,
            )
        }
        Text(
            text = stringResource(R.string.detail_screen_instructions),
            modifier = Modifier.padding(start = 20.dp, top = 15.dp, bottom = 10.dp),
            fontSize = 17.sp,
            color = MixologyText,
        )
        Text(
            text = instructions.orEmpty(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            fontSize = 17.sp,
            color = MixologyText,
        )
        Text(
            text = stringResource(R.string.detail_screen_ingredients),
            modifier = Modifier.padding(start = 20.dp, top = 15.dp, bottom = 10.dp),
            fontSize = 17.sp,
            color = MixologyText,
        )
        extraBottomContent()
        ingredients.forEach { item ->
            IngredientRow(item)
        }
    }
}

@Composable
fun DrinkHeroImage(
    url: String?,
    modifier: Modifier = Modifier,
) {
    DrinkImage(
        url = url,
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp),
    )
}
