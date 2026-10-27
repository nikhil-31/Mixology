package com.capstone.nik.mixology.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RecipeRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val service = FakeRecipeService()
    private lateinit var sessions: SessionStore
    private lateinit var subscriptions: SubscriptionRepository
    private lateinit var repository: NetworkRecipeRepository

    @Before
    fun setUp() {
        sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions)
        repository = NetworkRecipeRepository(service, subscriptions, sessions)
    }

    @Test
    fun loadListsTheRecipesNewestFirst() = runTest {
        service.recipes += ApiDrink(id = "u-2", name = "Second", thumb = "recipe-photos/b.jpg")
        service.recipes += ApiDrink(
            id = "u-1",
            name = "First",
            ingredients = listOf(IngredientMeasure("Gin", "2 oz")),
        )

        val loaded = repository.load()

        assertEquals(listOf("u-2", "u-1"), loaded.map { it.id })
        assertEquals(loaded, repository.recipes.value)
        assertEquals("recipe-photos/b.jpg", loaded.first().thumb)
        assertTrue(loaded.all { it.isUserRecipe })
    }

    @Test
    fun createSendsTheDraftAndAddsItToTheTop() = runTest {
        service.recipes += ApiDrink(id = "u-9", name = "Older")
        repository.load()

        val created = repository.create(
            RecipeDraft(
                name = "  Nik's Sour ",
                alcoholic = "Alcoholic",
                glass = "",
                instructions = "Shake hard.",
                ingredients = listOf(IngredientMeasure("Whiskey", "2 oz"), IngredientMeasure("Lemon", "")),
            ),
        )

        val sent = service.writes.single()
        assertEquals("Nik's Sour", sent.name)
        assertNull(sent.glass)
        assertNull(sent.ingredients[1].measure)
        assertEquals(listOf(created.id, "u-9"), repository.recipes.value.map { it.id })
        assertEquals("Nik's Sour", created.name)
    }

    @Test
    fun updateDeleteAndPhotosKeepTheListInSync() = runTest {
        val created = repository.create(RecipeDraft(name = "Draft"))

        repository.update(created.id, RecipeDraft(name = "Final"))
        assertEquals("Final", repository.recipes.value.single().name)

        val jpeg = byteArrayOf(1, 2, 3)
        val withPhoto = repository.uploadPhoto(created.id, jpeg)
        assertEquals("recipe-photos/${created.id}.jpg", withPhoto.thumb)
        assertArrayEquals(jpeg, service.uploads.single().second)
        assertEquals(withPhoto, repository.recipes.value.single())

        repository.deletePhoto(created.id)
        assertEquals("", repository.recipes.value.single().thumb)

        repository.delete(created.id)
        assertTrue(repository.recipes.value.isEmpty())
    }

    @Test
    fun forbiddenShowsThePaywall() = runTest {
        service.failWith = 403

        try {
            repository.load()
            fail("Expected a RecipeHttpException")
        } catch (error: RecipeHttpException) {
            assertEquals(403, error.code)
            assertEquals("Error 403", error.message)
        }
        assertEquals(Access.Free, subscriptions.access.value)
        assertTrue(subscriptions.showPaywall.value)
    }

    @Test
    fun unauthorizedSignsOutAndClearsTheList() = runTest {
        repository.create(RecipeDraft(name = "Mine"))
        service.failWith = 401

        try {
            repository.load()
            fail("Expected a RecipeHttpException")
        } catch (error: RecipeHttpException) {
            assertEquals(401, error.code)
        }
        assertNull(sessions.current())
        assertTrue(repository.recipes.value.isEmpty())
    }
}
