package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_RECIPE_CREATED
import com.capstone.nik.mixology.analytics.EVENT_RECIPE_DELETED
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
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
import java.io.IOException

private class FakePhotoLoader : RecipePhotoLoader {
    var bytes = byteArrayOf(9, 8, 7)
    var fail = false
    val loaded = mutableListOf<Uri>()

    override suspend fun load(uri: Uri): ByteArray {
        loaded += uri
        if (fail) throw IOException("unreadable")
        return bytes
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RecipeEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val service = FakeRecipeService()
    private val photos = FakePhotoLoader()
    private val analytics = AnalyticsTracker.forTests()
    private lateinit var repository: NetworkRecipeRepository
    private lateinit var viewModel: RecipeEditorViewModel
    private val photo: Uri = Uri.parse("content://media/picker/1")

    @Before
    fun setUp() {
        val sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        val subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions)
        repository = NetworkRecipeRepository(service, subscriptions, sessions)
        viewModel = RecipeEditorViewModel(repository, photos, analytics)
    }

    @Test
    fun validationShowsInlineErrorsAndSendsNothing() {
        viewModel.onIntent(RecipeEditorIntent.Bind(null))
        viewModel.onIntent(RecipeEditorIntent.GlassChanged("x".repeat(RecipeLimits.GLASS + 1)))
        val row = viewModel.state.value.form.ingredients.single().key
        viewModel.onIntent(RecipeEditorIntent.MeasureChanged(row, "2 oz"))

        viewModel.onIntent(RecipeEditorIntent.Save)

        val errors = viewModel.state.value.errors
        assertEquals(FieldError.Required, errors.name)
        assertEquals(FieldError.TooLong(RecipeLimits.GLASS), errors.glass)
        assertEquals(FieldError.Required, errors.ingredients[row])
        assertTrue(service.calls.isEmpty())

        // Errors follow the fields once shown.
        viewModel.onIntent(RecipeEditorIntent.NameChanged("Sour"))
        assertNull(viewModel.state.value.errors.name)
    }

    @Test
    fun validateRecipeChecksEveryLimit() {
        val form = RecipeForm(
            name = "n".repeat(RecipeLimits.NAME + 1),
            category = "c".repeat(RecipeLimits.CATEGORY + 1),
            instructions = "i".repeat(RecipeLimits.INSTRUCTIONS + 1),
            ingredients = listOf(
                IngredientField(0, "g".repeat(RecipeLimits.INGREDIENT + 1), "m".repeat(RecipeLimits.MEASURE + 1)),
                IngredientField(1),
            ),
        )
        val errors = validateRecipe(form)
        assertEquals(FieldError.TooLong(RecipeLimits.NAME), errors.name)
        assertEquals(FieldError.TooLong(RecipeLimits.CATEGORY), errors.category)
        assertEquals(FieldError.TooLong(RecipeLimits.INSTRUCTIONS), errors.instructions)
        assertEquals(mapOf(0L to FieldError.TooLong(RecipeLimits.INGREDIENT)), errors.ingredients)
        assertEquals(mapOf(0L to FieldError.TooLong(RecipeLimits.MEASURE)), errors.measures)
        assertTrue(validateRecipe(RecipeForm(name = "Fine")).isEmpty)
    }

    @Test
    fun saveCreatesTheRecipeThenUploadsThePhoto() = runTest {
        viewModel.onIntent(RecipeEditorIntent.Bind(null))
        fillSour()
        viewModel.onIntent(RecipeEditorIntent.PhotoPicked(photo))

        viewModel.effects.test {
            viewModel.onIntent(RecipeEditorIntent.Save)
            assertEquals(RecipeEditorEffect.Close, awaitItem())
        }

        assertEquals(listOf("create", "photo u-1"), service.calls)
        val sent = service.writes.single()
        assertEquals("Whiskey Sour", sent.name)
        assertEquals("Alcoholic", sent.alcoholic)
        assertEquals(listOf("Whiskey", "Lemon juice"), sent.ingredients.map { it.ingredient })
        assertArrayEquals(photos.bytes, service.uploads.single().second)
        val saved = repository.recipes.value.single()
        assertEquals("recipe-photos/u-1.jpg", saved.thumb)
        assertTrue(analytics.recorded.any { it.name == EVENT_RECIPE_CREATED })
    }

    @Test
    fun aFailedPhotoKeepsTheRecipeAndRetryOnlySendsThePhoto() = runTest {
        viewModel.onIntent(RecipeEditorIntent.Bind(null))
        fillSour()
        viewModel.onIntent(RecipeEditorIntent.PhotoPicked(photo))
        service.failPhotoWith = 500

        viewModel.onIntent(RecipeEditorIntent.Save)

        val failed = viewModel.state.value
        assertTrue(failed.photoFailed)
        assertFalse(failed.saving)
        assertEquals("u-1", failed.recipeId)
        assertEquals(listOf("u-1"), repository.recipes.value.map { it.id })
        // Leaving now keeps the saved recipe without asking.
        assertFalse(failed.dirty)

        service.failPhotoWith = null
        viewModel.effects.test {
            viewModel.onIntent(RecipeEditorIntent.RetryPhoto)
            assertEquals(RecipeEditorEffect.Close, awaitItem())
        }
        assertEquals(listOf("create", "photo u-1", "photo u-1"), service.calls)
        assertFalse(viewModel.state.value.photoFailed)
        // The photo was encoded once.
        assertEquals(listOf(photo), photos.loaded)
    }

