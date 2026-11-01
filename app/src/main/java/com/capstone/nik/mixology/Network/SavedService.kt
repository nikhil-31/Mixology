package com.capstone.nik.mixology.Network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Every field has a default, so Kotlin gives Gson a no-arg constructor and a
 * field the server leaves out keeps its default instead of becoming null.
 */
data class SavedSyncDto(
    val saved: List<String> = emptyList(),
    /** The account's drink notes by drink id; drinks without a note are left out. */
    val notes: Map<String, String> = emptyMap(),
)

/** `PUT me/notes/{id}` body; an empty string deletes the note. */
data class NoteBody(val notes: String)

interface SavedService {
    /** The account's synced state; only `saved` and `notes` are read here. */
    @GET("sync")
    suspend fun sync(): SavedSyncDto

    @PUT("me/saved/{id}")
    suspend fun save(@Path("id") drinkId: String)

    @DELETE("me/saved/{id}")
    suspend fun unsave(@Path("id") drinkId: String)

    /** Stores the drink's note (trimmed by the server); answers `{"ok": true}`, which is ignored. */
    @PUT("me/notes/{id}")
    suspend fun setNotes(@Path("id") id: String, @Body body: NoteBody)
}
