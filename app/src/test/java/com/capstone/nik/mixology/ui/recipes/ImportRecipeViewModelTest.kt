package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ImportRecipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val service = FakeRecipeService()
    private lateinit var repository: NetworkRecipeRepository
    private lateinit var viewModel: ImportRecipeViewModel

    @Before
    fun setUp() {
        val sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        val subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions)
        repository = NetworkRecipeRepository(service, subscriptions, sessions)
        viewModel = ImportRecipeViewModel(repository)
    }

    @Test
    fun importSavesTheRecipeAndOpensIt() = runTest {
        assertFalse(viewModel.state.value.canImport)
        viewModel.onIntent(ImportRecipeIntent.TextChanged("  Look! https://vm.tiktok.com/ZM6/  "))

        viewModel.effects.test {
            viewModel.onIntent(ImportRecipeIntent.Import)
            assertEquals(ImportRecipeEffect.Imported("u-import", duplicate = false), awaitItem())
        }

        assertEquals("Look! https://vm.tiktok.com/ZM6/", service.imports.single().text)
        assertEquals(listOf("u-import"), repository.recipes.value.map { it.id })
        assertFalse(viewModel.state.value.importing)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun importingTheSamePostAgainOpensTheSavedRecipe() = runTest {
        viewModel.onIntent(ImportRecipeIntent.TextChanged("https://vm.tiktok.com/ZM6/"))
        viewModel.effects.test {
            viewModel.onIntent(ImportRecipeIntent.Import)
            assertEquals(ImportRecipeEffect.Imported("u-import", duplicate = false), awaitItem())
            viewModel.onIntent(ImportRecipeIntent.Import)
            assertEquals(ImportRecipeEffect.Imported("u-import", duplicate = true), awaitItem())
        }
        assertEquals(1, repository.recipes.value.size)
    }

    @Test
    fun failuresShowWhatWentWrong() = runTest {
        viewModel.onIntent(ImportRecipeIntent.TextChanged("Our summer menu is coming soon."))

        service.failImportWith = 422 to "No drink recipe was found there"
        viewModel.onIntent(ImportRecipeIntent.Import)
        assertEquals(ImportRecipeError.Server("No drink recipe was found there"), viewModel.state.value.error)

        service.failImportWith = 402 to "Out of chat credits until 2026-11-01"
        viewModel.onIntent(ImportRecipeIntent.Import)
        assertEquals(ImportRecipeError.OutOfCredits, viewModel.state.value.error)

        service.failImportWith = 502 to "The page could not be fetched"
        viewModel.onIntent(ImportRecipeIntent.Import)
        assertEquals(ImportRecipeError.Unreachable, viewModel.state.value.error)
        assertFalse(viewModel.state.value.importing)

        // Editing clears the error; nothing was saved.
        viewModel.onIntent(ImportRecipeIntent.TextChanged("2 oz gin"))
        assertNull(viewModel.state.value.error)
        assertTrue(repository.recipes.value.isEmpty())
    }

    @Test
    fun textIsCutToTheServerLimit() {
        viewModel.onIntent(ImportRecipeIntent.TextChanged("x".repeat(IMPORT_TEXT_LIMIT + 10)))
        assertEquals(IMPORT_TEXT_LIMIT, viewModel.state.value.text.length)
    }
}
