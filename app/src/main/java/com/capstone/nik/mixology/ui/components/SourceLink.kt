package com.capstone.nik.mixology.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R

/** Social sites the server names in a recipe's `source`, by how they're written. */
private val SOURCE_NAMES = mapOf(
    "instagram" to "Instagram",
    "tiktok" to "TikTok",
    "youtube" to "YouTube",
    "x" to "X",
    "twitter" to "X",
    "reddit" to "Reddit",
    "threads" to "Threads",
    "facebook" to "Facebook",
    "pinterest" to "Pinterest",
)

/**
 * What to call where a recipe came from: the social site's name, else the page's
 * host (from [source], or from [sourceUrl] when the server gave no label).
 */
fun sourceLabel(source: String?, sourceUrl: String?): String? {
    val label = source?.trim()?.takeIf { it.isNotEmpty() }
    if (label != null) return SOURCE_NAMES[label.lowercase()] ?: label.removePrefix("www.")
    return sourceUrl?.let { runCatching { Uri.parse(it).host }.getOrNull() }
        ?.removePrefix("www.")
        ?.takeIf { it.isNotEmpty() }
}

/** "Imported from Instagram", opening the post or page in the browser or its app. */
@Composable
fun SourceLink(
    sourceUrl: String,
    source: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val label = sourceLabel(source, sourceUrl) ?: sourceUrl
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)))
                } catch (_: ActivityNotFoundException) {
                    // Nothing on the device opens links; there's nowhere to send it.
                }
            }
            .padding(vertical = 10.dp)
            .testTag("recipe_source_link"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.recipe_imported_from, label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = stringResource(R.string.content_desc_open_source),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}
