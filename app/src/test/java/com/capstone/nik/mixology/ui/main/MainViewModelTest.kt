package com.capstone.nik.mixology.ui.main

import android.app.Application
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.data.DrinkFilter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MainViewModelTest {

    @Test
    fun toggleSearch_opensSearch() = runBlocking {
        val viewModel = MainViewModel()
        val deferred = CompletableDeferred<MainEffect>()
        val job = launch { deferred.complete(viewModel.effects.first()) }
        yield()
        viewModel.onIntent(MainIntent.ToggleSearch)
        val effect = withTimeout(1_000) { deferred.await() }
        job.cancel()
        assertTrue(effect is MainEffect.OpenSearch)
        assertEquals("", (effect as MainEffect.OpenSearch).query)
    }

    @Test
    fun drinkSelected_twoPane_setsSelectedCocktail() {
        val viewModel = MainViewModel()
        val cocktail = Cocktail("11007", "Margarita", "")
        viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane = true))
        assertEquals("11007", viewModel.state.value.selectedCocktail?.getmDrinkId())
    }

    @Test
    fun selectFilterDestination_clearsSelectedCocktail() {
        val viewModel = MainViewModel()
        viewModel.onIntent(
            MainIntent.DrinkSelected(Cocktail("11007", "Margarita", ""), twoPane = true),
        )
        viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Filter(DrinkFilter.GIN)))
        assertNull(viewModel.state.value.selectedCocktail)
        assertEquals("grid/GIN", viewModel.state.value.destination.route)
    }

    @Test
    fun drinkSelected_phone_opensDetails() = runBlocking {
        val viewModel = MainViewModel()
        val deferred = CompletableDeferred<MainEffect>()
        val job = launch { deferred.complete(viewModel.effects.first()) }
        yield()
        viewModel.onIntent(MainIntent.DrinkSelected(Cocktail("11007", "Margarita", ""), twoPane = false))
        val effect = withTimeout(1_000) { deferred.await() }
        job.cancel()
        assertTrue(effect is MainEffect.OpenDetails)
        assertEquals("11007", (effect as MainEffect.OpenDetails).cocktail.getmDrinkId())
    }

    @Test
    fun dismissMenu_hidesDropdown() {
        val viewModel = MainViewModel()
        viewModel.onIntent(MainIntent.OpenMenu)
        assertTrue(viewModel.state.value.menuExpanded)
        viewModel.onIntent(MainIntent.DismissMenu)
        assertFalse(viewModel.state.value.menuExpanded)
    }
}
