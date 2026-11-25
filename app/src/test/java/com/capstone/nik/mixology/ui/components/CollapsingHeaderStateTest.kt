package com.capstone.nik.mixology.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Test

class CollapsingHeaderStateTest {

    private fun header(height: Float = 100f) = CollapsingHeaderState().apply { heightPx = height }

    private fun CollapsingHeaderState.pre(dy: Float) =
        connection.onPreScroll(Offset(0f, dy), NestedScrollSource.UserInput).y

    private fun CollapsingHeaderState.post(dy: Float) =
        connection.onPostScroll(Offset.Zero, Offset(0f, dy), NestedScrollSource.UserInput).y

    @Test
    fun scrollingDownTheListHidesTheHeaderFirst() {
        val header = header()

        assertEquals(-30f, header.pre(-30f))
        assertEquals(-30f, header.offsetPx)
    }

    @Test
    fun itHidesNoFurtherThanItsHeight() {
        val header = header()

        assertEquals(-100f, header.pre(-250f))
        assertEquals(-100f, header.offsetPx)
        assertEquals(0f, header.pre(-10f))
    }

    @Test
    fun scrollingBackUpMidListLeavesItHidden() {
        val header = header()
        header.pre(-100f)

        assertEquals(0f, header.pre(40f))
        assertEquals(-100f, header.offsetPx)
    }

    @Test
    fun itComesBackWithWhatTheListCouldNotUseAtTheTop() {
        val header = header()
        header.pre(-100f)

        assertEquals(60f, header.post(60f))
        assertEquals(-40f, header.offsetPx)
        // Only what it needs; the rest is left for pull-to-refresh.
        assertEquals(40f, header.post(90f))
        assertEquals(0f, header.offsetPx)
    }

    @Test
    fun aDisabledHeaderStaysPut() {
        val header = header().apply { enabled = false }

        assertEquals(0f, header.pre(-50f))
        assertEquals(0f, header.post(50f))
        assertEquals(0f, header.offsetPx)
    }

    @Test
    fun expandShowsItAgain() {
        val header = header()
        header.pre(-70f)

        header.expand()

        assertEquals(0f, header.offsetPx)
    }

    @Test
    fun beforeItIsMeasuredNothingIsConsumed() {
        val header = header(height = 0f)

        assertEquals(0f, header.pre(-50f))
    }
}
