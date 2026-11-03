package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeAuthRepository
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeDraftDto
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeDto
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeIngredientDto
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
import com.capstone.nik.mixology.ui.recipes.RecipePhotoLoader
import android.net.Uri
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_ADA_VOICE_INPUT
import com.capstone.nik.mixology.analytics.PARAM_RESULT
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.repository.AuthResult
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.ChatTurn
import com.capstone.nik.mixology.repository.DrinkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
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
class AdaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private val chat = FakeChatRepository()
    private val auth = FakeAuthRepository()
    private val analytics = AnalyticsTracker.forTests()
    private val network = NetworkMonitor.forTests()
    private lateinit var viewModel: AdaViewModel
    private val recipeService = FakeRecipeService()
    private lateinit var recipes: NetworkRecipeRepository
    private val photos = object : RecipePhotoLoader {
        val loaded = mutableListOf<Uri>()
        override suspend fun load(uri: Uri): ByteArray {
            loaded += uri
            return byteArrayOf(1, 2, 3)
        }
    }
    private val inbox = AdaInbox()
    private val handoff = RecipeDraftHandoff()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        val sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        recipes = NetworkRecipeRepository(
            recipeService,
            SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions),
            sessions,
        )
        viewModel = AdaViewModel(
            chat,
            auth,
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                FakeCocktailService(),
                context,
            ),
            network,
            context,
            recipes,
            photos,
            inbox,
            handoff,
            analytics,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_withSessionLoadsHistory() = runTest {
        chat.history = listOf(
            ChatTurn("user", "Alexander tonight"),
            ChatTurn("assistant", "A good fit.", listOf(alexander)),
        )

        signIn()

        val state = viewModel.state.value
        assertTrue(state.signedIn)
        assertEquals("Alexander tonight", state.messages[0].content)
        assertEquals("user", state.messages[0].role)
        assertEquals("A good fit.", state.messages[1].content)
        assertEquals("11014", state.messages[1].drinks.single().id)
    }

    @Test
    fun load_keepsShowMoreOnHistoryReplies() = runTest {
        chat.history = listOf(
            ChatTurn("user", "drinks with gin"),
            ChatTurn("assistant", "Here are a few.", listOf(alexander), hasMore = true),
        )

        signIn()

        val messages = viewModel.state.value.messages
        assertFalse(messages[0].hasMore)
        assertTrue(messages[1].hasMore)
    }

    @Test
    fun openMore_opensTheListForThatReplyWithoutSending() = runTest {
        chat.history = listOf(
            ChatTurn("user", "drinks with gin"),
            ChatTurn("assistant", "Here are a few.", listOf(alexander), hasMore = true),
        )
        signIn()
        val effects = mutableListOf<AdaEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(AdaIntent.OpenMore(viewModel.state.value.messages[1].id))

        assertEquals(listOf(AdaEffect.OpenMore("drinks with gin", listOf("11014"))), effects)
        assertTrue(chat.sent.isEmpty())
        assertEquals(2, viewModel.state.value.messages.size)
    }

    @Test
    fun load_opensAtNewestPageThenLoadsOlderOnRequest() = runTest {
        chat.history = (1L..5L).map { id -> ChatTurn("user", "message $id", serverId = id) }
        chat.pageSize = 2

        signIn()

        assertEquals(listOf("message 4", "message 5"), viewModel.state.value.messages.map { it.content })
        assertTrue(viewModel.state.value.hasOlder)

        viewModel.onIntent(AdaIntent.LoadOlder)
        assertEquals(
            listOf("message 2", "message 3", "message 4", "message 5"),
            viewModel.state.value.messages.map { it.content },
        )
        assertTrue(viewModel.state.value.hasOlder)

        viewModel.onIntent(AdaIntent.LoadOlder)
        val state = viewModel.state.value
        assertEquals((1..5).map { "message $it" }, state.messages.map { it.content })
        assertFalse(state.hasOlder)
        assertFalse(state.loadingOlder)
        assertEquals(listOf(null, 4L, 2L), chat.historyRequests)

        // Nothing older remains, so another request does not hit the server.
        viewModel.onIntent(AdaIntent.LoadOlder)
        assertEquals(3, chat.historyRequests.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun send_streamsTextThenAttachesDrinksAndFinishes() = runTest {
        signIn()
        val events = Channel<ChatStreamEvent>(capacity = Channel.BUFFERED)
        chat.sendFlow = events.receiveAsFlow()
        val seen = mutableListOf<AdaUiState>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { seen.add(it) }
        }

        viewModel.onIntent(AdaIntent.DraftChanged("Alexander tonight"))
        viewModel.onIntent(AdaIntent.Send)
        events.send(ChatStreamEvent.Delta("A "))
        events.send(ChatStreamEvent.Delta("good fit."))
        events.send(ChatStreamEvent.Recommendations(listOf(alexander)))
        events.send(ChatStreamEvent.Done("A good fit."))
        events.close()
        collector.cancel()

        assertEquals(listOf("Alexander tonight"), chat.sent)
        assertTrue(seen.any { it.sending && it.messages.lastOrNull()?.content == "A " })
        assertTrue(seen.any { state ->
            val assistant = state.messages.lastOrNull()
            state.sending && assistant?.content == "A good fit." && assistant.drinks.isEmpty()
        })
        val done = viewModel.state.value
        assertFalse(done.sending)
        assertEquals("", done.draft)
        assertEquals("Alexander tonight", done.messages[0].content)
        assertEquals("A good fit.", done.messages[1].content)
        assertEquals("11014", done.messages[1].drinks.single().id)
        assertFalse(done.messages[1].streaming)
    }

    @Test
    fun send_keepsHasMoreFromTheStream() = runTest {
        signIn()
        chat.sendFlow = flowOf(
            ChatStreamEvent.Recommendations(listOf(alexander), hasMore = true),
            ChatStreamEvent.Done("Here are a few."),
        )

        viewModel.onIntent(AdaIntent.DraftChanged("drinks with gin"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertEquals(listOf("drinks with gin"), chat.sent)
        assertEquals("", state.draft)
        assertTrue(state.messages.last().hasMore)
        assertFalse(state.sending)
    }

    @Test
    fun send_errorEventSurfacesDetailAndStops() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Error("LM Studio is unreachable"))

        viewModel.onIntent(AdaIntent.DraftChanged("Alexander tonight"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertEquals("LM Studio is unreachable", state.error)
        assertFalse(state.sending)
        assertEquals("Alexander tonight", state.messages.first().content)
        assertFalse(state.messages.last().streaming)
    }

    @Test
    fun load_unreachableServerShowsTheMessageNotTheException() = runTest {
        chat.failWithIo = true

        signIn()

        val state = viewModel.state.value
        assertEquals("Can’t reach Mixology right now. Try again in a moment.", state.error)
        assertFalse(state.loadingHistory)
    }

    @Test
    fun send_unreachableServerShowsTheMessageAndStops() = runTest {
        signIn()
        chat.failWithIo = true

        viewModel.onIntent(AdaIntent.DraftChanged("Alexander tonight"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertEquals("Can’t reach Mixology right now. Try again in a moment.", state.error)
        assertFalse(state.sending)
        assertFalse(state.messages.last().streaming)
    }

    @Test
    fun send_status503ShowsTheUnreachableMessage() = runTest {
        signIn()
        chat.sendFlow = flow { throw ChatHttpException(503, "Service Unavailable") }

        viewModel.onIntent(AdaIntent.DraftChanged("Alexander tonight"))
        viewModel.onIntent(AdaIntent.Send)

        assertEquals("Can’t reach Mixology right now. Try again in a moment.", viewModel.state.value.error)
    }

    @Test
    fun send_unauthorizedReturnsToSignIn() = runTest {
        signIn()
        chat.sendFlow = flow { throw ChatHttpException(401, "Not authenticated") }

        viewModel.onIntent(AdaIntent.DraftChanged("another round"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertFalse(state.signedIn)
        assertTrue(state.messages.isEmpty())
        assertTrue(auth.signedOut)
    }

    @Test
    fun send_outOfCreditsShowsTheNoticeInsteadOfAnError() = runTest {
        signIn()
        chat.sendFlow = flow { throw ChatHttpException(402, "Out of chat credits until 2026-11-01T00:00:00+00:00") }

        viewModel.onIntent(AdaIntent.DraftChanged("one more"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertTrue(state.outOfCredits)
        assertNull(state.error)
        assertFalse(state.sending)
        assertEquals(listOf("one more"), state.messages.map { it.content })

        chat.sendFlow = flowOf(ChatStreamEvent.Done("Here's one."))
        viewModel.onIntent(AdaIntent.DraftChanged("show more"))
        viewModel.onIntent(AdaIntent.Send)

        assertFalse(viewModel.state.value.outOfCredits)
    }

    @Test
    fun voice_toggleAsksForTheMicrophone() = runTest {
        signIn()
        val effects = mutableListOf<AdaEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(AdaIntent.ToggleVoice)

        assertEquals(listOf<AdaEffect>(AdaEffect.RequestMicrophone), effects)
        assertFalse(viewModel.state.value.listening)
    }

    @Test
    fun voice_transcriptsReplaceTheDraftWithoutSending() = runTest {
        signIn()
        viewModel.onIntent(AdaIntent.DraftChanged("typed"))

        viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
        assertTrue(viewModel.state.value.listening)
        assertEquals("", viewModel.state.value.draft)

        viewModel.onIntent(AdaIntent.VoiceTranscript("something smoky", final = false))
        assertEquals("something smoky", viewModel.state.value.draft)
        assertTrue(viewModel.state.value.listening)

        viewModel.onIntent(AdaIntent.VoiceTranscript("something smoky with mezcal", final = true))
        val state = viewModel.state.value
        assertEquals("something smoky with mezcal", state.draft)
        assertFalse(state.listening)
        assertTrue(chat.sent.isEmpty())
        assertEquals(listOf("started", "completed"), voiceResults())
    }

    @Test
    fun voice_transcriptsAfterStoppingAreIgnored() = runTest {
        signIn()
        viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
        viewModel.onIntent(AdaIntent.VoiceTranscript("negroni", final = false))

        viewModel.onIntent(AdaIntent.ToggleVoice)
        viewModel.onIntent(AdaIntent.VoiceTranscript("negroni please", final = true))

        assertFalse(viewModel.state.value.listening)
        assertEquals("negroni", viewModel.state.value.draft)
        assertEquals(listOf("started", "completed"), voiceResults())
    }

    @Test
    fun voice_sendStopsListening() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Done("Try a Paloma."))
        viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
        viewModel.onIntent(AdaIntent.VoiceTranscript("tequila", final = false))

        viewModel.onIntent(AdaIntent.Send)

        assertFalse(viewModel.state.value.listening)
        assertEquals(listOf("tequila"), chat.sent)
    }

    @Test
    fun voice_deniedShowsTheSettingsNotice() = runTest {
        signIn()

        viewModel.onIntent(AdaIntent.VoicePermission(granted = false))

        assertFalse(viewModel.state.value.listening)
        assertEquals(AdaVoiceProblem.Denied, viewModel.state.value.voiceProblem)
        assertEquals(listOf("denied"), voiceResults())
    }

    @Test
    fun voice_failureShowsANoticeButSilenceDoesNot() = runTest {
        signIn()
        viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
        viewModel.onIntent(AdaIntent.VoiceEnded(failed = false))
        assertFalse(viewModel.state.value.listening)
        assertNull(viewModel.state.value.voiceProblem)

        viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
        viewModel.onIntent(AdaIntent.VoiceEnded(failed = true))
        assertEquals(AdaVoiceProblem.Failed, viewModel.state.value.voiceProblem)
        assertEquals(listOf("started", "cancelled", "started", "error"), voiceResults())
    }

    private fun voiceResults(): List<Any?> =
        analytics.recorded.filter { it.name == EVENT_ADA_VOICE_INPUT }.map { it.params[PARAM_RESULT] }

    private fun draft(state: String = ChatRecipeDto.STATE_DRAFT, recipeId: String? = null) = ChatRecipeDto(
        recipe = ChatRecipeDraftDto(
            name = "Paloma",
            ingredients = listOf(ChatRecipeIngredientDto("Tequila", "2 oz")),
            sourceUrl = "https://www.instagram.com/p/abc/",
        ),
        photoUrl = "https://cdn.example.com/p.jpg",
        source = "instagram",
        state = state,
        recipeId = recipeId,
    )

    @Test
    fun recipeEvent_putsADraftCardOnTheReply() = runTest {
        signIn()
        chat.sendFlow = flowOf(
            ChatStreamEvent.Delta("I read a Paloma. Save it?"),
            ChatStreamEvent.Recipe(draft()),
            ChatStreamEvent.Done("I read a Paloma. Save it?"),
        )
        viewModel.onIntent(AdaIntent.DraftChanged("https://www.instagram.com/p/abc/"))
        viewModel.onIntent(AdaIntent.Send)

        val reply = viewModel.state.value.messages.last()
        assertEquals("Paloma", reply.recipe?.name)
        assertEquals(AdaRecipe.State.Draft, reply.recipe?.state)
        assertEquals("instagram", reply.recipe?.source)
    }

    @Test
    fun saveRecipe_sendsTheAnswer_andTheDraftCardTakesTheResult() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Recipe(draft()), ChatStreamEvent.Done("Save it?"))
        viewModel.onIntent(AdaIntent.DraftChanged("https://www.instagram.com/p/abc/"))
        viewModel.onIntent(AdaIntent.Send)
        val draftId = viewModel.state.value.messages.last().id
        recipeService.recipes += ApiDrink(id = "u-9", name = "Paloma")

        chat.sendFlow = flowOf(
            ChatStreamEvent.Recipe(draft(ChatRecipeDto.STATE_SAVED, recipeId = "u-9")),
            ChatStreamEvent.Done("Saved Paloma to your recipes."),
        )
        viewModel.onIntent(AdaIntent.SaveRecipe(draftId))

        assertEquals("Save it", chat.sent.last())
        val messages = viewModel.state.value.messages
        val card = messages.first { it.id == draftId }.recipe
        assertEquals(AdaRecipe.State.Saved, card?.state)
        assertEquals("u-9", card?.recipeId)
        // The answer's reply doesn't repeat the card.
        assertNull(messages.last().recipe)
        assertEquals(listOf("u-9"), recipes.recipes.value.map { it.id })
    }

    @Test
    fun dropRecipe_sendsNotNow() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Recipe(draft()), ChatStreamEvent.Done("Save it?"))
        viewModel.onIntent(AdaIntent.DraftChanged("https://www.instagram.com/p/abc/"))
        viewModel.onIntent(AdaIntent.Send)
        val draftId = viewModel.state.value.messages.last().id

        chat.sendFlow = flowOf(ChatStreamEvent.Recipe(draft(ChatRecipeDto.STATE_DROPPED)), ChatStreamEvent.Done("Okay."))
        viewModel.onIntent(AdaIntent.DropRecipe(draftId))

        assertEquals("Not now", chat.sent.last())
        assertEquals(AdaRecipe.State.Dropped, viewModel.state.value.messages.first { it.id == draftId }.recipe?.state)
    }

    @Test
    fun history_foldsTheAnswerOntoTheDraft() = runTest {
        chat.history = listOf(
            ChatTurn("user", "https://www.instagram.com/p/abc/"),
            ChatTurn("assistant", "Save it?", recipe = draft()),
            ChatTurn("user", "yes"),
            ChatTurn("assistant", "Saved.", recipe = draft(ChatRecipeDto.STATE_SAVED, recipeId = "u-9")),
        )
        signIn()

        val messages = viewModel.state.value.messages
        assertEquals(AdaRecipe.State.Saved, messages[1].recipe?.state)
        assertEquals("u-9", messages[1].recipe?.recipeId)
        assertNull(messages[3].recipe)
    }

    @Test
    fun openRecipe_opensTheSavedRecipe() = runTest {
        chat.history = listOf(
            ChatTurn("assistant", "Saved.", recipe = draft(ChatRecipeDto.STATE_SAVED, recipeId = "u-9")),
        )
        signIn()
        val effects = mutableListOf<AdaEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(AdaIntent.OpenRecipe(viewModel.state.value.messages.single().id))

        assertEquals("u-9", (effects.single() as AdaEffect.OpenDrink).drink.id)
    }

    @Test
    fun sendPhoto_sendsTheJpegWithTheDraft() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Recipe(draft()), ChatStreamEvent.Done("From your photo. Save it?"))
        val uri = Uri.parse("content://photos/1")
        viewModel.onIntent(AdaIntent.DraftChanged("my gran's recipe"))
        viewModel.onIntent(AdaIntent.SendPhoto(uri))

        assertEquals(listOf(uri), photos.loaded)
        assertEquals("my gran's recipe", chat.sentPhotos.single().second)
        val messages = viewModel.state.value.messages
        assertEquals(uri, messages[messages.size - 2].photo)
        assertEquals("Paloma", messages.last().recipe?.name)
    }

    @Test
    fun inbox_sendsAMessageLeftByImport() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Done("Let me look."))

        inbox.post("https://www.liquor.com/recipes/paper-plane/")

        assertEquals("https://www.liquor.com/recipes/paper-plane/", chat.sent.single())
        assertNull(inbox.pending.value)
    }

    private fun signIn() {
        chat.session = Session(
            idToken = "token",
            email = "ada@mixology.test",
            displayName = "Ada",
            expiresAtEpochMs = Long.MAX_VALUE,
        )
        viewModel.onIntent(AdaIntent.Load)
        assertTrue(viewModel.state.value.signedIn)
    }

    @Test
    fun editRecipe_handsTheDraftToTheEditor() = runTest {
        signIn()
        chat.sendFlow = flowOf(
            ChatStreamEvent.Delta("I read a Paloma. Save it?"),
            ChatStreamEvent.Recipe(draft()),
            ChatStreamEvent.Done("I read a Paloma. Save it?"),
        )
        viewModel.onIntent(AdaIntent.DraftChanged("https://www.instagram.com/p/abc/"))
        viewModel.onIntent(AdaIntent.Send)
        val reply = viewModel.state.value.messages.last()

        viewModel.effects.test {
            viewModel.onIntent(AdaIntent.EditRecipe(reply.id))
            assertEquals(AdaEffect.OpenDraftEditor, awaitItem())
        }

        val request = handoff.take()
        assertEquals(reply.id, request?.messageId)
        assertEquals("Paloma", request?.recipe?.name)
        // Nothing was sent to Ada.
        assertEquals(listOf("https://www.instagram.com/p/abc/"), chat.sent)
    }

    @Test
    fun draftSavedFromTheEditor_marksTheCardSaved() = runTest {
        signIn()
        chat.sendFlow = flowOf(ChatStreamEvent.Recipe(draft()), ChatStreamEvent.Done("Save it?"))
        viewModel.onIntent(AdaIntent.DraftChanged("https://www.instagram.com/p/abc/"))
        viewModel.onIntent(AdaIntent.Send)
        val reply = viewModel.state.value.messages.last()

        handoff.saved(reply.id, "u-9")

        val recipe = viewModel.state.value.messages.last().recipe
        assertEquals(AdaRecipe.State.Saved, recipe?.state)
        assertEquals("u-9", recipe?.recipeId)
    }

    @Test
    fun reconcileDrafts_showsDraftsAlreadyInMyRecipesAsSaved() {
        fun card(id: Long, name: String, link: String?) = AdaMessage(
            id = id,
            role = "assistant",
            content = "",
            recipe = AdaRecipe(name, null, null, link, emptyList(), null, AdaRecipe.State.Draft),
        )
        val messages = listOf(
            card(1, "Paloma", "https://www.instagram.com/p/abc/"),
            card(2, "Gimlet", null),
            card(3, "Negroni", "https://example.com/negroni"),
        )
        val recipes = listOf(
            Drink(id = "u-1", name = "My Paloma", thumb = "", sourceUrl = "https://www.instagram.com/p/abc/"),
            Drink(id = "u-2", name = "gimlet", thumb = ""),
        )

        val result = reconcileDrafts(messages, recipes)

        assertEquals("u-1", result[0].recipe?.recipeId)
        assertEquals(AdaRecipe.State.Saved, result[0].recipe?.state)
        assertEquals("u-2", result[1].recipe?.recipeId)
        assertEquals(AdaRecipe.State.Draft, result[2].recipe?.state)
    }
}

private val alexander = Drink(
    id = "11014",
    name = "Alexander",
    thumb = "https://example.com/a.jpg",
)