    @Test
    fun editingUpdatesAndRemovingThePhotoDeletesIt() = runTest {
        service.recipes += ApiDrink(
            id = "u-5",
            name = "Negroni-ish",
            thumb = "recipe-photos/u-5.jpg",
            alcoholic = "Optional alcohol",
            ingredients = listOf(IngredientMeasure("Gin", "1 oz"), IngredientMeasure("Campari", "1 oz")),
        )
        repository.load()
        viewModel.onIntent(RecipeEditorIntent.Bind("u-5"))
        val loaded = viewModel.state.value
        assertEquals(RecipeType.OPTIONAL, loaded.form.type)
        assertEquals("recipe-photos/u-5.jpg", loaded.previewPhoto)
        assertFalse(loaded.dirty)

        viewModel.onIntent(RecipeEditorIntent.MoveIngredient(loaded.form.ingredients[1].key, up = true))
        viewModel.onIntent(RecipeEditorIntent.RemovePhoto)
        assertTrue(viewModel.state.value.dirty)
        viewModel.onIntent(RecipeEditorIntent.Save)

        assertEquals(listOf("list", "update u-5", "delete photo u-5"), service.calls)
        assertEquals(listOf("Campari", "Gin"), service.writes.single().ingredients.map { it.ingredient })
        assertEquals("", repository.recipes.value.single().thumb)
    }

    @Test
    fun closingWithChangesAsksBeforeDiscarding() = runTest {
        viewModel.onIntent(RecipeEditorIntent.Bind(null))
        viewModel.effects.test {
            viewModel.onIntent(RecipeEditorIntent.Close)
            assertEquals(RecipeEditorEffect.Close, awaitItem())

            viewModel.onIntent(RecipeEditorIntent.NameChanged("Half done"))
            viewModel.onIntent(RecipeEditorIntent.Close)
            assertTrue(viewModel.state.value.confirmDiscard)
            expectNoEvents()

            viewModel.onIntent(RecipeEditorIntent.ConfirmDiscard)
            assertEquals(RecipeEditorEffect.Close, awaitItem())
        }
        assertTrue(service.calls.isEmpty())
    }

    @Test
    fun deleteAsksThenRemovesTheRecipe() = runTest {
        service.recipes += ApiDrink(id = "u-3", name = "Gone soon")
        viewModel.onIntent(RecipeEditorIntent.Bind("u-3"))
        assertEquals("Gone soon", viewModel.state.value.form.name)

        viewModel.onIntent(RecipeEditorIntent.Delete)
        assertTrue(viewModel.state.value.confirmDelete)
        viewModel.effects.test {
            viewModel.onIntent(RecipeEditorIntent.ConfirmDelete)
            assertEquals(RecipeEditorEffect.Deleted, awaitItem())
        }

        assertEquals(listOf("get u-3", "delete u-3"), service.calls)
        assertTrue(repository.recipes.value.isEmpty())
        assertTrue(analytics.recorded.any { it.name == EVENT_RECIPE_DELETED })
    }

    @Test
    fun aServerErrorKeepsTheFormAndShowsTheDetail() {
        viewModel.onIntent(RecipeEditorIntent.Bind(null))
        fillSour()
        service.failWith = 422

        viewModel.onIntent(RecipeEditorIntent.Save)

        val state = viewModel.state.value
        assertEquals("Error 422", state.error)
        assertFalse(state.saving)
        assertNull(state.recipeId)
        assertEquals("Whiskey Sour", state.form.name)
    }

    private fun fillSour() {
        viewModel.onIntent(RecipeEditorIntent.NameChanged("Whiskey Sour"))
        viewModel.onIntent(RecipeEditorIntent.TypeSelected(RecipeType.ALCOHOLIC))
        val first = viewModel.state.value.form.ingredients.single().key
        viewModel.onIntent(RecipeEditorIntent.IngredientChanged(first, "Whiskey"))
        viewModel.onIntent(RecipeEditorIntent.MeasureChanged(first, "2 oz"))
        viewModel.onIntent(RecipeEditorIntent.AddIngredient)
        val second = viewModel.state.value.form.ingredients.last().key
        viewModel.onIntent(RecipeEditorIntent.IngredientChanged(second, "Lemon juice"))
        // A blank trailing row is ignored.
        viewModel.onIntent(RecipeEditorIntent.AddIngredient)
        viewModel.onIntent(RecipeEditorIntent.InstructionsChanged("Shake with ice."))
    }
}
