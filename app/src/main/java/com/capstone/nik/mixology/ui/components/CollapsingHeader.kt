package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.flow.drop
import kotlin.math.roundToInt

/**
 * The app's toolbar as part of the page: scrolling the content down moves it off
 * screen, and it comes back only once the content is back at the top.
 */
class CollapsingHeaderState {
    /** The header's full height, measured by [collapsingHeader]. */
    var heightPx by mutableFloatStateOf(0f)
        internal set

    /** How far the header has scrolled off, from `-heightPx` (gone) to 0 (shown). */
    var offsetPx by mutableFloatStateOf(0f)
        private set

    /** Off for screens whose toolbar stays put, such as Ada's chat. */
    var enabled by mutableStateOf(true)

    fun expand() {
        offsetPx = 0f
    }

    /** Goes up by one each time the current tab is tapped again; see [ScrollToTopOnReselect]. */
    var scrollToTopRequests by mutableIntStateOf(0)
        private set

    /** The current tab was tapped again: bring the toolbar back and send its list to the top. */
    fun requestScrollToTop() {
        expand()
        scrollToTopRequests++
    }

    internal fun clampToHeight() {
        offsetPx = offsetPx.coerceIn(-heightPx, 0f)
    }

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        // Moving the content up hides the header before the list itself scrolls.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (!enabled || available.y >= 0f) return Offset.Zero
            return Offset(0f, consume(available.y))
        }

        // Pulling down shows it again only with what the list couldn't use, so at the top.
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (!enabled || available.y <= 0f) return Offset.Zero
            return Offset(0f, consume(available.y))
        }
    }

    private fun consume(delta: Float): Float {
        val before = offsetPx
        offsetPx = (before + delta).coerceIn(-heightPx, 0f)
        return offsetPx - before
    }
}

/** The shell's [CollapsingHeaderState], for lists that sit inside their own pull-to-refresh. */
val LocalCollapsingHeader = staticCompositionLocalOf<CollapsingHeaderState?> { null }

/**
 * For a list inside a pull-to-refresh: the refresh would otherwise take the pull at the
 * top before the toolbar could come back. With this the toolbar returns first, and only a
 * further pull refreshes.
 */
fun Modifier.collapsingHeaderScroll(state: CollapsingHeaderState?): Modifier =
    if (state == null) this else nestedScroll(state.connection)

/** Lays the header out shorter by however far it has scrolled off, so the content below moves up with it. */
fun Modifier.collapsingHeader(state: CollapsingHeaderState): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0))
        state.heightPx = placeable.height.toFloat()
        state.clampToHeight()
        val offset = state.offsetPx.roundToInt()
        layout(placeable.width, (placeable.height + offset).coerceAtLeast(0)) {
            placeable.place(0, offset)
        }
    }

/**
 * Scrolls [listState] to the top when the current tab is tapped again. Only taps made while
 * this screen is showing count, so coming back to a tab keeps its scroll position.
 */
@Composable
fun ScrollToTopOnReselect(listState: LazyListState) {
    val header = LocalCollapsingHeader.current ?: return
    LaunchedEffect(header, listState) {
        snapshotFlow { header.scrollToTopRequests }
            .drop(1)
            .collect { listState.animateScrollToItem(0) }
    }
}
