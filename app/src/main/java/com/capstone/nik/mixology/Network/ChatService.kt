package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ChatHistoryResponse
import com.capstone.nik.mixology.Network.remoteModel.ChatMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChatService {
    /** The newest [limit] messages, or those before message id [before]. Null params are omitted. */
    @GET("chat")
    suspend fun history(
        @Query("limit") limit: Int? = null,
        @Query("before") before: Long? = null,
    ): ChatHistoryResponse

    /** Drinks that fit request [q], minus [exclude] (comma-separated ids), from [offset]. */
    @GET("chat/matches")
    suspend fun matches(
        @Query("q") q: String,
        @Query("exclude") exclude: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int? = null,
    ): ChatMatchesResponse
}
