package com.capstone.nik.mixology.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.theme.PosterBadge

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

/** The server's name for a social link's site, such as `instagram`, or null for any other page. */
internal fun siteKey(url: String): String? {
    val host = runCatching { Uri.parse(url).host }.getOrNull()?.lowercase() ?: return null
    return listOf(
        "instagram" to listOf("instagram.com"),
        "tiktok" to listOf("tiktok.com"),
        "youtube" to listOf("youtube.com", "youtu.be"),
        "pinterest" to listOf("pinterest.", "pin.it"),
        "x" to listOf("x.com", "twitter.com"),
        "reddit" to listOf("reddit.com", "redd.it"),
        "threads" to listOf("threads.net", "threads.com"),
        "facebook" to listOf("facebook.com", "fb.watch"),
    ).firstOrNull { (_, hosts) -> hosts.any { host == it || host.endsWith(".$it") || host.startsWith(it) } }
        ?.first
}

/** The social site a recipe came from, from the server's [source] or else its [sourceUrl]. */
fun sourceKey(source: String?, sourceUrl: String?): String? {
    val label = source?.trim()?.lowercase()
    if (label != null && label in SOURCE_NAMES) return label
    return sourceUrl?.let(::siteKey)
}

/** The site's logo, or null when it's not a social site we know. */
@DrawableRes
fun sourceIconRes(key: String?): Int? = when (key) {
    "instagram" -> R.drawable.ic_source_instagram
    "tiktok" -> R.drawable.ic_source_tiktok
    "youtube" -> R.drawable.ic_source_youtube
    "x", "twitter" -> R.drawable.ic_source_x
    "reddit" -> R.drawable.ic_source_reddit
    "threads" -> R.drawable.ic_source_threads
    "facebook" -> R.drawable.ic_source_facebook
    "pinterest" -> R.drawable.ic_source_pinterest
    else -> null
}

/** The site's logo, or a globe for any other page. */
@Composable
private fun sourcePainter(source: String?, sourceUrl: String?): Painter =
    sourceIconRes(sourceKey(source, sourceUrl))?.let { painterResource(it) }
        ?: rememberVectorPainter(Icons.Outlined.Language)

/**
 * Where an imported recipe came from, as the site's logo. Nothing for a recipe
 * the user wrote. [overlay] sits it on a photo, like the card's save button.
 */
@Composable
fun SourceBadge(
    source: String?,
    sourceUrl: String?,
    modifier: Modifier = Modifier,
    overlay: Boolean = false,
) {
    val label = sourceLabel(source, sourceUrl) ?: return
    val description = stringResource(R.string.recipe_imported_from, label)
    val painter = sourcePainter(source, sourceUrl)
    val tagged = modifier
        .semantics { contentDescription = description }
        .testTag("recipe_source_badge")
    if (overlay) {
        Box(
            modifier = tagged
                .size(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PosterBadge),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    } else {
        Icon(
            painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = tagged.size(20.dp),
        )
    }
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
            painter = sourcePainter(source, sourceUrl),
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
