package com.capstone.nik.mixology.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SessionStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        context.getSharedPreferences("mixology_session", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun signingOut_isRememberedAsSignedOut() {
        val store = SessionStore(context)
        store.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1")
        assertNull(store.lastEnd.value)

        store.clear()

        assertNull(store.session.value)
        assertEquals(SessionEnd.SIGNED_OUT, store.lastEnd.value)
        assertTrue(store.signedOut())
    }

    @Test
    fun aRejectedToken_isRememberedAsExpired_andSigningInClearsIt() {
        val store = SessionStore(context)
        store.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1")

        store.expire()
        assertNull(store.session.value)
        assertEquals(SessionEnd.EXPIRED, store.lastEnd.value)
        assertFalse(store.signedOut())

        store.save("token-2", 3600, "ada@example.com", "Ada", uid = "uid-1")
        assertNotNull(store.session.value)
        assertNull(store.lastEnd.value)
    }

    @Test
    fun aTokenThatRanOutWhileTheAppWasClosed_isExpired_notSignedOut() {
        SessionStore(context).save("token", 3600, "ada@example.com", "Ada", uid = "uid-1")
        context.getSharedPreferences("mixology_session", Context.MODE_PRIVATE)
            .edit()
            .putLong("expires_at", System.currentTimeMillis() - 1_000L)
            .commit()

        val store = SessionStore(context)

        assertNull(store.session.value)
        assertEquals(SessionEnd.EXPIRED, store.lastEnd.value)
    }

    @Test
    fun aRunOutTokenWithARefreshToken_staysASessionThatNeedsRenewing() {
        SessionStore(context).save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")
        context.getSharedPreferences("mixology_session", Context.MODE_PRIVATE)
            .edit()
            .putLong("expires_at", System.currentTimeMillis() - 1_000L)
            .commit()

        val store = SessionStore(context)
        val session = store.session.value

        assertNotNull(session)
        assertEquals("refresh-1", session?.refreshToken)
        assertTrue(session!!.needsRefresh())
        assertNull(store.lastEnd.value)
    }

    @Test
    fun needsRefresh_isTrueOnlyInTheLastMinute() {
        val store = SessionStore(context)
        val fresh = store.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")
        assertFalse(fresh.needsRefresh())
        assertTrue(fresh.needsRefresh(now = fresh.expiresAtEpochMs - 30_000L))
    }

    @Test
    fun tokensAreSealedInThePrefsFileAndReadBack() {
        val cipher = ReversingCipher()
        SessionStore(context, cipher).save("id-token", 3600, "ada@example.com", "Ada", refreshToken = "refresh-1")

        assertEquals("sealed1:nekot-di", prefs.getString("id_token", null))
        assertEquals("sealed1:1-hserfer", prefs.getString("refresh_token", null))
        assertEquals("ada@example.com", prefs.getString("email", null))

        val session = SessionStore(context, cipher).session.value
        assertEquals("id-token", session?.idToken)
        assertEquals("refresh-1", session?.refreshToken)
    }

    @Test
    fun aSessionSavedBeforeTokensWereSealed_staysSignedInAndIsSealed() {
        prefs.edit()
            .putString("id_token", "old-token")
            .putString("refresh_token", "old-refresh")
            .putString("email", "ada@example.com")
            .putLong("expires_at", System.currentTimeMillis() + 3_600_000L)
            .commit()

        val session = SessionStore(context, ReversingCipher()).session.value

        assertEquals("old-token", session?.idToken)
        assertEquals("old-refresh", session?.refreshToken)
        assertEquals("sealed1:nekot-dlo", prefs.getString("id_token", null))
        assertEquals("sealed1:hserfer-dlo", prefs.getString("refresh_token", null))
    }

    @Test
    fun aTokenThatCanNoLongerBeOpened_endsTheSessionAsExpired() {
        val cipher = ReversingCipher()
        SessionStore(context, cipher).save("id-token", 3600, "ada@example.com", "Ada", refreshToken = "refresh-1")
        cipher.keyLost = true

        val store = SessionStore(context, cipher)

        assertNull(store.session.value)
        assertEquals(SessionEnd.EXPIRED, store.lastEnd.value)
        assertNull(prefs.getString("id_token", null))
    }

    @Test
    fun withoutAKeystoreTheSessionStillWorks() {
        val cipher = ReversingCipher().apply { unavailable = true }
        SessionStore(context, cipher).save("id-token", 3600, "ada@example.com", "Ada", refreshToken = "refresh-1")

        assertEquals("id-token", SessionStore(context, cipher).session.value?.idToken)
    }

    @Test
    fun readingTheSessionAgainDoesNotGoBackToTheKeystore() {
        val cipher = ReversingCipher()
        val store = SessionStore(context, cipher)
        store.save("id-token", 3600, "ada@example.com", "Ada", refreshToken = "refresh-1")

        repeat(5) { assertEquals("id-token", store.current()?.idToken) }

        assertEquals(0, cipher.opens)
    }

    private val prefs get() = context.getSharedPreferences("mixology_session", Context.MODE_PRIVATE)
}

/** Stands in for the keystore: "seals" by reversing, so a test can tell sealed from plain. */
private class ReversingCipher : TokenCipher {
    var unavailable = false
    var keyLost = false
    var opens = 0

    override fun seal(plain: String): String? = plain.reversed().takeUnless { unavailable }

    override fun open(sealed: String): String? {
        opens += 1
        return sealed.reversed().takeUnless { keyLost }
    }
}
