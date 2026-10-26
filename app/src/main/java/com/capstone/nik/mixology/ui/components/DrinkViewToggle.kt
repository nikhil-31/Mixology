package com.capstone.nik.mixology.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkViewToggle(
    listView: Boolean,
    onToggleListView: () -> Unit,
    label: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.orEmpty(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow {
            ViewSegment(
                selected = !listView,
                onClick = { if (listView) onToggleListView() },
                icon = Icons.Outlined.GridView,
                label = stringResource(R.string.saved_view_images),
                index = 0,
                tag = "saved_view_images",
            )
            ViewSegment(
                selected = listView,
                onClick = { if (!listView) onToggleListView() },
                icon = Icons.AutoMirrored.Outlined.ViewList,
                label = stringResource(R.string.saved_view_list),
                index = 1,
                tag = "saved_view_list",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleChoiceSegmentedButtonRowScope.ViewSegment(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    index: Int,
    tag: String,
) {
    SegmentedButton(
        selected = selected,
        onClick = onClick,
        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag(tag),
    ) {
        Text(label)
    }
}
