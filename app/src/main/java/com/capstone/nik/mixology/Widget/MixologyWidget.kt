package com.capstone.nik.mixology.Widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.capstone.nik.mixology.Activities.ActivityMain
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.di.WidgetEntryPoint
import com.capstone.nik.mixology.ui.DrinkIntents
import com.capstone.nik.mixology.ui.putDrinkExtra
import dagger.hilt.android.EntryPointAccessors

class MixologyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        ).drinkRepository()
        val drinks = repository.getSavedSync()
        val thumbs = drinks.associate { drink ->
            drink.id to loadThumb(context, drink.thumb)
        }
        provideContent {
            GlanceTheme {
                WidgetContent(drinks = drinks, thumbs = thumbs)
            }
        }
    }

    private suspend fun loadThumb(context: Context, url: String): Bitmap? {
        if (url.isBlank() || url == "null") return null
        val result = context.imageLoader.execute(
            ImageRequest.Builder(context)
                .data(url)
                .size(THUMB_SIZE)
                .allowHardware(false)
                .build(),
        )
        return (result as? SuccessResult)?.drawable?.toBitmap()
    }

    companion object {
        private const val THUMB_SIZE = 128
    }
}

@Composable
private fun WidgetContent(
    drinks: List<Drink>,
    thumbs: Map<String, Bitmap?>,
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground),
    ) {
        Text(
            text = context.getString(R.string.nav_item_saved_cocktails),
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.primary)
                .padding(12.dp)
                .clickable(actionStartActivity(openAppIntent(context))),
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (drinks.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_empty_saved_cocktails),
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                ),
            )
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(
                    items = drinks,
                    itemId = { it.id.toLongOrNull() ?: it.id.hashCode().toLong() },
                ) { drink ->
                    DrinkRow(drink = drink, thumb = thumbs[drink.id])
                }
            }
        }
    }
}

@Composable
private fun DrinkRow(
    drink: Drink,
    thumb: Bitmap?,
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable(actionStartActivity(openDrinkIntent(context, drink))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = if (thumb != null) {
                ImageProvider(thumb)
            } else {
                ImageProvider(R.drawable.empty_glass)
            },
            contentDescription = drink.name,
            modifier = GlanceModifier.size(48.dp),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = drink.name,
            modifier = GlanceModifier.padding(start = 12.dp),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
            ),
            maxLines = 2,
        )
    }
}

private fun openAppIntent(context: Context): Intent {
    return Intent(context, ActivityMain::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
}

private fun openDrinkIntent(context: Context, drink: Drink): Intent {
    return Intent(context, ActivityMain::class.java)
        .setAction(DrinkIntents.ACTION_OPEN_DRINK)
        .putDrinkExtra(drink)
        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
}
