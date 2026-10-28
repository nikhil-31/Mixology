package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.Network.ChatStreamEvent
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
    private val analytics = AnalyticsTracker.forTests()
    private lateinit var viewModel: AdaViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        viewModel = AdaViewModel(
            chat,
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                FakeCocktailService(),
                context,
            ),
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
    fun send_unauthorizedReturnsToSignIn() = runTest {
        signIn()
        chat.sendFlow = flow { throw ChatHttpException(401, "Not authenticated") }

        viewModel.onIntent(AdaIntent.DraftChanged("another round"))
        viewModel.onIntent(AdaIntent.Send)

        val state = viewModel.state.value
        assertFalse(state.signedIn)
        assertTrue(state.messages.isEmpty())
        assertTrue(chat.signedOut)
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
}

private val alexander = Drink(
    id = "11014",
    name = "Alexander",
    thumb = "https://example.com/a.jpg",
)
