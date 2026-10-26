package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.Network.ChatStreamEvent
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
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun signIn_loadsHistory() = runTest {
        chat.history = listOf(
            ChatTurn("user", "Alexander tonight"),
            ChatTurn("assistant", "A good fit.", listOf(alexander)),
        )

        viewModel.onIntent(AdaIntent.EmailChanged("ada@mixology.test"))
        viewModel.onIntent(AdaIntent.PasswordChanged("secret"))
        viewModel.onIntent(AdaIntent.SignIn)

        val state = viewModel.state.value
        assertTrue(state.signedIn)
        assertEquals("ada@mixology.test", state.email)
        assertEquals("", state.password)
        assertEquals("Alexander tonight", state.messages[0].content)
        assertEquals("user", state.messages[0].role)
        assertEquals("A good fit.", state.messages[1].content)
        assertEquals("11014", state.messages[1].drinks.single().id)
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
        assertEquals("Not authenticated", state.authError)
        assertTrue(state.messages.isEmpty())
        assertTrue(chat.signedOut)
    }

    private fun signIn() {
        viewModel.onIntent(AdaIntent.EmailChanged("ada@mixology.test"))
        viewModel.onIntent(AdaIntent.PasswordChanged("secret"))
        viewModel.onIntent(AdaIntent.SignIn)
        assertTrue(viewModel.state.value.signedIn)
    }
}

private val alexander = Drink(
    id = "11014",
    name = "Alexander",
    thumb = "https://example.com/a.jpg",
)

private class FakeChatRepository : ChatRepository {
    var session: Session? = null
    var history: List<ChatTurn> = emptyList()
    var signInResult: AuthResult = AuthResult.Success(
        Session(
            idToken = "token",
            email = "ada@mixology.test",
            displayName = "Ada",
            expiresAtEpochMs = Long.MAX_VALUE,
        ),
    )
    var sendFlow: Flow<ChatStreamEvent> = flowOf()
    val sent = mutableListOf<String>()
    var signedOut = false

    override fun currentSession(): Session? = session

    override suspend fun signIn(email: String, password: String): AuthResult {
        val result = signInResult
        if (result is AuthResult.Success) session = result.session
        return result
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return signIn(email, password)
    }

    override fun signOut() {
        session = null
        signedOut = true
    }

    override suspend fun history(): List<ChatTurn> = history

    override fun send(message: String): Flow<ChatStreamEvent> {
        sent += message
        return sendFlow
    }
}
