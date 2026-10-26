package com.capstone.nik.mixology.ui.details

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRating
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.RatingRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Keeps every user's rating like the backend does, keyed by user "me". */
private class FakeRatings : RatingRepository {
    val others = mutableListOf(5, 4)
    var mine: Int? = null
    var failNext = false
    val writes = mutableListOf<Int?>()

    override suspend fun rating(drinkId: String) = summary()

    override suspend fun rate(drinkId: String, stars: Int?): DrinkRating {
        writes += stars
        if (failNext) {
            failNext = false
            error("offline")
        }
        mine = stars
        return summary()
    }

    private fun summary(): DrinkRating {
        val all = others + listOfNotNull(mine)
        val average = if (all.isEmpty()) null else Math.round(all.average() * 10) / 10.0
        return DrinkRating(average, all.size, mine)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DrinkRatingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private val ratings = FakeRatings()
    private lateinit var viewModel: DrinkDetailsViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            FakeCocktailService(),
            context,
        )
        viewModel = DrinkDetailsViewModel(
            repository,
            NetworkMonitor.forTests(),
            context,
            AnalyticsTracker.forTests(),
            ratings,
        )
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun loadShowsTheCommunityAverage() {
        assertEquals(DrinkRating(4.5, 2, null), viewModel.state.value.rating)
    }

    @Test
    fun ratingUpdatesMyStarsAndTheAverage() {
        viewModel.onIntent(DrinkDetailsIntent.Rate(3))

        assertEquals(DrinkRating(4.0, 3, 3), viewModel.state.value.rating)
        assertEquals(listOf<Int?>(3), ratings.writes)
    }

    @Test
    fun tappingMyCurrentRatingClearsIt() {
        viewModel.onIntent(DrinkDetailsIntent.Rate(5))
        viewModel.onIntent(DrinkDetailsIntent.Rate(5))

        assertNull(viewModel.state.value.rating?.mine)
        assertEquals(listOf(5, null), ratings.writes)
        assertEquals(2, viewModel.state.value.rating?.count)
    }

    @Test
    fun aFailedSaveRestoresThePreviousRatingAndSaysSo() = runTest {
        ratings.failNext = true
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.Rate(2))
            assertEquals(
                DrinkDetailsEffect.ShowMessageRes(R.string.detail_rating_failed),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(DrinkRating(4.5, 2, null), viewModel.state.value.rating)
    }
}
