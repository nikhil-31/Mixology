package com.capstone.nik.mixology.ui.shopping.history

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.ShoppingTrip
import com.capstone.nik.mixology.data.ShoppingTripItem
import com.capstone.nik.mixology.repository.ShoppingTripRepository
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class ShoppingHistoryTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun at(daysAgo: Int, hour: Int, now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -daysAgo)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
    }.timeInMillis

    private val lime = ShoppingTripItem("Lime juice", "11007", "Margarita", "1 oz", addedToBar = true)
    private val tequila = ShoppingTripItem("Tequila", "11007", "Margarita", "2 oz")

    @Test
    fun groupTripsByDay_labelsTodayYesterdayAndOlderDates() {
        val now = at(0, 12, System.currentTimeMillis())
        val trips = listOf(
            ShoppingTrip(1, 1, at(0, 9, now), listOf(lime)),
            ShoppingTrip(2, 2, at(0, 8, now), listOf(lime)),
            ShoppingTrip(3, 3, at(1, 18, now), listOf(lime)),
            ShoppingTrip(4, 4, at(5, 10, now), listOf(lime)),
        )
        val groups = groupTripsByDay(trips, now)
        assertEquals(TripDay.Today, groups[0].day)
        assertEquals(listOf(1L, 2L), groups[0].trips.map { it.id })
        assertEquals(TripDay.Yesterday, groups[1].day)
        assertTrue(groups[2].day is TripDay.On)
        assertEquals(3, groups.size)
    }

    @Test
    fun history_showsSummary_opensTrip_andEmptyState() {
        val now = System.currentTimeMillis()
        var opened = -1L
        val state = ShoppingHistoryUiState(
            trips = listOf(ShoppingTrip(7, 1, now, listOf(lime, tequila))),
            loaded = true,
        )
        composeRule.setContent {
            MixologyTheme {
                ShoppingHistoryScreen(
                    state = state,
                    onBack = {},
                    onOpenTrip = { opened = it },
                    onDelete = {},
                    onRefresh = {},
                    nowMillis = now,
                )
            }
        }
        composeRule.onNodeWithText("Today").assertIsDisplayed()
        composeRule.onNodeWithText("2 items · 1 added to My Bar").assertIsDisplayed()
        composeRule.onNodeWithTag("trip_7").performClick()
        assertEquals(7L, opened)
    }

    @Test
    fun history_empty() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingHistoryScreen(
                    state = ShoppingHistoryUiState(loaded = true),
                    onBack = {},
                    onOpenTrip = {},
                    onDelete = {},
                    onRefresh = {},
                )
            }
        }
        composeRule.onNodeWithText("No shopping trips yet").assertIsDisplayed()
    }

    @Test
    fun tripDetail_listsAddedThenBought_withDrinkAndMeasure() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingTripScreen(
                    trip = ShoppingTrip(7, 1, System.currentTimeMillis(), listOf(tequila, lime)),
                    loaded = true,
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithText("Added to My Bar").assertIsDisplayed()
        composeRule.onNodeWithText("Bought").assertIsDisplayed()
        composeRule.onNodeWithText("Lime juice").assertIsDisplayed()
        composeRule.onNodeWithText("2 oz").assertIsDisplayed()
    }

    @Test
    fun delete_removesTheTripStraightAway() = runTest {
        val repo = ShoppingTripRepository.inMemory()
        val trip = repo.record(listOf(lime))
        val vm = ShoppingHistoryViewModel(repo)
        vm.state.test {
            var state = awaitItem()
            while (state.trips.isEmpty()) state = awaitItem()
            vm.onIntent(ShoppingHistoryIntent.Delete(trip.id))
            while (state.trips.isNotEmpty()) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(repo.trips.first().isEmpty())
    }
}
