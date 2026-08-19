package com.capstone.nik.mixology.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R

object DrinkViewPreferences {
    private const val PREFS = "mixology"
    const val PREF_LIST_VIEW = "saved_list_view"

    fun listView(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(PREF_LIST_VIEW, false)

    fun setListView(context: Context, listView: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_LIST_VIEW, listView)
            .apply()
    }
}

@Composable
fun DrinkViewToggle(
    listView: Boolean,
    onToggleListView: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = !listView,
            onClick = { if (listView) onToggleListView() },
            label = { Text(stringResource(R.string.saved_view_images)) },
            modifier = Modifier.testTag("saved_view_images"),
        )
        FilterChip(
            selected = listView,
            onClick = { if (!listView) onToggleListView() },
            label = { Text(stringResource(R.string.saved_view_list)) },
            modifier = Modifier.testTag("saved_view_list"),
        )
    }
}
